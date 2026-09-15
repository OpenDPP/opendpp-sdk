<!-- Copyright (c) Opendpp UAB. SPDX-License-Identifier: LicenseRef-OpenDPP-Proprietary -->

# Changelog

The SDK's version **tracks the OpenDPP API contract it speaks**, at **major.minor** — so "which SDK is
this?" answers "the contract it targets" (`openapi.json`'s `info.version`). The **PATCH digit is the
SDK's own lane**: a client-only fix ships against an unchanged contract, which is why the npm and Maven
artifacts may sit at different patch levels (1.11.1 and 1.11.0 both target contract 1.11.0). The lock is
enforced by `typescript/scripts/check-version-lock.mjs`, and drift from the LIVE contract by the weekly
`drift-check` workflow.

The three artifacts are tagged and released independently:

| Artifact | Tag | Registry |
| --- | --- | --- |
| TypeScript | `sdk-ts-<semver>` | npm — [`@opendpp/sdk`](https://www.npmjs.com/package/@opendpp/sdk) |
| Java / Kotlin | `sdk-java-<semver>` | Maven Central — `eu.opendpp-node:opendpp-sdk` |
| Python | `sdk-py-<semver>` | PyPI — [`opendpp-sdk`](https://pypi.org/project/opendpp-sdk/) |

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

## [1.16.0] — TypeScript · Java/Kotlin · Python

Targets API contract **1.16.0** (the passport document, its identifiers and the API's conduct aligned
with the EN 182xx Digital Product Passport standards). A regeneration with model changes and four new
operations:

- The JSON-LD passport models (`PublicPassportJsonLd`, `PassportListItem`), the unit documents and the
  tombstone model gain the nine EN 18223 header fields — `digitalProductPassportId`,
  `uniqueProductIdentifier`, `granularity`, `dppSchemaVersion`, `dppStatus`, `lastUpdated`,
  `economicOperatorId`, `facilityId`, `contentSpecificationIds` — seven of them REQUIRED, `facilityId`
  and `contentSpecificationIds` optional, and the required `productIdScheme` enum
  (`GS1_DIGITAL_LINK | IDENTIFICATION_LINK`). `granularity` is the enum `model | batch | item` on the
  passport models and the constant `item` on the unit models; `dppStatus` is a free string.
- The three passport-resolution operations declare an additional `application/xml` response — the
  EN 18223 Annex B serialisation of the same document, typed as a string. Generated clients that select
  a response by media type gain it; one that always sends `Accept: application/json` is unaffected,
  since the JSON-LD representation is still what an absent or JSON `Accept` returns.
- `PublicPassportJsonLd` and `PassportListItem` lose the `metadata` member: the passport's data elements
  are the document's additional root properties (EN 18223 clause 5.2) — `passport.metadata.category`
  becomes `passport.category`, in the strict lanes through the additional-properties map.
  `MerkleTreeAttestationProof` gains the required `sealedKeys` string list.
- `PassportListItem` loses its `proof` member. It was always `{}` when sealed and `null` otherwise, so no
  client could read anything from it; a client that needs the proof reads the single passport, and
  `digitalSeal` on the list item still says whether one exists.
- `PassportListItem.manufacturingFacility` widens from `null` to the same nullable facility node
  `PublicPassportJsonLd` declares. Only the declared type changes — the listing always carried the node,
  so a client that validated list responses against the previous document was rejecting every entry.
- Inside that map, a document reference is an object (`{ contentType, url, language?, resourceTitle? }`
  under a name such as `declarationOfConformity` or `safetyDatasheet`, replacing the `…Url` strings), and
  language-dependent text (`productName`, care instructions, e-waste instructions and five prose fields)
  is an array of `{ value, language }`. No generated model changes; a client that wrote the string forms
  now receives a 400 it should surface. A `language` tag's two halves are checked against the ISO 639 and
  ISO 3166-1 code lists, so a well-shaped tag that names no language or no country (`xx-GB`) is a 400 too.
- The QR exports' `hri` option labels an Identification Link with its URL (the information the carrier
  encodes, EN 18220 5.7.2) where it returned a symbol with no label; a GS1 Digital Link keeps the element
  string. No model change.
- `RegisterOperatorRequest.regIdScheme` becomes REQUIRED, an enum of `VAT | DUNS | LEI | GLN`; the
  request, `UpdateOperatorRequest` and `OperatorRow` gain the optional `eori` field; `OperatorRow.regIdScheme`
  and `EconomicOperatorNode.regIdScheme` are non-nullable enums (with `UNDECLARED`). A client that
  registered operators with `regIdScheme: "EORI"`, or without a scheme, now receives a 400 — the EORI
  belongs in `eori`.
- The public passport read, the owner-tier read and the GS1 resolver operations gain the optional
  `representation` query parameter (`compressed` | `full` | `expanded` — `full` is the value EN 18222
  clause 8.1 names, `expanded` an accepted alias for the same Annex A form); new models
  `PublicPassportJsonLdExpanded` and the recursive `En18223DataElement`; `Error.code` gains
  `UNSUPPORTED_REPRESENTATION`, `UNSUPPORTED_KEY_QUALIFIER` and `DRAFT_DEMOTION_REFUSED`, and is now
  generated from the service's error catalog. The `DRAFT_DEMOTED` advisory code is removed from the
  `AdvisoryItem.code` enum along with the behaviour it described.
- The three passport-history operations no longer declare a required permission: an access grant
  (`dpp_li_…` / `dpp_auth_…` as a Bearer token or `?grant=`) now reads an archived version, masked to
  the grant tier. A client sending no credential gets 401 where it previously got 401 as well, but one
  sending a credential that unlocks nothing now gets 404 rather than 403. Attribution (`changedBy`,
  `changeReason`) is `null` for a grant holder.
- Behaviour a client reading passport HISTORY must handle: `getPassportVersion` and
  `getPassportVersionAtDate` now return the version as a document in a new `passport` member, and
  `metadata` is `null` whenever that is present — read `passport.<key>` where you read
  `metadata.<key>`. `PassportHistoryVersion` also gains `contentHash` (the chained integrity hash) and
  `documentAvailable` (false only for a version archived before whole-row snapshots existed, where
  `metadata` still carries the data).
- Behaviour a client reading the EXPANDED representation must handle: `PublicPassportJsonLdExpanded`
  moves `economicOperator` and `manufacturingFacility` out of the root and into `elements` (they are data
  elements; the compressed form is unchanged), and a restricted element now appears with
  `redacted: true`, its dictionary-defined `objectType` and NO `value` — where it previously appeared as
  a string element carrying the redaction placeholder. Read the operator and facility from `elements`,
  and branch on `redacted` rather than string-matching the placeholder.
- Behaviour a client that saves drafts must handle: `updatePassport` with `draft: true` on a passport
  that is already published now answers 409 `DRAFT_DEMOTION_REFUSED` instead of taking it offline and
  returning 200 with a warning. Publishing is one-way; use the status operation (`RECALLED` or
  `DECOMMISSIONED`) to withdraw a passport, which preserves its archived versions.
- Four new operations: `listPassportHistory`, `getPassportVersion`, `getPassportVersionAtDate`
  (models `PassportHistoryList`, `PassportHistoryVersionSummary`, `PassportHistoryVersion`,
  `PassportHistoryError`) and `getDppVocabulary` (`GET /ns/dpp`).
- A new `CarrierDeclaration` model (EN 18220: `symbology`, `placement`, and the optional
  `xDimensionMm`, `errorCorrection`, `targetEnvironment`, `printQualityGrade`, `durabilityAssessment`)
  reaches five request models as an optional `carrier` — `PassportCreateRequest`,
  `PassportUpdateRequest`, `PassportBulkRow` and, since this release, `PassportValidateOnlyRequest` on
  both validate-only operations. It records what the operator applied to the product; nothing is
  persisted by the dry-run. Two cross-field rules are refusals, not warnings: a radio-frequency
  symbology carries no print fields, and `errorCorrection` applies to `QR_CODE` only — an invalid
  declaration is a 400 on every one of the five, the dry-run included, so a pre-flight check cannot
  bless a payload the save would reject. `AdvisoryItem.code` gains `CARRIER_SYMBOLOGY_NOT_RENDERED`
  (the declared symbology is not the one this service encodes; the declaration is still kept) and
  `CATEGORY_GRANULARITY_UNEXPECTED` (the product group's own instrument fixes a granularity this
  passport does not use). Both are non-blocking, and both now arrive from create, update, bulk and
  validate-only alike.
- Optional `xDimensionMm` on the QR export operations and the bulk-label request model (a nullable
  number; existing calls unaffected). The owner-tier `id` path parameter documents the passport's
  Digital Link URL as an accepted key; the webhook-subscription `url` documents https. Description
  changes only.
- Behaviour a client's error handling sees: a Digital Link with a batch or variant qualifier now
  receives a 400 where it received the model-level passport; a verb mistake receives 405 with `Allow`
  where it received 404; an `http://` webhook receiver receives 400.

## [1.15.0] — TypeScript · Java/Kotlin · Python

Targets API contract **1.15.0** (the verifier says what it declines; delivery records state what
happened). A regeneration with two model changes and one docstring correction:

- `SealVerifyResponse.message` loses its two-member string `enum`, so every lane's model widens from
  an enumerated type to a plain string — code that matched the exact decline messages must switch on
  `verified` instead (the messages also reworded from `Cryptographic verification failed: …` to
  `Verification declined: …, so the seal was not evaluated`).
- The docstring of `getSectorSchema` (`GET /api/v1/schemas/{category}`) no longer says only five
  categories have a published schema and the other four return 404 — all nine do. No model change.
- The generated webhook delivery model gains the `NO_SUBSCRIBERS` status value, so a client
  switching on the status handles the case where an event had no endpoint to reach instead of
  reading it as a successful delivery.
- The generated docstrings for `createWebhookSubscription` and `deleteWebhookSubscription` no longer tell
  the caller to delete and re-create a subscription to change its address or rotate its secret; they point
  at `updateWebhookSubscription` and `rotateWebhookSecret`, which every lane already exposes. No model
  change.

No ergonomics module or lane-specific behaviour changed; no lane-local (patch-level) changes ride
along.

## [1.14.0] — TypeScript · Java/Kotlin · Python

Targets API contract **1.14.0** (contract hygiene for the SDK lanes) and debuts the **Python lane**:
[`opendpp-sdk` on PyPI](https://pypi.org/project/opendpp-sdk/), generated with the same
openapi-generator toolchain as the Java lane (pydantic v2 + urllib3, sync), with an `ergonomics`
module at parity with the TypeScript lane (the `Accept: application/ld+json` pin on the four
content-negotiated resolvers plus the four `resolve_*_as` helpers).

All three lanes now generate from ONE shared spec normalizer (`scripts/normalize-spec.mjs`,
authored and tested upstream) instead of per-lane private rewrites. The retrofit itself is inert —
the Java lane regenerates byte-identical under the ported transforms against contract 1.13.0 — and
the normalizer then adds one new transform with a deliberate effect in both openapi-generator
lanes: **boolean single-value constraints are stripped from the generation input**, because the
generator stringifies the allowed value into its validators (python's `set(['true'])` rejected
every real `ok: true` / `success: false` payload; Java rendered one-value `SuccessEnum`/`OkEnum`
wrappers around what is just a `Boolean`). Java fields typed by those wrappers become plain
`Boolean` — same wire format, simpler surface.

- **Model renames (breaking, under the recorded pre-GA waiver — all lanes).** The contract names its error/result unions, so
  the per-operation synthesized model names disappear in favour of the contract's own:
  `PassportCreateBadRequest`, `PassportBulkBadRequest`, `AasIngestBadRequest`,
  `PassportGetNotFound`, `PassportGetTooManyRequests`, `PassportUpdateBadRequest`,
  `BatteryUnitSerialiseBadRequest`, `BatteryUnitEventBadRequest`, `GrantRevokeForbidden`,
  `OperatorMinimalErrorResponse`, and `Gs1BatchDecodeResult`/`Gs1BatchDecodeOk`/`Gs1BatchDecodeError`.
  `FastifyDefaultBadRequest` is renamed too — a server implementation detail had leaked into a public
  type name — consolidating into the shape-identical `DefaultRequestRejectionError`.
- **Deserialization is tolerant of additive server changes.** Response models stop enforcing
  closed-world shapes (`additionalProperties: false` left every response schema), so a future MINOR
  server release adding a response field no longer breaks deployed clients — the same policy the
  Java enums already follow (`UNKNOWN_DEFAULT_OPEN_API`).
- The gs1-batch result union's `ok` discriminant is now a `const`, so the ok/error variants resolve
  deterministically in every lane.
- **Corrected terminal-unit description (#1080).** The recycled-unit refusal on
  `POST /api/v1/units/{id}/events` no longer advises a remedy the API refuses; the corrected wording
  reaches the generated types' doc comments and the READMEs. No method signature or model field change
  from this item.

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
against 1.13.0 — a breaking regeneration, shipped under the recorded pre-GA waiver.

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
