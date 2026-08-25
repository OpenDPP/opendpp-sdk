/**
 * Shared SDK codegen spec normalizer — the ONE place the published OpenAPI 3.1 contract is rewritten
 * into a generation input openapi-generator (7.12.0, `java` + `python` lanes) can consume correctly.
 *
 * HISTORY: these five transforms began life as a private Groovy `prepareSpec` task inside
 * opendpp-sdk/java/build.gradle.kts. The Python lane hits the same constructs and fails DIFFERENTLY
 * (e.g. `additionalProperties: false` → pydantic `extra="forbid"` → runtime rejection of real
 * payloads), so rather than a second private rewriter per lane, the normalization lives here — beside
 * the contract that makes it necessary — and is EXECUTED in the mirror: `sdk-regen-verify.ts` copies
 * this file to `scripts/normalize-spec.mjs` in the OpenDPP/opendpp-sdk checkout (the same mechanism
 * that carries sdk-notes/CHANGELOG.md), where the Java Gradle build and the Python regen script invoke
 * it. One commit carries the normalizer and the sources it regenerated.
 *
 * Plain dependency-free ESM JavaScript, NOT TypeScript: the mirror runs it with a bare `node` (no tsx,
 * no install). The sibling `spec-codegen-normalize.d.mts` types the import for node-repo consumers
 * (same pattern as the vendored gs1encoder glue). Unit-pinned by
 * tests/functional/spec-codegen-normalize.test.ts against both synthetic fixtures and the live
 * openapi.json.
 *
 * SCOPE: generation input ONLY. Each lane's vendored openapi.json stays the pristine published
 * contract — it drives the version locks and the drift checks and must never be normalized.
 * The TypeScript lane (@hey-api/openapi-ts) handles these 3.1 constructs natively and keeps
 * generating from the pristine spec.
 *
 * Copyright (c) Opendpp UAB.
 * SPDX-License-Identifier: LicenseRef-OpenDPP-Proprietary
 */

import { mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname } from "node:path";
import { pathToFileURL } from "node:url";

/** JSON object test matching the Groovy `is Map` checks — arrays and null are not maps. */
function isMap(v) {
  return typeof v === "object" && v !== null && !Array.isArray(v);
}

/**
 * Transforms 2–4, applied by one recursive walk (ported verbatim from the Groovy `relax`):
 *
 * 2. JSON-LD `@context` is polymorphic (string | array | object). openapi-generator's 3.1 anyOf
 *    deserializer emits illegal `List<Object>.class` / `Map<..>.class` for those composed models
 *    (EpcisDocumentContext, PassportListItemContextInner, PublicBatteryUnitJsonLdContextInner),
 *    which break javac; the python generator degrades to try-each-variant with junk model names.
 *    A typed client never hand-builds @context, so every `@context` property is relaxed to a
 *    free-form schema (→ Object / Any), keeping only its description.
 *
 * 3. A schema with typed `properties` PLUS `additionalProperties` (the JSON-LD documents flatten
 *    metadata onto the root, hence additionalProperties:true) makes the Java generator emit
 *    `class X extends HashMap` — and Jackson deserializes any Map subtype AS A MAP, never calling
 *    the typed setters: every typed getter silently returns null. In Python,
 *    `additionalProperties:false` becomes pydantic `extra="forbid"`, so a future ADDITIVE server
 *    field crashes deployed clients at parse time. Strip `additionalProperties` from schemas that
 *    also declare non-empty typed `properties`; nothing is lost — the flattened root keys are
 *    documented duplicates of the `metadata` object, which the typed surface still exposes in full.
 *
 * 4. 3.1 quirks the generators mishandle, collapsed to free-form (keeping the description) on
 *    property-less schemas:
 *    • a container multi-type (free-form event `payload`, type:["object","array","null"]) → the
 *      Java generator emits a Map with an EMPTY value type (`Map<String, >`);
 *    • a bare `type:"null"` OUTSIDE a composition (PassportListItem.manufacturingFacility, always
 *      null in the list view) → an un-generated `ModelNull` class reference.
 *    Direct anyOf/oneOf/allOf branches are compositions: a `{type:"null"}` branch there is the
 *    standard 3.1 nullable idiom, which the generators handle natively (typed field + nullable) —
 *    those must NOT be collapsed.
 *
 * 6. BOOLEAN single-value constraints (`const: true|false`, or an all-boolean `enum`) are stripped
 *    from the generation input: openapi-generator STRINGIFIES the allowed value into the emitted
 *    validator (`if value not in set(['true'])` in python), which then rejects the boolean the
 *    server actually sends — every `success: false` error body and the gs1-batch `ok` discriminant
 *    failed to parse. The union stays unambiguous without it: the branches' `required` sets are
 *    disjoint. The published contract keeps the `const` — it is true and idiomatic 3.1; only the
 *    generators can't be trusted with it. (String consts generate correct validators and are kept.)
 */
