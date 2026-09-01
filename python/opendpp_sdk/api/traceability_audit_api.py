# coding: utf-8

"""
    OpenDPP Integration API

    OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.

    The version of the OpenAPI document: 1.15.0
    Contact: support@opendpp-node.eu
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501

import warnings
from pydantic import validate_call, Field, StrictFloat, StrictStr, StrictInt
from typing import Any, Dict, List, Optional, Tuple, Union
from typing_extensions import Annotated

from pydantic import Field, StrictStr
from typing_extensions import Annotated
from opendpp_sdk.models.epcis_capture_response import EpcisCaptureResponse
from opendpp_sdk.models.epcis_document import EpcisDocument
from opendpp_sdk.models.seal_verify_request import SealVerifyRequest
from opendpp_sdk.models.seal_verify_response import SealVerifyResponse
from opendpp_sdk.models.trace_event_registered import TraceEventRegistered
from opendpp_sdk.models.trace_lineage_response import TraceLineageResponse
from opendpp_sdk.models.untp_event_credential import UntpEventCredential

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class TraceabilityAuditApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def capture_epcis_document(
        self,
        epcis_document: EpcisDocument,
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
    ) -> EpcisCaptureResponse:
        """Capture a native GS1 EPCIS 2.0 document (JSON/JSON-LD)

        Captures a native **GS1 EPCIS 2.0 document** — the standard's own JSON/JSON-LD interchange format — and persists each supported event as an EPCIS event row scoped to your tenant, alongside the VC-shaped `POST /api/v1/events` path. Send the document exactly as your EPCIS infrastructure produces it.  **Permission:** `passport:update` (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy `REQUIRED`, or `DEFAULT` with the workspace's MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the `X-CSRF-Token` header (double-submit with the `opendpp_csrf` cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Validation:** the WHOLE document is validated against the official GS1 EPCIS **2.0.1** JSON Schema (vendored and pinned on the node) before any event is stored — a non-conformant document is rejected 400 with the first few schema violations under `errors[]`. Notable rules the OFFICIAL schema enforces: `@context` and `creationDate` are required; `bizStep`/`disposition` must use the CBV **short names** (e.g. `commissioning`, `in_transit`) or a custom (non-CBV) URI — the legacy `urn:epcglobal:cbv:*` URN form is REJECTED by the standard's schema; `action` is forbidden on `TransformationEvent`; `readPoint`/`bizLocation` carry `{id: <uri>}`. Only `type: \"EPCISDocument\"` is accepted (no `EPCISQueryDocument`, no bare events), and `epcisBody.eventList` must be non-empty.  **Per-event capture (partial success):** events are processed independently and the 201 response reports `results[]` (captured) and `errors[]` (rejected) by `index`. An event is rejected — never silently dropped — when its type is outside this node's traceability model (`ObjectEvent`, `AggregationEvent`, `TransformationEvent`, `AssociationEvent` are supported; `TransactionEvent` is not) or when it identifies stock ONLY by quantity lists (no `epcList`/`parentID`/`childEPCs`/`inputEPCList`/`outputEPCList` — nothing EPC-identified would remain to trace). If EVERY event is rejected the response is 400 `No Events Captured` with the same `errors[]`.  **Fidelity disclosure:** recognized EPCIS fields the node does not persist (`eventID`, quantity lists, `sensorElementList`, `bizTransactionList`, `sourceList`/`destinationList`, `persistentDisposition`, `errorDeclaration`, `ilmd`, custom extension fields, …) are listed per event under `results[].ignoredFields` instead of being silently discarded.  **Persistence:** row ids are ALWAYS server-generated (UUID) — a client-supplied `eventID` is never adopted as the primary key (it is disclosed under `ignoredFields`); CBV short names are normalized to the node's stored URN form (`urn:epcglobal:cbv:bizstep:*` / `urn:epcglobal:cbv:disp:*`, the same form the VC-shaped path stores and the lineage projection reads); defaults when absent: `bizStep` → `receiving`, `disposition` → `in_progress`. Rows captured on this path carry **no per-event credential** and are stored with `isUntpCompliant: false` (API-key provenance only) — they are never presented as UNTP-verified. This endpoint does not create lineage edges between events.

        :param epcis_document: (required)
        :type epcis_document: EpcisDocument
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

        _param = self._capture_epcis_document_serialize(
            epcis_document=epcis_document,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "EpcisCaptureResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '429': "InlineObject",
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
    def capture_epcis_document_with_http_info(
        self,
        epcis_document: EpcisDocument,
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
    ) -> ApiResponse[EpcisCaptureResponse]:
        """Capture a native GS1 EPCIS 2.0 document (JSON/JSON-LD)

        Captures a native **GS1 EPCIS 2.0 document** — the standard's own JSON/JSON-LD interchange format — and persists each supported event as an EPCIS event row scoped to your tenant, alongside the VC-shaped `POST /api/v1/events` path. Send the document exactly as your EPCIS infrastructure produces it.  **Permission:** `passport:update` (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy `REQUIRED`, or `DEFAULT` with the workspace's MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the `X-CSRF-Token` header (double-submit with the `opendpp_csrf` cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Validation:** the WHOLE document is validated against the official GS1 EPCIS **2.0.1** JSON Schema (vendored and pinned on the node) before any event is stored — a non-conformant document is rejected 400 with the first few schema violations under `errors[]`. Notable rules the OFFICIAL schema enforces: `@context` and `creationDate` are required; `bizStep`/`disposition` must use the CBV **short names** (e.g. `commissioning`, `in_transit`) or a custom (non-CBV) URI — the legacy `urn:epcglobal:cbv:*` URN form is REJECTED by the standard's schema; `action` is forbidden on `TransformationEvent`; `readPoint`/`bizLocation` carry `{id: <uri>}`. Only `type: \"EPCISDocument\"` is accepted (no `EPCISQueryDocument`, no bare events), and `epcisBody.eventList` must be non-empty.  **Per-event capture (partial success):** events are processed independently and the 201 response reports `results[]` (captured) and `errors[]` (rejected) by `index`. An event is rejected — never silently dropped — when its type is outside this node's traceability model (`ObjectEvent`, `AggregationEvent`, `TransformationEvent`, `AssociationEvent` are supported; `TransactionEvent` is not) or when it identifies stock ONLY by quantity lists (no `epcList`/`parentID`/`childEPCs`/`inputEPCList`/`outputEPCList` — nothing EPC-identified would remain to trace). If EVERY event is rejected the response is 400 `No Events Captured` with the same `errors[]`.  **Fidelity disclosure:** recognized EPCIS fields the node does not persist (`eventID`, quantity lists, `sensorElementList`, `bizTransactionList`, `sourceList`/`destinationList`, `persistentDisposition`, `errorDeclaration`, `ilmd`, custom extension fields, …) are listed per event under `results[].ignoredFields` instead of being silently discarded.  **Persistence:** row ids are ALWAYS server-generated (UUID) — a client-supplied `eventID` is never adopted as the primary key (it is disclosed under `ignoredFields`); CBV short names are normalized to the node's stored URN form (`urn:epcglobal:cbv:bizstep:*` / `urn:epcglobal:cbv:disp:*`, the same form the VC-shaped path stores and the lineage projection reads); defaults when absent: `bizStep` → `receiving`, `disposition` → `in_progress`. Rows captured on this path carry **no per-event credential** and are stored with `isUntpCompliant: false` (API-key provenance only) — they are never presented as UNTP-verified. This endpoint does not create lineage edges between events.

        :param epcis_document: (required)
        :type epcis_document: EpcisDocument
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

        _param = self._capture_epcis_document_serialize(
            epcis_document=epcis_document,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "EpcisCaptureResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '429': "InlineObject",
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
    def capture_epcis_document_without_preload_content(
        self,
        epcis_document: EpcisDocument,
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
        """Capture a native GS1 EPCIS 2.0 document (JSON/JSON-LD)

        Captures a native **GS1 EPCIS 2.0 document** — the standard's own JSON/JSON-LD interchange format — and persists each supported event as an EPCIS event row scoped to your tenant, alongside the VC-shaped `POST /api/v1/events` path. Send the document exactly as your EPCIS infrastructure produces it.  **Permission:** `passport:update` (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy `REQUIRED`, or `DEFAULT` with the workspace's MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the `X-CSRF-Token` header (double-submit with the `opendpp_csrf` cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Validation:** the WHOLE document is validated against the official GS1 EPCIS **2.0.1** JSON Schema (vendored and pinned on the node) before any event is stored — a non-conformant document is rejected 400 with the first few schema violations under `errors[]`. Notable rules the OFFICIAL schema enforces: `@context` and `creationDate` are required; `bizStep`/`disposition` must use the CBV **short names** (e.g. `commissioning`, `in_transit`) or a custom (non-CBV) URI — the legacy `urn:epcglobal:cbv:*` URN form is REJECTED by the standard's schema; `action` is forbidden on `TransformationEvent`; `readPoint`/`bizLocation` carry `{id: <uri>}`. Only `type: \"EPCISDocument\"` is accepted (no `EPCISQueryDocument`, no bare events), and `epcisBody.eventList` must be non-empty.  **Per-event capture (partial success):** events are processed independently and the 201 response reports `results[]` (captured) and `errors[]` (rejected) by `index`. An event is rejected — never silently dropped — when its type is outside this node's traceability model (`ObjectEvent`, `AggregationEvent`, `TransformationEvent`, `AssociationEvent` are supported; `TransactionEvent` is not) or when it identifies stock ONLY by quantity lists (no `epcList`/`parentID`/`childEPCs`/`inputEPCList`/`outputEPCList` — nothing EPC-identified would remain to trace). If EVERY event is rejected the response is 400 `No Events Captured` with the same `errors[]`.  **Fidelity disclosure:** recognized EPCIS fields the node does not persist (`eventID`, quantity lists, `sensorElementList`, `bizTransactionList`, `sourceList`/`destinationList`, `persistentDisposition`, `errorDeclaration`, `ilmd`, custom extension fields, …) are listed per event under `results[].ignoredFields` instead of being silently discarded.  **Persistence:** row ids are ALWAYS server-generated (UUID) — a client-supplied `eventID` is never adopted as the primary key (it is disclosed under `ignoredFields`); CBV short names are normalized to the node's stored URN form (`urn:epcglobal:cbv:bizstep:*` / `urn:epcglobal:cbv:disp:*`, the same form the VC-shaped path stores and the lineage projection reads); defaults when absent: `bizStep` → `receiving`, `disposition` → `in_progress`. Rows captured on this path carry **no per-event credential** and are stored with `isUntpCompliant: false` (API-key provenance only) — they are never presented as UNTP-verified. This endpoint does not create lineage edges between events.

        :param epcis_document: (required)
        :type epcis_document: EpcisDocument
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

        _param = self._capture_epcis_document_serialize(
            epcis_document=epcis_document,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "EpcisCaptureResponse",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '429': "InlineObject",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _capture_epcis_document_serialize(
        self,
        epcis_document,
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
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if epcis_document is not None:
            _body_params = epcis_document


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
            resource_path='/api/v1/events/epcis',
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
    def get_event_lineage(
        self,
        id: Annotated[StrictStr, Field(description="EPCIS event id — the server-generated UUID returned as `eventId` by `POST /api/v1/events`.")],
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
    ) -> TraceLineageResponse:
        """Retrieve the upstream pedigree of an event as a recursive lineage DAG

        Returns the full upstream pedigree of a traceability event as a recursive Directed Acyclic Graph: the root event plus, in `parents`, every event linked upstream through lineage relations registered on the node, walked transitively (parents of parents). A shared ancestor reached through multiple downstream paths is repeated under EACH path — the DAG is expanded into a tree in the response, not deduplicated; only a true cycle aborts the walk (400).  **Permission:** `passport:read`. Every node in the walk — the root AND each upstream parent — is scoped to the caller's tenant; an event belonging to another tenant is invisible and the request fails with 404 (no cross-tenant pedigree reads).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Caveats:** if the lineage graph contains a circular reference the walk aborts with 400. Any other failure (unknown id, other-tenant id, missing parent) is reported as the same deliberately generic 404 body. `eventTime` is serialized as ISO 8601 UTC; `epcs` is parsed from the stored EPC list (a non-array value degrades to `[]`); `location` mirrors the stored `bizLocation`.  **Content negotiation (EPCIS projection):** `Accept: application/ld+json` returns the SAME tenant-scoped lineage as a native **GS1 EPCIS 2.0 document** (`EPCISDocument` with the walk's events under `epcisBody.eventList`, ordered by `eventTime`, bounded to 500 events) instead of the recursive JSON tree — another projection of the same canonical rows, mirroring the AAS/VC Accept-header pattern on the passport resolution routes. Emitted documents use the official CBV short names (stored `urn:epcglobal:cbv:*` values are mapped back), expose row ids as `eventID` URNs (`urn:uuid:*`, or `urn:opendpp:event:*` for non-UUID ids), and wrap non-URI stored locations as `urn:opendpp:location:*`.

        :param id: EPCIS event id — the server-generated UUID returned as `eventId` by `POST /api/v1/events`. (required)
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

        _param = self._get_event_lineage_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "TraceLineageResponse",
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
    def get_event_lineage_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="EPCIS event id — the server-generated UUID returned as `eventId` by `POST /api/v1/events`.")],
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
    ) -> ApiResponse[TraceLineageResponse]:
        """Retrieve the upstream pedigree of an event as a recursive lineage DAG

        Returns the full upstream pedigree of a traceability event as a recursive Directed Acyclic Graph: the root event plus, in `parents`, every event linked upstream through lineage relations registered on the node, walked transitively (parents of parents). A shared ancestor reached through multiple downstream paths is repeated under EACH path — the DAG is expanded into a tree in the response, not deduplicated; only a true cycle aborts the walk (400).  **Permission:** `passport:read`. Every node in the walk — the root AND each upstream parent — is scoped to the caller's tenant; an event belonging to another tenant is invisible and the request fails with 404 (no cross-tenant pedigree reads).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Caveats:** if the lineage graph contains a circular reference the walk aborts with 400. Any other failure (unknown id, other-tenant id, missing parent) is reported as the same deliberately generic 404 body. `eventTime` is serialized as ISO 8601 UTC; `epcs` is parsed from the stored EPC list (a non-array value degrades to `[]`); `location` mirrors the stored `bizLocation`.  **Content negotiation (EPCIS projection):** `Accept: application/ld+json` returns the SAME tenant-scoped lineage as a native **GS1 EPCIS 2.0 document** (`EPCISDocument` with the walk's events under `epcisBody.eventList`, ordered by `eventTime`, bounded to 500 events) instead of the recursive JSON tree — another projection of the same canonical rows, mirroring the AAS/VC Accept-header pattern on the passport resolution routes. Emitted documents use the official CBV short names (stored `urn:epcglobal:cbv:*` values are mapped back), expose row ids as `eventID` URNs (`urn:uuid:*`, or `urn:opendpp:event:*` for non-UUID ids), and wrap non-URI stored locations as `urn:opendpp:location:*`.

        :param id: EPCIS event id — the server-generated UUID returned as `eventId` by `POST /api/v1/events`. (required)
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

        _param = self._get_event_lineage_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "TraceLineageResponse",
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
    def get_event_lineage_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="EPCIS event id — the server-generated UUID returned as `eventId` by `POST /api/v1/events`.")],
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
        """Retrieve the upstream pedigree of an event as a recursive lineage DAG

        Returns the full upstream pedigree of a traceability event as a recursive Directed Acyclic Graph: the root event plus, in `parents`, every event linked upstream through lineage relations registered on the node, walked transitively (parents of parents). A shared ancestor reached through multiple downstream paths is repeated under EACH path — the DAG is expanded into a tree in the response, not deduplicated; only a true cycle aborts the walk (400).  **Permission:** `passport:read`. Every node in the walk — the root AND each upstream parent — is scoped to the caller's tenant; an event belonging to another tenant is invisible and the request fails with 404 (no cross-tenant pedigree reads).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Caveats:** if the lineage graph contains a circular reference the walk aborts with 400. Any other failure (unknown id, other-tenant id, missing parent) is reported as the same deliberately generic 404 body. `eventTime` is serialized as ISO 8601 UTC; `epcs` is parsed from the stored EPC list (a non-array value degrades to `[]`); `location` mirrors the stored `bizLocation`.  **Content negotiation (EPCIS projection):** `Accept: application/ld+json` returns the SAME tenant-scoped lineage as a native **GS1 EPCIS 2.0 document** (`EPCISDocument` with the walk's events under `epcisBody.eventList`, ordered by `eventTime`, bounded to 500 events) instead of the recursive JSON tree — another projection of the same canonical rows, mirroring the AAS/VC Accept-header pattern on the passport resolution routes. Emitted documents use the official CBV short names (stored `urn:epcglobal:cbv:*` values are mapped back), expose row ids as `eventID` URNs (`urn:uuid:*`, or `urn:opendpp:event:*` for non-UUID ids), and wrap non-URI stored locations as `urn:opendpp:location:*`.

        :param id: EPCIS event id — the server-generated UUID returned as `eventId` by `POST /api/v1/events`. (required)
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

        _param = self._get_event_lineage_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "TraceLineageResponse",
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


    def _get_event_lineage_serialize(
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
            method='GET',
            resource_path='/api/v1/events/{id}/lineage',
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
    def register_traceability_event(
        self,
        untp_event_credential: UntpEventCredential,
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
    ) -> TraceEventRegistered:
        """Register a UNTP/EPCIS 2.0 traceability event (VC-shaped)

        Registers a supply-chain traceability event carried as a VC-shaped UNTP credential and persists it as an EPCIS 2.0 event row scoped to your tenant.  **Permission:** `passport:update` (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy `REQUIRED`, or `DEFAULT` with the workspace's MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the `X-CSRF-Token` header (double-submit with the `opendpp_csrf` cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Validation pipeline (in order):** 1. *Structural* — the body must be an object containing `credentialSubject`, otherwise 400 `Bad Request`. 2. *EPCIS rule* — `action` is strictly forbidden on `TransformationEvent` (any non-null value → 400 `Schema Validation Error`). 3. *Cryptographic* — the credential's `proof` MUST be a conformant W3C `DataIntegrityProof` with `cryptosuite: \"ecdsa-jcs-2019\"` and a multibase base58btc (`z…`) `proofValue`; any other proof shape (e.g. the legacy key-sorted `MerkleTreeAttestationProof`) is rejected. The ECDSA P-256 signature is verified per that cryptosuite over `sha256(JCS(proof options)) ‖ sha256(JCS(credential without proof))` — RFC 8785 JCS canonicalization, IEEE-P1363 raw r‖s — a conformant, interoperable Data Integrity suite, which is what makes the persisted `isUntpCompliant: true` honest. The verification key is resolved in trust order: (a) an embedded `proof.verificationMethod.x5c` chain, accepted ONLY when the node has trust anchors configured, the chain validates against them, every certificate is currently valid, and the leaf attests the issuer; (b) ALL of the authoritative vault keys (current + retired, so a pre-rotation credential still verifies) of the tenant whose UNIQUE subdomain EXACTLY equals the trailing `:`-segment of the issuer DID. If no key resolves or the signature does not verify → 400 `Cryptographic Verification Failed`. 4. *Operator scoping* — if your API key is scoped to an Economic Operator, the credential's declared operator DID — the `issuer` DID, or `credentialSubject.responsibleOperatorDid` only when `issuer` is absent — must contain the bound operator's registration id (e.g. `EU-DEFAULT-001`), otherwise 403 with `message: \"Your access is restricted to Economic Operator: <operatorId> (<regId>)\"`.  **Persistence:** the stored event id is ALWAYS server-generated (UUID) — the credential's own `id` is never used as the primary key (prevents cross-tenant id squatting); the issuer DID is retained as `issuerDid`. Defaults applied on write: `bizStep` → `urn:epcglobal:cbv:bizstep:receiving`; `disposition` → `urn:epcglobal:cbv:disp:in_progress`; `readPoint` → `geo:<latitude>,<longitude>` derived from `credentialSubject.originLocation` when present; `bizLocation` → `responsibleOperatorDid`; `eventTime` → `issuanceDate`, else the server clock; `epcList` → `[credentialSubject.id]` when not supplied as an array (or `[]`). The row is stored with `isUntpCompliant: true` and the `proof.proofValue` retained.  **Caveats:** `credentialSubject.eventType` must be one of the documented event-type values and `action` (when present) one of `ADD`/`OBSERVE`/`DELETE` — both map to server-side enums, and a missing or unknown value is only rejected at the persistence layer and surfaces as the 500 `Database Persistence Failed` body, not as a 400. Note the 201 envelope is `{status: \"success\", ...}`, NOT the usual `{success: true, ...}` shape. This endpoint does not create lineage edges between events; the lineage DAG read by `GET /api/v1/events/{id}/lineage` is built from lineage relations maintained separately on the node.

        :param untp_event_credential: (required)
        :type untp_event_credential: UntpEventCredential
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

        _param = self._register_traceability_event_serialize(
            untp_event_credential=untp_event_credential,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "TraceEventRegistered",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
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
    def register_traceability_event_with_http_info(
        self,
        untp_event_credential: UntpEventCredential,
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
    ) -> ApiResponse[TraceEventRegistered]:
        """Register a UNTP/EPCIS 2.0 traceability event (VC-shaped)

        Registers a supply-chain traceability event carried as a VC-shaped UNTP credential and persists it as an EPCIS 2.0 event row scoped to your tenant.  **Permission:** `passport:update` (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy `REQUIRED`, or `DEFAULT` with the workspace's MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the `X-CSRF-Token` header (double-submit with the `opendpp_csrf` cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Validation pipeline (in order):** 1. *Structural* — the body must be an object containing `credentialSubject`, otherwise 400 `Bad Request`. 2. *EPCIS rule* — `action` is strictly forbidden on `TransformationEvent` (any non-null value → 400 `Schema Validation Error`). 3. *Cryptographic* — the credential's `proof` MUST be a conformant W3C `DataIntegrityProof` with `cryptosuite: \"ecdsa-jcs-2019\"` and a multibase base58btc (`z…`) `proofValue`; any other proof shape (e.g. the legacy key-sorted `MerkleTreeAttestationProof`) is rejected. The ECDSA P-256 signature is verified per that cryptosuite over `sha256(JCS(proof options)) ‖ sha256(JCS(credential without proof))` — RFC 8785 JCS canonicalization, IEEE-P1363 raw r‖s — a conformant, interoperable Data Integrity suite, which is what makes the persisted `isUntpCompliant: true` honest. The verification key is resolved in trust order: (a) an embedded `proof.verificationMethod.x5c` chain, accepted ONLY when the node has trust anchors configured, the chain validates against them, every certificate is currently valid, and the leaf attests the issuer; (b) ALL of the authoritative vault keys (current + retired, so a pre-rotation credential still verifies) of the tenant whose UNIQUE subdomain EXACTLY equals the trailing `:`-segment of the issuer DID. If no key resolves or the signature does not verify → 400 `Cryptographic Verification Failed`. 4. *Operator scoping* — if your API key is scoped to an Economic Operator, the credential's declared operator DID — the `issuer` DID, or `credentialSubject.responsibleOperatorDid` only when `issuer` is absent — must contain the bound operator's registration id (e.g. `EU-DEFAULT-001`), otherwise 403 with `message: \"Your access is restricted to Economic Operator: <operatorId> (<regId>)\"`.  **Persistence:** the stored event id is ALWAYS server-generated (UUID) — the credential's own `id` is never used as the primary key (prevents cross-tenant id squatting); the issuer DID is retained as `issuerDid`. Defaults applied on write: `bizStep` → `urn:epcglobal:cbv:bizstep:receiving`; `disposition` → `urn:epcglobal:cbv:disp:in_progress`; `readPoint` → `geo:<latitude>,<longitude>` derived from `credentialSubject.originLocation` when present; `bizLocation` → `responsibleOperatorDid`; `eventTime` → `issuanceDate`, else the server clock; `epcList` → `[credentialSubject.id]` when not supplied as an array (or `[]`). The row is stored with `isUntpCompliant: true` and the `proof.proofValue` retained.  **Caveats:** `credentialSubject.eventType` must be one of the documented event-type values and `action` (when present) one of `ADD`/`OBSERVE`/`DELETE` — both map to server-side enums, and a missing or unknown value is only rejected at the persistence layer and surfaces as the 500 `Database Persistence Failed` body, not as a 400. Note the 201 envelope is `{status: \"success\", ...}`, NOT the usual `{success: true, ...}` shape. This endpoint does not create lineage edges between events; the lineage DAG read by `GET /api/v1/events/{id}/lineage` is built from lineage relations maintained separately on the node.

        :param untp_event_credential: (required)
        :type untp_event_credential: UntpEventCredential
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

        _param = self._register_traceability_event_serialize(
            untp_event_credential=untp_event_credential,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "TraceEventRegistered",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
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
    def register_traceability_event_without_preload_content(
        self,
        untp_event_credential: UntpEventCredential,
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
        """Register a UNTP/EPCIS 2.0 traceability event (VC-shaped)

        Registers a supply-chain traceability event carried as a VC-shaped UNTP credential and persists it as an EPCIS 2.0 event row scoped to your tenant.  **Permission:** `passport:update` (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy `REQUIRED`, or `DEFAULT` with the workspace's MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the `X-CSRF-Token` header (double-submit with the `opendpp_csrf` cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.  **Validation pipeline (in order):** 1. *Structural* — the body must be an object containing `credentialSubject`, otherwise 400 `Bad Request`. 2. *EPCIS rule* — `action` is strictly forbidden on `TransformationEvent` (any non-null value → 400 `Schema Validation Error`). 3. *Cryptographic* — the credential's `proof` MUST be a conformant W3C `DataIntegrityProof` with `cryptosuite: \"ecdsa-jcs-2019\"` and a multibase base58btc (`z…`) `proofValue`; any other proof shape (e.g. the legacy key-sorted `MerkleTreeAttestationProof`) is rejected. The ECDSA P-256 signature is verified per that cryptosuite over `sha256(JCS(proof options)) ‖ sha256(JCS(credential without proof))` — RFC 8785 JCS canonicalization, IEEE-P1363 raw r‖s — a conformant, interoperable Data Integrity suite, which is what makes the persisted `isUntpCompliant: true` honest. The verification key is resolved in trust order: (a) an embedded `proof.verificationMethod.x5c` chain, accepted ONLY when the node has trust anchors configured, the chain validates against them, every certificate is currently valid, and the leaf attests the issuer; (b) ALL of the authoritative vault keys (current + retired, so a pre-rotation credential still verifies) of the tenant whose UNIQUE subdomain EXACTLY equals the trailing `:`-segment of the issuer DID. If no key resolves or the signature does not verify → 400 `Cryptographic Verification Failed`. 4. *Operator scoping* — if your API key is scoped to an Economic Operator, the credential's declared operator DID — the `issuer` DID, or `credentialSubject.responsibleOperatorDid` only when `issuer` is absent — must contain the bound operator's registration id (e.g. `EU-DEFAULT-001`), otherwise 403 with `message: \"Your access is restricted to Economic Operator: <operatorId> (<regId>)\"`.  **Persistence:** the stored event id is ALWAYS server-generated (UUID) — the credential's own `id` is never used as the primary key (prevents cross-tenant id squatting); the issuer DID is retained as `issuerDid`. Defaults applied on write: `bizStep` → `urn:epcglobal:cbv:bizstep:receiving`; `disposition` → `urn:epcglobal:cbv:disp:in_progress`; `readPoint` → `geo:<latitude>,<longitude>` derived from `credentialSubject.originLocation` when present; `bizLocation` → `responsibleOperatorDid`; `eventTime` → `issuanceDate`, else the server clock; `epcList` → `[credentialSubject.id]` when not supplied as an array (or `[]`). The row is stored with `isUntpCompliant: true` and the `proof.proofValue` retained.  **Caveats:** `credentialSubject.eventType` must be one of the documented event-type values and `action` (when present) one of `ADD`/`OBSERVE`/`DELETE` — both map to server-side enums, and a missing or unknown value is only rejected at the persistence layer and surfaces as the 500 `Database Persistence Failed` body, not as a 400. Note the 201 envelope is `{status: \"success\", ...}`, NOT the usual `{success: true, ...}` shape. This endpoint does not create lineage edges between events; the lineage DAG read by `GET /api/v1/events/{id}/lineage` is built from lineage relations maintained separately on the node.

        :param untp_event_credential: (required)
        :type untp_event_credential: UntpEventCredential
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

        _param = self._register_traceability_event_serialize(
            untp_event_credential=untp_event_credential,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "TraceEventRegistered",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _register_traceability_event_serialize(
        self,
        untp_event_credential,
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
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if untp_event_credential is not None:
            _body_params = untp_event_credential


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
            resource_path='/api/v1/events',
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
    def verify_passport_seal(
        self,
        seal_verify_request: SealVerifyRequest,
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
    ) -> SealVerifyResponse:
        """Publicly verify a passport's seal, certificate chain and timestamp

        **Public seal-verification API** — cryptographically verifies that a Digital Product Passport document was sealed by an economic-operator tenant registered on this node and has not been tampered with. No authentication required.  **Rate limit:** **30 requests/min per IP**. This bucket emits no rate-limit headers of its own — any `x-ratelimit-*` headers on responses (including this 429) come from the global 100 req/min limiter and describe that budget, not the 30/min one. The 429 body is the two-field `{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded.\"}`.  **Input resolution.** `payload` is required. `signature` and `publicKey` may be supplied top-level, or are extracted from the document's embedded proof block: `signature` ← `payload.proof.proofValue` (else `payload.proof.signatureValue`); `publicKey` ← `payload.proof.publicKeyPem`, else the leaf certificate's SPKI when `payload.proof.x5c` is present. If, after extraction, any of the three is still missing → 400. The public key is CRLF-normalized and trimmed before matching.  **Verification pipeline (in order):** 1. **Certificate-chain report (optional).** If `payload.proof.x5c` is a non-empty array of base64-DER certificates (leaf first), the chain is parsed and a `certificate` report is built: the leaf's `subject` / `issuer` / `validFrom` / `validTo` (X.509 textual dates such as `Jan 10 00:00:00 2026 GMT` — NOT ISO 8601), `chainValid` (every link signature-verifies against the next certificate, every certificate is inside its validity window, and the top of the chain is anchored to this node's seal CA — SHA-256 fingerprint match or signature under the CA key; the CA is published at `GET /.well-known/opendpp-seal-ca.pem`), and `keyMatchesProof` (the leaf SPKI equals the supplied `publicKey`, whitespace-insensitive; always `true` when no explicit key was supplied). An unparseable chain yields `{\"chainValid\": false, \"error\": \"Unparseable x5c certificate chain\"}` and does NOT fail the request. This reports the CERTIFIED identity of the seal creator. SECURITY: the report is attached ONLY on a `verified: true` outcome whose chain is TRUSTED (`chainValid` AND `keyMatchesProof` both true) — an untrusted/self-signed chain, one outside its validity window or not anchored to this node's seal CA, or one whose leaf key does not match the verifying key, is never surfaced as a `certificate` block (it must not present an unverified identity as authoritative). The two policy-failure responses omit it too. 2. **Key-registration gate.** The `publicKey` must exactly match the registered signing public key of a tenant on this node (trailing-newline tolerant) — otherwise HTTP **200** with `verified: false` and an explanatory `message`. Verification-policy failures are reported in-band, never as HTTP errors. 3. **Operator-binding gate (fail-closed).** If the payload declares an operator registration id (`payload.operator.regId`, else `payload.economicOperator.regId`), that id MUST resolve to an Economic Operator registered on this node AND that operator MUST be bound to the signing tenant (a workspace–operator binding registered on this node). A declared operator that is unregistered, or registered but not bound to the key-owning tenant, → 200 `verified: false` with an explanatory `message`. Payloads that declare no operator id skip this gate. 4. **Signature verification (two phases).** *Phase 1 — Merkle seal:* when `payload.metadata` is an object (or, when the `metadata` key is entirely absent, the whole `payload` is treated as the metadata), the SHA-256 Merkle tree over the metadata's top-level properties is rebuilt and the base64 ECDSA (P-256 / SHA-256) `signature` is verified against the recomputed root. Every leaf is recomputed from the actual values — caller-supplied redacted-leaf hashes are NOT accepted (they would let a tampered field be smuggled past verification), so a publicly redacted document will not pass the Merkle phase: verify the unredacted, privileged document. *Phase 2 — fallback:* if the Merkle phase does not verify, the signature is verified over the deterministic key-sorted canonicalization of the entire `payload`. 5. **RFC 3161 timestamp report (optional).** When `payload.proof.rfc3161.token` is a non-empty base64-DER TimeStampToken, the response includes `timestamp` with the TSA-asserted `genTime` parsed from the token's TSTInfo (or `genTime: null` plus a `note` when the token cannot be parsed). When the node has a TSA trust anchor configured, the report also carries `timeAuthenticated` — the node's own verification of the token's CMS SignedData signature over its TSTInfo PLUS full RFC 3161 trust-path validation of the signer to that anchor (a critical `id-kp-timeStamping` EKU, validity at the asserted `genTime`, CA-constrained intermediates) (`false`, and the asserted time unauthenticated, when no CA is configured, the signature fails, or the path is not policy-valid); a verifier may still run its own `openssl ts -verify`. Like `certificate`, it appears only on the final verification outcome.  **Outcome.** A processed verification ALWAYS returns HTTP 200 with `verified: true|false`; 400 is reserved for missing parameters or an exception thrown while verifying (e.g. an undecodable public key). `timestamp` is attached only when verification proceeds past the key-registration and operator-binding gates; `certificate` is attached only on a `verified: true` outcome whose chain is trusted (`chainValid` AND `keyMatchesProof`) — the two policy `verified: false` responses (and any untrusted-chain outcome) omit `certificate`, even when an x5c chain and/or an RFC 3161 token were supplied. The 400 bodies on this public endpoint are `{\"success\": false, \"message\": \"...\"}` — they include `success` but OMIT the `error` field. (A syntactically malformed JSON body is rejected earlier by the framework with its default `{statusCode, error, message}` body; a POST with no body at all — no `Content-Type` — fails before processing with a framework-default 500, so send at least `{}`. An empty `application/json` body is treated as `{}` and yields the documented 400.)

        :param seal_verify_request: (required)
        :type seal_verify_request: SealVerifyRequest
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

        _param = self._verify_passport_seal_serialize(
            seal_verify_request=seal_verify_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "SealVerifyResponse",
            '400': "VerifyPassportSeal400Response",
            '429': "Error",
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
    def verify_passport_seal_with_http_info(
        self,
        seal_verify_request: SealVerifyRequest,
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
    ) -> ApiResponse[SealVerifyResponse]:
        """Publicly verify a passport's seal, certificate chain and timestamp

        **Public seal-verification API** — cryptographically verifies that a Digital Product Passport document was sealed by an economic-operator tenant registered on this node and has not been tampered with. No authentication required.  **Rate limit:** **30 requests/min per IP**. This bucket emits no rate-limit headers of its own — any `x-ratelimit-*` headers on responses (including this 429) come from the global 100 req/min limiter and describe that budget, not the 30/min one. The 429 body is the two-field `{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded.\"}`.  **Input resolution.** `payload` is required. `signature` and `publicKey` may be supplied top-level, or are extracted from the document's embedded proof block: `signature` ← `payload.proof.proofValue` (else `payload.proof.signatureValue`); `publicKey` ← `payload.proof.publicKeyPem`, else the leaf certificate's SPKI when `payload.proof.x5c` is present. If, after extraction, any of the three is still missing → 400. The public key is CRLF-normalized and trimmed before matching.  **Verification pipeline (in order):** 1. **Certificate-chain report (optional).** If `payload.proof.x5c` is a non-empty array of base64-DER certificates (leaf first), the chain is parsed and a `certificate` report is built: the leaf's `subject` / `issuer` / `validFrom` / `validTo` (X.509 textual dates such as `Jan 10 00:00:00 2026 GMT` — NOT ISO 8601), `chainValid` (every link signature-verifies against the next certificate, every certificate is inside its validity window, and the top of the chain is anchored to this node's seal CA — SHA-256 fingerprint match or signature under the CA key; the CA is published at `GET /.well-known/opendpp-seal-ca.pem`), and `keyMatchesProof` (the leaf SPKI equals the supplied `publicKey`, whitespace-insensitive; always `true` when no explicit key was supplied). An unparseable chain yields `{\"chainValid\": false, \"error\": \"Unparseable x5c certificate chain\"}` and does NOT fail the request. This reports the CERTIFIED identity of the seal creator. SECURITY: the report is attached ONLY on a `verified: true` outcome whose chain is TRUSTED (`chainValid` AND `keyMatchesProof` both true) — an untrusted/self-signed chain, one outside its validity window or not anchored to this node's seal CA, or one whose leaf key does not match the verifying key, is never surfaced as a `certificate` block (it must not present an unverified identity as authoritative). The two policy-failure responses omit it too. 2. **Key-registration gate.** The `publicKey` must exactly match the registered signing public key of a tenant on this node (trailing-newline tolerant) — otherwise HTTP **200** with `verified: false` and an explanatory `message`. Verification-policy failures are reported in-band, never as HTTP errors. 3. **Operator-binding gate (fail-closed).** If the payload declares an operator registration id (`payload.operator.regId`, else `payload.economicOperator.regId`), that id MUST resolve to an Economic Operator registered on this node AND that operator MUST be bound to the signing tenant (a workspace–operator binding registered on this node). A declared operator that is unregistered, or registered but not bound to the key-owning tenant, → 200 `verified: false` with an explanatory `message`. Payloads that declare no operator id skip this gate. 4. **Signature verification (two phases).** *Phase 1 — Merkle seal:* when `payload.metadata` is an object (or, when the `metadata` key is entirely absent, the whole `payload` is treated as the metadata), the SHA-256 Merkle tree over the metadata's top-level properties is rebuilt and the base64 ECDSA (P-256 / SHA-256) `signature` is verified against the recomputed root. Every leaf is recomputed from the actual values — caller-supplied redacted-leaf hashes are NOT accepted (they would let a tampered field be smuggled past verification), so a publicly redacted document will not pass the Merkle phase: verify the unredacted, privileged document. *Phase 2 — fallback:* if the Merkle phase does not verify, the signature is verified over the deterministic key-sorted canonicalization of the entire `payload`. 5. **RFC 3161 timestamp report (optional).** When `payload.proof.rfc3161.token` is a non-empty base64-DER TimeStampToken, the response includes `timestamp` with the TSA-asserted `genTime` parsed from the token's TSTInfo (or `genTime: null` plus a `note` when the token cannot be parsed). When the node has a TSA trust anchor configured, the report also carries `timeAuthenticated` — the node's own verification of the token's CMS SignedData signature over its TSTInfo PLUS full RFC 3161 trust-path validation of the signer to that anchor (a critical `id-kp-timeStamping` EKU, validity at the asserted `genTime`, CA-constrained intermediates) (`false`, and the asserted time unauthenticated, when no CA is configured, the signature fails, or the path is not policy-valid); a verifier may still run its own `openssl ts -verify`. Like `certificate`, it appears only on the final verification outcome.  **Outcome.** A processed verification ALWAYS returns HTTP 200 with `verified: true|false`; 400 is reserved for missing parameters or an exception thrown while verifying (e.g. an undecodable public key). `timestamp` is attached only when verification proceeds past the key-registration and operator-binding gates; `certificate` is attached only on a `verified: true` outcome whose chain is trusted (`chainValid` AND `keyMatchesProof`) — the two policy `verified: false` responses (and any untrusted-chain outcome) omit `certificate`, even when an x5c chain and/or an RFC 3161 token were supplied. The 400 bodies on this public endpoint are `{\"success\": false, \"message\": \"...\"}` — they include `success` but OMIT the `error` field. (A syntactically malformed JSON body is rejected earlier by the framework with its default `{statusCode, error, message}` body; a POST with no body at all — no `Content-Type` — fails before processing with a framework-default 500, so send at least `{}`. An empty `application/json` body is treated as `{}` and yields the documented 400.)

        :param seal_verify_request: (required)
        :type seal_verify_request: SealVerifyRequest
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

        _param = self._verify_passport_seal_serialize(
            seal_verify_request=seal_verify_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "SealVerifyResponse",
            '400': "VerifyPassportSeal400Response",
            '429': "Error",
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
    def verify_passport_seal_without_preload_content(
        self,
        seal_verify_request: SealVerifyRequest,
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
        """Publicly verify a passport's seal, certificate chain and timestamp

        **Public seal-verification API** — cryptographically verifies that a Digital Product Passport document was sealed by an economic-operator tenant registered on this node and has not been tampered with. No authentication required.  **Rate limit:** **30 requests/min per IP**. This bucket emits no rate-limit headers of its own — any `x-ratelimit-*` headers on responses (including this 429) come from the global 100 req/min limiter and describe that budget, not the 30/min one. The 429 body is the two-field `{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded.\"}`.  **Input resolution.** `payload` is required. `signature` and `publicKey` may be supplied top-level, or are extracted from the document's embedded proof block: `signature` ← `payload.proof.proofValue` (else `payload.proof.signatureValue`); `publicKey` ← `payload.proof.publicKeyPem`, else the leaf certificate's SPKI when `payload.proof.x5c` is present. If, after extraction, any of the three is still missing → 400. The public key is CRLF-normalized and trimmed before matching.  **Verification pipeline (in order):** 1. **Certificate-chain report (optional).** If `payload.proof.x5c` is a non-empty array of base64-DER certificates (leaf first), the chain is parsed and a `certificate` report is built: the leaf's `subject` / `issuer` / `validFrom` / `validTo` (X.509 textual dates such as `Jan 10 00:00:00 2026 GMT` — NOT ISO 8601), `chainValid` (every link signature-verifies against the next certificate, every certificate is inside its validity window, and the top of the chain is anchored to this node's seal CA — SHA-256 fingerprint match or signature under the CA key; the CA is published at `GET /.well-known/opendpp-seal-ca.pem`), and `keyMatchesProof` (the leaf SPKI equals the supplied `publicKey`, whitespace-insensitive; always `true` when no explicit key was supplied). An unparseable chain yields `{\"chainValid\": false, \"error\": \"Unparseable x5c certificate chain\"}` and does NOT fail the request. This reports the CERTIFIED identity of the seal creator. SECURITY: the report is attached ONLY on a `verified: true` outcome whose chain is TRUSTED (`chainValid` AND `keyMatchesProof` both true) — an untrusted/self-signed chain, one outside its validity window or not anchored to this node's seal CA, or one whose leaf key does not match the verifying key, is never surfaced as a `certificate` block (it must not present an unverified identity as authoritative). The two policy-failure responses omit it too. 2. **Key-registration gate.** The `publicKey` must exactly match the registered signing public key of a tenant on this node (trailing-newline tolerant) — otherwise HTTP **200** with `verified: false` and an explanatory `message`. Verification-policy failures are reported in-band, never as HTTP errors. 3. **Operator-binding gate (fail-closed).** If the payload declares an operator registration id (`payload.operator.regId`, else `payload.economicOperator.regId`), that id MUST resolve to an Economic Operator registered on this node AND that operator MUST be bound to the signing tenant (a workspace–operator binding registered on this node). A declared operator that is unregistered, or registered but not bound to the key-owning tenant, → 200 `verified: false` with an explanatory `message`. Payloads that declare no operator id skip this gate. 4. **Signature verification (two phases).** *Phase 1 — Merkle seal:* when `payload.metadata` is an object (or, when the `metadata` key is entirely absent, the whole `payload` is treated as the metadata), the SHA-256 Merkle tree over the metadata's top-level properties is rebuilt and the base64 ECDSA (P-256 / SHA-256) `signature` is verified against the recomputed root. Every leaf is recomputed from the actual values — caller-supplied redacted-leaf hashes are NOT accepted (they would let a tampered field be smuggled past verification), so a publicly redacted document will not pass the Merkle phase: verify the unredacted, privileged document. *Phase 2 — fallback:* if the Merkle phase does not verify, the signature is verified over the deterministic key-sorted canonicalization of the entire `payload`. 5. **RFC 3161 timestamp report (optional).** When `payload.proof.rfc3161.token` is a non-empty base64-DER TimeStampToken, the response includes `timestamp` with the TSA-asserted `genTime` parsed from the token's TSTInfo (or `genTime: null` plus a `note` when the token cannot be parsed). When the node has a TSA trust anchor configured, the report also carries `timeAuthenticated` — the node's own verification of the token's CMS SignedData signature over its TSTInfo PLUS full RFC 3161 trust-path validation of the signer to that anchor (a critical `id-kp-timeStamping` EKU, validity at the asserted `genTime`, CA-constrained intermediates) (`false`, and the asserted time unauthenticated, when no CA is configured, the signature fails, or the path is not policy-valid); a verifier may still run its own `openssl ts -verify`. Like `certificate`, it appears only on the final verification outcome.  **Outcome.** A processed verification ALWAYS returns HTTP 200 with `verified: true|false`; 400 is reserved for missing parameters or an exception thrown while verifying (e.g. an undecodable public key). `timestamp` is attached only when verification proceeds past the key-registration and operator-binding gates; `certificate` is attached only on a `verified: true` outcome whose chain is trusted (`chainValid` AND `keyMatchesProof`) — the two policy `verified: false` responses (and any untrusted-chain outcome) omit `certificate`, even when an x5c chain and/or an RFC 3161 token were supplied. The 400 bodies on this public endpoint are `{\"success\": false, \"message\": \"...\"}` — they include `success` but OMIT the `error` field. (A syntactically malformed JSON body is rejected earlier by the framework with its default `{statusCode, error, message}` body; a POST with no body at all — no `Content-Type` — fails before processing with a framework-default 500, so send at least `{}`. An empty `application/json` body is treated as `{}` and yields the documented 400.)

        :param seal_verify_request: (required)
        :type seal_verify_request: SealVerifyRequest
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

        _param = self._verify_passport_seal_serialize(
            seal_verify_request=seal_verify_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "SealVerifyResponse",
            '400': "VerifyPassportSeal400Response",
            '429': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _verify_passport_seal_serialize(
        self,
        seal_verify_request,
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
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if seal_verify_request is not None:
            _body_params = seal_verify_request


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
        ]

        return self.api_client.param_serialize(
            method='POST',
            resource_path='/api/v1/audit/verify',
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


