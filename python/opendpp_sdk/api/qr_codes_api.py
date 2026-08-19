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

from pydantic import Field, StrictBool, StrictBytes, StrictStr, field_validator
from typing import Optional, Tuple, Union
from typing_extensions import Annotated
from opendpp_sdk.models.bulk_export_passport_labels_request import BulkExportPassportLabelsRequest

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class QRCodesApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def bulk_export_passport_labels(
        self,
        bulk_export_passport_labels_request: BulkExportPassportLabelsRequest,
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
    ) -> bytearray:
        """Bulk-export print-grade QR labels for many passports as a ZIP

        Renders a GS1 Digital Link QR code for each of the supplied passports and returns them as a single `application/zip` download (`Content-Disposition: attachment; filename=\"labels.zip\"`) — the export counterpart to the bulk import. One image entry per resolved passport, named `<productId>.<png|svg>` (characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 chars; duplicate names get a `-2`, `-3`, … suffix), plus a `manifest.json` listing what was `included` and `skipped`.  **Permission:** `passport:read` (read-only — no subscription/402 gate, and NOT subject to the programmatic API-write entitlement).  **Partial success:** an id that is unknown, not owned by your tenant, or outside an operator-scoped key's bound operator is **skipped and reported** in `manifest.json` (`{ id, reason }`) — it never fails the whole batch. Only the caller's own passports resolve, so this cannot enumerate another tenant's catalog.  **Limits:** at most **200** ids per call (mirrors the bulk-import cap); more returns **400** pointing at the async export. `hri: true` requires `format: \"svg\"` (same constraint as the single QR). `size` is clamped to 128–2048.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param bulk_export_passport_labels_request: (required)
        :type bulk_export_passport_labels_request: BulkExportPassportLabelsRequest
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

        _param = self._bulk_export_passport_labels_serialize(
            bulk_export_passport_labels_request=bulk_export_passport_labels_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytearray",
            '400': "Error",
            '401': "Error",
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
    def bulk_export_passport_labels_with_http_info(
        self,
        bulk_export_passport_labels_request: BulkExportPassportLabelsRequest,
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
    ) -> ApiResponse[bytearray]:
        """Bulk-export print-grade QR labels for many passports as a ZIP

        Renders a GS1 Digital Link QR code for each of the supplied passports and returns them as a single `application/zip` download (`Content-Disposition: attachment; filename=\"labels.zip\"`) — the export counterpart to the bulk import. One image entry per resolved passport, named `<productId>.<png|svg>` (characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 chars; duplicate names get a `-2`, `-3`, … suffix), plus a `manifest.json` listing what was `included` and `skipped`.  **Permission:** `passport:read` (read-only — no subscription/402 gate, and NOT subject to the programmatic API-write entitlement).  **Partial success:** an id that is unknown, not owned by your tenant, or outside an operator-scoped key's bound operator is **skipped and reported** in `manifest.json` (`{ id, reason }`) — it never fails the whole batch. Only the caller's own passports resolve, so this cannot enumerate another tenant's catalog.  **Limits:** at most **200** ids per call (mirrors the bulk-import cap); more returns **400** pointing at the async export. `hri: true` requires `format: \"svg\"` (same constraint as the single QR). `size` is clamped to 128–2048.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param bulk_export_passport_labels_request: (required)
        :type bulk_export_passport_labels_request: BulkExportPassportLabelsRequest
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

        _param = self._bulk_export_passport_labels_serialize(
            bulk_export_passport_labels_request=bulk_export_passport_labels_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytearray",
            '400': "Error",
            '401': "Error",
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
    def bulk_export_passport_labels_without_preload_content(
        self,
        bulk_export_passport_labels_request: BulkExportPassportLabelsRequest,
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
        """Bulk-export print-grade QR labels for many passports as a ZIP

        Renders a GS1 Digital Link QR code for each of the supplied passports and returns them as a single `application/zip` download (`Content-Disposition: attachment; filename=\"labels.zip\"`) — the export counterpart to the bulk import. One image entry per resolved passport, named `<productId>.<png|svg>` (characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 chars; duplicate names get a `-2`, `-3`, … suffix), plus a `manifest.json` listing what was `included` and `skipped`.  **Permission:** `passport:read` (read-only — no subscription/402 gate, and NOT subject to the programmatic API-write entitlement).  **Partial success:** an id that is unknown, not owned by your tenant, or outside an operator-scoped key's bound operator is **skipped and reported** in `manifest.json` (`{ id, reason }`) — it never fails the whole batch. Only the caller's own passports resolve, so this cannot enumerate another tenant's catalog.  **Limits:** at most **200** ids per call (mirrors the bulk-import cap); more returns **400** pointing at the async export. `hri: true` requires `format: \"svg\"` (same constraint as the single QR). `size` is clamped to 128–2048.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param bulk_export_passport_labels_request: (required)
        :type bulk_export_passport_labels_request: BulkExportPassportLabelsRequest
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

        _param = self._bulk_export_passport_labels_serialize(
            bulk_export_passport_labels_request=bulk_export_passport_labels_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "bytearray",
            '400': "Error",
            '401': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _bulk_export_passport_labels_serialize(
        self,
        bulk_export_passport_labels_request,
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
        if bulk_export_passport_labels_request is not None:
            _body_params = bulk_export_passport_labels_request


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/zip', 
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
            resource_path='/api/v1/passports/labels',
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
    def get_battery_unit_qr_code(
        self,
        id: Annotated[StrictStr, Field(description="BatteryUnit UUID (primary key). Serial numbers are NOT accepted here.")],
        format: Annotated[Optional[StrictStr], Field(description="Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).")] = None,
        size: Annotated[Optional[Annotated[int, Field(le=2048, strict=True, ge=128)]], Field(description="Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.")] = None,
        ecl: Annotated[Optional[StrictStr], Field(description="QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`).")] = None,
        hri: Annotated[Optional[StrictBool], Field(description="When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).")] = None,
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
        """Export a print-grade QR code for an individual battery unit

        Renders the battery unit's GS1 Digital Link URI as a print-grade QR code — the AI-21 path segment carries the unit's **real physical serial number** (e.g. `https://opendpp-node.eu/01/09501101530003/21/BAT-2026-000123`). This is the carrier each individual battery must wear (per-unit passports, per the EU Battery Regulation).  **Permission:** `battery:read` (read-only — subscription status is **not** checked on `:read` permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session.  **Identifier resolution:** `{id}` is the BatteryUnit **UUID only** — unlike the passport QR route there is **no** serial-number fallback. Lookup is tenant-scoped. Credentials scoped to an Economic Operator receive **403** (`Your access is restricted to Economic Operator: <operatorId>`) when the unit's parent passport belongs to a different operator.  **QR rendering:** identical pipeline to the passport QR export — 4-module quiet zone, `ecl` default `Q`, `size` clamped to 128–2048 px (clamped, not rejected; fractions truncated). The response carries `Content-Disposition: attachment; filename=\"qr-<serialNumber>.png\"` (or `.svg`); the filename base is the unit's `serialNumber` with characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: `format must be png or svg`, `size must be a number`, `ecl must be M, Q or H`. An unknown unit returns **404** with message `Battery unit <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: BatteryUnit UUID (primary key). Serial numbers are NOT accepted here. (required)
        :type id: str
        :param format: Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).
        :type format: str
        :param size: Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.
        :type size: int
        :param ecl: QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`).
        :type ecl: str
        :param hri: When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).
        :type hri: bool
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

        _param = self._get_battery_unit_qr_code_serialize(
            id=id,
            format=format,
            size=size,
            ecl=ecl,
            hri=hri,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': None,
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
    def get_battery_unit_qr_code_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="BatteryUnit UUID (primary key). Serial numbers are NOT accepted here.")],
        format: Annotated[Optional[StrictStr], Field(description="Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).")] = None,
        size: Annotated[Optional[Annotated[int, Field(le=2048, strict=True, ge=128)]], Field(description="Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.")] = None,
        ecl: Annotated[Optional[StrictStr], Field(description="QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`).")] = None,
        hri: Annotated[Optional[StrictBool], Field(description="When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).")] = None,
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
        """Export a print-grade QR code for an individual battery unit

        Renders the battery unit's GS1 Digital Link URI as a print-grade QR code — the AI-21 path segment carries the unit's **real physical serial number** (e.g. `https://opendpp-node.eu/01/09501101530003/21/BAT-2026-000123`). This is the carrier each individual battery must wear (per-unit passports, per the EU Battery Regulation).  **Permission:** `battery:read` (read-only — subscription status is **not** checked on `:read` permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session.  **Identifier resolution:** `{id}` is the BatteryUnit **UUID only** — unlike the passport QR route there is **no** serial-number fallback. Lookup is tenant-scoped. Credentials scoped to an Economic Operator receive **403** (`Your access is restricted to Economic Operator: <operatorId>`) when the unit's parent passport belongs to a different operator.  **QR rendering:** identical pipeline to the passport QR export — 4-module quiet zone, `ecl` default `Q`, `size` clamped to 128–2048 px (clamped, not rejected; fractions truncated). The response carries `Content-Disposition: attachment; filename=\"qr-<serialNumber>.png\"` (or `.svg`); the filename base is the unit's `serialNumber` with characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: `format must be png or svg`, `size must be a number`, `ecl must be M, Q or H`. An unknown unit returns **404** with message `Battery unit <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: BatteryUnit UUID (primary key). Serial numbers are NOT accepted here. (required)
        :type id: str
        :param format: Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).
        :type format: str
        :param size: Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.
        :type size: int
        :param ecl: QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`).
        :type ecl: str
        :param hri: When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).
        :type hri: bool
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

        _param = self._get_battery_unit_qr_code_serialize(
            id=id,
            format=format,
            size=size,
            ecl=ecl,
            hri=hri,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': None,
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
    def get_battery_unit_qr_code_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="BatteryUnit UUID (primary key). Serial numbers are NOT accepted here.")],
        format: Annotated[Optional[StrictStr], Field(description="Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).")] = None,
        size: Annotated[Optional[Annotated[int, Field(le=2048, strict=True, ge=128)]], Field(description="Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.")] = None,
        ecl: Annotated[Optional[StrictStr], Field(description="QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`).")] = None,
        hri: Annotated[Optional[StrictBool], Field(description="When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).")] = None,
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
        """Export a print-grade QR code for an individual battery unit

        Renders the battery unit's GS1 Digital Link URI as a print-grade QR code — the AI-21 path segment carries the unit's **real physical serial number** (e.g. `https://opendpp-node.eu/01/09501101530003/21/BAT-2026-000123`). This is the carrier each individual battery must wear (per-unit passports, per the EU Battery Regulation).  **Permission:** `battery:read` (read-only — subscription status is **not** checked on `:read` permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session.  **Identifier resolution:** `{id}` is the BatteryUnit **UUID only** — unlike the passport QR route there is **no** serial-number fallback. Lookup is tenant-scoped. Credentials scoped to an Economic Operator receive **403** (`Your access is restricted to Economic Operator: <operatorId>`) when the unit's parent passport belongs to a different operator.  **QR rendering:** identical pipeline to the passport QR export — 4-module quiet zone, `ecl` default `Q`, `size` clamped to 128–2048 px (clamped, not rejected; fractions truncated). The response carries `Content-Disposition: attachment; filename=\"qr-<serialNumber>.png\"` (or `.svg`); the filename base is the unit's `serialNumber` with characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: `format must be png or svg`, `size must be a number`, `ecl must be M, Q or H`. An unknown unit returns **404** with message `Battery unit <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: BatteryUnit UUID (primary key). Serial numbers are NOT accepted here. (required)
        :type id: str
        :param format: Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).
        :type format: str
        :param size: Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.
        :type size: int
        :param ecl: QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`).
        :type ecl: str
        :param hri: When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).
        :type hri: bool
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

        _param = self._get_battery_unit_qr_code_serialize(
            id=id,
            format=format,
            size=size,
            ecl=ecl,
            hri=hri,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': None,
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


    def _get_battery_unit_qr_code_serialize(
        self,
        id,
        format,
        size,
        ecl,
        hri,
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
        if format is not None:
            
            _query_params.append(('format', format))
            
        if size is not None:
            
            _query_params.append(('size', size))
            
        if ecl is not None:
            
            _query_params.append(('ecl', ecl))
            
        if hri is not None:
            
            _query_params.append(('hri', hri))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'image/png', 
                    'image/svg+xml', 
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/units/{id}/qr',
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
    def get_passport_qr_code(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID, or the caller-supplied `productId` (GTIN-14/GRAI/SKU) as a fallback. Resolution is tenant-scoped.")],
        format: Annotated[Optional[StrictStr], Field(description="Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).")] = None,
        size: Annotated[Optional[Annotated[int, Field(le=2048, strict=True, ge=128)]], Field(description="Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.")] = None,
        ecl: Annotated[Optional[StrictStr], Field(description="QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`). `L` is intentionally not offered.")] = None,
        hri: Annotated[Optional[StrictBool], Field(description="When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).")] = None,
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
        """Export a print-grade GS1 Digital Link QR code for a passport

        Renders the passport's GS1 Digital Link URI (its `digitalLinkUri`, e.g. `https://opendpp-node.eu/01/09501101530003`) as a print-grade QR code and returns it as a binary file download. The printed carrier resolves through the public GS1 gateway.  **Permission:** `passport:read` (read-only — subscription status is **not** checked on `:read` permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session — plain same-origin `<a href>` downloads are supported.  **Identifier resolution:** `{id}` is matched first against the passport UUID, then against the caller-supplied `productId` (GTIN-14/GRAI/SKU), always scoped to your tenant. Credentials scoped to an Economic Operator receive **403** (`Your access is restricted to Economic Operator: <operatorId>`) when the passport belongs to a different operator.  **QR rendering:** 4-module quiet zone (GS1 guidance); error-correction level per `ecl` (default `Q`, the GS1 recommendation for product labels); `size` is **clamped** to 128–2048 px — out-of-range values are clamped to the nearest bound, not rejected, and fractional values are truncated. The response carries `Content-Disposition: attachment; filename=\"qr-<productId>.png\"` (or `.svg`); the filename base is the passport's `productId` with characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: `format must be png or svg`, `size must be a number`, `ecl must be M, Q or H`. An unknown passport returns **404** with message `Passport <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID, or the caller-supplied `productId` (GTIN-14/GRAI/SKU) as a fallback. Resolution is tenant-scoped. (required)
        :type id: str
        :param format: Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).
        :type format: str
        :param size: Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.
        :type size: int
        :param ecl: QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`). `L` is intentionally not offered.
        :type ecl: str
        :param hri: When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).
        :type hri: bool
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

        _param = self._get_passport_qr_code_serialize(
            id=id,
            format=format,
            size=size,
            ecl=ecl,
            hri=hri,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': None,
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
    def get_passport_qr_code_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID, or the caller-supplied `productId` (GTIN-14/GRAI/SKU) as a fallback. Resolution is tenant-scoped.")],
        format: Annotated[Optional[StrictStr], Field(description="Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).")] = None,
        size: Annotated[Optional[Annotated[int, Field(le=2048, strict=True, ge=128)]], Field(description="Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.")] = None,
        ecl: Annotated[Optional[StrictStr], Field(description="QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`). `L` is intentionally not offered.")] = None,
        hri: Annotated[Optional[StrictBool], Field(description="When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).")] = None,
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
        """Export a print-grade GS1 Digital Link QR code for a passport

        Renders the passport's GS1 Digital Link URI (its `digitalLinkUri`, e.g. `https://opendpp-node.eu/01/09501101530003`) as a print-grade QR code and returns it as a binary file download. The printed carrier resolves through the public GS1 gateway.  **Permission:** `passport:read` (read-only — subscription status is **not** checked on `:read` permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session — plain same-origin `<a href>` downloads are supported.  **Identifier resolution:** `{id}` is matched first against the passport UUID, then against the caller-supplied `productId` (GTIN-14/GRAI/SKU), always scoped to your tenant. Credentials scoped to an Economic Operator receive **403** (`Your access is restricted to Economic Operator: <operatorId>`) when the passport belongs to a different operator.  **QR rendering:** 4-module quiet zone (GS1 guidance); error-correction level per `ecl` (default `Q`, the GS1 recommendation for product labels); `size` is **clamped** to 128–2048 px — out-of-range values are clamped to the nearest bound, not rejected, and fractional values are truncated. The response carries `Content-Disposition: attachment; filename=\"qr-<productId>.png\"` (or `.svg`); the filename base is the passport's `productId` with characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: `format must be png or svg`, `size must be a number`, `ecl must be M, Q or H`. An unknown passport returns **404** with message `Passport <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID, or the caller-supplied `productId` (GTIN-14/GRAI/SKU) as a fallback. Resolution is tenant-scoped. (required)
        :type id: str
        :param format: Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).
        :type format: str
        :param size: Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.
        :type size: int
        :param ecl: QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`). `L` is intentionally not offered.
        :type ecl: str
        :param hri: When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).
        :type hri: bool
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

        _param = self._get_passport_qr_code_serialize(
            id=id,
            format=format,
            size=size,
            ecl=ecl,
            hri=hri,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': None,
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
    def get_passport_qr_code_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Passport UUID, or the caller-supplied `productId` (GTIN-14/GRAI/SKU) as a fallback. Resolution is tenant-scoped.")],
        format: Annotated[Optional[StrictStr], Field(description="Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).")] = None,
        size: Annotated[Optional[Annotated[int, Field(le=2048, strict=True, ge=128)]], Field(description="Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.")] = None,
        ecl: Annotated[Optional[StrictStr], Field(description="QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`). `L` is intentionally not offered.")] = None,
        hri: Annotated[Optional[StrictBool], Field(description="When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).")] = None,
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
        """Export a print-grade GS1 Digital Link QR code for a passport

        Renders the passport's GS1 Digital Link URI (its `digitalLinkUri`, e.g. `https://opendpp-node.eu/01/09501101530003`) as a print-grade QR code and returns it as a binary file download. The printed carrier resolves through the public GS1 gateway.  **Permission:** `passport:read` (read-only — subscription status is **not** checked on `:read` permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session — plain same-origin `<a href>` downloads are supported.  **Identifier resolution:** `{id}` is matched first against the passport UUID, then against the caller-supplied `productId` (GTIN-14/GRAI/SKU), always scoped to your tenant. Credentials scoped to an Economic Operator receive **403** (`Your access is restricted to Economic Operator: <operatorId>`) when the passport belongs to a different operator.  **QR rendering:** 4-module quiet zone (GS1 guidance); error-correction level per `ecl` (default `Q`, the GS1 recommendation for product labels); `size` is **clamped** to 128–2048 px — out-of-range values are clamped to the nearest bound, not rejected, and fractional values are truncated. The response carries `Content-Disposition: attachment; filename=\"qr-<productId>.png\"` (or `.svg`); the filename base is the passport's `productId` with characters outside `[A-Za-z0-9._-]` replaced by `_`, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: `format must be png or svg`, `size must be a number`, `ecl must be M, Q or H`. An unknown passport returns **404** with message `Passport <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Passport UUID, or the caller-supplied `productId` (GTIN-14/GRAI/SKU) as a fallback. Resolution is tenant-scoped. (required)
        :type id: str
        :param format: Output image format. Case-insensitive; any other value returns 400 (`format must be png or svg`).
        :type format: str
        :param size: Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (`size must be a number`). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response.
        :type size: int
        :param ecl: QR error-correction level: `M` (~15% recovery), `Q` (~25%, GS1 product-label guidance, default) or `H` (~30%). Case-insensitive; any other value returns 400 (`ecl must be M, Q or H`). `L` is intentionally not offered.
        :type ecl: str
        :param hri: When `1`/`true`, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. `(01) 09501101530003 (21) BAT-2026-000123`) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires `format=svg`; combining it with `format=png` returns 400 (`hri (Human-Readable Interpretation) labels require format=svg`).
        :type hri: bool
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

        _param = self._get_passport_qr_code_serialize(
            id=id,
            format=format,
            size=size,
            ecl=ecl,
            hri=hri,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': None,
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


    def _get_passport_qr_code_serialize(
        self,
        id,
        format,
        size,
        ecl,
        hri,
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
        if format is not None:
            
            _query_params.append(('format', format))
            
        if size is not None:
            
            _query_params.append(('size', size))
            
        if ecl is not None:
            
            _query_params.append(('ecl', ecl))
            
        if hri is not None:
            
            _query_params.append(('hri', hri))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'image/png', 
                    'image/svg+xml', 
                    'application/json'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
            'ApiKeyAuth'
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/passports/{id}/qr',
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


