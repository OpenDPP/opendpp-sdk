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

from pydantic import Field, StrictStr
from typing import Optional
from typing_extensions import Annotated
from opendpp_sdk.models.facility_create_request import FacilityCreateRequest
from opendpp_sdk.models.facility_created_envelope import FacilityCreatedEnvelope
from opendpp_sdk.models.facility_deleted_envelope import FacilityDeletedEnvelope
from opendpp_sdk.models.facility_envelope import FacilityEnvelope
from opendpp_sdk.models.facility_list_envelope import FacilityListEnvelope
from opendpp_sdk.models.facility_update_request import FacilityUpdateRequest

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class FacilitiesApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def create_facility(
        self,
        facility_create_request: FacilityCreateRequest,
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
    ) -> FacilityCreatedEnvelope:
        """Register a facility (GS1 GLN)

        Registers a manufacturing/processing facility as tenant-scoped master data, backing the Unique Facility Identifier (UFI, EN 18219). Passports reference facilities via `facilityId`.  **Permission:** `facility:write`. Authenticate with a Bearer API key (`op_dpp_token_…`) or a session JWT; cookie-authenticated sessions must also send the `X-CSRF-Token` header (double-submit against the `opendpp_csrf` cookie) — Bearer clients are exempt. Write permissions are subscription-gated: a lapsed workspace subscription returns **402**.  **GLN validation:** `gln` is trimmed, then must be exactly 13 digits with a valid GS1 modulo-10 check digit (same weighting algorithm as GTIN). The GLN is unique **platform-wide** (database unique constraint), so a duplicate returns **409** even if the existing facility belongs to another tenant.  **Country:** `country` must match `^[A-Za-z]{2}$` after trimming and is stored uppercased.  **Operator binding:** if `operatorId` is supplied (non-empty), that Economic Operator must be bound to your tenant workspace, otherwise **403**. An empty/whitespace `operatorId` is stored as `null`. Requests authenticated with an **operator-scoped API key** may only attach facilities to their own operator: a mismatched `operatorId` returns **403**, and when omitted the key's operator id is applied automatically.  `activity`, `streetAddress`, `city` and `postalCode` are trimmed; empty/whitespace values are stored as `null`.  **Public/privileged field split:** the public **JSON-LD** passport document exposes `id`, `gln`, `name`, `activity` and `country` of a linked facility; the public **AAS** export emits only the GLN, name and country (`manufacturingFacilityGln` / `manufacturingFacilityName` / `manufacturingFacilityCountry`, plus the GLN as a `urn:gs1:gln:` global asset reference) — the facility `id` and `activity` are never emitted in AAS. `streetAddress`, `city` and `postalCode` are owner-only in both formats. This endpoint returns the full row to you as the owner.  Emits a `facility.created` audit event and an in-app notification.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param facility_create_request: (required)
        :type facility_create_request: FacilityCreateRequest
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

        _param = self._create_facility_serialize(
            facility_create_request=facility_create_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "FacilityCreatedEnvelope",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
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
    def create_facility_with_http_info(
        self,
        facility_create_request: FacilityCreateRequest,
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
    ) -> ApiResponse[FacilityCreatedEnvelope]:
        """Register a facility (GS1 GLN)

        Registers a manufacturing/processing facility as tenant-scoped master data, backing the Unique Facility Identifier (UFI, EN 18219). Passports reference facilities via `facilityId`.  **Permission:** `facility:write`. Authenticate with a Bearer API key (`op_dpp_token_…`) or a session JWT; cookie-authenticated sessions must also send the `X-CSRF-Token` header (double-submit against the `opendpp_csrf` cookie) — Bearer clients are exempt. Write permissions are subscription-gated: a lapsed workspace subscription returns **402**.  **GLN validation:** `gln` is trimmed, then must be exactly 13 digits with a valid GS1 modulo-10 check digit (same weighting algorithm as GTIN). The GLN is unique **platform-wide** (database unique constraint), so a duplicate returns **409** even if the existing facility belongs to another tenant.  **Country:** `country` must match `^[A-Za-z]{2}$` after trimming and is stored uppercased.  **Operator binding:** if `operatorId` is supplied (non-empty), that Economic Operator must be bound to your tenant workspace, otherwise **403**. An empty/whitespace `operatorId` is stored as `null`. Requests authenticated with an **operator-scoped API key** may only attach facilities to their own operator: a mismatched `operatorId` returns **403**, and when omitted the key's operator id is applied automatically.  `activity`, `streetAddress`, `city` and `postalCode` are trimmed; empty/whitespace values are stored as `null`.  **Public/privileged field split:** the public **JSON-LD** passport document exposes `id`, `gln`, `name`, `activity` and `country` of a linked facility; the public **AAS** export emits only the GLN, name and country (`manufacturingFacilityGln` / `manufacturingFacilityName` / `manufacturingFacilityCountry`, plus the GLN as a `urn:gs1:gln:` global asset reference) — the facility `id` and `activity` are never emitted in AAS. `streetAddress`, `city` and `postalCode` are owner-only in both formats. This endpoint returns the full row to you as the owner.  Emits a `facility.created` audit event and an in-app notification.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param facility_create_request: (required)
        :type facility_create_request: FacilityCreateRequest
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

        _param = self._create_facility_serialize(
            facility_create_request=facility_create_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "FacilityCreatedEnvelope",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
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
    def create_facility_without_preload_content(
        self,
        facility_create_request: FacilityCreateRequest,
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
        """Register a facility (GS1 GLN)

        Registers a manufacturing/processing facility as tenant-scoped master data, backing the Unique Facility Identifier (UFI, EN 18219). Passports reference facilities via `facilityId`.  **Permission:** `facility:write`. Authenticate with a Bearer API key (`op_dpp_token_…`) or a session JWT; cookie-authenticated sessions must also send the `X-CSRF-Token` header (double-submit against the `opendpp_csrf` cookie) — Bearer clients are exempt. Write permissions are subscription-gated: a lapsed workspace subscription returns **402**.  **GLN validation:** `gln` is trimmed, then must be exactly 13 digits with a valid GS1 modulo-10 check digit (same weighting algorithm as GTIN). The GLN is unique **platform-wide** (database unique constraint), so a duplicate returns **409** even if the existing facility belongs to another tenant.  **Country:** `country` must match `^[A-Za-z]{2}$` after trimming and is stored uppercased.  **Operator binding:** if `operatorId` is supplied (non-empty), that Economic Operator must be bound to your tenant workspace, otherwise **403**. An empty/whitespace `operatorId` is stored as `null`. Requests authenticated with an **operator-scoped API key** may only attach facilities to their own operator: a mismatched `operatorId` returns **403**, and when omitted the key's operator id is applied automatically.  `activity`, `streetAddress`, `city` and `postalCode` are trimmed; empty/whitespace values are stored as `null`.  **Public/privileged field split:** the public **JSON-LD** passport document exposes `id`, `gln`, `name`, `activity` and `country` of a linked facility; the public **AAS** export emits only the GLN, name and country (`manufacturingFacilityGln` / `manufacturingFacilityName` / `manufacturingFacilityCountry`, plus the GLN as a `urn:gs1:gln:` global asset reference) — the facility `id` and `activity` are never emitted in AAS. `streetAddress`, `city` and `postalCode` are owner-only in both formats. This endpoint returns the full row to you as the owner.  Emits a `facility.created` audit event and an in-app notification.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param facility_create_request: (required)
        :type facility_create_request: FacilityCreateRequest
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

        _param = self._create_facility_serialize(
            facility_create_request=facility_create_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "FacilityCreatedEnvelope",
            '400': "Error",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '409': "Error",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _create_facility_serialize(
        self,
        facility_create_request,
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
        if facility_create_request is not None:
            _body_params = facility_create_request


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
            resource_path='/api/v1/facilities',
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
    def delete_facility(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
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
    ) -> FacilityDeletedEnvelope:
        """Delete a facility (passports are unlinked, never deleted)

        Removes the facility master-data row. **Passports are never deleted by this operation**: `Passport.facilityId` is a `SET NULL` foreign key, so any passports referencing the facility simply lose their UFI link (`facilityId` becomes `null`) and remain fully intact and publicly resolvable.  **Permission:** `facility:write`. Cookie sessions must send `X-CSRF-Token`; write permissions are subscription-gated (**402** when lapsed).  **Operator-scoped keys:** deleting a facility that belongs to a different Economic Operator returns **403**; facilities with `operatorId: null` are deletable.  Emits a `facility.deleted` audit event and an in-app notification. **404 body:** standard envelope with message `Facility <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
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

        _param = self._delete_facility_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityDeletedEnvelope",
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
    def delete_facility_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
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
    ) -> ApiResponse[FacilityDeletedEnvelope]:
        """Delete a facility (passports are unlinked, never deleted)

        Removes the facility master-data row. **Passports are never deleted by this operation**: `Passport.facilityId` is a `SET NULL` foreign key, so any passports referencing the facility simply lose their UFI link (`facilityId` becomes `null`) and remain fully intact and publicly resolvable.  **Permission:** `facility:write`. Cookie sessions must send `X-CSRF-Token`; write permissions are subscription-gated (**402** when lapsed).  **Operator-scoped keys:** deleting a facility that belongs to a different Economic Operator returns **403**; facilities with `operatorId: null` are deletable.  Emits a `facility.deleted` audit event and an in-app notification. **404 body:** standard envelope with message `Facility <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
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

        _param = self._delete_facility_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityDeletedEnvelope",
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
    def delete_facility_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
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
        """Delete a facility (passports are unlinked, never deleted)

        Removes the facility master-data row. **Passports are never deleted by this operation**: `Passport.facilityId` is a `SET NULL` foreign key, so any passports referencing the facility simply lose their UFI link (`facilityId` becomes `null`) and remain fully intact and publicly resolvable.  **Permission:** `facility:write`. Cookie sessions must send `X-CSRF-Token`; write permissions are subscription-gated (**402** when lapsed).  **Operator-scoped keys:** deleting a facility that belongs to a different Economic Operator returns **403**; facilities with `operatorId: null` are deletable.  Emits a `facility.deleted` audit event and an in-app notification. **404 body:** standard envelope with message `Facility <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
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

        _param = self._delete_facility_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityDeletedEnvelope",
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


    def _delete_facility_serialize(
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
            resource_path='/api/v1/facilities/{id}',
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
    def get_facility(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
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
    ) -> FacilityEnvelope:
        """Get a single facility

        Fetches one facility by id, scoped to your tenant workspace.  **Permission:** `facility:read`.  **Operator-scoped keys:** if the facility belongs to a *different* Economic Operator than the key's scope, the response is **403**. Facilities with no operator (`operatorId: null`) **are** readable by operator-scoped keys here, even though they are excluded from the list endpoint.  Returns the full row including the privileged address fields (`streetAddress`, `city`, `postalCode`) that public passport documents never expose. (Public exposure of a linked facility: the JSON-LD document shows `id`/`gln`/`name`/`activity`/`country`; the AAS export only the GLN, name and country.)  **404 body:** standard envelope with message `Facility <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
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

        _param = self._get_facility_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityEnvelope",
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
    def get_facility_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
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
    ) -> ApiResponse[FacilityEnvelope]:
        """Get a single facility

        Fetches one facility by id, scoped to your tenant workspace.  **Permission:** `facility:read`.  **Operator-scoped keys:** if the facility belongs to a *different* Economic Operator than the key's scope, the response is **403**. Facilities with no operator (`operatorId: null`) **are** readable by operator-scoped keys here, even though they are excluded from the list endpoint.  Returns the full row including the privileged address fields (`streetAddress`, `city`, `postalCode`) that public passport documents never expose. (Public exposure of a linked facility: the JSON-LD document shows `id`/`gln`/`name`/`activity`/`country`; the AAS export only the GLN, name and country.)  **404 body:** standard envelope with message `Facility <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
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

        _param = self._get_facility_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityEnvelope",
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
    def get_facility_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
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
        """Get a single facility

        Fetches one facility by id, scoped to your tenant workspace.  **Permission:** `facility:read`.  **Operator-scoped keys:** if the facility belongs to a *different* Economic Operator than the key's scope, the response is **403**. Facilities with no operator (`operatorId: null`) **are** readable by operator-scoped keys here, even though they are excluded from the list endpoint.  Returns the full row including the privileged address fields (`streetAddress`, `city`, `postalCode`) that public passport documents never expose. (Public exposure of a linked facility: the JSON-LD document shows `id`/`gln`/`name`/`activity`/`country`; the AAS export only the GLN, name and country.)  **404 body:** standard envelope with message `Facility <id> not found under your Tenant workspace`.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
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

        _param = self._get_facility_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityEnvelope",
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


    def _get_facility_serialize(
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
            resource_path='/api/v1/facilities/{id}',
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
    def list_facilities(
        self,
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
    ) -> FacilityListEnvelope:
        """List facilities in the tenant workspace

        Lists all facilities registered under your tenant workspace, sorted by `createdAt` descending. **Paginated** with `?page` (default 1) and `?limit` (default 100, max 200); `count` is this page's size, `total`/`totalPages` describe the full set. A non-numeric `page`/`limit` falls back to its default.  **Permission:** `facility:read` (Bearer API key or session JWT/cookie).  **Operator-scoped keys:** when authenticated with an API key scoped to an Economic Operator, the list contains only facilities whose `operatorId` equals the key's operator — facilities with no operator (`operatorId: null`) are **excluded** from the list (they remain readable individually via `GET /api/v1/facilities/{id}`).  The full row is returned to the owner, including the privileged address fields (`streetAddress`, `city`, `postalCode`) that public passport documents never expose (owner-only in JSON-LD; never emitted in AAS).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

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

        _param = self._list_facilities_serialize(
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityListEnvelope",
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
    def list_facilities_with_http_info(
        self,
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
    ) -> ApiResponse[FacilityListEnvelope]:
        """List facilities in the tenant workspace

        Lists all facilities registered under your tenant workspace, sorted by `createdAt` descending. **Paginated** with `?page` (default 1) and `?limit` (default 100, max 200); `count` is this page's size, `total`/`totalPages` describe the full set. A non-numeric `page`/`limit` falls back to its default.  **Permission:** `facility:read` (Bearer API key or session JWT/cookie).  **Operator-scoped keys:** when authenticated with an API key scoped to an Economic Operator, the list contains only facilities whose `operatorId` equals the key's operator — facilities with no operator (`operatorId: null`) are **excluded** from the list (they remain readable individually via `GET /api/v1/facilities/{id}`).  The full row is returned to the owner, including the privileged address fields (`streetAddress`, `city`, `postalCode`) that public passport documents never expose (owner-only in JSON-LD; never emitted in AAS).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

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

        _param = self._list_facilities_serialize(
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityListEnvelope",
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
    def list_facilities_without_preload_content(
        self,
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
        """List facilities in the tenant workspace

        Lists all facilities registered under your tenant workspace, sorted by `createdAt` descending. **Paginated** with `?page` (default 1) and `?limit` (default 100, max 200); `count` is this page's size, `total`/`totalPages` describe the full set. A non-numeric `page`/`limit` falls back to its default.  **Permission:** `facility:read` (Bearer API key or session JWT/cookie).  **Operator-scoped keys:** when authenticated with an API key scoped to an Economic Operator, the list contains only facilities whose `operatorId` equals the key's operator — facilities with no operator (`operatorId: null`) are **excluded** from the list (they remain readable individually via `GET /api/v1/facilities/{id}`).  The full row is returned to the owner, including the privileged address fields (`streetAddress`, `city`, `postalCode`) that public passport documents never expose (owner-only in JSON-LD; never emitted in AAS).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

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

        _param = self._list_facilities_serialize(
            page=page,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityListEnvelope",
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


    def _list_facilities_serialize(
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
            resource_path='/api/v1/facilities',
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
    def update_facility(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
        facility_update_request: Optional[FacilityUpdateRequest] = None,
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
    ) -> FacilityEnvelope:
        """Update facility master data (GLN is immutable)

        Partially updates a facility's master data. **The GLN itself is immutable** — it is the resolvable UFI identifier; a `gln` key in the body is silently ignored (as is `operatorId` — the operator binding cannot be changed here).  **Permission:** `facility:write`. Cookie sessions must send `X-CSRF-Token`; write permissions are subscription-gated (**402** when lapsed).  **Field semantics (all optional):** - `name` — applied only when a non-empty string; an empty/whitespace or non-string value is silently ignored (`name` can never be cleared). - `activity`, `streetAddress`, `city`, `postalCode` — applied whenever the key is *present* in the body: the value is stringified and trimmed; anything that trims to empty (`null`, `\"\"`, or a whitespace-only string) **clears the field to null** — the same normalization as POST. - `country` — when present as a string it must match `^[A-Za-z]{2}$` (else **400**) and is stored uppercased; a non-string value is silently ignored.  An empty body (or one with no recognized fields) is accepted: the response is **200** with the otherwise-unchanged row, though `updatedAt` is still bumped.  **Operator-scoped keys:** updating a facility that belongs to a different Economic Operator returns **403**; facilities with `operatorId: null` are updatable.  Emits a `facility.updated` audit event recording the changed fields.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
        :type id: str
        :param facility_update_request:
        :type facility_update_request: FacilityUpdateRequest
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

        _param = self._update_facility_serialize(
            id=id,
            facility_update_request=facility_update_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityEnvelope",
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
    def update_facility_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
        facility_update_request: Optional[FacilityUpdateRequest] = None,
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
    ) -> ApiResponse[FacilityEnvelope]:
        """Update facility master data (GLN is immutable)

        Partially updates a facility's master data. **The GLN itself is immutable** — it is the resolvable UFI identifier; a `gln` key in the body is silently ignored (as is `operatorId` — the operator binding cannot be changed here).  **Permission:** `facility:write`. Cookie sessions must send `X-CSRF-Token`; write permissions are subscription-gated (**402** when lapsed).  **Field semantics (all optional):** - `name` — applied only when a non-empty string; an empty/whitespace or non-string value is silently ignored (`name` can never be cleared). - `activity`, `streetAddress`, `city`, `postalCode` — applied whenever the key is *present* in the body: the value is stringified and trimmed; anything that trims to empty (`null`, `\"\"`, or a whitespace-only string) **clears the field to null** — the same normalization as POST. - `country` — when present as a string it must match `^[A-Za-z]{2}$` (else **400**) and is stored uppercased; a non-string value is silently ignored.  An empty body (or one with no recognized fields) is accepted: the response is **200** with the otherwise-unchanged row, though `updatedAt` is still bumped.  **Operator-scoped keys:** updating a facility that belongs to a different Economic Operator returns **403**; facilities with `operatorId: null` are updatable.  Emits a `facility.updated` audit event recording the changed fields.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
        :type id: str
        :param facility_update_request:
        :type facility_update_request: FacilityUpdateRequest
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

        _param = self._update_facility_serialize(
            id=id,
            facility_update_request=facility_update_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityEnvelope",
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
    def update_facility_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404.")],
        facility_update_request: Optional[FacilityUpdateRequest] = None,
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
        """Update facility master data (GLN is immutable)

        Partially updates a facility's master data. **The GLN itself is immutable** — it is the resolvable UFI identifier; a `gln` key in the body is silently ignored (as is `operatorId` — the operator binding cannot be changed here).  **Permission:** `facility:write`. Cookie sessions must send `X-CSRF-Token`; write permissions are subscription-gated (**402** when lapsed).  **Field semantics (all optional):** - `name` — applied only when a non-empty string; an empty/whitespace or non-string value is silently ignored (`name` can never be cleared). - `activity`, `streetAddress`, `city`, `postalCode` — applied whenever the key is *present* in the body: the value is stringified and trimmed; anything that trims to empty (`null`, `\"\"`, or a whitespace-only string) **clears the field to null** — the same normalization as POST. - `country` — when present as a string it must match `^[A-Za-z]{2}$` (else **400**) and is stored uppercased; a non-string value is silently ignored.  An empty body (or one with no recognized fields) is accepted: the response is **200** with the otherwise-unchanged row, though `updatedAt` is still bumped.  **Operator-scoped keys:** updating a facility that belongs to a different Economic Operator returns **403**; facilities with `operatorId: null` are updatable.  Emits a `facility.updated` audit event recording the changed fields.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Facility id (UUID, as returned at registration). Non-existent or other-tenant ids return 404. (required)
        :type id: str
        :param facility_update_request:
        :type facility_update_request: FacilityUpdateRequest
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

        _param = self._update_facility_serialize(
            id=id,
            facility_update_request=facility_update_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "FacilityEnvelope",
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


    def _update_facility_serialize(
        self,
        id,
        facility_update_request,
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
        if facility_update_request is not None:
            _body_params = facility_update_request


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
            resource_path='/api/v1/facilities/{id}',
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


