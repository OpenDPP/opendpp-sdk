# coding: utf-8

"""
    OpenDPP Integration API

    OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) Anyone can verify a seal — no account required. `POST /api/v1/audit/verify` recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.

    The version of the OpenAPI document: 1.14.0
    Contact: support@opendpp-node.eu
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501

import warnings
from pydantic import validate_call, Field, StrictFloat, StrictStr, StrictInt
from typing import Any, Dict, List, Optional, Tuple, Union
from typing_extensions import Annotated

from pydantic import Field, StrictStr, field_validator
from typing import Optional
from typing_extensions import Annotated
from opendpp_sdk.models.aas_environment_input import AasEnvironmentInput
from opendpp_sdk.models.aas_ingest_created import AasIngestCreated
from opendpp_sdk.models.delete_draft_passport200_response import DeleteDraftPassport200Response
from opendpp_sdk.models.passport_bulk_request import PassportBulkRequest
from opendpp_sdk.models.passport_bulk_result import PassportBulkResult
from opendpp_sdk.models.passport_create_request import PassportCreateRequest
from opendpp_sdk.models.passport_ingest_created import PassportIngestCreated
from opendpp_sdk.models.passport_list_response import PassportListResponse
from opendpp_sdk.models.passport_seal_response import PassportSealResponse
from opendpp_sdk.models.passport_status_update_request import PassportStatusUpdateRequest
from opendpp_sdk.models.passport_status_update_response import PassportStatusUpdateResponse
from opendpp_sdk.models.passport_update_request import PassportUpdateRequest
from opendpp_sdk.models.passport_update_response import PassportUpdateResponse
from opendpp_sdk.models.passport_validate_only_request import PassportValidateOnlyRequest
from opendpp_sdk.models.passport_validate_only_result import PassportValidateOnlyResult
from opendpp_sdk.models.passport_vc_readiness_report200_response import PassportVcReadinessReport200Response
from opendpp_sdk.models.public_passport_json_ld import PublicPassportJsonLd

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class PassportsApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def bulk_ingest_passports(
        self,
        passport_bulk_request: PassportBulkRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying a real (non-`dryRun`) import with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. A `dryRun` preview is never captured or replayed. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally (the `(productId, operator)` unique constraint still prevents duplicate rows).")] = None,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for the localized validation text inside per-row `errors[]` strings (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportBulkResult:
        """Bulk-ingest up to 200 passports with per-row error reporting

        Ingests up to **200** passports in one request with **partial-success semantics**: each row is validated and inserted independently; failed rows are skipped and reported as human-readable strings in `errors[]`. Returns **201** as long as at least one row was inserted (even with row errors); returns **400 Bulk Ingestion Failed** only when **every** row failed.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that; in practice the `maxItems: 200` envelope cap is the effective bound for typical rows. Envelope violations — empty array, more than 200 items, missing `passports` — are rejected before any row is processed, with the full default validation error body (`{statusCode, code, error, message}`).  **Per-row behavior (differences from `POST /api/v1/passports`):** - No `draft` support: every inserted row is created with `status: \"ACTIVE\"`. No `enrichment` support. - A valid GTIN-14/GRAI `productId` is **not** auto-copied into `metadata.gtin`/`metadata.grai` (unlike single ingestion). - Operator resolution per row: explicit `operatorId` must be bound to your workspace; otherwise the workspace's first bound operator is used; operator-scoped API keys force their operator. Lookups are cached within the request. - Duplicate `(productId, operatorId)` rows, unknown facilities, and per-row DB failures become `errors[]` strings (prefixed `[SKU: <productId>]`), never a request-level failure. - Each successfully inserted row **transactionally enqueues a `passport.ingested` webhook event** (public redacted JSON-LD payload). - Row validation messages use the localized `friendlyMessage` where the engine provides one (`?lang=` / `Accept-Language`); category-validity errors fall back to the technical `path: message` form.  Note the 400 `Bulk Ingestion Failed` body has **no `message` field**, and `errors` is an array of **strings** (not objects).

        :param passport_bulk_request: (required)
        :type passport_bulk_request: PassportBulkRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying a real (non-`dryRun`) import with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. A `dryRun` preview is never captured or replayed. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally (the `(productId, operator)` unique constraint still prevents duplicate rows).
        :type idempotency_key: str
        :param lang: Locale for the localized validation text inside per-row `errors[]` strings (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._bulk_ingest_passports_serialize(
            passport_bulk_request=passport_bulk_request,
            idempotency_key=idempotency_key,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "PassportBulkResult",
            '400': "PassportBulkBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def bulk_ingest_passports_with_http_info(
        self,
        passport_bulk_request: PassportBulkRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying a real (non-`dryRun`) import with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. A `dryRun` preview is never captured or replayed. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally (the `(productId, operator)` unique constraint still prevents duplicate rows).")] = None,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for the localized validation text inside per-row `errors[]` strings (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportBulkResult]:
        """Bulk-ingest up to 200 passports with per-row error reporting

        Ingests up to **200** passports in one request with **partial-success semantics**: each row is validated and inserted independently; failed rows are skipped and reported as human-readable strings in `errors[]`. Returns **201** as long as at least one row was inserted (even with row errors); returns **400 Bulk Ingestion Failed** only when **every** row failed.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that; in practice the `maxItems: 200` envelope cap is the effective bound for typical rows. Envelope violations — empty array, more than 200 items, missing `passports` — are rejected before any row is processed, with the full default validation error body (`{statusCode, code, error, message}`).  **Per-row behavior (differences from `POST /api/v1/passports`):** - No `draft` support: every inserted row is created with `status: \"ACTIVE\"`. No `enrichment` support. - A valid GTIN-14/GRAI `productId` is **not** auto-copied into `metadata.gtin`/`metadata.grai` (unlike single ingestion). - Operator resolution per row: explicit `operatorId` must be bound to your workspace; otherwise the workspace's first bound operator is used; operator-scoped API keys force their operator. Lookups are cached within the request. - Duplicate `(productId, operatorId)` rows, unknown facilities, and per-row DB failures become `errors[]` strings (prefixed `[SKU: <productId>]`), never a request-level failure. - Each successfully inserted row **transactionally enqueues a `passport.ingested` webhook event** (public redacted JSON-LD payload). - Row validation messages use the localized `friendlyMessage` where the engine provides one (`?lang=` / `Accept-Language`); category-validity errors fall back to the technical `path: message` form.  Note the 400 `Bulk Ingestion Failed` body has **no `message` field**, and `errors` is an array of **strings** (not objects).

        :param passport_bulk_request: (required)
        :type passport_bulk_request: PassportBulkRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying a real (non-`dryRun`) import with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. A `dryRun` preview is never captured or replayed. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally (the `(productId, operator)` unique constraint still prevents duplicate rows).
        :type idempotency_key: str
        :param lang: Locale for the localized validation text inside per-row `errors[]` strings (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._bulk_ingest_passports_serialize(
            passport_bulk_request=passport_bulk_request,
            idempotency_key=idempotency_key,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "PassportBulkResult",
            '400': "PassportBulkBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def bulk_ingest_passports_without_preload_content(
        self,
        passport_bulk_request: PassportBulkRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying a real (non-`dryRun`) import with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. A `dryRun` preview is never captured or replayed. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally (the `(productId, operator)` unique constraint still prevents duplicate rows).")] = None,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for the localized validation text inside per-row `errors[]` strings (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Bulk-ingest up to 200 passports with per-row error reporting

        Ingests up to **200** passports in one request with **partial-success semantics**: each row is validated and inserted independently; failed rows are skipped and reported as human-readable strings in `errors[]`. Returns **201** as long as at least one row was inserted (even with row errors); returns **400 Bulk Ingestion Failed** only when **every** row failed.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that; in practice the `maxItems: 200` envelope cap is the effective bound for typical rows. Envelope violations — empty array, more than 200 items, missing `passports` — are rejected before any row is processed, with the full default validation error body (`{statusCode, code, error, message}`).  **Per-row behavior (differences from `POST /api/v1/passports`):** - No `draft` support: every inserted row is created with `status: \"ACTIVE\"`. No `enrichment` support. - A valid GTIN-14/GRAI `productId` is **not** auto-copied into `metadata.gtin`/`metadata.grai` (unlike single ingestion). - Operator resolution per row: explicit `operatorId` must be bound to your workspace; otherwise the workspace's first bound operator is used; operator-scoped API keys force their operator. Lookups are cached within the request. - Duplicate `(productId, operatorId)` rows, unknown facilities, and per-row DB failures become `errors[]` strings (prefixed `[SKU: <productId>]`), never a request-level failure. - Each successfully inserted row **transactionally enqueues a `passport.ingested` webhook event** (public redacted JSON-LD payload). - Row validation messages use the localized `friendlyMessage` where the engine provides one (`?lang=` / `Accept-Language`); category-validity errors fall back to the technical `path: message` form.  Note the 400 `Bulk Ingestion Failed` body has **no `message` field**, and `errors` is an array of **strings** (not objects).

        :param passport_bulk_request: (required)
        :type passport_bulk_request: PassportBulkRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying a real (non-`dryRun`) import with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. A `dryRun` preview is never captured or replayed. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally (the `(productId, operator)` unique constraint still prevents duplicate rows).
        :type idempotency_key: str
        :param lang: Locale for the localized validation text inside per-row `errors[]` strings (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._bulk_ingest_passports_serialize(
            passport_bulk_request=passport_bulk_request,
            idempotency_key=idempotency_key,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "PassportBulkResult",
            '400': "PassportBulkBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _bulk_ingest_passports_serialize(
        self,
        passport_bulk_request,
        idempotency_key,
        lang,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        if lang is not None:
            
            _query_params.append(('lang', lang))
            
        # process the header parameters
        if idempotency_key is not None:
            _header_params['Idempotency-Key'] = idempotency_key
        # process the form parameters
        # process the body parameter
        if passport_bulk_request is not None:
            _body_params = passport_bulk_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/passports/bulk',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def create_passport(
        self,
        passport_create_request: PassportCreateRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of creating a duplicate passport or returning **409**. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry falls back to normal processing — never a double write, but you may then see the normal **409** instead of a replay.")] = None,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts. One of: en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr. Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportIngestCreated:
        """Create (ingest) a Digital Product Passport

        Creates a SKU/type-level Digital Product Passport.  **Permission:** `passport:create` (Bearer `op_dpp_token_…` API key or session JWT; cookie sessions must also send the `X-CSRF-Token` double-submit header). Write operations are subject to subscription gating (**402**) and, where the workspace enforces it, MFA (**403**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that.  **Validation.** Unless `draft: true`, `metadata` is validated against the ESPR category rules for `metadata.category` plus cross-field rules (e.g. `materialComposition` percentages must sum to 100 ±0.1, `originCountry` must be a real ISO 3166-1 alpha-2 code). For five categories (textiles, batteries, electronics, chemicals, construction) the authoritative per-category JSON Schema is served live at `GET /api/v1/schemas/{category}`; the other four (cosmetics, toys, iron-steel, aluminium) are validated by built-in server-side rules and `GET /api/v1/schemas/{category}` returns **404** for them. Failure returns the **400 Validation Failed** body with per-field `errors[]` (plus `warnings[]` when any exist — the key is omitted entirely when there are none). A passing payload may still produce non-blocking `warnings[]`, echoed in the 201 — including a **privacy-by-design advisory** when the metadata *looks* like it carries personal data (a clearly-personal field name such as `email`/`firstName`, or an email-shaped value; scanned one level deep, at most one such advisory). A DPP should carry PRODUCT data, not PII (ESPR FAQ Q16); this advisory never blocks the save. `friendlyMessage` texts are localized via `?lang=` or `Accept-Language` (default `en`); category-validity errors (`metadata.category` missing or unknown) carry no `friendlyMessage`.  **Drafts.** `draft: true` skips ALL validation, stores the passport with `status: \"DRAFT\"` (not publicly resolvable), returns `message: \"Draft passport saved\"` with `warnings: []`, and does **not** emit a webhook.  **Identifier handling.** `productId` may be a GTIN-14 (14 digits, GS1 mod-10 check digit), a GRAI (14-digit numeric asset id + up to 16 alphanumeric serial chars), or a free-form SKU. A 14-digit `productId` whose GS1 mod-10 check digit is invalid is rejected with **400** (a typo'd GTIN is never silently downgraded to a SKU); a non-numeric or non-14-digit `productId` is accepted as a non-GS1 SKU and carries a non-blocking `warnings[]` advisory that it resolves via `/passport/{id}` with no scannable GS1 QR. A valid GTIN-14 is auto-copied into `metadata.gtin` (a GRAI into `metadata.grai`) before storage. The server mints a UUID passport id and a GS1 Digital Link URI `https://opendpp-node.eu/{01|8003}/{productId}`.  **Operator binding.** With `operatorId` omitted, the passport is attributed to the first economic operator bound to your workspace; if no operator is bound at all the request fails **400** (the API never fabricates an operator identity — register one via `POST /api/v1/operators`). An `operatorId` not bound to your workspace → **403**. Operator-scoped API keys force their own operator and **403** on mismatch. The `(productId, operatorId)` pair is unique → **409** on duplicates. An optional `facilityId` must reference a Facility in your workspace (**400** otherwise).  **Webhook:** non-draft creation transactionally enqueues a `passport.ingested` event whose payload is the public redacted JSON-LD passport document (same masking as the 201 `passport` field). Drafts emit nothing.  **Response caveats:** the 201 `passport` field is the **public, redacted** JSON-LD representation — even for the creator. The owner-only metadata key `facilityDetails` is replaced with the literal placeholder `\"[REDACTED - Privileged Access Required]\"` (it appears as the placeholder even when you did not supply it), and for `category: \"batteries\"` the restricted legitimate-interest keys `detailedPerformance`, `lifecycleAndInUse` and `circularityAndDisassembly` (Battery Reg. Annex XIII parts 2-4) are masked the same way when present. `enrichment` is stored outside the validated metadata and Merkle seal and never appears in the JSON-LD document. The 201 body's top-level fields are `success`, `message`, `passport`, `warnings`, and the `vcReady`/`vcReadyReason` UNTP Verifiable-Credential readiness signal.  **Other 400 bodies:** non-validation failures (whitespace-only `productId`, no bound operator, unknown `facilityId`) reuse status 400 with the plain `{\"success\": false, \"error\": \"Bad Request\", \"message\": …}` triple and **no** `errors`/`warnings` arrays. Requests rejected before the handler runs — request-body schema violations (e.g. missing `productId`) and malformed JSON — come back as just `{\"error\": \"Bad Request\", \"message\": …}`.

        :param passport_create_request: (required)
        :type passport_create_request: PassportCreateRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of creating a duplicate passport or returning **409**. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry falls back to normal processing — never a double write, but you may then see the normal **409** instead of a replay.
        :type idempotency_key: str
        :param lang: Locale for localized `friendlyMessage` validation texts. One of: en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr. Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._create_passport_serialize(
            passport_create_request=passport_create_request,
            idempotency_key=idempotency_key,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "PassportIngestCreated",
            '400': "PassportCreateBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '409': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def create_passport_with_http_info(
        self,
        passport_create_request: PassportCreateRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of creating a duplicate passport or returning **409**. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry falls back to normal processing — never a double write, but you may then see the normal **409** instead of a replay.")] = None,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts. One of: en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr. Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportIngestCreated]:
        """Create (ingest) a Digital Product Passport

        Creates a SKU/type-level Digital Product Passport.  **Permission:** `passport:create` (Bearer `op_dpp_token_…` API key or session JWT; cookie sessions must also send the `X-CSRF-Token` double-submit header). Write operations are subject to subscription gating (**402**) and, where the workspace enforces it, MFA (**403**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that.  **Validation.** Unless `draft: true`, `metadata` is validated against the ESPR category rules for `metadata.category` plus cross-field rules (e.g. `materialComposition` percentages must sum to 100 ±0.1, `originCountry` must be a real ISO 3166-1 alpha-2 code). For five categories (textiles, batteries, electronics, chemicals, construction) the authoritative per-category JSON Schema is served live at `GET /api/v1/schemas/{category}`; the other four (cosmetics, toys, iron-steel, aluminium) are validated by built-in server-side rules and `GET /api/v1/schemas/{category}` returns **404** for them. Failure returns the **400 Validation Failed** body with per-field `errors[]` (plus `warnings[]` when any exist — the key is omitted entirely when there are none). A passing payload may still produce non-blocking `warnings[]`, echoed in the 201 — including a **privacy-by-design advisory** when the metadata *looks* like it carries personal data (a clearly-personal field name such as `email`/`firstName`, or an email-shaped value; scanned one level deep, at most one such advisory). A DPP should carry PRODUCT data, not PII (ESPR FAQ Q16); this advisory never blocks the save. `friendlyMessage` texts are localized via `?lang=` or `Accept-Language` (default `en`); category-validity errors (`metadata.category` missing or unknown) carry no `friendlyMessage`.  **Drafts.** `draft: true` skips ALL validation, stores the passport with `status: \"DRAFT\"` (not publicly resolvable), returns `message: \"Draft passport saved\"` with `warnings: []`, and does **not** emit a webhook.  **Identifier handling.** `productId` may be a GTIN-14 (14 digits, GS1 mod-10 check digit), a GRAI (14-digit numeric asset id + up to 16 alphanumeric serial chars), or a free-form SKU. A 14-digit `productId` whose GS1 mod-10 check digit is invalid is rejected with **400** (a typo'd GTIN is never silently downgraded to a SKU); a non-numeric or non-14-digit `productId` is accepted as a non-GS1 SKU and carries a non-blocking `warnings[]` advisory that it resolves via `/passport/{id}` with no scannable GS1 QR. A valid GTIN-14 is auto-copied into `metadata.gtin` (a GRAI into `metadata.grai`) before storage. The server mints a UUID passport id and a GS1 Digital Link URI `https://opendpp-node.eu/{01|8003}/{productId}`.  **Operator binding.** With `operatorId` omitted, the passport is attributed to the first economic operator bound to your workspace; if no operator is bound at all the request fails **400** (the API never fabricates an operator identity — register one via `POST /api/v1/operators`). An `operatorId` not bound to your workspace → **403**. Operator-scoped API keys force their own operator and **403** on mismatch. The `(productId, operatorId)` pair is unique → **409** on duplicates. An optional `facilityId` must reference a Facility in your workspace (**400** otherwise).  **Webhook:** non-draft creation transactionally enqueues a `passport.ingested` event whose payload is the public redacted JSON-LD passport document (same masking as the 201 `passport` field). Drafts emit nothing.  **Response caveats:** the 201 `passport` field is the **public, redacted** JSON-LD representation — even for the creator. The owner-only metadata key `facilityDetails` is replaced with the literal placeholder `\"[REDACTED - Privileged Access Required]\"` (it appears as the placeholder even when you did not supply it), and for `category: \"batteries\"` the restricted legitimate-interest keys `detailedPerformance`, `lifecycleAndInUse` and `circularityAndDisassembly` (Battery Reg. Annex XIII parts 2-4) are masked the same way when present. `enrichment` is stored outside the validated metadata and Merkle seal and never appears in the JSON-LD document. The 201 body's top-level fields are `success`, `message`, `passport`, `warnings`, and the `vcReady`/`vcReadyReason` UNTP Verifiable-Credential readiness signal.  **Other 400 bodies:** non-validation failures (whitespace-only `productId`, no bound operator, unknown `facilityId`) reuse status 400 with the plain `{\"success\": false, \"error\": \"Bad Request\", \"message\": …}` triple and **no** `errors`/`warnings` arrays. Requests rejected before the handler runs — request-body schema violations (e.g. missing `productId`) and malformed JSON — come back as just `{\"error\": \"Bad Request\", \"message\": …}`.

        :param passport_create_request: (required)
        :type passport_create_request: PassportCreateRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of creating a duplicate passport or returning **409**. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry falls back to normal processing — never a double write, but you may then see the normal **409** instead of a replay.
        :type idempotency_key: str
        :param lang: Locale for localized `friendlyMessage` validation texts. One of: en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr. Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._create_passport_serialize(
            passport_create_request=passport_create_request,
            idempotency_key=idempotency_key,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "PassportIngestCreated",
            '400': "PassportCreateBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '409': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def create_passport_without_preload_content(
        self,
        passport_create_request: PassportCreateRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of creating a duplicate passport or returning **409**. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry falls back to normal processing — never a double write, but you may then see the normal **409** instead of a replay.")] = None,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts. One of: en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr. Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Create (ingest) a Digital Product Passport

        Creates a SKU/type-level Digital Product Passport.  **Permission:** `passport:create` (Bearer `op_dpp_token_…` API key or session JWT; cookie sessions must also send the `X-CSRF-Token` double-submit header). Write operations are subject to subscription gating (**402**) and, where the workspace enforces it, MFA (**403**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that.  **Validation.** Unless `draft: true`, `metadata` is validated against the ESPR category rules for `metadata.category` plus cross-field rules (e.g. `materialComposition` percentages must sum to 100 ±0.1, `originCountry` must be a real ISO 3166-1 alpha-2 code). For five categories (textiles, batteries, electronics, chemicals, construction) the authoritative per-category JSON Schema is served live at `GET /api/v1/schemas/{category}`; the other four (cosmetics, toys, iron-steel, aluminium) are validated by built-in server-side rules and `GET /api/v1/schemas/{category}` returns **404** for them. Failure returns the **400 Validation Failed** body with per-field `errors[]` (plus `warnings[]` when any exist — the key is omitted entirely when there are none). A passing payload may still produce non-blocking `warnings[]`, echoed in the 201 — including a **privacy-by-design advisory** when the metadata *looks* like it carries personal data (a clearly-personal field name such as `email`/`firstName`, or an email-shaped value; scanned one level deep, at most one such advisory). A DPP should carry PRODUCT data, not PII (ESPR FAQ Q16); this advisory never blocks the save. `friendlyMessage` texts are localized via `?lang=` or `Accept-Language` (default `en`); category-validity errors (`metadata.category` missing or unknown) carry no `friendlyMessage`.  **Drafts.** `draft: true` skips ALL validation, stores the passport with `status: \"DRAFT\"` (not publicly resolvable), returns `message: \"Draft passport saved\"` with `warnings: []`, and does **not** emit a webhook.  **Identifier handling.** `productId` may be a GTIN-14 (14 digits, GS1 mod-10 check digit), a GRAI (14-digit numeric asset id + up to 16 alphanumeric serial chars), or a free-form SKU. A 14-digit `productId` whose GS1 mod-10 check digit is invalid is rejected with **400** (a typo'd GTIN is never silently downgraded to a SKU); a non-numeric or non-14-digit `productId` is accepted as a non-GS1 SKU and carries a non-blocking `warnings[]` advisory that it resolves via `/passport/{id}` with no scannable GS1 QR. A valid GTIN-14 is auto-copied into `metadata.gtin` (a GRAI into `metadata.grai`) before storage. The server mints a UUID passport id and a GS1 Digital Link URI `https://opendpp-node.eu/{01|8003}/{productId}`.  **Operator binding.** With `operatorId` omitted, the passport is attributed to the first economic operator bound to your workspace; if no operator is bound at all the request fails **400** (the API never fabricates an operator identity — register one via `POST /api/v1/operators`). An `operatorId` not bound to your workspace → **403**. Operator-scoped API keys force their own operator and **403** on mismatch. The `(productId, operatorId)` pair is unique → **409** on duplicates. An optional `facilityId` must reference a Facility in your workspace (**400** otherwise).  **Webhook:** non-draft creation transactionally enqueues a `passport.ingested` event whose payload is the public redacted JSON-LD passport document (same masking as the 201 `passport` field). Drafts emit nothing.  **Response caveats:** the 201 `passport` field is the **public, redacted** JSON-LD representation — even for the creator. The owner-only metadata key `facilityDetails` is replaced with the literal placeholder `\"[REDACTED - Privileged Access Required]\"` (it appears as the placeholder even when you did not supply it), and for `category: \"batteries\"` the restricted legitimate-interest keys `detailedPerformance`, `lifecycleAndInUse` and `circularityAndDisassembly` (Battery Reg. Annex XIII parts 2-4) are masked the same way when present. `enrichment` is stored outside the validated metadata and Merkle seal and never appears in the JSON-LD document. The 201 body's top-level fields are `success`, `message`, `passport`, `warnings`, and the `vcReady`/`vcReadyReason` UNTP Verifiable-Credential readiness signal.  **Other 400 bodies:** non-validation failures (whitespace-only `productId`, no bound operator, unknown `facilityId`) reuse status 400 with the plain `{\"success\": false, \"error\": \"Bad Request\", \"message\": …}` triple and **no** `errors`/`warnings` arrays. Requests rejected before the handler runs — request-body schema violations (e.g. missing `productId`) and malformed JSON — come back as just `{\"error\": \"Bad Request\", \"message\": …}`.

        :param passport_create_request: (required)
        :type passport_create_request: PassportCreateRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of creating a duplicate passport or returning **409**. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry falls back to normal processing — never a double write, but you may then see the normal **409** instead of a replay.
        :type idempotency_key: str
        :param lang: Locale for localized `friendlyMessage` validation texts. One of: en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr. Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._create_passport_serialize(
            passport_create_request=passport_create_request,
            idempotency_key=idempotency_key,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "PassportIngestCreated",
            '400': "PassportCreateBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '409': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _create_passport_serialize(
        self,
        passport_create_request,
        idempotency_key,
        lang,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        if lang is not None:
            
            _query_params.append(('lang', lang))
            
        # process the header parameters
        if idempotency_key is not None:
            _header_params['Idempotency-Key'] = idempotency_key
        # process the form parameters
        # process the body parameter
        if passport_create_request is not None:
            _body_params = passport_create_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/passports',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def delete_draft_passport(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID. `productId` aliasing is NOT supported here.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> DeleteDraftPassport200Response:
        """Permanently delete a DRAFT passport

        Hard-deletes a passport **only while it is a DRAFT** (never published, not publicly resolvable, no retention duty). Children (history, access logs, battery units) cascade on delete.  Published passports (ACTIVE/RECALLED/DECOMMISSIONED) are refused with **409** — they must be decommissioned/archived through the status lifecycle (`PUT /api/v1/passports/{id}/status`) to satisfy the ESPR persistence duty.  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** (no `productId` aliasing) and only within the passport's **owning tenant** — an operator-binding alone is not sufficient, unlike PUT.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID. `productId` aliasing is NOT supported here. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._delete_draft_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DeleteDraftPassport200Response",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '409': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def delete_draft_passport_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID. `productId` aliasing is NOT supported here.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[DeleteDraftPassport200Response]:
        """Permanently delete a DRAFT passport

        Hard-deletes a passport **only while it is a DRAFT** (never published, not publicly resolvable, no retention duty). Children (history, access logs, battery units) cascade on delete.  Published passports (ACTIVE/RECALLED/DECOMMISSIONED) are refused with **409** — they must be decommissioned/archived through the status lifecycle (`PUT /api/v1/passports/{id}/status`) to satisfy the ESPR persistence duty.  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** (no `productId` aliasing) and only within the passport's **owning tenant** — an operator-binding alone is not sufficient, unlike PUT.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID. `productId` aliasing is NOT supported here. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._delete_draft_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DeleteDraftPassport200Response",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '409': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def delete_draft_passport_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID. `productId` aliasing is NOT supported here.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Permanently delete a DRAFT passport

        Hard-deletes a passport **only while it is a DRAFT** (never published, not publicly resolvable, no retention duty). Children (history, access logs, battery units) cascade on delete.  Published passports (ACTIVE/RECALLED/DECOMMISSIONED) are refused with **409** — they must be decommissioned/archived through the status lifecycle (`PUT /api/v1/passports/{id}/status`) to satisfy the ESPR persistence duty.  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** (no `productId` aliasing) and only within the passport's **owning tenant** — an operator-binding alone is not sufficient, unlike PUT.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID. `productId` aliasing is NOT supported here. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._delete_draft_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DeleteDraftPassport200Response",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '409': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _delete_draft_passport_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='DELETE',
            resource_path='/api/v1/passports/{id}',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def get_passport(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first, then `productId`.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PublicPassportJsonLd:
        """Fetch a single passport (content-negotiated JSON-LD / AAS / HTML)

        Owner-side alias of the public resolver. Accepts either the passport **UUID** or its caller-supplied **`productId`** (GTIN-14 / GRAI / SKU), scoped to operators bound to your workspace. After the scoped lookup the request is **re-dispatched internally to `GET /passport/{uuid}`**, forwarding all request headers, and the inner response (status, content type, body) is returned as-is.  **Permission:** `passport:read` (read-only — no subscription/402 gate).  **Content negotiation** (substring match on `Accept`): `application/aas+json` → role-filtered AAS environment; `application/vc+jwt` → enveloping UNTP Verifiable Credential; `application/vc+ld+json` → the same credential with an embedded `ecdsa-jcs-2019` W3C Data Integrity proof; `application/dc+sd-jwt` (legacy `vc+sd-jwt` accepted) → SD-JWT-VC selective disclosure (these three return `406 Not Acceptable` when the passport has no manufacturing facility with a country of production); `text/html` → SSR passport page; anything else (including `application/json`, `*/*`, or no header) → JSON-LD with `Content-Type: application/ld+json` (the default). The VC and SD-JWT representations are forwarded verbatim from `GET /passport/{id}` — see that operation for the full credential semantics.  **Access-tier caveat (privilege is resolved from the *forwarded* headers, not the already-authenticated context):** only **database API keys** (`Authorization: Bearer op_dpp_token_…`) of the owning or operator-bound tenant are recognized as owner by the inner resolver. Those callers get the **owner-tier** document: `facilityDetails` and battery restricted keys unmasked, `manufacturingFacility` includes `streetAddress`/`city`/`postalCode`, and DRAFT passports are visible. Callers authenticated with a **JWT session** (login cookie or bearer JWT) receive the **public-redacted** tier instead, and DRAFT passports answer 404 with the forwarded public body (no `success` field).  Every successful resolution records an anonymized-IP access audit entry.  **Rate limits:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys, under a per-IP ceiling; `x-ratelimit-*` headers and a `Retry-After` on **429**. **Plus** the forwarded public resolver's own limiter (30 req/min/IP, no headers) — both 429 shapes are possible (see 429).

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first, then `productId`. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._get_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '401': "Error",
            '403': "Error",
            '404': "PassportGetNotFound",
            '406': "Error",
            '429': "PassportGetTooManyRequests",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def get_passport_with_http_info(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first, then `productId`.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PublicPassportJsonLd]:
        """Fetch a single passport (content-negotiated JSON-LD / AAS / HTML)

        Owner-side alias of the public resolver. Accepts either the passport **UUID** or its caller-supplied **`productId`** (GTIN-14 / GRAI / SKU), scoped to operators bound to your workspace. After the scoped lookup the request is **re-dispatched internally to `GET /passport/{uuid}`**, forwarding all request headers, and the inner response (status, content type, body) is returned as-is.  **Permission:** `passport:read` (read-only — no subscription/402 gate).  **Content negotiation** (substring match on `Accept`): `application/aas+json` → role-filtered AAS environment; `application/vc+jwt` → enveloping UNTP Verifiable Credential; `application/vc+ld+json` → the same credential with an embedded `ecdsa-jcs-2019` W3C Data Integrity proof; `application/dc+sd-jwt` (legacy `vc+sd-jwt` accepted) → SD-JWT-VC selective disclosure (these three return `406 Not Acceptable` when the passport has no manufacturing facility with a country of production); `text/html` → SSR passport page; anything else (including `application/json`, `*/*`, or no header) → JSON-LD with `Content-Type: application/ld+json` (the default). The VC and SD-JWT representations are forwarded verbatim from `GET /passport/{id}` — see that operation for the full credential semantics.  **Access-tier caveat (privilege is resolved from the *forwarded* headers, not the already-authenticated context):** only **database API keys** (`Authorization: Bearer op_dpp_token_…`) of the owning or operator-bound tenant are recognized as owner by the inner resolver. Those callers get the **owner-tier** document: `facilityDetails` and battery restricted keys unmasked, `manufacturingFacility` includes `streetAddress`/`city`/`postalCode`, and DRAFT passports are visible. Callers authenticated with a **JWT session** (login cookie or bearer JWT) receive the **public-redacted** tier instead, and DRAFT passports answer 404 with the forwarded public body (no `success` field).  Every successful resolution records an anonymized-IP access audit entry.  **Rate limits:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys, under a per-IP ceiling; `x-ratelimit-*` headers and a `Retry-After` on **429**. **Plus** the forwarded public resolver's own limiter (30 req/min/IP, no headers) — both 429 shapes are possible (see 429).

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first, then `productId`. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._get_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '401': "Error",
            '403': "Error",
            '404': "PassportGetNotFound",
            '406': "Error",
            '429': "PassportGetTooManyRequests",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def get_passport_without_preload_content(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first, then `productId`.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Fetch a single passport (content-negotiated JSON-LD / AAS / HTML)

        Owner-side alias of the public resolver. Accepts either the passport **UUID** or its caller-supplied **`productId`** (GTIN-14 / GRAI / SKU), scoped to operators bound to your workspace. After the scoped lookup the request is **re-dispatched internally to `GET /passport/{uuid}`**, forwarding all request headers, and the inner response (status, content type, body) is returned as-is.  **Permission:** `passport:read` (read-only — no subscription/402 gate).  **Content negotiation** (substring match on `Accept`): `application/aas+json` → role-filtered AAS environment; `application/vc+jwt` → enveloping UNTP Verifiable Credential; `application/vc+ld+json` → the same credential with an embedded `ecdsa-jcs-2019` W3C Data Integrity proof; `application/dc+sd-jwt` (legacy `vc+sd-jwt` accepted) → SD-JWT-VC selective disclosure (these three return `406 Not Acceptable` when the passport has no manufacturing facility with a country of production); `text/html` → SSR passport page; anything else (including `application/json`, `*/*`, or no header) → JSON-LD with `Content-Type: application/ld+json` (the default). The VC and SD-JWT representations are forwarded verbatim from `GET /passport/{id}` — see that operation for the full credential semantics.  **Access-tier caveat (privilege is resolved from the *forwarded* headers, not the already-authenticated context):** only **database API keys** (`Authorization: Bearer op_dpp_token_…`) of the owning or operator-bound tenant are recognized as owner by the inner resolver. Those callers get the **owner-tier** document: `facilityDetails` and battery restricted keys unmasked, `manufacturingFacility` includes `streetAddress`/`city`/`postalCode`, and DRAFT passports are visible. Callers authenticated with a **JWT session** (login cookie or bearer JWT) receive the **public-redacted** tier instead, and DRAFT passports answer 404 with the forwarded public body (no `success` field).  Every successful resolution records an anonymized-IP access audit entry.  **Rate limits:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys, under a per-IP ceiling; `x-ratelimit-*` headers and a `Retry-After` on **429**. **Plus** the forwarded public resolver's own limiter (30 req/min/IP, no headers) — both 429 shapes are possible (see 429).

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first, then `productId`. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._get_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '401': "Error",
            '403': "Error",
            '404': "PassportGetNotFound",
            '406': "Error",
            '429': "PassportGetTooManyRequests",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _get_passport_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/ld+json', 
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/passports/{id}',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def ingest_passport_from_aas(
        self,
        aas_environment_input: AasEnvironmentInput,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> AasIngestCreated:
        """Ingest a passport from an AAS JSON Environment (seal-verified)

        Ingests (creates **or updates**) a Digital Product Passport from an Industry-4.0 **Asset Administration Shell (AAS) JSON Environment** — the same format produced by OpenDPP's own AAS export.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 262,144 bytes (256 KiB)** → **413**.  **Parsing.** The environment must contain a `submodels` array including a submodel with `idShort: \"ComplianceMetadata\"`, whose `submodelElements` are parsed back into the metadata object; missing it fails 400 (`Ingestion Failed`). `productId` is resolved from `metadata.gtin` || `metadata.grai` || `metadata.productId` || the first shell's `assetInformation.specificAssetIds` entry named `productId` — unresolvable → 400 `Bad Request`. The parsed metadata then passes the full ESPR category validation (400 `Validation Failed` with `errors[]`).  **seal verification.** If the environment embeds an `eidasVerificationSeal` submodel (`digitalSealHash` / `cryptographicSignature` / `pemPublicKey` elements), the seal is verified against **your tenant's server-held signing public key** — never the key embedded in the request (self-signing is rejected by design). An embedded seal that fails verification → **400 `Signature Verification Failed`**; this includes the case where your workspace holds no matching key. `isSealed`/`signatureVerified` in the 201 echo the outcome (both `false` for unsealed documents).  **Upsert semantics.** If a passport already exists for the resolved `(productId, operator)` pair: a **sealed** existing passport refuses re-ingestion (**403** — re-seal explicitly after changes); an unsealed one has its metadata, Merkle tree and seal fields **replaced**, still answering **201**. Operator resolution: operator-scoped API keys use their own operator and **403** when that operator is not bound to your workspace; otherwise the workspace's first bound operator is used; none bound → 400.  **Caveats:** - **NO webhook is emitted** — unlike `POST /api/v1/passports` and `/bulk`, AAS ingestion never enqueues `passport.ingested` (or any other event). - The catch-all error path returns **400 `Ingestion Failed`** with the underlying parse/processing message — even for internal failures (this handler does not emit its own 500). - Validation `friendlyMessage` localization via `?lang=` / `Accept-Language`; category-validity errors carry no `friendlyMessage`.

        :param aas_environment_input: (required)
        :type aas_environment_input: AasEnvironmentInput
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._ingest_passport_from_aas_serialize(
            aas_environment_input=aas_environment_input,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "AasIngestCreated",
            '400': "AasIngestBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def ingest_passport_from_aas_with_http_info(
        self,
        aas_environment_input: AasEnvironmentInput,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[AasIngestCreated]:
        """Ingest a passport from an AAS JSON Environment (seal-verified)

        Ingests (creates **or updates**) a Digital Product Passport from an Industry-4.0 **Asset Administration Shell (AAS) JSON Environment** — the same format produced by OpenDPP's own AAS export.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 262,144 bytes (256 KiB)** → **413**.  **Parsing.** The environment must contain a `submodels` array including a submodel with `idShort: \"ComplianceMetadata\"`, whose `submodelElements` are parsed back into the metadata object; missing it fails 400 (`Ingestion Failed`). `productId` is resolved from `metadata.gtin` || `metadata.grai` || `metadata.productId` || the first shell's `assetInformation.specificAssetIds` entry named `productId` — unresolvable → 400 `Bad Request`. The parsed metadata then passes the full ESPR category validation (400 `Validation Failed` with `errors[]`).  **seal verification.** If the environment embeds an `eidasVerificationSeal` submodel (`digitalSealHash` / `cryptographicSignature` / `pemPublicKey` elements), the seal is verified against **your tenant's server-held signing public key** — never the key embedded in the request (self-signing is rejected by design). An embedded seal that fails verification → **400 `Signature Verification Failed`**; this includes the case where your workspace holds no matching key. `isSealed`/`signatureVerified` in the 201 echo the outcome (both `false` for unsealed documents).  **Upsert semantics.** If a passport already exists for the resolved `(productId, operator)` pair: a **sealed** existing passport refuses re-ingestion (**403** — re-seal explicitly after changes); an unsealed one has its metadata, Merkle tree and seal fields **replaced**, still answering **201**. Operator resolution: operator-scoped API keys use their own operator and **403** when that operator is not bound to your workspace; otherwise the workspace's first bound operator is used; none bound → 400.  **Caveats:** - **NO webhook is emitted** — unlike `POST /api/v1/passports` and `/bulk`, AAS ingestion never enqueues `passport.ingested` (or any other event). - The catch-all error path returns **400 `Ingestion Failed`** with the underlying parse/processing message — even for internal failures (this handler does not emit its own 500). - Validation `friendlyMessage` localization via `?lang=` / `Accept-Language`; category-validity errors carry no `friendlyMessage`.

        :param aas_environment_input: (required)
        :type aas_environment_input: AasEnvironmentInput
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._ingest_passport_from_aas_serialize(
            aas_environment_input=aas_environment_input,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "AasIngestCreated",
            '400': "AasIngestBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def ingest_passport_from_aas_without_preload_content(
        self,
        aas_environment_input: AasEnvironmentInput,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Ingest a passport from an AAS JSON Environment (seal-verified)

        Ingests (creates **or updates**) a Digital Product Passport from an Industry-4.0 **Asset Administration Shell (AAS) JSON Environment** — the same format produced by OpenDPP's own AAS export.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 262,144 bytes (256 KiB)** → **413**.  **Parsing.** The environment must contain a `submodels` array including a submodel with `idShort: \"ComplianceMetadata\"`, whose `submodelElements` are parsed back into the metadata object; missing it fails 400 (`Ingestion Failed`). `productId` is resolved from `metadata.gtin` || `metadata.grai` || `metadata.productId` || the first shell's `assetInformation.specificAssetIds` entry named `productId` — unresolvable → 400 `Bad Request`. The parsed metadata then passes the full ESPR category validation (400 `Validation Failed` with `errors[]`).  **seal verification.** If the environment embeds an `eidasVerificationSeal` submodel (`digitalSealHash` / `cryptographicSignature` / `pemPublicKey` elements), the seal is verified against **your tenant's server-held signing public key** — never the key embedded in the request (self-signing is rejected by design). An embedded seal that fails verification → **400 `Signature Verification Failed`**; this includes the case where your workspace holds no matching key. `isSealed`/`signatureVerified` in the 201 echo the outcome (both `false` for unsealed documents).  **Upsert semantics.** If a passport already exists for the resolved `(productId, operator)` pair: a **sealed** existing passport refuses re-ingestion (**403** — re-seal explicitly after changes); an unsealed one has its metadata, Merkle tree and seal fields **replaced**, still answering **201**. Operator resolution: operator-scoped API keys use their own operator and **403** when that operator is not bound to your workspace; otherwise the workspace's first bound operator is used; none bound → 400.  **Caveats:** - **NO webhook is emitted** — unlike `POST /api/v1/passports` and `/bulk`, AAS ingestion never enqueues `passport.ingested` (or any other event). - The catch-all error path returns **400 `Ingestion Failed`** with the underlying parse/processing message — even for internal failures (this handler does not emit its own 500). - Validation `friendlyMessage` localization via `?lang=` / `Accept-Language`; category-validity errors carry no `friendlyMessage`.

        :param aas_environment_input: (required)
        :type aas_environment_input: AasEnvironmentInput
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._ingest_passport_from_aas_serialize(
            aas_environment_input=aas_environment_input,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "AasIngestCreated",
            '400': "AasIngestBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _ingest_passport_from_aas_serialize(
        self,
        aas_environment_input,
        lang,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        if lang is not None:
            
            _query_params.append(('lang', lang))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if aas_environment_input is not None:
            _body_params = aas_environment_input


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/passports/aas/ingest',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def list_passports(
        self,
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=100, strict=True, ge=1)]], Field(description="Page size.")] = None,
        category: Annotated[Optional[StrictStr], Field(description="Exact-match filter on `metadata.category`. Known values: textiles, batteries, electronics, cosmetics, toys, iron-steel, aluminium, chemicals, construction.")] = None,
        origin_country: Annotated[Optional[StrictStr], Field(description="Exact-match filter on `metadata.originCountry` (ISO 3166-1 alpha-2, e.g. `PT`).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportListResponse:
        """List passports in your workspace (paginated JSON-LD)

        Returns the **non-archived** passports of every economic operator bound to your workspace, newest first (`createdAt DESC`). Operator-scoped API keys only see passports of their bound operator.  **Permission:** `passport:read` (read-only — no subscription/402 gate).  **Filtering:** `category` and `originCountry` are exact-match filters on the top-level `metadata` keys of the same name. Known `metadata.category` values: `textiles`, `batteries`, `electronics`, `cosmetics`, `toys`, `iron-steel`, `aluminium`, `chemicals`, `construction`; `originCountry` is ISO 3166-1 alpha-2.  **Pagination:** `page` (default 1) and `limit` (default 10) are numeric strings matching `^[0-9]+$` — any other value is rejected with the framework's default 400 validation body (see 400). Parsed values are clamped server-side to `page >= 1` and `1 <= limit <= 100`. There is **no `total` count**; page until you receive fewer than `limit` items.  **Serialization caveats:** - The redaction tier of each item depends on the credential's **role**: only `BRAND_OPERATOR` credentials receive the unredacted owner-tier document. Every other role — including `TENANT_ADMIN` — receives the public tier: `facilityDetails` (and, for `batteries`, `detailedPerformance` / `lifecycleAndInUse` / `circularityAndDisassembly`) are masked to the literal string `\"[REDACTED - Privileged Access Required]\"`. - `economicOperator.role` is **absent** from list items and `manufacturingFacility` is always `null` here — fetch a single passport (`GET /api/v1/passports/{id}`) for the facility node and operator role. - The response passes through a declared response schema: top-level keys other than `success`, `page`, `limit`, `passports` are stripped. Passport items allow additional properties, so undeclared item keys (`status`, `archivedAt`, `retentionUntil`, `manufacturingFacility`, the flattened metadata keys) pass through intact — but two **declared** item keys are mangled by their subschemas: the `@context` term-map object (second array element) is always emptied to `{}`, and `proof` is emptied to `{}` on sealed items (`null` on unsealed) — `signatureValue`, `merkleRoot`, `redactedLeaves`, `x5c` and `rfc3161` are all stripped from list output. Fetch a single passport (`GET /api/v1/passports/{id}`) or the public resolver for the verifiable proof block.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param page: 1-based page number (digits only).
        :type page: int
        :param limit: Page size.
        :type limit: int
        :param category: Exact-match filter on `metadata.category`. Known values: textiles, batteries, electronics, cosmetics, toys, iron-steel, aluminium, chemicals, construction.
        :type category: str
        :param origin_country: Exact-match filter on `metadata.originCountry` (ISO 3166-1 alpha-2, e.g. `PT`).
        :type origin_country: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._list_passports_serialize(
            page=page,
            limit=limit,
            category=category,
            origin_country=origin_country,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportListResponse",
            '400': "ListPassports400Response",
            '401': "Error",
            '403': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def list_passports_with_http_info(
        self,
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=100, strict=True, ge=1)]], Field(description="Page size.")] = None,
        category: Annotated[Optional[StrictStr], Field(description="Exact-match filter on `metadata.category`. Known values: textiles, batteries, electronics, cosmetics, toys, iron-steel, aluminium, chemicals, construction.")] = None,
        origin_country: Annotated[Optional[StrictStr], Field(description="Exact-match filter on `metadata.originCountry` (ISO 3166-1 alpha-2, e.g. `PT`).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportListResponse]:
        """List passports in your workspace (paginated JSON-LD)

        Returns the **non-archived** passports of every economic operator bound to your workspace, newest first (`createdAt DESC`). Operator-scoped API keys only see passports of their bound operator.  **Permission:** `passport:read` (read-only — no subscription/402 gate).  **Filtering:** `category` and `originCountry` are exact-match filters on the top-level `metadata` keys of the same name. Known `metadata.category` values: `textiles`, `batteries`, `electronics`, `cosmetics`, `toys`, `iron-steel`, `aluminium`, `chemicals`, `construction`; `originCountry` is ISO 3166-1 alpha-2.  **Pagination:** `page` (default 1) and `limit` (default 10) are numeric strings matching `^[0-9]+$` — any other value is rejected with the framework's default 400 validation body (see 400). Parsed values are clamped server-side to `page >= 1` and `1 <= limit <= 100`. There is **no `total` count**; page until you receive fewer than `limit` items.  **Serialization caveats:** - The redaction tier of each item depends on the credential's **role**: only `BRAND_OPERATOR` credentials receive the unredacted owner-tier document. Every other role — including `TENANT_ADMIN` — receives the public tier: `facilityDetails` (and, for `batteries`, `detailedPerformance` / `lifecycleAndInUse` / `circularityAndDisassembly`) are masked to the literal string `\"[REDACTED - Privileged Access Required]\"`. - `economicOperator.role` is **absent** from list items and `manufacturingFacility` is always `null` here — fetch a single passport (`GET /api/v1/passports/{id}`) for the facility node and operator role. - The response passes through a declared response schema: top-level keys other than `success`, `page`, `limit`, `passports` are stripped. Passport items allow additional properties, so undeclared item keys (`status`, `archivedAt`, `retentionUntil`, `manufacturingFacility`, the flattened metadata keys) pass through intact — but two **declared** item keys are mangled by their subschemas: the `@context` term-map object (second array element) is always emptied to `{}`, and `proof` is emptied to `{}` on sealed items (`null` on unsealed) — `signatureValue`, `merkleRoot`, `redactedLeaves`, `x5c` and `rfc3161` are all stripped from list output. Fetch a single passport (`GET /api/v1/passports/{id}`) or the public resolver for the verifiable proof block.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param page: 1-based page number (digits only).
        :type page: int
        :param limit: Page size.
        :type limit: int
        :param category: Exact-match filter on `metadata.category`. Known values: textiles, batteries, electronics, cosmetics, toys, iron-steel, aluminium, chemicals, construction.
        :type category: str
        :param origin_country: Exact-match filter on `metadata.originCountry` (ISO 3166-1 alpha-2, e.g. `PT`).
        :type origin_country: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._list_passports_serialize(
            page=page,
            limit=limit,
            category=category,
            origin_country=origin_country,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportListResponse",
            '400': "ListPassports400Response",
            '401': "Error",
            '403': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def list_passports_without_preload_content(
        self,
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=100, strict=True, ge=1)]], Field(description="Page size.")] = None,
        category: Annotated[Optional[StrictStr], Field(description="Exact-match filter on `metadata.category`. Known values: textiles, batteries, electronics, cosmetics, toys, iron-steel, aluminium, chemicals, construction.")] = None,
        origin_country: Annotated[Optional[StrictStr], Field(description="Exact-match filter on `metadata.originCountry` (ISO 3166-1 alpha-2, e.g. `PT`).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """List passports in your workspace (paginated JSON-LD)

        Returns the **non-archived** passports of every economic operator bound to your workspace, newest first (`createdAt DESC`). Operator-scoped API keys only see passports of their bound operator.  **Permission:** `passport:read` (read-only — no subscription/402 gate).  **Filtering:** `category` and `originCountry` are exact-match filters on the top-level `metadata` keys of the same name. Known `metadata.category` values: `textiles`, `batteries`, `electronics`, `cosmetics`, `toys`, `iron-steel`, `aluminium`, `chemicals`, `construction`; `originCountry` is ISO 3166-1 alpha-2.  **Pagination:** `page` (default 1) and `limit` (default 10) are numeric strings matching `^[0-9]+$` — any other value is rejected with the framework's default 400 validation body (see 400). Parsed values are clamped server-side to `page >= 1` and `1 <= limit <= 100`. There is **no `total` count**; page until you receive fewer than `limit` items.  **Serialization caveats:** - The redaction tier of each item depends on the credential's **role**: only `BRAND_OPERATOR` credentials receive the unredacted owner-tier document. Every other role — including `TENANT_ADMIN` — receives the public tier: `facilityDetails` (and, for `batteries`, `detailedPerformance` / `lifecycleAndInUse` / `circularityAndDisassembly`) are masked to the literal string `\"[REDACTED - Privileged Access Required]\"`. - `economicOperator.role` is **absent** from list items and `manufacturingFacility` is always `null` here — fetch a single passport (`GET /api/v1/passports/{id}`) for the facility node and operator role. - The response passes through a declared response schema: top-level keys other than `success`, `page`, `limit`, `passports` are stripped. Passport items allow additional properties, so undeclared item keys (`status`, `archivedAt`, `retentionUntil`, `manufacturingFacility`, the flattened metadata keys) pass through intact — but two **declared** item keys are mangled by their subschemas: the `@context` term-map object (second array element) is always emptied to `{}`, and `proof` is emptied to `{}` on sealed items (`null` on unsealed) — `signatureValue`, `merkleRoot`, `redactedLeaves`, `x5c` and `rfc3161` are all stripped from list output. Fetch a single passport (`GET /api/v1/passports/{id}`) or the public resolver for the verifiable proof block.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param page: 1-based page number (digits only).
        :type page: int
        :param limit: Page size.
        :type limit: int
        :param category: Exact-match filter on `metadata.category`. Known values: textiles, batteries, electronics, cosmetics, toys, iron-steel, aluminium, chemicals, construction.
        :type category: str
        :param origin_country: Exact-match filter on `metadata.originCountry` (ISO 3166-1 alpha-2, e.g. `PT`).
        :type origin_country: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._list_passports_serialize(
            page=page,
            limit=limit,
            category=category,
            origin_country=origin_country,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportListResponse",
            '400': "ListPassports400Response",
            '401': "Error",
            '403': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _list_passports_serialize(
        self,
        page,
        limit,
        category,
        origin_country,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        if page is not None:
            
            _query_params.append(('page', page))
            
        if limit is not None:
            
            _query_params.append(('limit', limit))
            
        if category is not None:
            
            _query_params.append(('category', category))
            
        if origin_country is not None:
            
            _query_params.append(('originCountry', origin_country))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/passports',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def passport_vc_readiness_report(
        self,
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=100, strict=True, ge=1)]], Field(description="Page size.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportVcReadinessReport200Response:
        """Catalog-wide UNTP Verifiable-Credential readiness report

        A read-only, tenant-scoped report of which SKUs in your catalog can / can't emit a UNTP **Verifiable Credential**, and why — so you can fix a whole catalog before relying on VCs, instead of probing passports one at a time. It **aggregates the same per-passport `vcReady` signal** returned on every ingest response: a passport is VC-ready only when it links a manufacturing `Facility` with a country of production.  **Permission:** `passport:read` (read-only — no subscription/402 gate). Operator scoping + the non-archived filter match the passports list.  **Shape:** each `results[]` row is `{ id, productId, vcReady, blockers[] }` — `blockers[]` reuses the SAME actionable reason the single-passport signal exposes (empty when ready). The top-level `ready` / `notReady` rollup is **catalog-wide** (counts every non-archived passport), while `results` is **paginated** — `page` (default 1) + `limit` (default 100, max 200), with `total` / `totalPages`. NOTE: because the rollup is catalog-wide but `results` is one page, `ready` is generally NOT the count of `vcReady:true` rows on the current page — page through all `totalPages` to enumerate every SKU.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param page: 1-based page number (digits only).
        :type page: int
        :param limit: Page size.
        :type limit: int
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._passport_vc_readiness_report_serialize(
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportVcReadinessReport200Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def passport_vc_readiness_report_with_http_info(
        self,
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=100, strict=True, ge=1)]], Field(description="Page size.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportVcReadinessReport200Response]:
        """Catalog-wide UNTP Verifiable-Credential readiness report

        A read-only, tenant-scoped report of which SKUs in your catalog can / can't emit a UNTP **Verifiable Credential**, and why — so you can fix a whole catalog before relying on VCs, instead of probing passports one at a time. It **aggregates the same per-passport `vcReady` signal** returned on every ingest response: a passport is VC-ready only when it links a manufacturing `Facility` with a country of production.  **Permission:** `passport:read` (read-only — no subscription/402 gate). Operator scoping + the non-archived filter match the passports list.  **Shape:** each `results[]` row is `{ id, productId, vcReady, blockers[] }` — `blockers[]` reuses the SAME actionable reason the single-passport signal exposes (empty when ready). The top-level `ready` / `notReady` rollup is **catalog-wide** (counts every non-archived passport), while `results` is **paginated** — `page` (default 1) + `limit` (default 100, max 200), with `total` / `totalPages`. NOTE: because the rollup is catalog-wide but `results` is one page, `ready` is generally NOT the count of `vcReady:true` rows on the current page — page through all `totalPages` to enumerate every SKU.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param page: 1-based page number (digits only).
        :type page: int
        :param limit: Page size.
        :type limit: int
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._passport_vc_readiness_report_serialize(
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportVcReadinessReport200Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def passport_vc_readiness_report_without_preload_content(
        self,
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=100, strict=True, ge=1)]], Field(description="Page size.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Catalog-wide UNTP Verifiable-Credential readiness report

        A read-only, tenant-scoped report of which SKUs in your catalog can / can't emit a UNTP **Verifiable Credential**, and why — so you can fix a whole catalog before relying on VCs, instead of probing passports one at a time. It **aggregates the same per-passport `vcReady` signal** returned on every ingest response: a passport is VC-ready only when it links a manufacturing `Facility` with a country of production.  **Permission:** `passport:read` (read-only — no subscription/402 gate). Operator scoping + the non-archived filter match the passports list.  **Shape:** each `results[]` row is `{ id, productId, vcReady, blockers[] }` — `blockers[]` reuses the SAME actionable reason the single-passport signal exposes (empty when ready). The top-level `ready` / `notReady` rollup is **catalog-wide** (counts every non-archived passport), while `results` is **paginated** — `page` (default 1) + `limit` (default 100, max 200), with `total` / `totalPages`. NOTE: because the rollup is catalog-wide but `results` is one page, `ready` is generally NOT the count of `vcReady:true` rows on the current page — page through all `totalPages` to enumerate every SKU.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param page: 1-based page number (digits only).
        :type page: int
        :param limit: Page size.
        :type limit: int
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._passport_vc_readiness_report_serialize(
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportVcReadinessReport200Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _passport_vc_readiness_report_serialize(
        self,
        page,
        limit,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        if page is not None:
            
            _query_params.append(('page', page))
            
        if limit is not None:
            
            _query_params.append(('limit', limit))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/passports/vc-readiness',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def seal_passport(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportSealResponse:
        """Apply the tenant's advanced electronic seal

        Signs the passport's Merkle root (SHA-256 tree over the key-sorted top-level `metadata` entries) with the tenant's vault-held **ECDSA P-256 (prime256v1)** private key, producing an **advanced** electronic seal (this is a local cryptographic seal — NOT a Commission/EU-registry registration, and NOT a qualified seal). The base64 signature is stored as `digitalSeal` together with the signing public key (PEM), the X.509 chain binding the key to the tenant's legal identity (surfaced as `proof.x5c`, leaf first, base64 DER), and — **best-effort, opt-in** — an RFC 3161 trusted timestamp over SHA-256(merkleRoot) (`proof.rfc3161`; a TSA outage or missing configuration never blocks sealing, the field is simply absent).  A `passport.sealed` webhook is enqueued transactionally with the update (payload: the public-redacted JSON-LD document including the full `proof` block).  **Permission:** `passport:seal` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or `productId`** (UUID tried first), restricted to the passport's **owning tenant**.  **Behavioral caveats:** - The route does **not** modify the passport's `status` — despite the success message's \"and published\" wording, a DRAFT stays a DRAFT after sealing. Publish via `PUT /api/v1/passports/{id}` (validated save) instead. - Re-sealing an already-sealed passport is allowed and **overwrites** the previous seal/timestamp. - Once sealed, in-place metadata edits are refused (403 on `PUT /api/v1/passports/{id}`). - Requires the tenant's signing key pair to exist — otherwise 400. - The returned `passport` document is serialized at the **public** redaction tier (masked keys keep their true leaf hashes in `proof.redactedLeaves`, so the seal stays offline-verifiable after redaction).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._seal_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportSealResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def seal_passport_with_http_info(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportSealResponse]:
        """Apply the tenant's advanced electronic seal

        Signs the passport's Merkle root (SHA-256 tree over the key-sorted top-level `metadata` entries) with the tenant's vault-held **ECDSA P-256 (prime256v1)** private key, producing an **advanced** electronic seal (this is a local cryptographic seal — NOT a Commission/EU-registry registration, and NOT a qualified seal). The base64 signature is stored as `digitalSeal` together with the signing public key (PEM), the X.509 chain binding the key to the tenant's legal identity (surfaced as `proof.x5c`, leaf first, base64 DER), and — **best-effort, opt-in** — an RFC 3161 trusted timestamp over SHA-256(merkleRoot) (`proof.rfc3161`; a TSA outage or missing configuration never blocks sealing, the field is simply absent).  A `passport.sealed` webhook is enqueued transactionally with the update (payload: the public-redacted JSON-LD document including the full `proof` block).  **Permission:** `passport:seal` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or `productId`** (UUID tried first), restricted to the passport's **owning tenant**.  **Behavioral caveats:** - The route does **not** modify the passport's `status` — despite the success message's \"and published\" wording, a DRAFT stays a DRAFT after sealing. Publish via `PUT /api/v1/passports/{id}` (validated save) instead. - Re-sealing an already-sealed passport is allowed and **overwrites** the previous seal/timestamp. - Once sealed, in-place metadata edits are refused (403 on `PUT /api/v1/passports/{id}`). - Requires the tenant's signing key pair to exist — otherwise 400. - The returned `passport` document is serialized at the **public** redaction tier (masked keys keep their true leaf hashes in `proof.redactedLeaves`, so the seal stays offline-verifiable after redaction).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._seal_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportSealResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def seal_passport_without_preload_content(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first.")],
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Apply the tenant's advanced electronic seal

        Signs the passport's Merkle root (SHA-256 tree over the key-sorted top-level `metadata` entries) with the tenant's vault-held **ECDSA P-256 (prime256v1)** private key, producing an **advanced** electronic seal (this is a local cryptographic seal — NOT a Commission/EU-registry registration, and NOT a qualified seal). The base64 signature is stored as `digitalSeal` together with the signing public key (PEM), the X.509 chain binding the key to the tenant's legal identity (surfaced as `proof.x5c`, leaf first, base64 DER), and — **best-effort, opt-in** — an RFC 3161 trusted timestamp over SHA-256(merkleRoot) (`proof.rfc3161`; a TSA outage or missing configuration never blocks sealing, the field is simply absent).  A `passport.sealed` webhook is enqueued transactionally with the update (payload: the public-redacted JSON-LD document including the full `proof` block).  **Permission:** `passport:seal` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or `productId`** (UUID tried first), restricted to the passport's **owning tenant**.  **Behavioral caveats:** - The route does **not** modify the passport's `status` — despite the success message's \"and published\" wording, a DRAFT stays a DRAFT after sealing. Publish via `PUT /api/v1/passports/{id}` (validated save) instead. - Re-sealing an already-sealed passport is allowed and **overwrites** the previous seal/timestamp. - Once sealed, in-place metadata edits are refused (403 on `PUT /api/v1/passports/{id}`). - Requires the tenant's signing key pair to exist — otherwise 400. - The returned `passport` document is serialized at the **public** redaction tier (masked keys keep their true leaf hashes in `proof.redactedLeaves`, so the seal stays offline-verifiable after redaction).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
        :type id: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._seal_passport_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportSealResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _seal_passport_serialize(
        self,
        id,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/passports/{id}/seal',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def update_passport(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID. `productId` aliasing is NOT supported here.")],
        passport_update_request: PassportUpdateRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for `friendlyMessage` localization in validation errors. Falls back to `Accept-Language`, then `en`. Unsupported values are ignored (no error).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportUpdateResponse:
        """Update passport metadata (versioned to history)

        Replaces the passport's `metadata` (the Merkle root and leaf hashes are recomputed) and snapshots the **previous** metadata into the passport's version history (version = count + 1, `changedBy` = user email or `api-key:<id>`, `changeReason` defaults to `\"API Update\"`).  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token` (double-submit); Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** — `productId` aliasing is NOT supported on this endpoint. The passport must belong to an operator bound to your workspace.  **Draft semantics (`draft` flag):** - `draft: true` **skips ESPR validation entirely** and forces `status: \"DRAFT\"` — note this also demotes an already-published (ACTIVE/RECALLED/DECOMMISSIONED) passport back to DRAFT. - `draft` absent/false: `metadata` is validated against the ESPR category rules (400 on failure — see below). If the passport was a DRAFT it is **published**: status becomes `ACTIVE`, a `passport.ingested` webhook is enqueued transactionally (public-redacted JSON-LD payload) and an in-app notification is created best-effort afterwards. Editing an already-published (live) passport leaves its status untouched and enqueues a `passport.updated` webhook instead (same public-redacted JSON-LD payload).  **Validation divergence:** the 400 validation body here contains `errors` but — unlike `POST /api/v1/passports` — **never a `warnings` array**. `friendlyMessage` is localized via the `lang` query parameter or `Accept-Language` (28 languages, default `en`; unsupported values silently fall back).  **Sealed passports are immutable in place:** if `digitalSeal` is set the update is refused with **403** (message: \"This passport is sealed and cannot be edited in place — editing would invalidate the eIDAS advanced electronic seal. Re-seal explicitly after any change.\").  **Facility:** omit `facilityId` to leave it unchanged; pass `null` or `\"\"` to detach; pass a facility UUID owned by your tenant to attach (400 if not found in your workspace).  **Enrichment:** include the `enrichment` key (even as `null`/`{}`) to overwrite the presentational marketing block; omit it to leave it unchanged. Values are sanitized server-side (truncated/sliced, http(s) URLs only), never rejected.  **Response caveat:** the returned `passport` document is serialized at the **public** redaction tier — `facilityDetails` (and battery restricted keys) appear as `\"[REDACTED - Privileged Access Required]\"` even though you are the owner.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID. `productId` aliasing is NOT supported here. (required)
        :type id: str
        :param passport_update_request: (required)
        :type passport_update_request: PassportUpdateRequest
        :param lang: Locale for `friendlyMessage` localization in validation errors. Falls back to `Accept-Language`, then `en`. Unsupported values are ignored (no error).
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._update_passport_serialize(
            id=id,
            passport_update_request=passport_update_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportUpdateResponse",
            '400': "PassportUpdateBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def update_passport_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID. `productId` aliasing is NOT supported here.")],
        passport_update_request: PassportUpdateRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for `friendlyMessage` localization in validation errors. Falls back to `Accept-Language`, then `en`. Unsupported values are ignored (no error).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportUpdateResponse]:
        """Update passport metadata (versioned to history)

        Replaces the passport's `metadata` (the Merkle root and leaf hashes are recomputed) and snapshots the **previous** metadata into the passport's version history (version = count + 1, `changedBy` = user email or `api-key:<id>`, `changeReason` defaults to `\"API Update\"`).  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token` (double-submit); Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** — `productId` aliasing is NOT supported on this endpoint. The passport must belong to an operator bound to your workspace.  **Draft semantics (`draft` flag):** - `draft: true` **skips ESPR validation entirely** and forces `status: \"DRAFT\"` — note this also demotes an already-published (ACTIVE/RECALLED/DECOMMISSIONED) passport back to DRAFT. - `draft` absent/false: `metadata` is validated against the ESPR category rules (400 on failure — see below). If the passport was a DRAFT it is **published**: status becomes `ACTIVE`, a `passport.ingested` webhook is enqueued transactionally (public-redacted JSON-LD payload) and an in-app notification is created best-effort afterwards. Editing an already-published (live) passport leaves its status untouched and enqueues a `passport.updated` webhook instead (same public-redacted JSON-LD payload).  **Validation divergence:** the 400 validation body here contains `errors` but — unlike `POST /api/v1/passports` — **never a `warnings` array**. `friendlyMessage` is localized via the `lang` query parameter or `Accept-Language` (28 languages, default `en`; unsupported values silently fall back).  **Sealed passports are immutable in place:** if `digitalSeal` is set the update is refused with **403** (message: \"This passport is sealed and cannot be edited in place — editing would invalidate the eIDAS advanced electronic seal. Re-seal explicitly after any change.\").  **Facility:** omit `facilityId` to leave it unchanged; pass `null` or `\"\"` to detach; pass a facility UUID owned by your tenant to attach (400 if not found in your workspace).  **Enrichment:** include the `enrichment` key (even as `null`/`{}`) to overwrite the presentational marketing block; omit it to leave it unchanged. Values are sanitized server-side (truncated/sliced, http(s) URLs only), never rejected.  **Response caveat:** the returned `passport` document is serialized at the **public** redaction tier — `facilityDetails` (and battery restricted keys) appear as `\"[REDACTED - Privileged Access Required]\"` even though you are the owner.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID. `productId` aliasing is NOT supported here. (required)
        :type id: str
        :param passport_update_request: (required)
        :type passport_update_request: PassportUpdateRequest
        :param lang: Locale for `friendlyMessage` localization in validation errors. Falls back to `Accept-Language`, then `en`. Unsupported values are ignored (no error).
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._update_passport_serialize(
            id=id,
            passport_update_request=passport_update_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportUpdateResponse",
            '400': "PassportUpdateBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def update_passport_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID. `productId` aliasing is NOT supported here.")],
        passport_update_request: PassportUpdateRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for `friendlyMessage` localization in validation errors. Falls back to `Accept-Language`, then `en`. Unsupported values are ignored (no error).")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Update passport metadata (versioned to history)

        Replaces the passport's `metadata` (the Merkle root and leaf hashes are recomputed) and snapshots the **previous** metadata into the passport's version history (version = count + 1, `changedBy` = user email or `api-key:<id>`, `changeReason` defaults to `\"API Update\"`).  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token` (double-submit); Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** — `productId` aliasing is NOT supported on this endpoint. The passport must belong to an operator bound to your workspace.  **Draft semantics (`draft` flag):** - `draft: true` **skips ESPR validation entirely** and forces `status: \"DRAFT\"` — note this also demotes an already-published (ACTIVE/RECALLED/DECOMMISSIONED) passport back to DRAFT. - `draft` absent/false: `metadata` is validated against the ESPR category rules (400 on failure — see below). If the passport was a DRAFT it is **published**: status becomes `ACTIVE`, a `passport.ingested` webhook is enqueued transactionally (public-redacted JSON-LD payload) and an in-app notification is created best-effort afterwards. Editing an already-published (live) passport leaves its status untouched and enqueues a `passport.updated` webhook instead (same public-redacted JSON-LD payload).  **Validation divergence:** the 400 validation body here contains `errors` but — unlike `POST /api/v1/passports` — **never a `warnings` array**. `friendlyMessage` is localized via the `lang` query parameter or `Accept-Language` (28 languages, default `en`; unsupported values silently fall back).  **Sealed passports are immutable in place:** if `digitalSeal` is set the update is refused with **403** (message: \"This passport is sealed and cannot be edited in place — editing would invalidate the eIDAS advanced electronic seal. Re-seal explicitly after any change.\").  **Facility:** omit `facilityId` to leave it unchanged; pass `null` or `\"\"` to detach; pass a facility UUID owned by your tenant to attach (400 if not found in your workspace).  **Enrichment:** include the `enrichment` key (even as `null`/`{}`) to overwrite the presentational marketing block; omit it to leave it unchanged. Values are sanitized server-side (truncated/sliced, http(s) URLs only), never rejected.  **Response caveat:** the returned `passport` document is serialized at the **public** redaction tier — `facilityDetails` (and battery restricted keys) appear as `\"[REDACTED - Privileged Access Required]\"` even though you are the owner.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID. `productId` aliasing is NOT supported here. (required)
        :type id: str
        :param passport_update_request: (required)
        :type passport_update_request: PassportUpdateRequest
        :param lang: Locale for `friendlyMessage` localization in validation errors. Falls back to `Accept-Language`, then `en`. Unsupported values are ignored (no error).
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._update_passport_serialize(
            id=id,
            passport_update_request=passport_update_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportUpdateResponse",
            '400': "PassportUpdateBadRequest",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _update_passport_serialize(
        self,
        id,
        passport_update_request,
        lang,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        if lang is not None:
            
            _query_params.append(('lang', lang))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if passport_update_request is not None:
            _body_params = passport_update_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='PUT',
            resource_path='/api/v1/passports/{id}',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def update_passport_status(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first.")],
        passport_status_update_request: PassportStatusUpdateRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportStatusUpdateResponse:
        """Transition passport lifecycle status (recall / decommission / reactivate)

        Transitions a **published** passport between live lifecycle states. The request body carries only `status` (any other keys are ignored — there is no `reason` field; the history entry's change reason is auto-generated as `Status changed: <from> → <to>`).  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or `productId`** (UUID tried first), scoped to operators bound to your workspace.  **Effects:** - `DECOMMISSIONED` — sets `retentionUntil = now + the configured retention period` (default 15 years), starting the minimum-availability retention clock. The passport stays publicly resolvable. - `ACTIVE` (reactivation) — clears `retentionUntil` **and** `archivedAt`. - `RECALLED` — marks the product recalled. - The status change, the version-history entry (who/when/what) and the webhook enqueue are **transactional**; an in-app notification is created **best-effort after the transaction commits** (a notification failure never affects the response).  **Webhooks:** `RECALLED` enqueues `passport.recalled`; any other transition (`DECOMMISSIONED`, reactivate-to-`ACTIVE`) enqueues `passport.status_updated` — note that `passport.status_updated` is **not** an explicitly subscribable event filter, so only wildcard (`\"*\"`) webhook subscriptions receive it. Payloads are the public-redacted JSON-LD document.  **Caveats:** DRAFT passports are refused with 409 (publish first via a validated `PUT /api/v1/passports/{id}`). Sealed passports CAN change status — `status` is stored alongside the document, not inside the sealed metadata Merkle tree. The returned `passport` document is serialized at the **public** redaction tier.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
        :type id: str
        :param passport_status_update_request: (required)
        :type passport_status_update_request: PassportStatusUpdateRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._update_passport_status_serialize(
            id=id,
            passport_status_update_request=passport_status_update_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportStatusUpdateResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '409': "Error",
            '429': "InlineObject",
            '500': "UpdatePassportStatus500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def update_passport_status_with_http_info(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first.")],
        passport_status_update_request: PassportStatusUpdateRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportStatusUpdateResponse]:
        """Transition passport lifecycle status (recall / decommission / reactivate)

        Transitions a **published** passport between live lifecycle states. The request body carries only `status` (any other keys are ignored — there is no `reason` field; the history entry's change reason is auto-generated as `Status changed: <from> → <to>`).  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or `productId`** (UUID tried first), scoped to operators bound to your workspace.  **Effects:** - `DECOMMISSIONED` — sets `retentionUntil = now + the configured retention period` (default 15 years), starting the minimum-availability retention clock. The passport stays publicly resolvable. - `ACTIVE` (reactivation) — clears `retentionUntil` **and** `archivedAt`. - `RECALLED` — marks the product recalled. - The status change, the version-history entry (who/when/what) and the webhook enqueue are **transactional**; an in-app notification is created **best-effort after the transaction commits** (a notification failure never affects the response).  **Webhooks:** `RECALLED` enqueues `passport.recalled`; any other transition (`DECOMMISSIONED`, reactivate-to-`ACTIVE`) enqueues `passport.status_updated` — note that `passport.status_updated` is **not** an explicitly subscribable event filter, so only wildcard (`\"*\"`) webhook subscriptions receive it. Payloads are the public-redacted JSON-LD document.  **Caveats:** DRAFT passports are refused with 409 (publish first via a validated `PUT /api/v1/passports/{id}`). Sealed passports CAN change status — `status` is stored alongside the document, not inside the sealed metadata Merkle tree. The returned `passport` document is serialized at the **public** redaction tier.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
        :type id: str
        :param passport_status_update_request: (required)
        :type passport_status_update_request: PassportStatusUpdateRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._update_passport_status_serialize(
            id=id,
            passport_status_update_request=passport_status_update_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportStatusUpdateResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '409': "Error",
            '429': "InlineObject",
            '500': "UpdatePassportStatus500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def update_passport_status_without_preload_content(
        self,
        id: Annotated[str, Field(min_length=1, strict=True, description="Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first.")],
        passport_status_update_request: PassportStatusUpdateRequest,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Transition passport lifecycle status (recall / decommission / reactivate)

        Transitions a **published** passport between live lifecycle states. The request body carries only `status` (any other keys are ignored — there is no `reason` field; the history entry's change reason is auto-generated as `Status changed: <from> → <to>`).  **Permission:** `passport:update` (write — subscription gating applies, see 402). Cookie sessions must send `X-CSRF-Token`; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or `productId`** (UUID tried first), scoped to operators bound to your workspace.  **Effects:** - `DECOMMISSIONED` — sets `retentionUntil = now + the configured retention period` (default 15 years), starting the minimum-availability retention clock. The passport stays publicly resolvable. - `ACTIVE` (reactivation) — clears `retentionUntil` **and** `archivedAt`. - `RECALLED` — marks the product recalled. - The status change, the version-history entry (who/when/what) and the webhook enqueue are **transactional**; an in-app notification is created **best-effort after the transaction commits** (a notification failure never affects the response).  **Webhooks:** `RECALLED` enqueues `passport.recalled`; any other transition (`DECOMMISSIONED`, reactivate-to-`ACTIVE`) enqueues `passport.status_updated` — note that `passport.status_updated` is **not** an explicitly subscribable event filter, so only wildcard (`\"*\"`) webhook subscriptions receive it. Payloads are the public-redacted JSON-LD document.  **Caveats:** DRAFT passports are refused with 409 (publish first via a validated `PUT /api/v1/passports/{id}`). Sealed passports CAN change status — `status` is stored alongside the document, not inside the sealed metadata Merkle tree. The returned `passport` document is serialized at the **public** redaction tier.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID **or** caller-supplied `productId` (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
        :type id: str
        :param passport_status_update_request: (required)
        :type passport_status_update_request: PassportStatusUpdateRequest
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._update_passport_status_serialize(
            id=id,
            passport_status_update_request=passport_status_update_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportStatusUpdateResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
            '409': "Error",
            '429': "InlineObject",
            '500': "UpdatePassportStatus500Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _update_passport_status_serialize(
        self,
        id,
        passport_status_update_request,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        if id is not None:
            _path_params['id'] = id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if passport_status_update_request is not None:
            _body_params = passport_status_update_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='PUT',
            resource_path='/api/v1/passports/{id}/status',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def validate_passport(
        self,
        passport_validate_only_request: PassportValidateOnlyRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportValidateOnlyResult:
        """Dry-run ESPR validation of passport metadata (nothing is stored)

        Runs the full ESPR category schema validation on a metadata payload **without persisting anything** — intended for pre-flight checks in integration pipelines.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions). Despite being read-only in effect, it is gated as a write permission, so subscription gating (**402**) applies.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 262,144 bytes (256 KiB)** → **413** beyond that.  **Behavioral caveats:** - `operatorId` is accepted by the body schema but **ignored** by the handler. - The 200 body always carries `errors: []`; `warnings` is **omitted entirely** when there are none (it is not an empty array). The same omission applies to `warnings` on the 400 Validation Failed body. - `friendlyMessage` localization via `?lang=` / `Accept-Language` (28 languages, default `en`); category-validity errors (`metadata.category` missing or unknown) carry no `friendlyMessage`. - Structural rejections of the request body (e.g. missing `productId`, non-object `metadata`) and malformed JSON return just `{\"error\": \"Bad Request\", \"message\": …}`; the structurally bad inputs that reach the handler are a whitespace-only `productId` and a malformed GTIN-14 `productId` (14 digits failing the GS1 mod-10 check), each answered with the fuller `Bad Request` body shown below.

        :param passport_validate_only_request: (required)
        :type passport_validate_only_request: PassportValidateOnlyRequest
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._validate_passport_serialize(
            passport_validate_only_request=passport_validate_only_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportValidateOnlyResult",
            '400': "PassportValidateOnlyError",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def validate_passport_with_http_info(
        self,
        passport_validate_only_request: PassportValidateOnlyRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportValidateOnlyResult]:
        """Dry-run ESPR validation of passport metadata (nothing is stored)

        Runs the full ESPR category schema validation on a metadata payload **without persisting anything** — intended for pre-flight checks in integration pipelines.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions). Despite being read-only in effect, it is gated as a write permission, so subscription gating (**402**) applies.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 262,144 bytes (256 KiB)** → **413** beyond that.  **Behavioral caveats:** - `operatorId` is accepted by the body schema but **ignored** by the handler. - The 200 body always carries `errors: []`; `warnings` is **omitted entirely** when there are none (it is not an empty array). The same omission applies to `warnings` on the 400 Validation Failed body. - `friendlyMessage` localization via `?lang=` / `Accept-Language` (28 languages, default `en`); category-validity errors (`metadata.category` missing or unknown) carry no `friendlyMessage`. - Structural rejections of the request body (e.g. missing `productId`, non-object `metadata`) and malformed JSON return just `{\"error\": \"Bad Request\", \"message\": …}`; the structurally bad inputs that reach the handler are a whitespace-only `productId` and a malformed GTIN-14 `productId` (14 digits failing the GS1 mod-10 check), each answered with the fuller `Bad Request` body shown below.

        :param passport_validate_only_request: (required)
        :type passport_validate_only_request: PassportValidateOnlyRequest
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._validate_passport_serialize(
            passport_validate_only_request=passport_validate_only_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportValidateOnlyResult",
            '400': "PassportValidateOnlyError",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def validate_passport_without_preload_content(
        self,
        passport_validate_only_request: PassportValidateOnlyRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Dry-run ESPR validation of passport metadata (nothing is stored)

        Runs the full ESPR category schema validation on a metadata payload **without persisting anything** — intended for pre-flight checks in integration pipelines.  **Permission:** `passport:create` (Bearer API key or session JWT + CSRF for cookie sessions). Despite being read-only in effect, it is gated as a write permission, so subscription gating (**402**) applies.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`. **Body limit: 262,144 bytes (256 KiB)** → **413** beyond that.  **Behavioral caveats:** - `operatorId` is accepted by the body schema but **ignored** by the handler. - The 200 body always carries `errors: []`; `warnings` is **omitted entirely** when there are none (it is not an empty array). The same omission applies to `warnings` on the 400 Validation Failed body. - `friendlyMessage` localization via `?lang=` / `Accept-Language` (28 languages, default `en`); category-validity errors (`metadata.category` missing or unknown) carry no `friendlyMessage`. - Structural rejections of the request body (e.g. missing `productId`, non-object `metadata`) and malformed JSON return just `{\"error\": \"Bad Request\", \"message\": …}`; the structurally bad inputs that reach the handler are a whitespace-only `productId` and a malformed GTIN-14 `productId` (14 digits failing the GS1 mod-10 check), each answered with the fuller `Bad Request` body shown below.

        :param passport_validate_only_request: (required)
        :type passport_validate_only_request: PassportValidateOnlyRequest
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._validate_passport_serialize(
            passport_validate_only_request=passport_validate_only_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportValidateOnlyResult",
            '400': "PassportValidateOnlyError",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '413': "CreatePassport413Response",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _validate_passport_serialize(
        self,
        passport_validate_only_request,
        lang,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        if lang is not None:
            
            _query_params.append(('lang', lang))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if passport_validate_only_request is not None:
            _body_params = passport_validate_only_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/passports/validate-only',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )




    @validate_call
    def validate_passport_public(
        self,
        passport_validate_only_request: PassportValidateOnlyRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> PassportValidateOnlyResult:
        """Permission-free dry-run ESPR metadata validation (strictly rate-limited)

        Identical validation semantics to `POST /api/v1/passports/validate-only`, but requires **no specific permission** — any valid API key or Console session is accepted, so every plan including the free tier can call it. Nothing is persisted.  **Authentication is required.** Until contract 1.12.0 this endpoint was reachable anonymously; it is not any more, because it runs the full validation engine and was usable as free unauthenticated compute. An anonymous call now returns **401**. The path keeps its `-public` segment for continuity — \"public\" here means *no permission and no tenant scope*, not *unauthenticated*.  **Rate limit: 10 requests/min per IP** — a strict per-route limit that **replaces** the global ceiling for this endpoint (emits `x-ratelimit-limit` / `x-ratelimit-remaining` / `x-ratelimit-reset` headers and `retry-after` on 429). **Body limit: 65,536 bytes (64 KiB)** → **413** beyond that. Both caps remain as defence in depth against authenticated abuse. The credential is checked **before the body is parsed**, so an anonymous oversized body is rejected as **401**, not 413.  **Behavioral caveats:** - No tenant context: `operatorId` is accepted but ignored. - The 200 body always carries `errors: []`; `warnings` is omitted entirely when there are none (same omission on the 400 Validation Failed body). - Error/warning `friendlyMessage` localization via `?lang=` / `Accept-Language` (28 languages, default `en`); category-validity errors carry no `friendlyMessage`. - Structural rejections of the request body (e.g. missing `productId`) and malformed JSON return just `{\"error\": \"Bad Request\", \"message\": …}`; a whitespace-only `productId` or a malformed GTIN-14 `productId` (14 digits failing the GS1 mod-10 check) gets the fuller `Bad Request` body shown below.

        :param passport_validate_only_request: (required)
        :type passport_validate_only_request: PassportValidateOnlyRequest
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._validate_passport_public_serialize(
            passport_validate_only_request=passport_validate_only_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportValidateOnlyResult",
            '400': "PassportValidateOnlyError",
            '401': "Error",
            '413': "CreatePassport413Response",
            '429': "ValidatePassportPublic429Response",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        ).data


    @validate_call
    def validate_passport_public_with_http_info(
        self,
        passport_validate_only_request: PassportValidateOnlyRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> ApiResponse[PassportValidateOnlyResult]:
        """Permission-free dry-run ESPR metadata validation (strictly rate-limited)

        Identical validation semantics to `POST /api/v1/passports/validate-only`, but requires **no specific permission** — any valid API key or Console session is accepted, so every plan including the free tier can call it. Nothing is persisted.  **Authentication is required.** Until contract 1.12.0 this endpoint was reachable anonymously; it is not any more, because it runs the full validation engine and was usable as free unauthenticated compute. An anonymous call now returns **401**. The path keeps its `-public` segment for continuity — \"public\" here means *no permission and no tenant scope*, not *unauthenticated*.  **Rate limit: 10 requests/min per IP** — a strict per-route limit that **replaces** the global ceiling for this endpoint (emits `x-ratelimit-limit` / `x-ratelimit-remaining` / `x-ratelimit-reset` headers and `retry-after` on 429). **Body limit: 65,536 bytes (64 KiB)** → **413** beyond that. Both caps remain as defence in depth against authenticated abuse. The credential is checked **before the body is parsed**, so an anonymous oversized body is rejected as **401**, not 413.  **Behavioral caveats:** - No tenant context: `operatorId` is accepted but ignored. - The 200 body always carries `errors: []`; `warnings` is omitted entirely when there are none (same omission on the 400 Validation Failed body). - Error/warning `friendlyMessage` localization via `?lang=` / `Accept-Language` (28 languages, default `en`); category-validity errors carry no `friendlyMessage`. - Structural rejections of the request body (e.g. missing `productId`) and malformed JSON return just `{\"error\": \"Bad Request\", \"message\": …}`; a whitespace-only `productId` or a malformed GTIN-14 `productId` (14 digits failing the GS1 mod-10 check) gets the fuller `Bad Request` body shown below.

        :param passport_validate_only_request: (required)
        :type passport_validate_only_request: PassportValidateOnlyRequest
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._validate_passport_public_serialize(
            passport_validate_only_request=passport_validate_only_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportValidateOnlyResult",
            '400': "PassportValidateOnlyError",
            '401': "Error",
            '413': "CreatePassport413Response",
            '429': "ValidatePassportPublic429Response",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        response_data.read()
        return self.api_client.response_deserialize(
            response_data=response_data,
            response_types_map=_response_types_map,
        )


    @validate_call
    def validate_passport_public_without_preload_content(
        self,
        passport_validate_only_request: PassportValidateOnlyRequest,
        lang: Annotated[Optional[StrictStr], Field(description="Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.")] = None,
        _request_timeout: Union[
            None,
            Annotated[StrictFloat, Field(gt=0)],
            Tuple[
                Annotated[StrictFloat, Field(gt=0)],
                Annotated[StrictFloat, Field(gt=0)]
            ]
        ] = None,
        _request_auth: Optional[Dict[StrictStr, Any]] = None,
        _content_type: Optional[StrictStr] = None,
        _headers: Optional[Dict[StrictStr, Any]] = None,
        _host_index: Annotated[StrictInt, Field(ge=0, le=0)] = 0,
    ) -> RESTResponseType:
        """Permission-free dry-run ESPR metadata validation (strictly rate-limited)

        Identical validation semantics to `POST /api/v1/passports/validate-only`, but requires **no specific permission** — any valid API key or Console session is accepted, so every plan including the free tier can call it. Nothing is persisted.  **Authentication is required.** Until contract 1.12.0 this endpoint was reachable anonymously; it is not any more, because it runs the full validation engine and was usable as free unauthenticated compute. An anonymous call now returns **401**. The path keeps its `-public` segment for continuity — \"public\" here means *no permission and no tenant scope*, not *unauthenticated*.  **Rate limit: 10 requests/min per IP** — a strict per-route limit that **replaces** the global ceiling for this endpoint (emits `x-ratelimit-limit` / `x-ratelimit-remaining` / `x-ratelimit-reset` headers and `retry-after` on 429). **Body limit: 65,536 bytes (64 KiB)** → **413** beyond that. Both caps remain as defence in depth against authenticated abuse. The credential is checked **before the body is parsed**, so an anonymous oversized body is rejected as **401**, not 413.  **Behavioral caveats:** - No tenant context: `operatorId` is accepted but ignored. - The 200 body always carries `errors: []`; `warnings` is omitted entirely when there are none (same omission on the 400 Validation Failed body). - Error/warning `friendlyMessage` localization via `?lang=` / `Accept-Language` (28 languages, default `en`); category-validity errors carry no `friendlyMessage`. - Structural rejections of the request body (e.g. missing `productId`) and malformed JSON return just `{\"error\": \"Bad Request\", \"message\": …}`; a whitespace-only `productId` or a malformed GTIN-14 `productId` (14 digits failing the GS1 mod-10 check) gets the fuller `Bad Request` body shown below.

        :param passport_validate_only_request: (required)
        :type passport_validate_only_request: PassportValidateOnlyRequest
        :param lang: Locale for localized `friendlyMessage` validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to `Accept-Language`, then `en`.
        :type lang: str
        :param _request_timeout: timeout setting for this request. If one
                                 number provided, it will be total request
                                 timeout. It can also be a pair (tuple) of
                                 (connection, read) timeouts.
        :type _request_timeout: int, tuple(int, int), optional
        :param _request_auth: set to override the auth_settings for an a single
                              request; this effectively ignores the
                              authentication in the spec for a single request.
        :type _request_auth: dict, optional
        :param _content_type: force content-type for the request.
        :type _content_type: str, Optional
        :param _headers: set to override the headers for a single
                         request; this effectively ignores the headers
                         in the spec for a single request.
        :type _headers: dict, optional
        :param _host_index: set to override the host_index for a single
                            request; this effectively ignores the host_index
                            in the spec for a single request.
        :type _host_index: int, optional
        :return: Returns the result object.
        """ # noqa: E501

        _param = self._validate_passport_public_serialize(
            passport_validate_only_request=passport_validate_only_request,
            lang=lang,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PassportValidateOnlyResult",
            '400': "PassportValidateOnlyError",
            '401': "Error",
            '413': "CreatePassport413Response",
            '429': "ValidatePassportPublic429Response",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _validate_passport_public_serialize(
        self,
        passport_validate_only_request,
        lang,
        _request_auth,
        _content_type,
        _headers,
        _host_index,
    ) -> RequestSerialized:

        _host = None

        _collection_formats: Dict[str, str] = {
        }

        _path_params: Dict[str, str] = {}
        _query_params: List[Tuple[str, str]] = []
        _header_params: Dict[str, Optional[str]] = _headers or {}
        _form_params: List[Tuple[str, str]] = []
        _files: Dict[
            str, Union[str, bytes, List[str], List[bytes], List[Tuple[str, bytes]]]
        ] = {}
        _body_params: Optional[bytes] = None

        # process the path parameters
        # process the query parameters
        if lang is not None:
            
            _query_params.append(('lang', lang))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if passport_validate_only_request is not None:
            _body_params = passport_validate_only_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json'
                ]
            )

        # set the HTTP header `Content-Type`
        if _content_type:
            _header_params['Content-Type'] = _content_type
        else:
            _default_content_type = (
                self.api_client.select_header_content_type(
                    [
                        'application/json'
                    ]
                )
            )
            if _default_content_type is not None:
                _header_params['Content-Type'] = _default_content_type

        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/passports/validate-only-public',
            path_params=_path_params,
            query_params=_query_params,
            header_params=_header_params,
            body=_body_params,
            post_params=_form_params,
            files=_files,
            auth_settings=_auth_settings,
            collection_formats=_collection_formats,
            _host=_host,
            _request_auth=_request_auth
        )


