# @opendpp/sdk

Official **TypeScript SDK** for the [OpenDPP](https://opendpp-node.eu) Digital Product Passport API —
a fully-typed client for every endpoint, **generated from the public OpenAPI contract** and
**version-locked** to it (this package's major.minor **is** the API contract version). ESM, Node ≥ 20,
**zero runtime dependencies** (the fetch client is bundled).

> Part of the OpenDPP **open client** surface (Apache-2.0). The SDK is **ergonomics only** — it embeds
> no tier/masking logic and no restricted-key knowledge; every privileged operation is a typed call to
> the hosted node behind your Developer-Plan key. A rival could regenerate an equivalent from the
> public spec in an afternoon — the value is the hosted node it calls, not the client.

## Install

```sh
npm install @opendpp/sdk
```

## Quick start

```ts
import { createOpenDppClient, getHealth, createPassport, getPassport } from "@opendpp/sdk";

const client = createOpenDppClient({ apiKey: process.env.OPENDPP_API_KEY });

// Public, no key needed:
const health = await getHealth({ client });

// Behind your Developer-Plan key:
const created = await createPassport({
  client,
  body: { productId: "09501101531000", metadata: { category: "batteries", /* … */ } },
});

const passport = await getPassport({ client, path: { id: created.data!.id } });
```

Every operation is a tree-shakeable, fully-typed function (`createPassport`, `sealPassport`,
`decodeGs1`, `resolveGs1Gtin`, `whoami`, …) and every request/response shape is an exported type. The
OpenAPI description for each endpoint comes through as JSDoc, so your editor shows permissions, rate
limits, and content-negotiation notes inline.

### Configuring the client

`createOpenDppClient({ baseUrl?, apiKey? })` defaults `baseUrl` to the public node
(`https://opendpp-node.eu`) and sends the key as `Authorization: Bearer …` on operations that require
it. You can also configure the exported default `client` globally instead of passing it per call:

```ts
import { client, listPassports } from "@opendpp/sdk";
client.setConfig({ baseUrl: "https://opendpp-node.eu", auth: () => process.env.OPENDPP_API_KEY });
const page = await listPassports();
```

### Content-negotiated public resolvers

The 200 of the public resolvers (`GET /passport/{id}`, `/01/{gtin14}`, `/8003/{grai}`, `/unit/{id}`)
is negotiated via the `Accept` header, but the generated operations are typed against the **default
JSON-LD** representation only. Two guards keep the typed surface honest:

- Clients from this package (both `createOpenDppClient()` and the default `client`) **pin
  `Accept: application/ld+json`** on those paths when you don't set an `Accept` yourself, so a typed
  `resolvePublicPassport(...)` receives the JSON-LD document by construction.
- To request an **alternate representation** (AAS environment, `vc+jwt` / `vc+ld+json` /
  `dc+sd-jwt` credential, HTML page), use the `*As` helpers — `resolvePublicPassportAs`,
  `resolveGs1GtinAs`, `resolveGs1GraiAs`, `resolvePublicBatteryUnitAs` — which set the `Accept`
  header **and** the matching body parsing, and type `data` per media type. (Hand-rolling the
  `Accept` header on a generated resolver operation would silently mismatch its declared response
  type — the bundled fetch client would even hand you a `Blob` for the JWT representations.)

```ts
import { resolvePublicPassport, resolvePublicPassportAs } from "@opendpp/sdk";

const { data: jsonLd } = await resolvePublicPassport({ client, path: { id } }); // PublicPassportJsonLd
const { data: jws } = await resolvePublicPassportAs({ client, path: { id }, accept: "application/vc+jwt" }); // string
const { data: aas } = await resolvePublicPassportAs({ client, path: { id }, accept: "application/aas+json" }); // AasEnvironment
```

## Versioning

`@opendpp/sdk`'s **major.minor is version-locked to `OPENAPI_VERSION`** — e.g. `@opendpp/sdk@1.11.x`
targets API contract `1.11`. The **patch digit is the SDK's own lane**: client-only fixes ship as
patch releases against the same contract (e.g. `1.11.1` fixes the client, still targeting contract
`1.11.0`), so always take the latest patch of your contract's major.minor. The generated client under
`src/generated/` is committed and CI-checked against `openapi.json` (a drift guard: regenerating must
produce no diff). Install the SDK major that matches the `/api/v1` major you call.

## License

[Apache-2.0](./LICENSE) © Opendpp UAB. See [`NOTICE`](./NOTICE). "OpenDPP" is a trademark of Opendpp
UAB; this license grants no rights to the marks.