function relax(node, inComposition) {
  if (Array.isArray(node)) {
    for (const item of node) relax(item, inComposition);
    return;
  }
  if (!isMap(node)) return;

  const props = node.properties;
  if (isMap(props) && Object.prototype.hasOwnProperty.call(props, "@context")) {
    const relaxed = {};
    const desc = isMap(props["@context"]) ? props["@context"].description : undefined;
    if (desc !== undefined) relaxed.description = desc;
    props["@context"] = relaxed;
  }
  if (isMap(props) && Object.keys(props).length > 0 && Object.prototype.hasOwnProperty.call(node, "additionalProperties")) {
    delete node.additionalProperties;
  }
  for (const [k, v] of Object.entries(node)) {
    const comp = Array.isArray(v) && (k === "anyOf" || k === "oneOf" || k === "allOf");
    if (comp) {
      for (const branch of v) relax(branch, true);
    } else {
      relax(v, false);
    }
  }
  const t = node.type;
  const containerMulti = Array.isArray(t) && (t.includes("object") || t.includes("array"));
  const bareNull = t === "null" && !inComposition;
  if ((containerMulti || bareNull) && !Object.prototype.hasOwnProperty.call(node, "properties")) {
    const desc = node.description;
    for (const key of Object.keys(node)) delete node[key];
    if (desc !== undefined) node.description = desc;
  }
  if (t === "boolean" || (Array.isArray(t) && t.includes("boolean"))) {
    if (typeof node.const === "boolean") delete node.const;
    if (Array.isArray(node.enum) && node.enum.every((v) => typeof v === "boolean")) delete node.enum;
  }
}

/**
 * Transform 5 — content-negotiation pruning. The public resolvers are content-negotiated (RFC 7231):
 * their responses offer JSON-LD, AAS, VC-JWT, SD-JWT, and HTML alongside application/json. The
 * generator turns that into an equal-q multi-representation Accept header, so the server may
 * legitimately answer with a representation the typed return model does NOT match — which
 * deserializes as an all-null object (unknown properties are ignored). A typed JSON client can only
 * consume the JSON document, so wherever a response offers application/json among alternates keep
 * ONLY application/json (binary-only responses — QR images, ZIP export — are left untouched). The
 * alternate representations stay reachable by plain URL fetch, and each lane's hand-written
 * ergonomics module re-exposes them by pinning the Accept header explicitly.
 * NOTE the two generator behaviors this must defeat:
 *  • the Accept header is the UNION of content types across ALL of an operation's responses
 *    (errors and redirects included), so every inline response must be pruned, not just the 2xx;
 *  • the negotiated resolvers' 200 offers NO plain application/json — their JSON document
 *    representation is application/ld+json (the errors are what carry application/json).
 */
function pruneContentNegotiation(doc) {
  const jsonTypes = ["application/json", "application/ld+json"];
  const httpMethods = new Set(["get", "put", "post", "delete", "options", "head", "patch", "trace"]);
  const paths = isMap(doc.paths) ? doc.paths : {};
  for (const pathItem of Object.values(paths)) {
    if (!isMap(pathItem)) continue;
    for (const [method, op] of Object.entries(pathItem)) {
      if (!httpMethods.has(method) || !isMap(op)) continue;
      const responses = isMap(op.responses) ? op.responses : null;
      if (!responses) continue;
      // Only operations whose SUCCESS representation has a JSON variant — binary-only ops
      // (QR images, ZIP export) and AAS/VC-typed exports keep their declarations untouched.
      const jsonSuccess = Object.entries(responses).some(
        ([code, resp]) =>
          String(code).startsWith("2") &&
          isMap(resp) &&
          isMap(resp.content) &&
          Object.keys(resp.content).some((mt) => jsonTypes.includes(mt)),
      );
      if (!jsonSuccess) continue;
      for (const resp of Object.values(responses)) {
        if (!isMap(resp)) continue;
        const content = resp.content;
        if (!isMap(content)) continue;
        const pick = jsonTypes.find((mt) => Object.prototype.hasOwnProperty.call(content, mt));
        // A response with no JSON variant at all (e.g. an HTML-only redirect body) is dropped from
        // the declaration so it can't pollute the Accept union.
        if (pick !== undefined) {
          for (const mt of Object.keys(content)) {
            if (mt !== pick) delete content[mt];
          }
        } else {
          delete resp.content;
        }
      }
    }
  }
}

/**
 * Pure entry point: returns a normalized DEEP COPY of the parsed spec; the input is not mutated.
 * Transform order matters and matches the retired Gradle task exactly: webhooks removal, then the
 * recursive relax walk, then content-negotiation pruning.
 */
export function normalizeForCodegen(spec) {
  const doc = structuredClone(spec);
  // Transform 1: the published contract is OpenAPI 3.1 with a top-level `webhooks:` block (inbound
  // delivery callbacks: passport.ingested/sealed/recalled/status_updated/updated). A CLIENT SDK never
  // *calls* those, and openapi-generator maps them to the SAME WebhooksApi class as the real
  // `Webhooks`-tagged management endpoints (create/list/update/delete subscription, rotate-secret,
  // deliveries, test) — so the inbound block silently OVERWRITES the management operations. Strip it
  // from the generation input only; the WebhookEnvelope payload model — a named component schema —
  // is still generated so consumers can deserialize deliveries.
  delete doc.webhooks;
  relax(doc, false);
  pruneContentNegotiation(doc);
  return doc;
}

// CLI mode: `node spec-codegen-normalize.mjs <pristine-spec.json> <normalized-out.json>` — how the
// mirror's lanes invoke it (Gradle `prepareSpec` Exec task; the Python regen script).
if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const [input, output] = process.argv.slice(2);
  if (!input || !output) {
    console.error("usage: node spec-codegen-normalize.mjs <pristine-spec.json> <normalized-out.json>");
    process.exit(2);
  }
  const normalized = normalizeForCodegen(JSON.parse(readFileSync(input, "utf8")));
  mkdirSync(dirname(output), { recursive: true });
  writeFileSync(output, `${JSON.stringify(normalized, null, 2)}\n`);
  console.log(`normalized ${input} → ${output}`);
}
