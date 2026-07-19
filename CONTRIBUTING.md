# Contributing to the OpenDPP SDKs

Thanks for your interest in OpenDPP. This short guide explains what this repository is, why pull
requests are closed automatically, and which doors *are* open.

## What this repository is (and isn't)

This repo is a **generated mirror**. Every SDK here — TypeScript (`@opendpp/sdk` on npm), Java/Kotlin
(`eu.opendpp-node:opendpp-sdk` on Maven Central), Python next — is **mechanically generated from the
public OpenDPP OpenAPI contract** and version-locked (major.minor) to it. The OpenDPP **product backend is a
separate, private repository** — it is the source of truth for the API contract the SDKs are generated
from.

That split drives the one rule that matters most:

> ### 🚫 The SDK sources are **generated — do not hand-edit them, and pull requests here are closed automatically.**
>
> A change made in a PR here can't flow back into the contract it was generated from, and would be
> overwritten on the next regeneration. So an automated responder politely closes fork PRs with a
> pointer back here. This is not a rejection of your idea — it's the wrong door for it.

## The doors that *are* open

- **Found a bug, a generation glitch, or want a change?** Please
  [**open an issue**](https://github.com/OpenDPP/opendpp-sdk/issues) here — issues are welcome and are
  the right report/proposal door. If the fix belongs in the API contract itself, that's a **product**
  change and we'll route it to the private backend.
- **Hand-authored interop / kit contributions** (schemas, the offline conformance validator, samples,
  field mappings) — **PRs are welcome** at
  [`OpenDPP/opendpp-interop`](https://github.com/OpenDPP/opendpp-interop).
- **Docs & live API reference:** <https://opendpp-node.eu> ·
  [API reference](https://opendpp-node.eu/api-reference) ·
  [`openapi.json`](https://opendpp-node.eu/openapi.json).

## Keep claims honest

The SDKs are **ergonomics only** — typed clients for the hosted node. They embed no server-side
capability, tier/masking logic, or restricted-key knowledge. Please don't describe them as anything
more in issues or discussions.

## License & trademark

The SDK content is [Apache-2.0](./LICENSE) © Opendpp UAB; see [`NOTICE`](./NOTICE) and
[`TRADEMARK.md`](./TRADEMARK.md). "OpenDPP" is a trademark of Opendpp UAB; the license grants no rights
to the marks.
