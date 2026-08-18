# coding: utf-8

"""
    OpenDPP Integration API

    OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) Anyone can verify a seal — no account required. `POST /api/v1/audit/verify` recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.

    The version of the OpenAPI document: 1.13.0
    Contact: support@opendpp-node.eu
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501

import warnings
from pydantic import validate_call, Field, StrictFloat, StrictStr, StrictInt
from typing import Any, Dict, List, Optional, Tuple, Union
from typing_extensions import Annotated

from pydantic import Field, StrictStr
from typing import Optional
from typing_extensions import Annotated
from opendpp_sdk.models.battery_unit_event_list_response import BatteryUnitEventListResponse
from opendpp_sdk.models.battery_unit_json_ld import BatteryUnitJsonLd
from opendpp_sdk.models.battery_unit_list_response import BatteryUnitListResponse
from opendpp_sdk.models.bulk_battery_unit_events_request import BulkBatteryUnitEventsRequest
from opendpp_sdk.models.bulk_battery_unit_events_response import BulkBatteryUnitEventsResponse
from opendpp_sdk.models.record_battery_unit_event_request import RecordBatteryUnitEventRequest
from opendpp_sdk.models.record_battery_unit_event_response import RecordBatteryUnitEventResponse
from opendpp_sdk.models.serialize_battery_units_request import SerializeBatteryUnitsRequest
from opendpp_sdk.models.serialize_battery_units_response import SerializeBatteryUnitsResponse
from opendpp_sdk.models.validate_battery_units200_response import ValidateBatteryUnits200Response

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class BatteryUnitsApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def bulk_record_battery_unit_events(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        bulk_battery_unit_events_request: BulkBatteryUnitEventsRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this batch with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally and readings may then be recorded twice.")] = None,
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
    ) -> BulkBatteryUnitEventsResponse:
        """Append a batch of telemetry events to a battery unit

        Appends up to **500** append-only dynamic-data records to one unit in a single request — the batch venue for backfilling a fleet's telemetry. **Telemetry only:** a record carrying `status` is refused per-item; a status transition is a lifecycle decision and goes through `POST /api/v1/units/{id}/events` one event at a time.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403). A terminal (`RECYCLED`) unit refuses the whole batch (400 `Terminal Unit Status`).  **Per-record validation (collected as `[index]`-prefixed strings in `errors`, not a rejection of the whole batch):** the same field checks as the single-event endpoint — `eventType` required and valid, numeric ranges, Date-parseable `recordedAt` (defaults to server time when omitted) — plus the physics consistency checks, judged in chronological order across the batch and the unit's recorded history, so a reading that only conflicts with another batch member is caught too.  **Partial success:** the response is **201 when at least one record was accepted**; skipped items are listed in `errors`. If *every* item failed you get **400** `Bulk Event Ingest Failed` with the same string array.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param bulk_battery_unit_events_request: (required)
        :type bulk_battery_unit_events_request: BulkBatteryUnitEventsRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this batch with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally and readings may then be recorded twice.
        :type idempotency_key: str
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

        _param = self._bulk_record_battery_unit_events_serialize(
            id=id,
            bulk_battery_unit_events_request=bulk_battery_unit_events_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "BulkBatteryUnitEventsResponse",
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
    def bulk_record_battery_unit_events_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        bulk_battery_unit_events_request: BulkBatteryUnitEventsRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this batch with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally and readings may then be recorded twice.")] = None,
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
    ) -> ApiResponse[BulkBatteryUnitEventsResponse]:
        """Append a batch of telemetry events to a battery unit

        Appends up to **500** append-only dynamic-data records to one unit in a single request — the batch venue for backfilling a fleet's telemetry. **Telemetry only:** a record carrying `status` is refused per-item; a status transition is a lifecycle decision and goes through `POST /api/v1/units/{id}/events` one event at a time.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403). A terminal (`RECYCLED`) unit refuses the whole batch (400 `Terminal Unit Status`).  **Per-record validation (collected as `[index]`-prefixed strings in `errors`, not a rejection of the whole batch):** the same field checks as the single-event endpoint — `eventType` required and valid, numeric ranges, Date-parseable `recordedAt` (defaults to server time when omitted) — plus the physics consistency checks, judged in chronological order across the batch and the unit's recorded history, so a reading that only conflicts with another batch member is caught too.  **Partial success:** the response is **201 when at least one record was accepted**; skipped items are listed in `errors`. If *every* item failed you get **400** `Bulk Event Ingest Failed` with the same string array.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param bulk_battery_unit_events_request: (required)
        :type bulk_battery_unit_events_request: BulkBatteryUnitEventsRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this batch with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally and readings may then be recorded twice.
        :type idempotency_key: str
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

        _param = self._bulk_record_battery_unit_events_serialize(
            id=id,
            bulk_battery_unit_events_request=bulk_battery_unit_events_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "BulkBatteryUnitEventsResponse",
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
    def bulk_record_battery_unit_events_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        bulk_battery_unit_events_request: BulkBatteryUnitEventsRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this batch with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally and readings may then be recorded twice.")] = None,
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
        """Append a batch of telemetry events to a battery unit

        Appends up to **500** append-only dynamic-data records to one unit in a single request — the batch venue for backfilling a fleet's telemetry. **Telemetry only:** a record carrying `status` is refused per-item; a status transition is a lifecycle decision and goes through `POST /api/v1/units/{id}/events` one event at a time.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403). A terminal (`RECYCLED`) unit refuses the whole batch (400 `Terminal Unit Status`).  **Per-record validation (collected as `[index]`-prefixed strings in `errors`, not a rejection of the whole batch):** the same field checks as the single-event endpoint — `eventType` required and valid, numeric ranges, Date-parseable `recordedAt` (defaults to server time when omitted) — plus the physics consistency checks, judged in chronological order across the batch and the unit's recorded history, so a reading that only conflicts with another batch member is caught too.  **Partial success:** the response is **201 when at least one record was accepted**; skipped items are listed in `errors`. If *every* item failed you get **400** `Bulk Event Ingest Failed` with the same string array.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param bulk_battery_unit_events_request: (required)
        :type bulk_battery_unit_events_request: BulkBatteryUnitEventsRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this batch with the same `Idempotency-Key` replays the ORIGINAL result — same status and body, plus an `idempotent-replayed: true` response header — instead of re-inserting the batch. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally and readings may then be recorded twice.
        :type idempotency_key: str
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

        _param = self._bulk_record_battery_unit_events_serialize(
            id=id,
            bulk_battery_unit_events_request=bulk_battery_unit_events_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "BulkBatteryUnitEventsResponse",
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


    def _bulk_record_battery_unit_events_serialize(
        self,
        id,
        bulk_battery_unit_events_request,
        idempotency_key,
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
        if idempotency_key is not None:
            _header_params['Idempotency-Key'] = idempotency_key
        # process the form parameters
        # process the body parameter
        if bulk_battery_unit_events_request is not None:
            _body_params = bulk_battery_unit_events_request


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
            resource_path='/api/v1/units/{id}/events/bulk',
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
    def delete_battery_unit(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
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
    ) -> None:
        """Not deletable: a serialised unit is a marketed physical item (always 409)

        **Always refused with 409.** A serialised unit is an item-level battery passport for a physical battery placed on the market, so the record — including its append-only telemetry — is retained (EU Battery Regulation persistence, mirroring the passport-level archive model). End a unit's life through the lifecycle instead: append a `STATUS_CHANGE` event via `POST /api/v1/units/{id}/events` — `RECYCLED` ceases it (public 410 tombstone), `DECOMMISSIONED` retires it. Hard removal is reserved for the node's retention-gated purge, never an ad-hoc API delete.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only address units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
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

        _param = self._delete_battery_unit_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
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
    def delete_battery_unit_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
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
    ) -> ApiResponse[None]:
        """Not deletable: a serialised unit is a marketed physical item (always 409)

        **Always refused with 409.** A serialised unit is an item-level battery passport for a physical battery placed on the market, so the record — including its append-only telemetry — is retained (EU Battery Regulation persistence, mirroring the passport-level archive model). End a unit's life through the lifecycle instead: append a `STATUS_CHANGE` event via `POST /api/v1/units/{id}/events` — `RECYCLED` ceases it (public 410 tombstone), `DECOMMISSIONED` retires it. Hard removal is reserved for the node's retention-gated purge, never an ad-hoc API delete.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only address units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
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

        _param = self._delete_battery_unit_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
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
    def delete_battery_unit_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
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
        """Not deletable: a serialised unit is a marketed physical item (always 409)

        **Always refused with 409.** A serialised unit is an item-level battery passport for a physical battery placed on the market, so the record — including its append-only telemetry — is retained (EU Battery Regulation persistence, mirroring the passport-level archive model). End a unit's life through the lifecycle instead: append a `STATUS_CHANGE` event via `POST /api/v1/units/{id}/events` — `RECYCLED` ceases it (public 410 tombstone), `DECOMMISSIONED` retires it. Hard removal is reserved for the node's retention-gated purge, never an ad-hoc API delete.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only address units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
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

        _param = self._delete_battery_unit_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
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


    def _delete_battery_unit_serialize(
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
            resource_path='/api/v1/units/{id}',
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
    def get_battery_unit(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
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
    ) -> BatteryUnitJsonLd:
        """Get one battery unit as JSON-LD with its dynamic-data history

        Returns the unit as a **JSON-LD document** (`Content-Type: application/ld+json`) in the **privileged tenant view**: `currentState` (the latest telemetry snapshot) and `dynamicData` (the **500 most recent** events, newest first by `recordedAt`) are included; the public `restrictedData` marker is absent. The embedded `ofModel` is the SKU/type passport document rendered in the **owner (unredacted) variant** — legitimate-interest-tier metadata and owner-only keys are NOT masked, unlike the anonymous public document.  **Caveat:** this authenticated endpoint does **not** load lineage relations, so `repurposedFrom` is always `null` and `successorUnits` is always `[]` here even when lineage exists; the public resolver view (`GET /unit/{id}`) does resolve them.  **Permission:** `battery:read`. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
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

        _param = self._get_battery_unit_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitJsonLd",
            '401': "Error",
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
    def get_battery_unit_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
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
    ) -> ApiResponse[BatteryUnitJsonLd]:
        """Get one battery unit as JSON-LD with its dynamic-data history

        Returns the unit as a **JSON-LD document** (`Content-Type: application/ld+json`) in the **privileged tenant view**: `currentState` (the latest telemetry snapshot) and `dynamicData` (the **500 most recent** events, newest first by `recordedAt`) are included; the public `restrictedData` marker is absent. The embedded `ofModel` is the SKU/type passport document rendered in the **owner (unredacted) variant** — legitimate-interest-tier metadata and owner-only keys are NOT masked, unlike the anonymous public document.  **Caveat:** this authenticated endpoint does **not** load lineage relations, so `repurposedFrom` is always `null` and `successorUnits` is always `[]` here even when lineage exists; the public resolver view (`GET /unit/{id}`) does resolve them.  **Permission:** `battery:read`. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
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

        _param = self._get_battery_unit_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitJsonLd",
            '401': "Error",
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
    def get_battery_unit_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
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
        """Get one battery unit as JSON-LD with its dynamic-data history

        Returns the unit as a **JSON-LD document** (`Content-Type: application/ld+json`) in the **privileged tenant view**: `currentState` (the latest telemetry snapshot) and `dynamicData` (the **500 most recent** events, newest first by `recordedAt`) are included; the public `restrictedData` marker is absent. The embedded `ofModel` is the SKU/type passport document rendered in the **owner (unredacted) variant** — legitimate-interest-tier metadata and owner-only keys are NOT masked, unlike the anonymous public document.  **Caveat:** this authenticated endpoint does **not** load lineage relations, so `repurposedFrom` is always `null` and `successorUnits` is always `[]` here even when lineage exists; the public resolver view (`GET /unit/{id}`) does resolve them.  **Permission:** `battery:read`. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
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

        _param = self._get_battery_unit_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitJsonLd",
            '401': "Error",
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


    def _get_battery_unit_serialize(
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
            resource_path='/api/v1/units/{id}',
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
    def list_battery_unit_events(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        limit: Annotated[Optional[Annotated[int, Field(le=500, strict=True, ge=1)]], Field(description="Page size (1–500). Out-of-range or non-integer values return **400**.")] = None,
        cursor: Annotated[Optional[StrictStr], Field(description="Opaque page cursor — the `nextCursor` value from the previous page. Omit for the first (newest) page; a malformed value returns **400**.")] = None,
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
    ) -> BatteryUnitEventListResponse:
        """List a battery unit's telemetry history (newest first, cursor-paginated)

        Returns one page of the unit's append-only dynamic-data history ordered by `recordedAt` DESC, ties broken by `id` DESC — so paging is deterministic. A page holds at most 500 events (`limit`, default 500); while older history remains the response carries a non-null `nextCursor` — pass it back as `cursor` to fetch the next (older) page, so the **full history is retrievable** however long it grows. The cursor is opaque; a malformed value returns **400**.  **Permission:** `battery:read`. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403). Events are returned as stored.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param limit: Page size (1–500). Out-of-range or non-integer values return **400**.
        :type limit: int
        :param cursor: Opaque page cursor — the `nextCursor` value from the previous page. Omit for the first (newest) page; a malformed value returns **400**.
        :type cursor: str
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

        _param = self._list_battery_unit_events_serialize(
            id=id,
            limit=limit,
            cursor=cursor,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitEventListResponse",
            '400': "Error",
            '401': "Error",
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
    def list_battery_unit_events_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        limit: Annotated[Optional[Annotated[int, Field(le=500, strict=True, ge=1)]], Field(description="Page size (1–500). Out-of-range or non-integer values return **400**.")] = None,
        cursor: Annotated[Optional[StrictStr], Field(description="Opaque page cursor — the `nextCursor` value from the previous page. Omit for the first (newest) page; a malformed value returns **400**.")] = None,
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
    ) -> ApiResponse[BatteryUnitEventListResponse]:
        """List a battery unit's telemetry history (newest first, cursor-paginated)

        Returns one page of the unit's append-only dynamic-data history ordered by `recordedAt` DESC, ties broken by `id` DESC — so paging is deterministic. A page holds at most 500 events (`limit`, default 500); while older history remains the response carries a non-null `nextCursor` — pass it back as `cursor` to fetch the next (older) page, so the **full history is retrievable** however long it grows. The cursor is opaque; a malformed value returns **400**.  **Permission:** `battery:read`. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403). Events are returned as stored.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param limit: Page size (1–500). Out-of-range or non-integer values return **400**.
        :type limit: int
        :param cursor: Opaque page cursor — the `nextCursor` value from the previous page. Omit for the first (newest) page; a malformed value returns **400**.
        :type cursor: str
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

        _param = self._list_battery_unit_events_serialize(
            id=id,
            limit=limit,
            cursor=cursor,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitEventListResponse",
            '400': "Error",
            '401': "Error",
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
    def list_battery_unit_events_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        limit: Annotated[Optional[Annotated[int, Field(le=500, strict=True, ge=1)]], Field(description="Page size (1–500). Out-of-range or non-integer values return **400**.")] = None,
        cursor: Annotated[Optional[StrictStr], Field(description="Opaque page cursor — the `nextCursor` value from the previous page. Omit for the first (newest) page; a malformed value returns **400**.")] = None,
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
        """List a battery unit's telemetry history (newest first, cursor-paginated)

        Returns one page of the unit's append-only dynamic-data history ordered by `recordedAt` DESC, ties broken by `id` DESC — so paging is deterministic. A page holds at most 500 events (`limit`, default 500); while older history remains the response carries a non-null `nextCursor` — pass it back as `cursor` to fetch the next (older) page, so the **full history is retrievable** however long it grows. The cursor is opaque; a malformed value returns **400**.  **Permission:** `battery:read`. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403). Events are returned as stored.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param limit: Page size (1–500). Out-of-range or non-integer values return **400**.
        :type limit: int
        :param cursor: Opaque page cursor — the `nextCursor` value from the previous page. Omit for the first (newest) page; a malformed value returns **400**.
        :type cursor: str
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

        _param = self._list_battery_unit_events_serialize(
            id=id,
            limit=limit,
            cursor=cursor,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitEventListResponse",
            '400': "Error",
            '401': "Error",
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


    def _list_battery_unit_events_serialize(
        self,
        id,
        limit,
        cursor,
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
        if limit is not None:
            
            _query_params.append(('limit', limit))
            
        if cursor is not None:
            
            _query_params.append(('cursor', cursor))
            
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
            resource_path='/api/v1/units/{id}/events',
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
    def list_battery_units(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant.")],
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only; non-numeric falls back to 1).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=200, strict=True, ge=1)]], Field(description="Page size. Clamped to 1–200; non-numeric falls back to the default 100.")] = None,
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
    ) -> BatteryUnitListResponse:
        """List serialised battery units under a passport

        Lists **all** serialised units of the passport, newest first (`createdAt` DESC). **Paginated** with `?page` (default 1) and `?limit` (default 100, max 200) — a SKU may carry many physical units; `count` is this page's size, `total`/`totalPages` describe the full set.  **Permission:** `battery:read`. Operator-scoped credentials may only read passports of their own Economic Operator (403). Units are returned as stored.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant. (required)
        :type passport_id: str
        :param page: 1-based page number (digits only; non-numeric falls back to 1).
        :type page: int
        :param limit: Page size. Clamped to 1–200; non-numeric falls back to the default 100.
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

        _param = self._list_battery_units_serialize(
            passport_id=passport_id,
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitListResponse",
            '401': "Error",
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
    def list_battery_units_with_http_info(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant.")],
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only; non-numeric falls back to 1).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=200, strict=True, ge=1)]], Field(description="Page size. Clamped to 1–200; non-numeric falls back to the default 100.")] = None,
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
    ) -> ApiResponse[BatteryUnitListResponse]:
        """List serialised battery units under a passport

        Lists **all** serialised units of the passport, newest first (`createdAt` DESC). **Paginated** with `?page` (default 1) and `?limit` (default 100, max 200) — a SKU may carry many physical units; `count` is this page's size, `total`/`totalPages` describe the full set.  **Permission:** `battery:read`. Operator-scoped credentials may only read passports of their own Economic Operator (403). Units are returned as stored.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant. (required)
        :type passport_id: str
        :param page: 1-based page number (digits only; non-numeric falls back to 1).
        :type page: int
        :param limit: Page size. Clamped to 1–200; non-numeric falls back to the default 100.
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

        _param = self._list_battery_units_serialize(
            passport_id=passport_id,
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitListResponse",
            '401': "Error",
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
    def list_battery_units_without_preload_content(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant.")],
        page: Annotated[Optional[Annotated[int, Field(strict=True, ge=1)]], Field(description="1-based page number (digits only; non-numeric falls back to 1).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=200, strict=True, ge=1)]], Field(description="Page size. Clamped to 1–200; non-numeric falls back to the default 100.")] = None,
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
        """List serialised battery units under a passport

        Lists **all** serialised units of the passport, newest first (`createdAt` DESC). **Paginated** with `?page` (default 1) and `?limit` (default 100, max 200) — a SKU may carry many physical units; `count` is this page's size, `total`/`totalPages` describe the full set.  **Permission:** `battery:read`. Operator-scoped credentials may only read passports of their own Economic Operator (403). Units are returned as stored.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant. (required)
        :type passport_id: str
        :param page: 1-based page number (digits only; non-numeric falls back to 1).
        :type page: int
        :param limit: Page size. Clamped to 1–200; non-numeric falls back to the default 100.
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

        _param = self._list_battery_units_serialize(
            passport_id=passport_id,
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "BatteryUnitListResponse",
            '401': "Error",
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


    def _list_battery_units_serialize(
        self,
        passport_id,
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
        if passport_id is not None:
            _path_params['passportId'] = passport_id
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
            resource_path='/api/v1/passports/{passportId}/units',
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
    def record_battery_unit_event(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        record_battery_unit_event_request: RecordBatteryUnitEventRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of appending a duplicate reading. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry appends normally — the reading may then be recorded twice.")] = None,
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
    ) -> RecordBatteryUnitEventResponse:
        """Append an immutable telemetry event to a battery unit

        Appends one **append-only** per-unit dynamic-data record (Annex XIII / Art. 77: SoH, cycle count, remaining capacity, temperature, negative events). History is immutable — there is **no update or delete path** for events.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Validation (400 with the standard error triple):** `eventType` is required and must be one of `SOH_MEASUREMENT|CHARGE_CYCLE|STATUS_CHANGE|NEGATIVE_EVENT|OTHER`; `stateOfHealth` 0–100; `cycleCount` and `remainingCapacityAh` 0–9007199254740991; `temperatureC` −273.15–10000 (each may also be `null`/omitted); `status`, if present, must be a valid unit status; `recordedAt` must be Date-parseable (defaults to server time when omitted). `cycleCount` is truncated to an integer before persisting; a `payload` that is not an object or array is silently dropped (stored as `null`) — JSON **arrays** pass the server's `typeof` check and are persisted verbatim.  **Status transition:** when `status` is present and differs from the unit's current status, the unit is updated **in the same transaction** as the event — this works with *any* `eventType`, though `STATUS_CHANGE` is the conventional carrier. Transitioning to **`RECYCLED`** additionally stamps `ceasedAt` (if not already set; never cleared), after which the public unit view becomes a 410 tombstone and the unit can no longer gain successor units. `RECYCLED` is terminal: once the unit's status is `RECYCLED` this endpoint refuses **every** further event with **400** `Terminal Unit Status` — neither `status` nor telemetry can change again; serialise a successor unit via `predecessorUnitId` instead.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param record_battery_unit_event_request: (required)
        :type record_battery_unit_event_request: RecordBatteryUnitEventRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of appending a duplicate reading. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry appends normally — the reading may then be recorded twice.
        :type idempotency_key: str
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

        _param = self._record_battery_unit_event_serialize(
            id=id,
            record_battery_unit_event_request=record_battery_unit_event_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "RecordBatteryUnitEventResponse",
            '400': "RecordBatteryUnitEvent400Response",
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
    def record_battery_unit_event_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        record_battery_unit_event_request: RecordBatteryUnitEventRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of appending a duplicate reading. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry appends normally — the reading may then be recorded twice.")] = None,
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
    ) -> ApiResponse[RecordBatteryUnitEventResponse]:
        """Append an immutable telemetry event to a battery unit

        Appends one **append-only** per-unit dynamic-data record (Annex XIII / Art. 77: SoH, cycle count, remaining capacity, temperature, negative events). History is immutable — there is **no update or delete path** for events.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Validation (400 with the standard error triple):** `eventType` is required and must be one of `SOH_MEASUREMENT|CHARGE_CYCLE|STATUS_CHANGE|NEGATIVE_EVENT|OTHER`; `stateOfHealth` 0–100; `cycleCount` and `remainingCapacityAh` 0–9007199254740991; `temperatureC` −273.15–10000 (each may also be `null`/omitted); `status`, if present, must be a valid unit status; `recordedAt` must be Date-parseable (defaults to server time when omitted). `cycleCount` is truncated to an integer before persisting; a `payload` that is not an object or array is silently dropped (stored as `null`) — JSON **arrays** pass the server's `typeof` check and are persisted verbatim.  **Status transition:** when `status` is present and differs from the unit's current status, the unit is updated **in the same transaction** as the event — this works with *any* `eventType`, though `STATUS_CHANGE` is the conventional carrier. Transitioning to **`RECYCLED`** additionally stamps `ceasedAt` (if not already set; never cleared), after which the public unit view becomes a 410 tombstone and the unit can no longer gain successor units. `RECYCLED` is terminal: once the unit's status is `RECYCLED` this endpoint refuses **every** further event with **400** `Terminal Unit Status` — neither `status` nor telemetry can change again; serialise a successor unit via `predecessorUnitId` instead.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param record_battery_unit_event_request: (required)
        :type record_battery_unit_event_request: RecordBatteryUnitEventRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of appending a duplicate reading. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry appends normally — the reading may then be recorded twice.
        :type idempotency_key: str
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

        _param = self._record_battery_unit_event_serialize(
            id=id,
            record_battery_unit_event_request=record_battery_unit_event_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "RecordBatteryUnitEventResponse",
            '400': "RecordBatteryUnitEvent400Response",
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
    def record_battery_unit_event_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Battery unit UUID (tenant-scoped).")],
        record_battery_unit_event_request: RecordBatteryUnitEventRequest,
        idempotency_key: Annotated[Optional[Annotated[str, Field(strict=True, max_length=255)]], Field(description="Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of appending a duplicate reading. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry appends normally — the reading may then be recorded twice.")] = None,
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
        """Append an immutable telemetry event to a battery unit

        Appends one **append-only** per-unit dynamic-data record (Annex XIII / Art. 77: SoH, cycle count, remaining capacity, temperature, negative events). History is immutable — there is **no update or delete path** for events.  **Permission:** `battery:write`. Cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Validation (400 with the standard error triple):** `eventType` is required and must be one of `SOH_MEASUREMENT|CHARGE_CYCLE|STATUS_CHANGE|NEGATIVE_EVENT|OTHER`; `stateOfHealth` 0–100; `cycleCount` and `remainingCapacityAh` 0–9007199254740991; `temperatureC` −273.15–10000 (each may also be `null`/omitted); `status`, if present, must be a valid unit status; `recordedAt` must be Date-parseable (defaults to server time when omitted). `cycleCount` is truncated to an integer before persisting; a `payload` that is not an object or array is silently dropped (stored as `null`) — JSON **arrays** pass the server's `typeof` check and are persisted verbatim.  **Status transition:** when `status` is present and differs from the unit's current status, the unit is updated **in the same transaction** as the event — this works with *any* `eventType`, though `STATUS_CHANGE` is the conventional carrier. Transitioning to **`RECYCLED`** additionally stamps `ceasedAt` (if not already set; never cleared), after which the public unit view becomes a 410 tombstone and the unit can no longer gain successor units. `RECYCLED` is terminal: once the unit's status is `RECYCLED` this endpoint refuses **every** further event with **400** `Terminal Unit Status` — neither `status` nor telemetry can change again; serialise a successor unit via `predecessorUnitId` instead.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Battery unit UUID (tenant-scoped). (required)
        :type id: str
        :param record_battery_unit_event_request: (required)
        :type record_battery_unit_event_request: RecordBatteryUnitEventRequest
        :param idempotency_key: Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same `Idempotency-Key` replays the ORIGINAL response — same status and body, plus an `idempotent-replayed: true` response header — instead of appending a duplicate reading. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry appends normally — the reading may then be recorded twice.
        :type idempotency_key: str
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

        _param = self._record_battery_unit_event_serialize(
            id=id,
            record_battery_unit_event_request=record_battery_unit_event_request,
            idempotency_key=idempotency_key,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "RecordBatteryUnitEventResponse",
            '400': "RecordBatteryUnitEvent400Response",
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


    def _record_battery_unit_event_serialize(
        self,
        id,
        record_battery_unit_event_request,
        idempotency_key,
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
        if idempotency_key is not None:
            _header_params['Idempotency-Key'] = idempotency_key
        # process the form parameters
        # process the body parameter
        if record_battery_unit_event_request is not None:
            _body_params = record_battery_unit_event_request


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
            resource_path='/api/v1/units/{id}/events',
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
    def serialize_battery_units(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant.")],
        serialize_battery_units_request: SerializeBatteryUnitsRequest,
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
    ) -> SerializeBatteryUnitsResponse:
        """Serialise individual battery units under a passport (bulk, up to 200)

        Creates one or many **individual physical battery units** (EU Battery Regulation) under a SKU/type-level passport. Send either a single unit object or `{\"units\": [...]}` with **at most 200 items** (if `units` is present and an array it is used; otherwise the whole body is treated as one unit).  **Permission:** `battery:write`. Bearer API key (`op_dpp_token_…`) or session JWT; cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only serialise under passports of their own Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Per-item validation (collected as plain-string errors, not a rejection of the whole batch):** `serialNumber` is trimmed then must match `^[A-Za-z0-9._-]{1,20}$` (a URL-safe subset of GS1 AI-21 CSET 82, ≤ 20 chars) AND is validated to full AI-21 conformance by GS1's authoritative engine — a GTIN-keyed unit through its full Digital Link, a non-GTIN unit through its AI-21 serial value; `status` must be a valid unit status; `manufacturedAt` must be Date-parseable; duplicate `(passport, serialNumber)` pairs are skipped with *\"A unit with this serial already exists for this passport\"*. Each created unit gets a per-unit GS1 Digital Link URI `/{01|8003}/{productId}/21/{serialNumber}` carrying the **real physical serial** in AI-21.  **Predecessor linkage (repurpose/remanufacture):** `predecessorUnitId` must reference an existing unit **in your tenant** (any passport). A recycled predecessor (`ceasedAt` set) is refused — its passport has ceased to exist. (A unit *created* with status `RECYCLED` is ceased from birth — `ceasedAt` is stamped at creation — and is refused as a predecessor exactly like one recycled via the events route.) In one transaction the new unit is created, an append-only `STATUS_CHANGE` event (`{status, successorUnitId, successorSerial}` payload) is written to the predecessor, and the predecessor's status is set to `predecessorStatus` (default `REPURPOSED`; only `REPURPOSED|REMANUFACTURED|REUSED` allowed).  **Partial success:** the response is **201 when at least one unit was created**; skipped items are listed in `errors`. If *every* item failed you get **400 `Serialisation Failed`** with the same string array. A `batteryunit.created` audit event and a tenant notification are emitted on success.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant. (required)
        :type passport_id: str
        :param serialize_battery_units_request: (required)
        :type serialize_battery_units_request: SerializeBatteryUnitsRequest
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

        _param = self._serialize_battery_units_serialize(
            passport_id=passport_id,
            serialize_battery_units_request=serialize_battery_units_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "SerializeBatteryUnitsResponse",
            '400': "SerializeBatteryUnits400Response",
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
    def serialize_battery_units_with_http_info(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant.")],
        serialize_battery_units_request: SerializeBatteryUnitsRequest,
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
    ) -> ApiResponse[SerializeBatteryUnitsResponse]:
        """Serialise individual battery units under a passport (bulk, up to 200)

        Creates one or many **individual physical battery units** (EU Battery Regulation) under a SKU/type-level passport. Send either a single unit object or `{\"units\": [...]}` with **at most 200 items** (if `units` is present and an array it is used; otherwise the whole body is treated as one unit).  **Permission:** `battery:write`. Bearer API key (`op_dpp_token_…`) or session JWT; cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only serialise under passports of their own Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Per-item validation (collected as plain-string errors, not a rejection of the whole batch):** `serialNumber` is trimmed then must match `^[A-Za-z0-9._-]{1,20}$` (a URL-safe subset of GS1 AI-21 CSET 82, ≤ 20 chars) AND is validated to full AI-21 conformance by GS1's authoritative engine — a GTIN-keyed unit through its full Digital Link, a non-GTIN unit through its AI-21 serial value; `status` must be a valid unit status; `manufacturedAt` must be Date-parseable; duplicate `(passport, serialNumber)` pairs are skipped with *\"A unit with this serial already exists for this passport\"*. Each created unit gets a per-unit GS1 Digital Link URI `/{01|8003}/{productId}/21/{serialNumber}` carrying the **real physical serial** in AI-21.  **Predecessor linkage (repurpose/remanufacture):** `predecessorUnitId` must reference an existing unit **in your tenant** (any passport). A recycled predecessor (`ceasedAt` set) is refused — its passport has ceased to exist. (A unit *created* with status `RECYCLED` is ceased from birth — `ceasedAt` is stamped at creation — and is refused as a predecessor exactly like one recycled via the events route.) In one transaction the new unit is created, an append-only `STATUS_CHANGE` event (`{status, successorUnitId, successorSerial}` payload) is written to the predecessor, and the predecessor's status is set to `predecessorStatus` (default `REPURPOSED`; only `REPURPOSED|REMANUFACTURED|REUSED` allowed).  **Partial success:** the response is **201 when at least one unit was created**; skipped items are listed in `errors`. If *every* item failed you get **400 `Serialisation Failed`** with the same string array. A `batteryunit.created` audit event and a tenant notification are emitted on success.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant. (required)
        :type passport_id: str
        :param serialize_battery_units_request: (required)
        :type serialize_battery_units_request: SerializeBatteryUnitsRequest
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

        _param = self._serialize_battery_units_serialize(
            passport_id=passport_id,
            serialize_battery_units_request=serialize_battery_units_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "SerializeBatteryUnitsResponse",
            '400': "SerializeBatteryUnits400Response",
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
    def serialize_battery_units_without_preload_content(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant.")],
        serialize_battery_units_request: SerializeBatteryUnitsRequest,
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
        """Serialise individual battery units under a passport (bulk, up to 200)

        Creates one or many **individual physical battery units** (EU Battery Regulation) under a SKU/type-level passport. Send either a single unit object or `{\"units\": [...]}` with **at most 200 items** (if `units` is present and an array it is used; otherwise the whole body is treated as one unit).  **Permission:** `battery:write`. Bearer API key (`op_dpp_token_…`) or session JWT; cookie-session clients must send `X-CSRF-Token`. Operator-scoped credentials may only serialise under passports of their own Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Per-item validation (collected as plain-string errors, not a rejection of the whole batch):** `serialNumber` is trimmed then must match `^[A-Za-z0-9._-]{1,20}$` (a URL-safe subset of GS1 AI-21 CSET 82, ≤ 20 chars) AND is validated to full AI-21 conformance by GS1's authoritative engine — a GTIN-keyed unit through its full Digital Link, a non-GTIN unit through its AI-21 serial value; `status` must be a valid unit status; `manufacturedAt` must be Date-parseable; duplicate `(passport, serialNumber)` pairs are skipped with *\"A unit with this serial already exists for this passport\"*. Each created unit gets a per-unit GS1 Digital Link URI `/{01|8003}/{productId}/21/{serialNumber}` carrying the **real physical serial** in AI-21.  **Predecessor linkage (repurpose/remanufacture):** `predecessorUnitId` must reference an existing unit **in your tenant** (any passport). A recycled predecessor (`ceasedAt` set) is refused — its passport has ceased to exist. (A unit *created* with status `RECYCLED` is ceased from birth — `ceasedAt` is stamped at creation — and is refused as a predecessor exactly like one recycled via the events route.) In one transaction the new unit is created, an append-only `STATUS_CHANGE` event (`{status, successorUnitId, successorSerial}` payload) is written to the predecessor, and the predecessor's status is set to `predecessorStatus` (default `REPURPOSED`; only `REPURPOSED|REMANUFACTURED|REUSED` allowed).  **Partial success:** the response is **201 when at least one unit was created**; skipped items are listed in `errors`. If *every* item failed you get **400 `Serialisation Failed`** with the same string array. A `batteryunit.created` audit event and a tenant notification are emitted on success.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied `productId` (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then `productId` — both scoped to your tenant. (required)
        :type passport_id: str
        :param serialize_battery_units_request: (required)
        :type serialize_battery_units_request: SerializeBatteryUnitsRequest
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

        _param = self._serialize_battery_units_serialize(
            passport_id=passport_id,
            serialize_battery_units_request=serialize_battery_units_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "SerializeBatteryUnitsResponse",
            '400': "SerializeBatteryUnits400Response",
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


    def _serialize_battery_units_serialize(
        self,
        passport_id,
        serialize_battery_units_request,
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
        if passport_id is not None:
            _path_params['passportId'] = passport_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if serialize_battery_units_request is not None:
            _body_params = serialize_battery_units_request


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
            resource_path='/api/v1/passports/{passportId}/units',
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
    def validate_battery_units(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed by its UUID or caller-supplied `productId` (GTIN-14 / GRAI / SKU), scoped to your tenant.")],
        serialize_battery_units_request: SerializeBatteryUnitsRequest,
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
    ) -> ValidateBatteryUnits200Response:
        """Pre-flight: validate battery-unit identifiers without persisting

        NON-MUTATING pre-flight for bulk unit import. Runs the SAME engine-backed AI-21 / GS1 Digital Link conformance + field checks as `POST /api/v1/passports/{passportId}/units` and returns a per-item verdict — **persisting nothing**. Send a single unit or `{\"units\": [...]}` (≤200). Lets a bulk importer ask \"would these serials be GS1-conformant?\" before committing a batch.  **Permission:** `battery:write` (gated as the write permission, like other validate-only checks; subscription gating → 402). **Validation:** `serialNumber` charset/length (`^[A-Za-z0-9._-]{1,20}$`, a URL-safe subset of GS1 AI-21 CSET 82) PLUS authoritative GS1-engine conformance for EVERY unit — a GTIN-keyed passport's unit Digital Link must parse cleanly through the engine, and a non-GTIN passport's AI-21 serial VALUE is validated through the same engine (CSET-82 charset + length); `status` must be a valid unit status; `manufacturedAt` must be Date-parseable. Predecessor linkage is NOT checked here (a persistence-time concern). The verdict order matches the input order.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed by its UUID or caller-supplied `productId` (GTIN-14 / GRAI / SKU), scoped to your tenant. (required)
        :type passport_id: str
        :param serialize_battery_units_request: (required)
        :type serialize_battery_units_request: SerializeBatteryUnitsRequest
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

        _param = self._validate_battery_units_serialize(
            passport_id=passport_id,
            serialize_battery_units_request=serialize_battery_units_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ValidateBatteryUnits200Response",
            '400': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
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
    def validate_battery_units_with_http_info(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed by its UUID or caller-supplied `productId` (GTIN-14 / GRAI / SKU), scoped to your tenant.")],
        serialize_battery_units_request: SerializeBatteryUnitsRequest,
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
    ) -> ApiResponse[ValidateBatteryUnits200Response]:
        """Pre-flight: validate battery-unit identifiers without persisting

        NON-MUTATING pre-flight for bulk unit import. Runs the SAME engine-backed AI-21 / GS1 Digital Link conformance + field checks as `POST /api/v1/passports/{passportId}/units` and returns a per-item verdict — **persisting nothing**. Send a single unit or `{\"units\": [...]}` (≤200). Lets a bulk importer ask \"would these serials be GS1-conformant?\" before committing a batch.  **Permission:** `battery:write` (gated as the write permission, like other validate-only checks; subscription gating → 402). **Validation:** `serialNumber` charset/length (`^[A-Za-z0-9._-]{1,20}$`, a URL-safe subset of GS1 AI-21 CSET 82) PLUS authoritative GS1-engine conformance for EVERY unit — a GTIN-keyed passport's unit Digital Link must parse cleanly through the engine, and a non-GTIN passport's AI-21 serial VALUE is validated through the same engine (CSET-82 charset + length); `status` must be a valid unit status; `manufacturedAt` must be Date-parseable. Predecessor linkage is NOT checked here (a persistence-time concern). The verdict order matches the input order.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed by its UUID or caller-supplied `productId` (GTIN-14 / GRAI / SKU), scoped to your tenant. (required)
        :type passport_id: str
        :param serialize_battery_units_request: (required)
        :type serialize_battery_units_request: SerializeBatteryUnitsRequest
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

        _param = self._validate_battery_units_serialize(
            passport_id=passport_id,
            serialize_battery_units_request=serialize_battery_units_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ValidateBatteryUnits200Response",
            '400': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
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
    def validate_battery_units_without_preload_content(
        self,
        passport_id: Annotated[StrictStr, Field(description="The SKU/type-level passport, addressed by its UUID or caller-supplied `productId` (GTIN-14 / GRAI / SKU), scoped to your tenant.")],
        serialize_battery_units_request: SerializeBatteryUnitsRequest,
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
        """Pre-flight: validate battery-unit identifiers without persisting

        NON-MUTATING pre-flight for bulk unit import. Runs the SAME engine-backed AI-21 / GS1 Digital Link conformance + field checks as `POST /api/v1/passports/{passportId}/units` and returns a per-item verdict — **persisting nothing**. Send a single unit or `{\"units\": [...]}` (≤200). Lets a bulk importer ask \"would these serials be GS1-conformant?\" before committing a batch.  **Permission:** `battery:write` (gated as the write permission, like other validate-only checks; subscription gating → 402). **Validation:** `serialNumber` charset/length (`^[A-Za-z0-9._-]{1,20}$`, a URL-safe subset of GS1 AI-21 CSET 82) PLUS authoritative GS1-engine conformance for EVERY unit — a GTIN-keyed passport's unit Digital Link must parse cleanly through the engine, and a non-GTIN passport's AI-21 serial VALUE is validated through the same engine (CSET-82 charset + length); `status` must be a valid unit status; `manufacturedAt` must be Date-parseable. Predecessor linkage is NOT checked here (a persistence-time concern). The verdict order matches the input order.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param passport_id: The SKU/type-level passport, addressed by its UUID or caller-supplied `productId` (GTIN-14 / GRAI / SKU), scoped to your tenant. (required)
        :type passport_id: str
        :param serialize_battery_units_request: (required)
        :type serialize_battery_units_request: SerializeBatteryUnitsRequest
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

        _param = self._validate_battery_units_serialize(
            passport_id=passport_id,
            serialize_battery_units_request=serialize_battery_units_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "ValidateBatteryUnits200Response",
            '400': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _validate_battery_units_serialize(
        self,
        passport_id,
        serialize_battery_units_request,
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
        if passport_id is not None:
            _path_params['passportId'] = passport_id
        # process the query parameters
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if serialize_battery_units_request is not None:
            _body_params = serialize_battery_units_request


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
            resource_path='/api/v1/passports/{passportId}/units/validate',
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


