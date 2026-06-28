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

import { createClient, createConfig } from "./generated/client";

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
  return createClient(
    createConfig({
      baseUrl: options.baseUrl ?? "https://opendpp-node.eu",
      ...(options.apiKey ? { auth: () => options.apiKey } : {}),
    }),
  );
}
