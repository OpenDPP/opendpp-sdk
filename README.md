# OpenDPP SDKs

Official client SDKs for the [OpenDPP](https://opendpp-node.eu) Digital Product Passport API —
**generated from the public OpenAPI contract** and **version-locked** to it.

| SDK | Package | Source |
| --- | --- | --- |
| **TypeScript** | [`@opendpp/sdk`](https://www.npmjs.com/package/@opendpp/sdk) (npm) | [`typescript/`](./typescript) |
| **Python** | `opendpp` (PyPI) — *planned* | — |

> Part of the OpenDPP **open client** surface (Apache-2.0). The SDKs are **ergonomics only** — they
> embed no tier/masking logic and no restricted-key knowledge; every privileged operation is a typed
> call to the hosted node behind a Developer-Plan key. A rival could regenerate an equivalent from the
> public spec in an afternoon — the value is the hosted node it calls, not the client.

## TypeScript

```sh
npm install @opendpp/sdk
```

```ts
import { createOpenDppClient, getHealth } from "@opendpp/sdk";

const client = createOpenDppClient({ apiKey: process.env.OPENDPP_API_KEY });
const health = await getHealth({ client });
```

Full SDK + docs: [`typescript/`](./typescript).

## Python

Coming next — the same generation pipeline, published to PyPI as `opendpp`.

## How it's built

Each SDK is mechanically generated from [`openapi.json`](https://opendpp-node.eu/openapi.json) (the
curated public API contract) and **version-locked to `OPENAPI_VERSION`** — `@opendpp/sdk@1.8.0`
targets contract `1.8.0`. CI regenerates and fails on any drift, so the committed client always matches
the spec. Releases are keyless (OIDC trusted publishing). Nothing here re-implements a server-side
capability.

## Related OpenDPP repositories

- [**opendpp-interop**](https://github.com/OpenDPP/opendpp-interop) — interop boundary kit: official AAS + UNTP schemas, validated samples, an offline conformance validator, the OpenAPI contract.
- [**opendpp-knowledge**](https://github.com/OpenDPP/opendpp-knowledge) — the API as an OKF (Open Knowledge Format) bundle for AI agents.
- [**Live service**](https://opendpp-node.eu) · [API reference](https://opendpp-node.eu/api-reference) · [`openapi.json`](https://opendpp-node.eu/openapi.json)

## License & trademark

[Apache-2.0](./LICENSE) © Opendpp UAB. See [`NOTICE`](./NOTICE) and [`TRADEMARK.md`](./TRADEMARK.md).
"OpenDPP" is a trademark of Opendpp UAB; the license grants no rights to the marks.
