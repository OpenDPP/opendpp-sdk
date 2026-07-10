/**
 * @opendpp/sdk — Official TypeScript SDK for the OpenDPP Digital Product Passport API.
 *
 * A fully-typed client for the OpenDPP public API, mechanically generated from the public OpenAPI
 * contract (`openapi.json`) and version-locked to it. Every export under `./generated` is a typed
 * operation or schema; the only hand-written code here is the thin `createOpenDppClient` ergonomics
 * helper below.
 *
 * Boundary: this SDK is **ergonomics only**. It embeds no tier/masking logic and no restricted-key
 * knowledge — every privileged operation is simply a typed call to the hosted node behind your
 * Developer-Plan key. It leaks nothing the public spec doesn't already.
 *
 * Copyright (c) Opendpp UAB.
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations.
 *
 * "OpenDPP" is a trademark of Opendpp UAB; the Apache-2.0 license grants no rights to the marks.
 */

// Every generated operation (createPassport, sealPassport, getPassport, decodeGs1, whoami, …) + every
// schema type. These are the typed calls to the hosted node.
export * from "./generated";

// The pre-configured default client instance (call `client.setConfig({ baseUrl, auth })` to configure
// it globally instead of passing a client per operation).
export { client } from "./generated/client.gen";

import {
  resolveGs1Grai,
  resolveGs1Gtin,
  resolvePublicBatteryUnit,
  resolvePublicPassport,
} from "./generated";
import type {
  AasEnvironment,
  Options,
  PublicBatteryUnitJsonLd,
  PublicPassportJsonLd,
  ResolveGs1GraiData,
  ResolveGs1GraiError,
  ResolveGs1GtinData,
  ResolveGs1GtinError,
  ResolvePublicBatteryUnitData,
  ResolvePublicBatteryUnitError,
  ResolvePublicPassportData,
  ResolvePublicPassportError,
} from "./generated";
import { createClient, createConfig } from "./generated/client";
import { client as defaultClient } from "./generated/client.gen";

export interface OpenDppClientOptions {
  /** Base URL of the OpenDPP node. Defaults to the public hosted node, `https://opendpp-node.eu`. */
  baseUrl?: string;
  /**
   * Developer-Plan API key. When set, it is sent as `Authorization: Bearer <apiKey>` on operations
   * that declare a security requirement. Public operations work without it.
   */
  apiKey?: string;
}

/**
 * Creates a configured OpenDPP API client. Pass the returned client to any operation via its `client`
 * option — e.g. `getPassport({ client, path: { id } })` — or call `client.setConfig(...)` to adjust it.
 *
 * Ergonomics only: this just wires the base URL and bearer auth. Every privileged operation remains a
 * call to the hosted node behind your Developer-Plan key.
 *
 * @example
 * ```ts
 * import { createOpenDppClient, createPassport, getHealth } from "@opendpp/sdk";
 *
 * const client = createOpenDppClient({ apiKey: process.env.OPENDPP_API_KEY });
 * const health = await getHealth({ client });
 * const created = await createPassport({ client, body: { productId, metadata } });
 * ```
 */
export function createOpenDppClient(options: OpenDppClientOptions = {}) {
  const client = createClient(
    createConfig({
      baseUrl: options.baseUrl ?? "https://opendpp-node.eu",
      ...(options.apiKey ? { auth: () => options.apiKey } : {}),
    }),
  );
  client.interceptors.request.use(pinDefaultJsonLdAccept);
  return client;
}

// ---------------------------------------------------------------------------
// Content-negotiated public resolvers
//
// The 200 of `GET /passport/{id}`, `/01/{gtin14}`, `/8003/{grai}` and `/unit/{id}` is negotiated via
// the `Accept` header (JSON-LD default / AAS / VC-JWT / VC-LD / SD-JWT-VC / HTML), but the generated
// operations are typed against the default JSON-LD document only, and the bundled fetch client parses
// an `application/vc+jwt` / `application/dc+sd-jwt` body as a Blob (its Content-Type inference only
// treats `+json` suffixes as JSON). Two guards keep the typed surface honest:
//
//  1. every client from this module pins `Accept: application/ld+json` on those paths when the caller
//     didn't choose a representation, so a typed `resolvePublicPassport(...)` receives the JSON-LD
//     document by construction — not because the runtime's implicit `Accept: */*` happens to hit the
//     server's default branch;
//  2. the `*As` helpers below are the supported way to request an alternate representation — they set
//     the `Accept` header AND the matching body parsing, and type `data` per media type. Passing a
//     hand-rolled `Accept` header to a generated resolver operation would silently mismatch its
//     declared response type.
// ---------------------------------------------------------------------------

/** The paths whose 200 is `Accept`-negotiated (the public resolvers). */
const NEGOTIATED_RESOLVER_PREFIXES = ["/passport/", "/01/", "/8003/", "/unit/"];

