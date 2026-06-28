# @opendpp/sdk

Official **TypeScript SDK** for the [OpenDPP](https://opendpp-node.eu) Digital Product Passport API —
a fully-typed client for every endpoint, **generated from the public OpenAPI contract** and
**version-locked** to it (this package's version **is** the API contract version). ESM, Node ≥ 20,
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

## Versioning

`@opendpp/sdk` is **version-locked to `OPENAPI_VERSION`** — e.g. `@opendpp/sdk@1.7.0` targets API
contract `1.7.0`. The generated client under `src/generated/` is committed and CI-checked against
`openapi.json` (a drift guard: regenerating must produce no diff). Install the SDK major that matches
the `/api/v1` major you call.

## License

[Apache-2.0](./LICENSE) © Opendpp UAB. See [`NOTICE`](./NOTICE). "OpenDPP" is a trademark of Opendpp
UAB; this license grants no rights to the marks.
