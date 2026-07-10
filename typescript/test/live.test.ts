// Live smoke test against the public hosted node (Apache-2.0, (c) Opendpp UAB) — the real proof the
// generated client works end-to-end: TLS, base-URL wiring, content negotiation, and JSON parsing of
// REAL payloads into the generated types. The TypeScript sibling of the Java SDK's OpenDppLiveIT.
//
// Opt-in (network + public rate limits): OPENDPP_LIVE_TEST=1 npm test. Uses only public endpoints
// (no API key) and a curated, stable demo passport listed in the public sitemap. Makes 5 requests
// against the 30 req/min public-resolution limit — keep it that small.
import test from "node:test";
import assert from "node:assert/strict";
import {
  createOpenDppClient,
  getApiVersion,
  getHealth,
  resolveGs1Gtin,
  resolvePublicPassport,
  resolvePublicPassportAs,
  type PublicPassportJsonLd,
} from "../src/index.ts";

const LIVE = process.env.OPENDPP_LIVE_TEST === "1";
const skip = LIVE ? false : "opt-in: set OPENDPP_LIVE_TEST=1 (network + public rate limits)";
const DEMO_PASSPORT_ID = "demo-batteries-lfp-cell-200";

test("live: health and version round-trip", { skip }, async () => {
  const client = createOpenDppClient();

  const health = await getHealth({ client });
  assert.equal(health.error, undefined);
  assert.ok(health.data?.status, "health.status");

  const version = await getApiVersion({ client });
  assert.equal(version.error, undefined);
  assert.ok(
    version.data?.apiVersion.startsWith("1."),
    `live contract major should match the SDK's /api/v1 major, got ${version.data?.apiVersion}`,
  );
});

test("live: resolves the demo passport into the typed JSON-LD document", { skip }, async () => {
  const client = createOpenDppClient();
  const { data, error, response } = await resolvePublicPassport({ client, path: { id: DEMO_PASSPORT_ID } });

  assert.equal(error, undefined);
  assert.match(
    response.headers.get("content-type") ?? "",
    /application\/ld\+json/,
    "the pinned Accept must land on the JSON-LD representation the types describe",
  );

  const passport = data as PublicPassportJsonLd;
  // Identity + typed fields survived the wire.
  assert.equal(passport.id, DEMO_PASSPORT_ID, "id");
  assert.ok(passport.productId, "productId");
  assert.ok(passport.createdAt, "createdAt");

  // The status is one of the contract's lifecycle states (a MINOR release may add values — the
  // runtime tolerates unknowns since nothing validates; this asserts today's live value is known).
  assert.ok(
    (["DRAFT", "ACTIVE", "RECALLED", "DECOMMISSIONED"] as const).includes(passport.status),
    `status should be a known lifecycle state, got ${passport.status}`,
  );

  // Demo passports are sealed — the proof block must populate.
  assert.ok(passport.proof, "sealed demo passport should carry a proof");
  assert.ok(passport.proof?.merkleRoot, "proof.merkleRoot");

  // The free-form metadata object arrives as a non-empty map.
  const metadata = passport.metadata as Record<string, unknown>;
  assert.ok(metadata && Object.keys(metadata).length > 0, "demo passport metadata should be non-empty");
});

test("live: resolves the same passport through the GS1 Digital Link path", { skip }, async () => {
  const client = createOpenDppClient();
  const byId = await resolvePublicPassport({ client, path: { id: DEMO_PASSPORT_ID } });
  assert.equal(byId.error, undefined);

  const gtin = byId.data?.productId;
  assert.ok(gtin && /^\d{14}$/.test(gtin), `demo battery passport should be GS1-keyed (14-digit GTIN), got ${gtin}`);

  const byGtin = await resolveGs1Gtin({ client, path: { gtin14: gtin } });
  assert.equal(byGtin.error, undefined);
  assert.equal(byGtin.data?.id, byId.data?.id, "GS1 Digital Link resolution should land on the same passport");
});

test("live: the vc+jwt representation arrives as a compact JWS string", { skip }, async () => {
  const client = createOpenDppClient();
  const { data, error } = await resolvePublicPassportAs({
    client,
    path: { id: DEMO_PASSPORT_ID },
    accept: "application/vc+jwt",
  });

  assert.equal(error, undefined);
  assert.equal(typeof data, "string", "vc+jwt must parse as text, not a Blob");
  assert.match(data as string, /^ey[\w-]+\.[\w-]+\.[\w-]+$/, "expected a compact JWS (three base64url segments)");
});

test("live: a missing passport surfaces the typed 404 error", { skip }, async () => {
  const client = createOpenDppClient();
  const { data, error, response } = await resolvePublicPassport({
    client,
    path: { id: "definitely-not-a-passport-xyz" },
  });

  assert.equal(data, undefined);
  assert.equal(response.status, 404, "expected a 404 for a missing passport");
  assert.ok(error && typeof error === "object", "error body should be captured for diagnostics");
});