/** Pins the canonical JSON-LD representation on resolver requests that don't state an `Accept`. */
function pinDefaultJsonLdAccept(request: Request): Request {
  if (request.method === "GET" && !request.headers.has("Accept")) {
    let pathname: string;
    try {
      pathname = new URL(request.url).pathname;
    } catch {
      return request;
    }
    if (NEGOTIATED_RESOLVER_PREFIXES.some((prefix) => pathname.startsWith(prefix))) {
      request.headers.set("Accept", "application/ld+json");
    }
  }
  return request;
}

// The default exported client gets the same guard as createOpenDppClient() clients.
defaultClient.interceptors.request.use(pinDefaultJsonLdAccept);

/**
 * Representations negotiable on `GET /passport/{id}`, `GET /01/{gtin14}` and `GET /8003/{grai}`,
 * mapped to the wire type each one yields: the JSON-LD passport document, the role-filtered AAS
 * environment, a UNTP credential as a compact JWS string (`vc+jwt`), the same credential with an
 * embedded Data Integrity proof (`vc+ld+json`), an SD-JWT-VC string (`dc+sd-jwt`), or the
 * server-rendered HTML page.
 */
export interface PassportRepresentations {
  "application/ld+json": PublicPassportJsonLd;
  "application/aas+json": AasEnvironment;
  "application/vc+jwt": string;
  "application/vc+ld+json": Record<string, unknown>;
  "application/dc+sd-jwt": string;
  "text/html": string;
}

/** Representations negotiable on `GET /unit/{id}` — same as passports, minus AAS (not offered there). */
export interface BatteryUnitRepresentations {
  "application/ld+json": PublicBatteryUnitJsonLd;
  "application/vc+jwt": string;
  "application/vc+ld+json": Record<string, unknown>;
  "application/dc+sd-jwt": string;
  "text/html": string;
}

/**
 * Result of the `*As` helpers: hey-api's non-throwing fields shape, with `data` typed per the
 * requested representation. A VC media type can also yield a 406 `error` when the passport has no
 * manufacturing facility with a country of production (see the operation docs).
 */
export type NegotiatedResult<TData, TError> = Promise<
  ({ data: TData; error: undefined } | { data: undefined; error: TError }) & {
    request: Request;
    response: Response;
  }
>;

/** `+json` representations parse as JSON; `vc+jwt`, `dc+sd-jwt` and HTML are text on the wire. */
const parseAsFor = (accept: string) => (accept.endsWith("+json") ? ("json" as const) : ("text" as const));

/** `GET /passport/{id}` in an explicitly chosen representation, correctly parsed and typed. */
export const resolvePublicPassportAs = <A extends keyof PassportRepresentations>(
  options: Omit<Options<ResolvePublicPassportData>, "headers"> & { accept: A },
): NegotiatedResult<PassportRepresentations[A], ResolvePublicPassportError> => {
  const { accept, ...rest } = options;
  return resolvePublicPassport({
    ...rest,
    headers: { Accept: accept },
    parseAs: parseAsFor(accept),
  }) as unknown as NegotiatedResult<PassportRepresentations[A], ResolvePublicPassportError>;
};

/** `GET /01/{gtin14}` in an explicitly chosen representation, correctly parsed and typed. */
export const resolveGs1GtinAs = <A extends keyof PassportRepresentations>(
  options: Omit<Options<ResolveGs1GtinData>, "headers"> & { accept: A },
): NegotiatedResult<PassportRepresentations[A], ResolveGs1GtinError> => {
  const { accept, ...rest } = options;
  return resolveGs1Gtin({
    ...rest,
    headers: { Accept: accept },
    parseAs: parseAsFor(accept),
  }) as unknown as NegotiatedResult<PassportRepresentations[A], ResolveGs1GtinError>;
};

/** `GET /8003/{grai}` in an explicitly chosen representation, correctly parsed and typed. */
export const resolveGs1GraiAs = <A extends keyof PassportRepresentations>(
  options: Omit<Options<ResolveGs1GraiData>, "headers"> & { accept: A },
): NegotiatedResult<PassportRepresentations[A], ResolveGs1GraiError> => {
  const { accept, ...rest } = options;
  return resolveGs1Grai({
    ...rest,
    headers: { Accept: accept },
    parseAs: parseAsFor(accept),
  }) as unknown as NegotiatedResult<PassportRepresentations[A], ResolveGs1GraiError>;
};

/** `GET /unit/{id}` in an explicitly chosen representation, correctly parsed and typed. */
export const resolvePublicBatteryUnitAs = <A extends keyof BatteryUnitRepresentations>(
  options: Omit<Options<ResolvePublicBatteryUnitData>, "headers"> & { accept: A },
): NegotiatedResult<BatteryUnitRepresentations[A], ResolvePublicBatteryUnitError> => {
  const { accept, ...rest } = options;
  return resolvePublicBatteryUnit({
    ...rest,
    headers: { Accept: accept },
    parseAs: parseAsFor(accept),
  }) as unknown as NegotiatedResult<BatteryUnitRepresentations[A], ResolvePublicBatteryUnitError>;
};
