# Changelog

The SDK's version **tracks the OpenDPP API contract it speaks**, at **major.minor** — so "which SDK is
this?" answers "the contract it targets" (`openapi.json`'s `info.version`). The **PATCH digit is the
SDK's own lane**: a client-only fix ships against an unchanged contract, which is why the npm and Maven
artifacts may sit at different patch levels (1.11.1 and 1.11.0 both target contract 1.11.0). The lock is
enforced by `typescript/scripts/check-version-lock.mjs`, and drift from the LIVE contract by the weekly
`drift-check` workflow.

The two artifacts are tagged and released independently:

| Artifact | Tag | Registry |
| --- | --- | --- |
| TypeScript | `sdk-ts-<semver>` | npm — [`@opendpp/sdk`](https://www.npmjs.com/package/@opendpp/sdk) |
| Java / Kotlin | `sdk-java-<semver>` | Maven Central — `eu.opendpp-node:opendpp-sdk` |

Both clients are **generated from the contract**, so a `X.Y.0` entry below is a regeneration: what
changed is whatever the contract changed. Those contract-level notes are published per version in the
[opendpp-interop changelog](https://github.com/OpenDPP/opendpp-interop/blob/main/CHANGELOG.md), which
carries the same `openapi.json`; this file records what changed **in the SDKs**. Format:
[Keep a Changelog](https://keepachangelog.com).

## [1.11.1] — 2026-07-10 · TypeScript

The first release to use the SDK patch lane: contract 1.11.0 is unchanged — these are client-only fixes.

### Added
- **major.minor version lock**, giving each SDK a patch lane for client-only fixes. Previously the
  version had to match the contract exactly, so a client bug could not be shipped without a contract
  bump. `check-version-lock.mjs` now requires `major.minor` to equal the vendored contract's and the
  patch digit never to trail it.

### Fixed
- **Honest content negotiation on the public resolvers** — the generated client's `Accept` handling for
  the content-negotiated resolver endpoints.
- **Publish provenance**: pinned the publish workflow's npm to major 11; npm 12.0.0 breaks provenance.

## [1.11.0] — 2026-07-06 · TypeScript · 2026-07-10 · Java/Kotlin (first release)

Targets API contract **1.11.0** (client idempotency — an optional `Idempotency-Key` request header on
passport create/bulk).

### Added
- **The Java/Kotlin SDK** — `eu.opendpp-node:opendpp-sdk` on Maven Central, generated and version-locked
  to contract 1.11.0. Kotlin consumes the same artifact.
- Java releases auto-release once Central validates the deployment.

### Fixed
- **Java: survive real payloads** — hardening of the generated client from a deep review against actual
  API responses.

### Changed
- TypeScript client regenerated for contract 1.11.0.

## [1.10.0] — 2026-07-06 · TypeScript

Targets API contract **1.10.0** (Audit Pass 2 — trust-stack + resolver hardening). Some contract changes
in this line are behavioural breaks on `/api/v1`; see the contract notes.

### Changed
- TypeScript client regenerated for contract 1.10.0.

## [1.9.0] — 2026-07-02 · TypeScript

Targets API contract **1.9.0**.

### Changed
- TypeScript client regenerated for contract 1.9.0.

## [1.8.0] — 2026-06-30 · TypeScript

Targets API contract **1.8.0**.

### Changed
- TypeScript client regenerated for contract 1.8.0.

## [1.7.0] — 2026-06-28 · TypeScript

Targets API contract **1.7.0**.

### Changed
- TypeScript client regenerated for contract 1.7.0.
- Publishing returned to **keyless** (OIDC trusted publishing) once the npm Trusted Publisher existed —
  1.6.0 had been bootstrapped with a one-time token (see below).

## [1.6.0] — 2026-06-28 · TypeScript (first release)

The first `@opendpp/sdk` release, targeting API contract **1.6.0**.

### Added
- **The TypeScript SDK** — a client generated from the public OpenDPP contract, with the version-lock
  guard and a keyless (OIDC trusted publishing) release path.

### Notes
- This one release was published with a one-time `NPM_TOKEN` + `--provenance`, because npm's Trusted
  Publisher configuration cannot be created until the package exists. Every release since is keyless.
