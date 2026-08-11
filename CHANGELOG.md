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

**Authored upstream, mirrored here.** This file is written in the OpenDPP node repository and pushed to
`opendpp-sdk` by the same job that regenerates the clients, so an edit made in the mirror is overwritten
by the next sync — send it upstream instead. A section is authored when a version is generated —
*before* its tags exist — so newer sections name the lanes without a release date; the dated headings
below predate that flow.

## [1.13.0] — TypeScript · Java/Kotlin

Targets API contract **1.13.0** (the 1.12.x line closed unpublished — 1.12.3's documentation-only
rewrite ships here). Both clients regenerate with real surface changes:

- **New operation** `bulkRecordBatteryUnitEvents` (`POST /api/v1/units/{id}/events/bulk`) and its
  `BulkBatteryUnitEventsRequest`/`BulkBatteryUnitEventsResponse` types.
- `listBatteryUnitEvents` gains `limit` and `cursor` parameters, and its response type gains the
  required nullable `nextCursor` field — cursor-paged reads replace the fixed newest-500 window.
- Both per-unit event writes accept an optional `Idempotency-Key` header parameter.
- **Removed:** the `auditEventLineage` operation and the
  `TraceComplianceAuditResponse`/`TraceComplianceCertificate` types (the screening surface is
  retired), and the `BatteryUnitDeleteResponse` type — `deleteBatteryUnit` now documents its
  only real outcome, the `409` refusal, so its generated success type is gone.

A client compiled against 1.12.x that referenced the removed operation or types will not compile
against 1.13.0 — pre-adoption break, shipped under the recorded contract waiver.

## [1.12.2] — TypeScript · Java/Kotlin

Targets API contract **1.12.2**. Both clients regenerated; **no generated operation, type or field
changed** — the diff is the doc comments the generator emits, plus the `GET /context/v1` example,
whose schema.org terms moved to https.

### Changed
- The QR exports (`getPassportQrCode`, `getBatteryUnitQrCode`, `bulkExportPassportLabels`) had
  documented the returned PNG as exactly `size` px wide. The comments now name the one case a raster
  cannot honour — a symbol whose own module grid exceeds the requested `size` renders at that grid
  width instead, at most 185 px — and that SVG output has no pixel floor and always carries the
  requested width.

## [1.12.1] — TypeScript · Java/Kotlin

Targets API contract **1.12.1** (documentation only). Both clients regenerated; **no generated
operation, type or field changed** — the diff is entirely in the doc comments the generator emits.

### Changed
- Forty-eight authenticated operations had described their rate limit as the flat anonymous per-IP
  ceiling. Their doc comments now state the per-key plan ladder (**Growth** 120/min, **Scale** 600/min,
  **Enterprise** unlimited), the 3x ceiling across a workspace's keys, and that `429` carries
  `Retry-After`. If you sized a client's throttling against the old figure, you were throttling well
  below your plan.
- Thirty-three request and response models gained a type-level description — across accounts, battery
  units, facilities, operators, passports, traceability and webhooks — so IDE hover and generated
  reference docs now say what each model is for instead of showing a bare name.

## [1.12.0] — TypeScript · Java/Kotlin

Targets API contract **1.12.0** (the anonymous compute surface is closed). Both clients regenerated; no
generated operation was removed or renamed.

### Changed
- `POST /api/v1/passports/validate-only-public` now carries a **security requirement** and a `401`
  response. Any API key or Console session is accepted and no permission is required, but an
  unauthenticated call is rejected — configure a credential on the client before calling it.
- The GS1 helpers (`POST /api/v1/gs1/decode`, `/decode/batch` and `/gtin`) remain anonymously callable
  but meter an anonymous caller to **2 requests/min per IP**, so an uncredentialed client can now see
  `429` on them. Sending an API key restores the normal rate-limit ladder.

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
