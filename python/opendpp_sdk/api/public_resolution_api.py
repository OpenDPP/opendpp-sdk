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

from pydantic import Field, StrictStr, field_validator
from typing import Optional
from typing_extensions import Annotated
from opendpp_sdk.models.decode_gs1200_response import DecodeGs1200Response
from opendpp_sdk.models.decode_gs1_batch200_response import DecodeGs1Batch200Response
from opendpp_sdk.models.decode_gs1_batch_request import DecodeGs1BatchRequest
from opendpp_sdk.models.decode_gs1_request import DecodeGs1Request
from opendpp_sdk.models.mint_gtin_check_digit200_response import MintGtinCheckDigit200Response
from opendpp_sdk.models.mint_gtin_check_digit_request import MintGtinCheckDigitRequest
from opendpp_sdk.models.public_battery_unit_json_ld import PublicBatteryUnitJsonLd
from opendpp_sdk.models.public_passport_json_ld import PublicPassportJsonLd

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class PublicResolutionApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def decode_gs1(
        self,
        decode_gs1_request: DecodeGs1Request,
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
    ) -> DecodeGs1200Response:
        """Decode GS1 scan data / element string / Digital Link into structured AIs + HRI

        Decodes raw scanner output — AIM-symbology-prefixed **scan data** (e.g. `]Q1https://id.gs1.org/01/09501101532007/21/VM-1`, `]C1010950…`), a bracketed GS1 **element string** (`(01)09501101532007(21)VM-1`), or a **Digital Link** URI — into its structured Application Identifiers, the Human-Readable Interpretation (HRI), and a Digital Link that resolves on this node. Parsing is performed by GS1's authoritative Barcode Syntax Engine (vendored WASM), so check digits and the AI grammar are validated, not approximated.  **Public + stateless** — no permission and no tenant data is touched; it complements the public resolver. Supply **exactly one** of `scanData`, `elementString`, `digitalLink`; zero or more than one returns 400. After decoding, `GET` the returned `digitalLinkUri` (the canonical path rehosted on this node) to resolve the passport/unit.  **Errors:** missing/multiple/over-long input, or a value GS1's grammar rejects, returns **400** (`Provide exactly one of: scanData, elementString, digitalLink` or `Not a valid GS1 <kind>: <engine message>`); **503** if the engine is unavailable.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling, so this endpoint stays a convenience for integrators rather than a free scripted service. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers on both paths; **429** with `retry-after` when exceeded.

        :param decode_gs1_request: (required)
        :type decode_gs1_request: DecodeGs1Request
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

        _param = self._decode_gs1_serialize(
            decode_gs1_request=decode_gs1_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DecodeGs1200Response",
            '400': "Error",
            '429': "InlineObject",
            '503': "Error",
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
    def decode_gs1_with_http_info(
        self,
        decode_gs1_request: DecodeGs1Request,
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
    ) -> ApiResponse[DecodeGs1200Response]:
        """Decode GS1 scan data / element string / Digital Link into structured AIs + HRI

        Decodes raw scanner output — AIM-symbology-prefixed **scan data** (e.g. `]Q1https://id.gs1.org/01/09501101532007/21/VM-1`, `]C1010950…`), a bracketed GS1 **element string** (`(01)09501101532007(21)VM-1`), or a **Digital Link** URI — into its structured Application Identifiers, the Human-Readable Interpretation (HRI), and a Digital Link that resolves on this node. Parsing is performed by GS1's authoritative Barcode Syntax Engine (vendored WASM), so check digits and the AI grammar are validated, not approximated.  **Public + stateless** — no permission and no tenant data is touched; it complements the public resolver. Supply **exactly one** of `scanData`, `elementString`, `digitalLink`; zero or more than one returns 400. After decoding, `GET` the returned `digitalLinkUri` (the canonical path rehosted on this node) to resolve the passport/unit.  **Errors:** missing/multiple/over-long input, or a value GS1's grammar rejects, returns **400** (`Provide exactly one of: scanData, elementString, digitalLink` or `Not a valid GS1 <kind>: <engine message>`); **503** if the engine is unavailable.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling, so this endpoint stays a convenience for integrators rather than a free scripted service. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers on both paths; **429** with `retry-after` when exceeded.

        :param decode_gs1_request: (required)
        :type decode_gs1_request: DecodeGs1Request
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

        _param = self._decode_gs1_serialize(
            decode_gs1_request=decode_gs1_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DecodeGs1200Response",
            '400': "Error",
            '429': "InlineObject",
            '503': "Error",
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
    def decode_gs1_without_preload_content(
        self,
        decode_gs1_request: DecodeGs1Request,
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
        """Decode GS1 scan data / element string / Digital Link into structured AIs + HRI

        Decodes raw scanner output — AIM-symbology-prefixed **scan data** (e.g. `]Q1https://id.gs1.org/01/09501101532007/21/VM-1`, `]C1010950…`), a bracketed GS1 **element string** (`(01)09501101532007(21)VM-1`), or a **Digital Link** URI — into its structured Application Identifiers, the Human-Readable Interpretation (HRI), and a Digital Link that resolves on this node. Parsing is performed by GS1's authoritative Barcode Syntax Engine (vendored WASM), so check digits and the AI grammar are validated, not approximated.  **Public + stateless** — no permission and no tenant data is touched; it complements the public resolver. Supply **exactly one** of `scanData`, `elementString`, `digitalLink`; zero or more than one returns 400. After decoding, `GET` the returned `digitalLinkUri` (the canonical path rehosted on this node) to resolve the passport/unit.  **Errors:** missing/multiple/over-long input, or a value GS1's grammar rejects, returns **400** (`Provide exactly one of: scanData, elementString, digitalLink` or `Not a valid GS1 <kind>: <engine message>`); **503** if the engine is unavailable.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling, so this endpoint stays a convenience for integrators rather than a free scripted service. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers on both paths; **429** with `retry-after` when exceeded.

        :param decode_gs1_request: (required)
        :type decode_gs1_request: DecodeGs1Request
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

        _param = self._decode_gs1_serialize(
            decode_gs1_request=decode_gs1_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DecodeGs1200Response",
            '400': "Error",
            '429': "InlineObject",
            '503': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _decode_gs1_serialize(
        self,
        decode_gs1_request,
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
        if decode_gs1_request is not None:
            _body_params = decode_gs1_request


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
            resource_path='/api/v1/gs1/decode',
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
    def decode_gs1_batch(
        self,
        decode_gs1_batch_request: DecodeGs1BatchRequest,
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
    ) -> DecodeGs1Batch200Response:
        """Batch-decode many GS1 scans / element strings / Digital Links in one request

        Batch form of `POST /api/v1/gs1/decode` for line-side / warehouse stations capturing many scans per second. Send `{ \"items\": [ … ] }` (≤200), each item exactly one of `scanData`/`elementString`/`digitalLink`, and receive a `results` array aligned to input order — each entry either a decoded scan (`ok: true`, the same fields as the single-scan 200 minus `success`) or an error (`ok: false` + `error`). **Partial-success:** one bad item never fails the batch — the request returns **200** and per-item failures are reported in place. Parsing uses GS1's authoritative Barcode Syntax Engine (vendored WASM). **Public + stateless** (no permission, no tenant data).  **Errors:** a missing/empty/non-array `items`, or more than 200 items, returns **400**; a body over the 256 KiB route cap returns **413**; **503** if the engine is unavailable.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling, so this endpoint stays a convenience for integrators rather than a free scripted service. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers on both paths; **429** with `retry-after` when exceeded.

        :param decode_gs1_batch_request: (required)
        :type decode_gs1_batch_request: DecodeGs1BatchRequest
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

        _param = self._decode_gs1_batch_serialize(
            decode_gs1_batch_request=decode_gs1_batch_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DecodeGs1Batch200Response",
            '400': "Error",
            '429': "InlineObject",
            '503': "Error",
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
    def decode_gs1_batch_with_http_info(
        self,
        decode_gs1_batch_request: DecodeGs1BatchRequest,
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
    ) -> ApiResponse[DecodeGs1Batch200Response]:
        """Batch-decode many GS1 scans / element strings / Digital Links in one request

        Batch form of `POST /api/v1/gs1/decode` for line-side / warehouse stations capturing many scans per second. Send `{ \"items\": [ … ] }` (≤200), each item exactly one of `scanData`/`elementString`/`digitalLink`, and receive a `results` array aligned to input order — each entry either a decoded scan (`ok: true`, the same fields as the single-scan 200 minus `success`) or an error (`ok: false` + `error`). **Partial-success:** one bad item never fails the batch — the request returns **200** and per-item failures are reported in place. Parsing uses GS1's authoritative Barcode Syntax Engine (vendored WASM). **Public + stateless** (no permission, no tenant data).  **Errors:** a missing/empty/non-array `items`, or more than 200 items, returns **400**; a body over the 256 KiB route cap returns **413**; **503** if the engine is unavailable.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling, so this endpoint stays a convenience for integrators rather than a free scripted service. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers on both paths; **429** with `retry-after` when exceeded.

        :param decode_gs1_batch_request: (required)
        :type decode_gs1_batch_request: DecodeGs1BatchRequest
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

        _param = self._decode_gs1_batch_serialize(
            decode_gs1_batch_request=decode_gs1_batch_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DecodeGs1Batch200Response",
            '400': "Error",
            '429': "InlineObject",
            '503': "Error",
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
    def decode_gs1_batch_without_preload_content(
        self,
        decode_gs1_batch_request: DecodeGs1BatchRequest,
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
        """Batch-decode many GS1 scans / element strings / Digital Links in one request

        Batch form of `POST /api/v1/gs1/decode` for line-side / warehouse stations capturing many scans per second. Send `{ \"items\": [ … ] }` (≤200), each item exactly one of `scanData`/`elementString`/`digitalLink`, and receive a `results` array aligned to input order — each entry either a decoded scan (`ok: true`, the same fields as the single-scan 200 minus `success`) or an error (`ok: false` + `error`). **Partial-success:** one bad item never fails the batch — the request returns **200** and per-item failures are reported in place. Parsing uses GS1's authoritative Barcode Syntax Engine (vendored WASM). **Public + stateless** (no permission, no tenant data).  **Errors:** a missing/empty/non-array `items`, or more than 200 items, returns **400**; a body over the 256 KiB route cap returns **413**; **503** if the engine is unavailable.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling, so this endpoint stays a convenience for integrators rather than a free scripted service. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers on both paths; **429** with `retry-after` when exceeded.

        :param decode_gs1_batch_request: (required)
        :type decode_gs1_batch_request: DecodeGs1BatchRequest
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

        _param = self._decode_gs1_batch_serialize(
            decode_gs1_batch_request=decode_gs1_batch_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DecodeGs1Batch200Response",
            '400': "Error",
            '429': "InlineObject",
            '503': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _decode_gs1_batch_serialize(
        self,
        decode_gs1_batch_request,
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
        if decode_gs1_batch_request is not None:
            _body_params = decode_gs1_batch_request


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
            resource_path='/api/v1/gs1/decode/batch',
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
    def mint_gtin_check_digit(
        self,
        mint_gtin_check_digit_request: MintGtinCheckDigitRequest,
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
    ) -> MintGtinCheckDigit200Response:
        """Compute a GTIN check digit from a company prefix + item reference

        The actionable counterpart to the non-GS1 ingest advisory: given the **GS1 company prefix your organisation legally owns** plus an item reference, OpenDPP computes the GS1 **mod-10 check digit** and returns the resulting 14-digit GTIN + a Digital Link preview. Set the GTIN as a passport `productId` to get a scannable GS1 Digital Link.  **It ONLY completes the check digit** — it never allocates a GS1 company prefix or asserts ownership (a real GTIN requires a prefix licensed to you by GS1). `gs1CompanyPrefix` is REQUIRED; a request with none is refused (**400**). `gs1CompanyPrefix + itemRef` must be exactly **13 digits** (the check digit forms the 14th) and both must be digit strings, else **400**.  Public + stateless (pure arithmetic; no tenant data). No authentication required.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers; **429** with `retry-after` when exceeded.

        :param mint_gtin_check_digit_request: (required)
        :type mint_gtin_check_digit_request: MintGtinCheckDigitRequest
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

        _param = self._mint_gtin_check_digit_serialize(
            mint_gtin_check_digit_request=mint_gtin_check_digit_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "MintGtinCheckDigit200Response",
            '400': "MintGtinCheckDigit400Response",
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
    def mint_gtin_check_digit_with_http_info(
        self,
        mint_gtin_check_digit_request: MintGtinCheckDigitRequest,
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
    ) -> ApiResponse[MintGtinCheckDigit200Response]:
        """Compute a GTIN check digit from a company prefix + item reference

        The actionable counterpart to the non-GS1 ingest advisory: given the **GS1 company prefix your organisation legally owns** plus an item reference, OpenDPP computes the GS1 **mod-10 check digit** and returns the resulting 14-digit GTIN + a Digital Link preview. Set the GTIN as a passport `productId` to get a scannable GS1 Digital Link.  **It ONLY completes the check digit** — it never allocates a GS1 company prefix or asserts ownership (a real GTIN requires a prefix licensed to you by GS1). `gs1CompanyPrefix` is REQUIRED; a request with none is refused (**400**). `gs1CompanyPrefix + itemRef` must be exactly **13 digits** (the check digit forms the 14th) and both must be digit strings, else **400**.  Public + stateless (pure arithmetic; no tenant data). No authentication required.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers; **429** with `retry-after` when exceeded.

        :param mint_gtin_check_digit_request: (required)
        :type mint_gtin_check_digit_request: MintGtinCheckDigitRequest
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

        _param = self._mint_gtin_check_digit_serialize(
            mint_gtin_check_digit_request=mint_gtin_check_digit_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "MintGtinCheckDigit200Response",
            '400': "MintGtinCheckDigit400Response",
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
    def mint_gtin_check_digit_without_preload_content(
        self,
        mint_gtin_check_digit_request: MintGtinCheckDigitRequest,
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
        """Compute a GTIN check digit from a company prefix + item reference

        The actionable counterpart to the non-GS1 ingest advisory: given the **GS1 company prefix your organisation legally owns** plus an item reference, OpenDPP computes the GS1 **mod-10 check digit** and returns the resulting 14-digit GTIN + a Digital Link preview. Set the GTIN as a passport `productId` to get a scannable GS1 Digital Link.  **It ONLY completes the check digit** — it never allocates a GS1 company prefix or asserts ownership (a real GTIN requires a prefix licensed to you by GS1). `gs1CompanyPrefix` is REQUIRED; a request with none is refused (**400**). `gs1CompanyPrefix + itemRef` must be exactly **13 digits** (the check digit forms the 14th) and both must be digit strings, else **400**.  Public + stateless (pure arithmetic; no tenant data). No authentication required.  **Rate limit (changed in 1.12.0):** an **anonymous** caller gets **2 requests/min per IP** — a per-route cap that replaces the global ceiling. Send an `Authorization` header (any valid API key) and the normal ladder applies instead: the global authenticated ceiling, with your per-key tier bucket underneath it. Standard `x-ratelimit-*` headers; **429** with `retry-after` when exceeded.

        :param mint_gtin_check_digit_request: (required)
        :type mint_gtin_check_digit_request: MintGtinCheckDigitRequest
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

        _param = self._mint_gtin_check_digit_serialize(
            mint_gtin_check_digit_request=mint_gtin_check_digit_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "MintGtinCheckDigit200Response",
            '400': "MintGtinCheckDigit400Response",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _mint_gtin_check_digit_serialize(
        self,
        mint_gtin_check_digit_request,
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
        if mint_gtin_check_digit_request is not None:
            _body_params = mint_gtin_check_digit_request


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
            resource_path='/api/v1/gs1/gtin',
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
    def resolve_gs1_grai(
        self,
        grai: Annotated[str, Field(min_length=14, strict=True, max_length=30, description="GRAI: a 14-digit numeric asset identifier with a valid GS1 modulo-10 check digit (validated server-side), followed by an optional alphanumeric serial component of up to 16 characters (total length 14-30).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.")] = None,
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
        """GS1 Digital Link resolution by GRAI (AI 8003)

        Unified GS1 Digital Link gateway, GRAI branch (Global Returnable Asset Identifier). The GRAI is matched against `metadata.gtin`, `metadata.grai`, or the passport's `productId`. Everything else — content negotiation (JSON-LD default / `application/aas+json` / `application/vc+jwt` / `application/vc+ld+json` / `application/dc+sd-jwt` / `text/html`, `Vary: Accept`), access tiers (public / grant `dpp_li_…`·`dpp_auth_…` via Bearer or `?grant=` / owner = tenant API key as Bearer, never a Console JWT session), DRAFT hiding, tenant-subdomain scoping, the no-tenant-scope ambiguity 400, access-audit logging, grant response headers, and the 30 req/min/IP rate limit (two-field 429 body without `success`; the limiter adds no headers of its own — `x-ratelimit-*` headers come from the global 100 req/min/IP limit, which applies on top) — is identical to `GET /01/{gtin14}`; see that operation and `GET /passport/{id}` for full semantics.  An additional `/21/{serial}` AI pair after the GRAI behaves exactly like `GET /01/{gtin14}/21/{serial}` (302 redirect to `/unit/{id}` or `/passport/{id}`). No permission string (public endpoint).

        :param grai: GRAI: a 14-digit numeric asset identifier with a valid GS1 modulo-10 check digit (validated server-side), followed by an optional alphanumeric serial component of up to 16 characters (total length 14-30). (required)
        :type grai: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.
        :type grant: str
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

        _param = self._resolve_gs1_grai_serialize(
            grai=grai,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
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
    def resolve_gs1_grai_with_http_info(
        self,
        grai: Annotated[str, Field(min_length=14, strict=True, max_length=30, description="GRAI: a 14-digit numeric asset identifier with a valid GS1 modulo-10 check digit (validated server-side), followed by an optional alphanumeric serial component of up to 16 characters (total length 14-30).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.")] = None,
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
        """GS1 Digital Link resolution by GRAI (AI 8003)

        Unified GS1 Digital Link gateway, GRAI branch (Global Returnable Asset Identifier). The GRAI is matched against `metadata.gtin`, `metadata.grai`, or the passport's `productId`. Everything else — content negotiation (JSON-LD default / `application/aas+json` / `application/vc+jwt` / `application/vc+ld+json` / `application/dc+sd-jwt` / `text/html`, `Vary: Accept`), access tiers (public / grant `dpp_li_…`·`dpp_auth_…` via Bearer or `?grant=` / owner = tenant API key as Bearer, never a Console JWT session), DRAFT hiding, tenant-subdomain scoping, the no-tenant-scope ambiguity 400, access-audit logging, grant response headers, and the 30 req/min/IP rate limit (two-field 429 body without `success`; the limiter adds no headers of its own — `x-ratelimit-*` headers come from the global 100 req/min/IP limit, which applies on top) — is identical to `GET /01/{gtin14}`; see that operation and `GET /passport/{id}` for full semantics.  An additional `/21/{serial}` AI pair after the GRAI behaves exactly like `GET /01/{gtin14}/21/{serial}` (302 redirect to `/unit/{id}` or `/passport/{id}`). No permission string (public endpoint).

        :param grai: GRAI: a 14-digit numeric asset identifier with a valid GS1 modulo-10 check digit (validated server-side), followed by an optional alphanumeric serial component of up to 16 characters (total length 14-30). (required)
        :type grai: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.
        :type grant: str
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

        _param = self._resolve_gs1_grai_serialize(
            grai=grai,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
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
    def resolve_gs1_grai_without_preload_content(
        self,
        grai: Annotated[str, Field(min_length=14, strict=True, max_length=30, description="GRAI: a 14-digit numeric asset identifier with a valid GS1 modulo-10 check digit (validated server-side), followed by an optional alphanumeric serial component of up to 16 characters (total length 14-30).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.")] = None,
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
        """GS1 Digital Link resolution by GRAI (AI 8003)

        Unified GS1 Digital Link gateway, GRAI branch (Global Returnable Asset Identifier). The GRAI is matched against `metadata.gtin`, `metadata.grai`, or the passport's `productId`. Everything else — content negotiation (JSON-LD default / `application/aas+json` / `application/vc+jwt` / `application/vc+ld+json` / `application/dc+sd-jwt` / `text/html`, `Vary: Accept`), access tiers (public / grant `dpp_li_…`·`dpp_auth_…` via Bearer or `?grant=` / owner = tenant API key as Bearer, never a Console JWT session), DRAFT hiding, tenant-subdomain scoping, the no-tenant-scope ambiguity 400, access-audit logging, grant response headers, and the 30 req/min/IP rate limit (two-field 429 body without `success`; the limiter adds no headers of its own — `x-ratelimit-*` headers come from the global 100 req/min/IP limit, which applies on top) — is identical to `GET /01/{gtin14}`; see that operation and `GET /passport/{id}` for full semantics.  An additional `/21/{serial}` AI pair after the GRAI behaves exactly like `GET /01/{gtin14}/21/{serial}` (302 redirect to `/unit/{id}` or `/passport/{id}`). No permission string (public endpoint).

        :param grai: GRAI: a 14-digit numeric asset identifier with a valid GS1 modulo-10 check digit (validated server-side), followed by an optional alphanumeric serial component of up to 16 characters (total length 14-30). (required)
        :type grai: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.
        :type grant: str
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

        _param = self._resolve_gs1_grai_serialize(
            grai=grai,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _resolve_gs1_grai_serialize(
        self,
        grai,
        grant,
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
        if grai is not None:
            _path_params['grai'] = grai
        # process the query parameters
        if grant is not None:
            
            _query_params.append(('grant', grant))
            
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
            resource_path='/8003/{grai}',
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
    def resolve_gs1_gtin(
        self,
        gtin14: Annotated[str, Field(strict=True, description="GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (the check digit is validated server-side — the pattern alone is not sufficient).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters, but the server matches any prefixed token against stored hashes, so the pattern is deliberately loose. Treat as a secret — grant-unlocked responses are `private, no-store` and the parameter is redacted from logs.")] = None,
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
        """GS1 Digital Link resolution by GTIN-14 (AI 01)

        Unified GS1 Digital Link gateway, GTIN branch. The GTIN-14 is matched against `metadata.gtin`, `metadata.grai`, or the passport's `productId`. On tenant workspaces (`https://{tenant}.opendpp-node.eu`) the lookup is scoped to that tenant — an unknown subdomain returns 404. Without a tenant scope, a GTIN matching more than one passport is rejected with 400 (ambiguous); disambiguate via a brand subdomain (the `?subdomain=` query override is honoured in non-production environments only).  Content negotiation (RFC 7231 §5.3.2 `Accept` q-value negotiation; JSON-LD default / `application/aas+json` / `application/vc+jwt` / `application/vc+ld+json` / `application/dc+sd-jwt` / `text/html`, `Vary: Accept` always set), access tiers (public / `dpp_li_…`·`dpp_auth_…` grant via Bearer or `?grant=` / owner = a tenant **API key** sent as a Bearer token — Console JWT login sessions do **not** unlock owner tier), DRAFT hiding, access-audit logging (anonymized IP), and grant response headers (`Cache-Control: private, no-store`, `Referrer-Policy: no-referrer`) are identical to `GET /passport/{id}` — see that operation for the full tier semantics. No permission string (public endpoint); invalid credentials silently degrade to the public tier, never 401/403.  The gateway also accepts additional GS1 AI key/value path pairs after the GTIN; the only one acted on is AI 21 (serial) — documented separately as `GET /01/{gtin14}/21/{serial}`. (The underlying route is `GET /{ai}/*`; AI prefixes other than `01` and `8003` get a 400.) This resolver handles only the **UNCOMPRESSED** GS1 Digital Link grammar; a **compressed** Digital Link (its AI data encoded as a base64url blob) is detected and rejected with a clear 400 that points to the uncompressed form.  **Rate limit:** 30 requests/min/IP; two-field 429 body without `success`. The limiter adds no headers of its own — `x-ratelimit-*` headers on responses come from the global platform limit (100 req/min/IP), which applies on top.

        :param gtin14: GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (the check digit is validated server-side — the pattern alone is not sufficient). (required)
        :type gtin14: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters, but the server matches any prefixed token against stored hashes, so the pattern is deliberately loose. Treat as a secret — grant-unlocked responses are `private, no-store` and the parameter is redacted from logs.
        :type grant: str
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

        _param = self._resolve_gs1_gtin_serialize(
            gtin14=gtin14,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
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
    def resolve_gs1_gtin_with_http_info(
        self,
        gtin14: Annotated[str, Field(strict=True, description="GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (the check digit is validated server-side — the pattern alone is not sufficient).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters, but the server matches any prefixed token against stored hashes, so the pattern is deliberately loose. Treat as a secret — grant-unlocked responses are `private, no-store` and the parameter is redacted from logs.")] = None,
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
        """GS1 Digital Link resolution by GTIN-14 (AI 01)

        Unified GS1 Digital Link gateway, GTIN branch. The GTIN-14 is matched against `metadata.gtin`, `metadata.grai`, or the passport's `productId`. On tenant workspaces (`https://{tenant}.opendpp-node.eu`) the lookup is scoped to that tenant — an unknown subdomain returns 404. Without a tenant scope, a GTIN matching more than one passport is rejected with 400 (ambiguous); disambiguate via a brand subdomain (the `?subdomain=` query override is honoured in non-production environments only).  Content negotiation (RFC 7231 §5.3.2 `Accept` q-value negotiation; JSON-LD default / `application/aas+json` / `application/vc+jwt` / `application/vc+ld+json` / `application/dc+sd-jwt` / `text/html`, `Vary: Accept` always set), access tiers (public / `dpp_li_…`·`dpp_auth_…` grant via Bearer or `?grant=` / owner = a tenant **API key** sent as a Bearer token — Console JWT login sessions do **not** unlock owner tier), DRAFT hiding, access-audit logging (anonymized IP), and grant response headers (`Cache-Control: private, no-store`, `Referrer-Policy: no-referrer`) are identical to `GET /passport/{id}` — see that operation for the full tier semantics. No permission string (public endpoint); invalid credentials silently degrade to the public tier, never 401/403.  The gateway also accepts additional GS1 AI key/value path pairs after the GTIN; the only one acted on is AI 21 (serial) — documented separately as `GET /01/{gtin14}/21/{serial}`. (The underlying route is `GET /{ai}/*`; AI prefixes other than `01` and `8003` get a 400.) This resolver handles only the **UNCOMPRESSED** GS1 Digital Link grammar; a **compressed** Digital Link (its AI data encoded as a base64url blob) is detected and rejected with a clear 400 that points to the uncompressed form.  **Rate limit:** 30 requests/min/IP; two-field 429 body without `success`. The limiter adds no headers of its own — `x-ratelimit-*` headers on responses come from the global platform limit (100 req/min/IP), which applies on top.

        :param gtin14: GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (the check digit is validated server-side — the pattern alone is not sufficient). (required)
        :type gtin14: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters, but the server matches any prefixed token against stored hashes, so the pattern is deliberately loose. Treat as a secret — grant-unlocked responses are `private, no-store` and the parameter is redacted from logs.
        :type grant: str
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

        _param = self._resolve_gs1_gtin_serialize(
            gtin14=gtin14,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
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
    def resolve_gs1_gtin_without_preload_content(
        self,
        gtin14: Annotated[str, Field(strict=True, description="GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (the check digit is validated server-side — the pattern alone is not sufficient).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters, but the server matches any prefixed token against stored hashes, so the pattern is deliberately loose. Treat as a secret — grant-unlocked responses are `private, no-store` and the parameter is redacted from logs.")] = None,
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
        """GS1 Digital Link resolution by GTIN-14 (AI 01)

        Unified GS1 Digital Link gateway, GTIN branch. The GTIN-14 is matched against `metadata.gtin`, `metadata.grai`, or the passport's `productId`. On tenant workspaces (`https://{tenant}.opendpp-node.eu`) the lookup is scoped to that tenant — an unknown subdomain returns 404. Without a tenant scope, a GTIN matching more than one passport is rejected with 400 (ambiguous); disambiguate via a brand subdomain (the `?subdomain=` query override is honoured in non-production environments only).  Content negotiation (RFC 7231 §5.3.2 `Accept` q-value negotiation; JSON-LD default / `application/aas+json` / `application/vc+jwt` / `application/vc+ld+json` / `application/dc+sd-jwt` / `text/html`, `Vary: Accept` always set), access tiers (public / `dpp_li_…`·`dpp_auth_…` grant via Bearer or `?grant=` / owner = a tenant **API key** sent as a Bearer token — Console JWT login sessions do **not** unlock owner tier), DRAFT hiding, access-audit logging (anonymized IP), and grant response headers (`Cache-Control: private, no-store`, `Referrer-Policy: no-referrer`) are identical to `GET /passport/{id}` — see that operation for the full tier semantics. No permission string (public endpoint); invalid credentials silently degrade to the public tier, never 401/403.  The gateway also accepts additional GS1 AI key/value path pairs after the GTIN; the only one acted on is AI 21 (serial) — documented separately as `GET /01/{gtin14}/21/{serial}`. (The underlying route is `GET /{ai}/*`; AI prefixes other than `01` and `8003` get a 400.) This resolver handles only the **UNCOMPRESSED** GS1 Digital Link grammar; a **compressed** Digital Link (its AI data encoded as a base64url blob) is detected and rejected with a clear 400 that points to the uncompressed form.  **Rate limit:** 30 requests/min/IP; two-field 429 body without `success`. The limiter adds no headers of its own — `x-ratelimit-*` headers on responses come from the global platform limit (100 req/min/IP), which applies on top.

        :param gtin14: GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (the check digit is validated server-side — the pattern alone is not sufficient). (required)
        :type gtin14: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters, but the server matches any prefixed token against stored hashes, so the pattern is deliberately loose. Treat as a secret — grant-unlocked responses are `private, no-store` and the parameter is redacted from logs.
        :type grant: str
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

        _param = self._resolve_gs1_gtin_serialize(
            gtin14=gtin14,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _resolve_gs1_gtin_serialize(
        self,
        gtin14,
        grant,
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
        if gtin14 is not None:
            _path_params['gtin14'] = gtin14
        # process the query parameters
        if grant is not None:
            
            _query_params.append(('grant', grant))
            
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
            resource_path='/01/{gtin14}',
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
    def resolve_gs1_gtin_serial(
        self,
        gtin14: Annotated[str, Field(strict=True, description="GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (validated server-side).")],
        serial: Annotated[StrictStr, Field(description="GS1 AI-21 serial. For serialised battery units this is the unit's physical serial (units are created matching `^[A-Za-z0-9._-]{1,20}$`); the legacy fallback also matches a passport UUID or the passport's `metadata.serialNumber` / `metadata[\"21\"]` value. Percent-encode reserved characters; the segment is URL-decoded before matching.")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token. Not evaluated by this redirect handler — it is preserved on the `Location` URL and takes effect at the redirect target.")] = None,
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
        """GS1 Digital Link serialised-item redirect (AI 01 + AI 21)

        GS1 Digital Link resolution of an *individual serialised item*. This path never returns a document directly — on success it issues a `302` redirect (the query string, including `?grant=`, is preserved on the `Location` URL):  1. If the GTIN resolves to a SKU/type passport that has a serialised battery unit whose `serialNumber` equals the AI-21 value → `302` to `/unit/{unitId}` (per-unit view). 2. Otherwise (legacy fallback) the AI-21 value is matched against the passport UUID, `metadata.serialNumber`, or `metadata[\"21\"]`; if a passport matches → `302` to `/passport/{passportId}`. 3. Otherwise → `404` (content-negotiated).  The ambiguity check of the bare-GTIN branch is skipped when an AI-21 serial is present. The redirect handler itself never evaluates credentials — access tiers (owner / grant / public) apply at the redirect target; carry the grant in `?grant=` (preserved across the redirect) or re-send the `Authorization` header to the target. On tenant subdomains the lookup is scoped to that tenant (unknown subdomain → 404, JSON only).  No permission string (public endpoint). **Rate limit:** 30 requests/min/IP (two-field 429 body without `success`). The limiter adds no headers of its own — `x-ratelimit-*` headers come from the global platform limit, which applies on top — and the redirect target counts as a second request against both.

        :param gtin14: GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (validated server-side). (required)
        :type gtin14: str
        :param serial: GS1 AI-21 serial. For serialised battery units this is the unit's physical serial (units are created matching `^[A-Za-z0-9._-]{1,20}$`); the legacy fallback also matches a passport UUID or the passport's `metadata.serialNumber` / `metadata[\"21\"]` value. Percent-encode reserved characters; the segment is URL-decoded before matching. (required)
        :type serial: str
        :param grant: Capability grant token. Not evaluated by this redirect handler — it is preserved on the `Location` URL and takes effect at the redirect target.
        :type grant: str
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

        _param = self._resolve_gs1_gtin_serial_serialize(
            gtin14=gtin14,
            serial=serial,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '302': None,
            '400': "Error",
            '404': "Error",
            '429': "Error",
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
    def resolve_gs1_gtin_serial_with_http_info(
        self,
        gtin14: Annotated[str, Field(strict=True, description="GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (validated server-side).")],
        serial: Annotated[StrictStr, Field(description="GS1 AI-21 serial. For serialised battery units this is the unit's physical serial (units are created matching `^[A-Za-z0-9._-]{1,20}$`); the legacy fallback also matches a passport UUID or the passport's `metadata.serialNumber` / `metadata[\"21\"]` value. Percent-encode reserved characters; the segment is URL-decoded before matching.")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token. Not evaluated by this redirect handler — it is preserved on the `Location` URL and takes effect at the redirect target.")] = None,
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
        """GS1 Digital Link serialised-item redirect (AI 01 + AI 21)

        GS1 Digital Link resolution of an *individual serialised item*. This path never returns a document directly — on success it issues a `302` redirect (the query string, including `?grant=`, is preserved on the `Location` URL):  1. If the GTIN resolves to a SKU/type passport that has a serialised battery unit whose `serialNumber` equals the AI-21 value → `302` to `/unit/{unitId}` (per-unit view). 2. Otherwise (legacy fallback) the AI-21 value is matched against the passport UUID, `metadata.serialNumber`, or `metadata[\"21\"]`; if a passport matches → `302` to `/passport/{passportId}`. 3. Otherwise → `404` (content-negotiated).  The ambiguity check of the bare-GTIN branch is skipped when an AI-21 serial is present. The redirect handler itself never evaluates credentials — access tiers (owner / grant / public) apply at the redirect target; carry the grant in `?grant=` (preserved across the redirect) or re-send the `Authorization` header to the target. On tenant subdomains the lookup is scoped to that tenant (unknown subdomain → 404, JSON only).  No permission string (public endpoint). **Rate limit:** 30 requests/min/IP (two-field 429 body without `success`). The limiter adds no headers of its own — `x-ratelimit-*` headers come from the global platform limit, which applies on top — and the redirect target counts as a second request against both.

        :param gtin14: GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (validated server-side). (required)
        :type gtin14: str
        :param serial: GS1 AI-21 serial. For serialised battery units this is the unit's physical serial (units are created matching `^[A-Za-z0-9._-]{1,20}$`); the legacy fallback also matches a passport UUID or the passport's `metadata.serialNumber` / `metadata[\"21\"]` value. Percent-encode reserved characters; the segment is URL-decoded before matching. (required)
        :type serial: str
        :param grant: Capability grant token. Not evaluated by this redirect handler — it is preserved on the `Location` URL and takes effect at the redirect target.
        :type grant: str
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

        _param = self._resolve_gs1_gtin_serial_serialize(
            gtin14=gtin14,
            serial=serial,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '302': None,
            '400': "Error",
            '404': "Error",
            '429': "Error",
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
    def resolve_gs1_gtin_serial_without_preload_content(
        self,
        gtin14: Annotated[str, Field(strict=True, description="GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (validated server-side).")],
        serial: Annotated[StrictStr, Field(description="GS1 AI-21 serial. For serialised battery units this is the unit's physical serial (units are created matching `^[A-Za-z0-9._-]{1,20}$`); the legacy fallback also matches a passport UUID or the passport's `metadata.serialNumber` / `metadata[\"21\"]` value. Percent-encode reserved characters; the segment is URL-decoded before matching.")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token. Not evaluated by this redirect handler — it is preserved on the `Location` URL and takes effect at the redirect target.")] = None,
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
        """GS1 Digital Link serialised-item redirect (AI 01 + AI 21)

        GS1 Digital Link resolution of an *individual serialised item*. This path never returns a document directly — on success it issues a `302` redirect (the query string, including `?grant=`, is preserved on the `Location` URL):  1. If the GTIN resolves to a SKU/type passport that has a serialised battery unit whose `serialNumber` equals the AI-21 value → `302` to `/unit/{unitId}` (per-unit view). 2. Otherwise (legacy fallback) the AI-21 value is matched against the passport UUID, `metadata.serialNumber`, or `metadata[\"21\"]`; if a passport matches → `302` to `/passport/{passportId}`. 3. Otherwise → `404` (content-negotiated).  The ambiguity check of the bare-GTIN branch is skipped when an AI-21 serial is present. The redirect handler itself never evaluates credentials — access tiers (owner / grant / public) apply at the redirect target; carry the grant in `?grant=` (preserved across the redirect) or re-send the `Authorization` header to the target. On tenant subdomains the lookup is scoped to that tenant (unknown subdomain → 404, JSON only).  No permission string (public endpoint). **Rate limit:** 30 requests/min/IP (two-field 429 body without `success`). The limiter adds no headers of its own — `x-ratelimit-*` headers come from the global platform limit, which applies on top — and the redirect target counts as a second request against both.

        :param gtin14: GTIN-14: exactly 14 digits with a valid GS1 modulo-10 check digit (validated server-side). (required)
        :type gtin14: str
        :param serial: GS1 AI-21 serial. For serialised battery units this is the unit's physical serial (units are created matching `^[A-Za-z0-9._-]{1,20}$`); the legacy fallback also matches a passport UUID or the passport's `metadata.serialNumber` / `metadata[\"21\"]` value. Percent-encode reserved characters; the segment is URL-decoded before matching. (required)
        :type serial: str
        :param grant: Capability grant token. Not evaluated by this redirect handler — it is preserved on the `Location` URL and takes effect at the redirect target.
        :type grant: str
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

        _param = self._resolve_gs1_gtin_serial_serialize(
            gtin14=gtin14,
            serial=serial,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '302': None,
            '400': "Error",
            '404': "Error",
            '429': "Error",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _resolve_gs1_gtin_serial_serialize(
        self,
        gtin14,
        serial,
        grant,
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
        if gtin14 is not None:
            _path_params['gtin14'] = gtin14
        if serial is not None:
            _path_params['serial'] = serial
        # process the query parameters
        if grant is not None:
            
            _query_params.append(('grant', grant))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/json', 
                    'text/html'
                ]
            )


        # authentication setting
        _auth_settings: List[str] = [
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/01/{gtin14}/21/{serial}',
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
    def resolve_public_battery_unit(
        self,
        id: Annotated[StrictStr, Field(description="The battery unit's server-assigned UUID (AI-21 serial resolution via `GET /01/{gtin14}/21/{serial}` redirects here).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.")] = None,
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
    ) -> PublicBatteryUnitJsonLd:
        """Resolve an individual serialised battery unit

        Public, content-negotiated view of one individual serialised unit (battery) by its unit UUID, including the embedded SKU/type passport (`ofModel`, masked by the same tier rules as `GET /passport/{id}`).  **Content negotiation:** `Accept` containing `application/vc+jwt` (or bare `vc+jwt`) → a signed PER-UNIT (item-granularity) UNTP DigitalProductPassport credential (public tier; `406 Not Acceptable` when the unit's type passport has no manufacturing facility with a country of production); `application/vc+ld+json` (or bare `vc+ld+json`) → the same per-unit credential with an embedded `ecdsa-jcs-2019` Data Integrity proof, same `406`; `text/html` → server-rendered unit page; everything else → JSON-LD (`application/ld+json`). No AAS representation on this route. `Vary: Accept` always set on the 200. The `410` tombstone check (below) precedes content negotiation, so a recycled/ceased unit never yields a `vc+jwt` or `vc+ld+json`.  **Per-unit telemetry is never public** (Annex XIII(2)-(4)): anonymous responses omit `currentState`/`dynamicData` and instead carry a `restrictedData` notice with a `/request-access` pointer. An owner credential — a tenant **API key** (`op_dpp_token_…`) of the owning or operator-bound tenant, sent as a Bearer token (a Console JWT login session does **not** unlock owner tier) — or a valid grant token (`dpp_li_…`/`dpp_auth_…` as Bearer or `?grant=`; TENANT, PASSPORT or UNIT scope) unlocks `currentState` and `dynamicData` — up to the 500 most recent events, newest first. Invalid credentials silently degrade to the public tier (never 401/402/403). Grant-unlocked responses add `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`. No permission string (public endpoint).  **Tombstone:** once the unit's status is `RECYCLED` (or `ceasedAt` is set) this URL answers `410 Gone` with a minimal tombstone for everyone — grants and owner credentials do NOT override it (the owning tenant retains internal access via `GET /api/v1/units/{id}`).  Every resolution is access-audit-logged with an anonymized IP. **Rate limit:** 30 requests/min/IP (two-field 429 body without `success`). The limiter adds no headers of its own — `x-ratelimit-*` headers come from the global platform limit, which applies on top.

        :param id: The battery unit's server-assigned UUID (AI-21 serial resolution via `GET /01/{gtin14}/21/{serial}` redirects here). (required)
        :type id: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.
        :type grant: str
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

        _param = self._resolve_public_battery_unit_serialize(
            id=id,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicBatteryUnitJsonLd",
            '404': "Error",
            '406': "Error",
            '410': "BatteryUnitTombstoneJsonLd",
            '429': "Error",
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
    def resolve_public_battery_unit_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="The battery unit's server-assigned UUID (AI-21 serial resolution via `GET /01/{gtin14}/21/{serial}` redirects here).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.")] = None,
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
    ) -> ApiResponse[PublicBatteryUnitJsonLd]:
        """Resolve an individual serialised battery unit

        Public, content-negotiated view of one individual serialised unit (battery) by its unit UUID, including the embedded SKU/type passport (`ofModel`, masked by the same tier rules as `GET /passport/{id}`).  **Content negotiation:** `Accept` containing `application/vc+jwt` (or bare `vc+jwt`) → a signed PER-UNIT (item-granularity) UNTP DigitalProductPassport credential (public tier; `406 Not Acceptable` when the unit's type passport has no manufacturing facility with a country of production); `application/vc+ld+json` (or bare `vc+ld+json`) → the same per-unit credential with an embedded `ecdsa-jcs-2019` Data Integrity proof, same `406`; `text/html` → server-rendered unit page; everything else → JSON-LD (`application/ld+json`). No AAS representation on this route. `Vary: Accept` always set on the 200. The `410` tombstone check (below) precedes content negotiation, so a recycled/ceased unit never yields a `vc+jwt` or `vc+ld+json`.  **Per-unit telemetry is never public** (Annex XIII(2)-(4)): anonymous responses omit `currentState`/`dynamicData` and instead carry a `restrictedData` notice with a `/request-access` pointer. An owner credential — a tenant **API key** (`op_dpp_token_…`) of the owning or operator-bound tenant, sent as a Bearer token (a Console JWT login session does **not** unlock owner tier) — or a valid grant token (`dpp_li_…`/`dpp_auth_…` as Bearer or `?grant=`; TENANT, PASSPORT or UNIT scope) unlocks `currentState` and `dynamicData` — up to the 500 most recent events, newest first. Invalid credentials silently degrade to the public tier (never 401/402/403). Grant-unlocked responses add `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`. No permission string (public endpoint).  **Tombstone:** once the unit's status is `RECYCLED` (or `ceasedAt` is set) this URL answers `410 Gone` with a minimal tombstone for everyone — grants and owner credentials do NOT override it (the owning tenant retains internal access via `GET /api/v1/units/{id}`).  Every resolution is access-audit-logged with an anonymized IP. **Rate limit:** 30 requests/min/IP (two-field 429 body without `success`). The limiter adds no headers of its own — `x-ratelimit-*` headers come from the global platform limit, which applies on top.

        :param id: The battery unit's server-assigned UUID (AI-21 serial resolution via `GET /01/{gtin14}/21/{serial}` redirects here). (required)
        :type id: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.
        :type grant: str
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

        _param = self._resolve_public_battery_unit_serialize(
            id=id,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicBatteryUnitJsonLd",
            '404': "Error",
            '406': "Error",
            '410': "BatteryUnitTombstoneJsonLd",
            '429': "Error",
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
    def resolve_public_battery_unit_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="The battery unit's server-assigned UUID (AI-21 serial resolution via `GET /01/{gtin14}/21/{serial}` redirects here).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.")] = None,
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
        """Resolve an individual serialised battery unit

        Public, content-negotiated view of one individual serialised unit (battery) by its unit UUID, including the embedded SKU/type passport (`ofModel`, masked by the same tier rules as `GET /passport/{id}`).  **Content negotiation:** `Accept` containing `application/vc+jwt` (or bare `vc+jwt`) → a signed PER-UNIT (item-granularity) UNTP DigitalProductPassport credential (public tier; `406 Not Acceptable` when the unit's type passport has no manufacturing facility with a country of production); `application/vc+ld+json` (or bare `vc+ld+json`) → the same per-unit credential with an embedded `ecdsa-jcs-2019` Data Integrity proof, same `406`; `text/html` → server-rendered unit page; everything else → JSON-LD (`application/ld+json`). No AAS representation on this route. `Vary: Accept` always set on the 200. The `410` tombstone check (below) precedes content negotiation, so a recycled/ceased unit never yields a `vc+jwt` or `vc+ld+json`.  **Per-unit telemetry is never public** (Annex XIII(2)-(4)): anonymous responses omit `currentState`/`dynamicData` and instead carry a `restrictedData` notice with a `/request-access` pointer. An owner credential — a tenant **API key** (`op_dpp_token_…`) of the owning or operator-bound tenant, sent as a Bearer token (a Console JWT login session does **not** unlock owner tier) — or a valid grant token (`dpp_li_…`/`dpp_auth_…` as Bearer or `?grant=`; TENANT, PASSPORT or UNIT scope) unlocks `currentState` and `dynamicData` — up to the 500 most recent events, newest first. Invalid credentials silently degrade to the public tier (never 401/402/403). Grant-unlocked responses add `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`. No permission string (public endpoint).  **Tombstone:** once the unit's status is `RECYCLED` (or `ceasedAt` is set) this URL answers `410 Gone` with a minimal tombstone for everyone — grants and owner credentials do NOT override it (the owning tenant retains internal access via `GET /api/v1/units/{id}`).  Every resolution is access-audit-logged with an anonymized IP. **Rate limit:** 30 requests/min/IP (two-field 429 body without `success`). The limiter adds no headers of its own — `x-ratelimit-*` headers come from the global platform limit, which applies on top.

        :param id: The battery unit's server-assigned UUID (AI-21 serial resolution via `GET /01/{gtin14}/21/{serial}` redirects here). (required)
        :type id: str
        :param grant: Capability grant token (`dpp_li_…` / `dpp_auth_…`); equivalent to `Authorization: Bearer`. Minted tokens are the prefix + 32 hex characters; the server matches any prefixed token against stored hashes. Treat as a secret.
        :type grant: str
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

        _param = self._resolve_public_battery_unit_serialize(
            id=id,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicBatteryUnitJsonLd",
            '404': "Error",
            '406': "Error",
            '410': "BatteryUnitTombstoneJsonLd",
            '429': "Error",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _resolve_public_battery_unit_serialize(
        self,
        id,
        grant,
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
        if grant is not None:
            
            _query_params.append(('grant', grant))
            
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
            resource_path='/unit/{id}',
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
    def resolve_public_passport(
        self,
        id: Annotated[StrictStr, Field(description="The passport's server-assigned UUID (returned as `id` on creation and embedded as AI-21 in the SKU-level Digital Link URI).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` legitimate-interest, `dpp_auth_…` authority) — the inspection-link path for QR-scanning inspectors who cannot set headers. Equivalent to sending the token as `Authorization: Bearer`. Tokens minted by the platform are the prefix followed by 32 hex characters, but the server matches any prefixed token against its stored hashes (the demo workspace's sample tokens use a different suffix), so the pattern here is deliberately loose. Treat as a secret: responses unlocked this way carry `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`, and the server log redacts the parameter.")] = None,
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
        """Resolve a passport by UUID (JSON-LD / AAS / HTML)

        Public, content-negotiated resolution of a Digital Product Passport by its server-assigned UUID. Lookup is by primary key only — GTIN/GRAI/serial lookups go through the GS1 Digital Link gateway (`GET /01/{gtin14}`, `GET /8003/{grai}`).  **Content negotiation** — the representation is chosen by RFC 7231 §5.3.2 `Accept` q-value negotiation (highest q wins; ties broken by media-range specificity, then the client's stated order): `application/aas+json` (or bare `aas+json`) → role-filtered Asset Administration Shell environment; `application/vc+jwt` (or bare `vc+jwt`) → a signed UNTP DigitalProductPassport credential (public tier; `406 Not Acceptable` when the passport has no manufacturing facility with a country of production); `application/vc+ld+json` (or bare `vc+ld+json`) → the same credential with an embedded W3C Data Integrity proof (`ecdsa-jcs-2019`), same `406` condition; `application/dc+sd-jwt` (or the legacy `vc+sd-jwt`) → the same credential as an SD-JWT-VC for cryptographic selective disclosure (a holder presents a subset of `credentialSubject` claims), same `406` condition; `text/html` → server-rendered passport page. An absent `Accept`, or one matching only `*/*`, yields the canonical default JSON-LD (`application/ld+json`); an unsupported type is ignored. Because q-values and client order are honoured, `Accept: text/html, application/vc+jwt` selects HTML (the client's first preference), NOT `vc+jwt`. `Vary: Accept` is always set on the 200.  **Access tiers** — no permission string (public endpoint). Credentials are *optional* and never produce 401/402/403 here; an invalid or foreign credential silently degrades to the public tier: - **Public** (anonymous): restricted metadata keys (for category `batteries`: `detailedPerformance`, `lifecycleAndInUse`, `circularityAndDisassembly` — masked only when present) and the owner-only key `facilityDetails` (present-as-placeholder in every non-owner response, even when the underlying metadata never contained it) carry the literal placeholder `[REDACTED - Privileged Access Required]`. Each masked key that exists in the sealed metadata keeps its true Merkle leaf hash in `proof.redactedLeaves`, so the seal stays offline-verifiable after redaction; a placeholder-valued key with no `redactedLeaves` entry was never in the sealed metadata and must be excluded when rebuilding the root. - **Legitimate interest / authority**: a capability grant token — `dpp_li_…` (tenant-issued) or `dpp_auth_…` (platform-issued, not tenant-revocable) — sent as `Authorization: Bearer <token>` or `?grant=<token>`, with TENANT or PASSPORT scope covering this passport, unlocks the restricted tier-2 keys. `facilityDetails`, the facility street address and DRAFT passports stay hidden. Grant-unlocked responses add `Cache-Control: private, no-store` and `Referrer-Policy: no-referrer`. - **Owner**: a tenant **API key** (`op_dpp_token_…`, shown once at creation) belonging to the owning tenant or to a tenant bound to the passport's economic operator — sent as a Bearer token. Only API keys are matched on the public resolvers: a Console JWT login session does **not** unlock owner tier (it silently resolves as public). Owners see everything, including DRAFT passports, owner-only metadata keys and the facility street address (`manufacturingFacility.streetAddress`/`city`/`postalCode`). In the AAS representation the owner credential's API-key role drives element filtering; a grant maps to the `legitimate_interest` filter tier, anonymous to `public`.  DRAFT passports are hidden from everyone but the owner (404 with a body identical to a true miss). Every resolution is recorded in the passport's access audit log with an anonymized IP.  **Rate limit:** 30 requests/min/IP; its 429 body is the two-field public error shape (no `success` field). This limiter adds no headers of its own — the `x-ratelimit-*` headers still present on responses (including these 429s) belong to the global platform limit (100 req/min/IP, 600/min for known crawler user agents), which applies on top.

        :param id: The passport's server-assigned UUID (returned as `id` on creation and embedded as AI-21 in the SKU-level Digital Link URI). (required)
        :type id: str
        :param grant: Capability grant token (`dpp_li_…` legitimate-interest, `dpp_auth_…` authority) — the inspection-link path for QR-scanning inspectors who cannot set headers. Equivalent to sending the token as `Authorization: Bearer`. Tokens minted by the platform are the prefix followed by 32 hex characters, but the server matches any prefixed token against its stored hashes (the demo workspace's sample tokens use a different suffix), so the pattern here is deliberately loose. Treat as a secret: responses unlocked this way carry `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`, and the server log redacts the parameter.
        :type grant: str
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

        _param = self._resolve_public_passport_serialize(
            id=id,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
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
    def resolve_public_passport_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="The passport's server-assigned UUID (returned as `id` on creation and embedded as AI-21 in the SKU-level Digital Link URI).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` legitimate-interest, `dpp_auth_…` authority) — the inspection-link path for QR-scanning inspectors who cannot set headers. Equivalent to sending the token as `Authorization: Bearer`. Tokens minted by the platform are the prefix followed by 32 hex characters, but the server matches any prefixed token against its stored hashes (the demo workspace's sample tokens use a different suffix), so the pattern here is deliberately loose. Treat as a secret: responses unlocked this way carry `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`, and the server log redacts the parameter.")] = None,
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
        """Resolve a passport by UUID (JSON-LD / AAS / HTML)

        Public, content-negotiated resolution of a Digital Product Passport by its server-assigned UUID. Lookup is by primary key only — GTIN/GRAI/serial lookups go through the GS1 Digital Link gateway (`GET /01/{gtin14}`, `GET /8003/{grai}`).  **Content negotiation** — the representation is chosen by RFC 7231 §5.3.2 `Accept` q-value negotiation (highest q wins; ties broken by media-range specificity, then the client's stated order): `application/aas+json` (or bare `aas+json`) → role-filtered Asset Administration Shell environment; `application/vc+jwt` (or bare `vc+jwt`) → a signed UNTP DigitalProductPassport credential (public tier; `406 Not Acceptable` when the passport has no manufacturing facility with a country of production); `application/vc+ld+json` (or bare `vc+ld+json`) → the same credential with an embedded W3C Data Integrity proof (`ecdsa-jcs-2019`), same `406` condition; `application/dc+sd-jwt` (or the legacy `vc+sd-jwt`) → the same credential as an SD-JWT-VC for cryptographic selective disclosure (a holder presents a subset of `credentialSubject` claims), same `406` condition; `text/html` → server-rendered passport page. An absent `Accept`, or one matching only `*/*`, yields the canonical default JSON-LD (`application/ld+json`); an unsupported type is ignored. Because q-values and client order are honoured, `Accept: text/html, application/vc+jwt` selects HTML (the client's first preference), NOT `vc+jwt`. `Vary: Accept` is always set on the 200.  **Access tiers** — no permission string (public endpoint). Credentials are *optional* and never produce 401/402/403 here; an invalid or foreign credential silently degrades to the public tier: - **Public** (anonymous): restricted metadata keys (for category `batteries`: `detailedPerformance`, `lifecycleAndInUse`, `circularityAndDisassembly` — masked only when present) and the owner-only key `facilityDetails` (present-as-placeholder in every non-owner response, even when the underlying metadata never contained it) carry the literal placeholder `[REDACTED - Privileged Access Required]`. Each masked key that exists in the sealed metadata keeps its true Merkle leaf hash in `proof.redactedLeaves`, so the seal stays offline-verifiable after redaction; a placeholder-valued key with no `redactedLeaves` entry was never in the sealed metadata and must be excluded when rebuilding the root. - **Legitimate interest / authority**: a capability grant token — `dpp_li_…` (tenant-issued) or `dpp_auth_…` (platform-issued, not tenant-revocable) — sent as `Authorization: Bearer <token>` or `?grant=<token>`, with TENANT or PASSPORT scope covering this passport, unlocks the restricted tier-2 keys. `facilityDetails`, the facility street address and DRAFT passports stay hidden. Grant-unlocked responses add `Cache-Control: private, no-store` and `Referrer-Policy: no-referrer`. - **Owner**: a tenant **API key** (`op_dpp_token_…`, shown once at creation) belonging to the owning tenant or to a tenant bound to the passport's economic operator — sent as a Bearer token. Only API keys are matched on the public resolvers: a Console JWT login session does **not** unlock owner tier (it silently resolves as public). Owners see everything, including DRAFT passports, owner-only metadata keys and the facility street address (`manufacturingFacility.streetAddress`/`city`/`postalCode`). In the AAS representation the owner credential's API-key role drives element filtering; a grant maps to the `legitimate_interest` filter tier, anonymous to `public`.  DRAFT passports are hidden from everyone but the owner (404 with a body identical to a true miss). Every resolution is recorded in the passport's access audit log with an anonymized IP.  **Rate limit:** 30 requests/min/IP; its 429 body is the two-field public error shape (no `success` field). This limiter adds no headers of its own — the `x-ratelimit-*` headers still present on responses (including these 429s) belong to the global platform limit (100 req/min/IP, 600/min for known crawler user agents), which applies on top.

        :param id: The passport's server-assigned UUID (returned as `id` on creation and embedded as AI-21 in the SKU-level Digital Link URI). (required)
        :type id: str
        :param grant: Capability grant token (`dpp_li_…` legitimate-interest, `dpp_auth_…` authority) — the inspection-link path for QR-scanning inspectors who cannot set headers. Equivalent to sending the token as `Authorization: Bearer`. Tokens minted by the platform are the prefix followed by 32 hex characters, but the server matches any prefixed token against its stored hashes (the demo workspace's sample tokens use a different suffix), so the pattern here is deliberately loose. Treat as a secret: responses unlocked this way carry `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`, and the server log redacts the parameter.
        :type grant: str
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

        _param = self._resolve_public_passport_serialize(
            id=id,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
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
    def resolve_public_passport_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="The passport's server-assigned UUID (returned as `id` on creation and embedded as AI-21 in the SKU-level Digital Link URI).")],
        grant: Annotated[Optional[Annotated[str, Field(strict=True)]], Field(description="Capability grant token (`dpp_li_…` legitimate-interest, `dpp_auth_…` authority) — the inspection-link path for QR-scanning inspectors who cannot set headers. Equivalent to sending the token as `Authorization: Bearer`. Tokens minted by the platform are the prefix followed by 32 hex characters, but the server matches any prefixed token against its stored hashes (the demo workspace's sample tokens use a different suffix), so the pattern here is deliberately loose. Treat as a secret: responses unlocked this way carry `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`, and the server log redacts the parameter.")] = None,
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
        """Resolve a passport by UUID (JSON-LD / AAS / HTML)

        Public, content-negotiated resolution of a Digital Product Passport by its server-assigned UUID. Lookup is by primary key only — GTIN/GRAI/serial lookups go through the GS1 Digital Link gateway (`GET /01/{gtin14}`, `GET /8003/{grai}`).  **Content negotiation** — the representation is chosen by RFC 7231 §5.3.2 `Accept` q-value negotiation (highest q wins; ties broken by media-range specificity, then the client's stated order): `application/aas+json` (or bare `aas+json`) → role-filtered Asset Administration Shell environment; `application/vc+jwt` (or bare `vc+jwt`) → a signed UNTP DigitalProductPassport credential (public tier; `406 Not Acceptable` when the passport has no manufacturing facility with a country of production); `application/vc+ld+json` (or bare `vc+ld+json`) → the same credential with an embedded W3C Data Integrity proof (`ecdsa-jcs-2019`), same `406` condition; `application/dc+sd-jwt` (or the legacy `vc+sd-jwt`) → the same credential as an SD-JWT-VC for cryptographic selective disclosure (a holder presents a subset of `credentialSubject` claims), same `406` condition; `text/html` → server-rendered passport page. An absent `Accept`, or one matching only `*/*`, yields the canonical default JSON-LD (`application/ld+json`); an unsupported type is ignored. Because q-values and client order are honoured, `Accept: text/html, application/vc+jwt` selects HTML (the client's first preference), NOT `vc+jwt`. `Vary: Accept` is always set on the 200.  **Access tiers** — no permission string (public endpoint). Credentials are *optional* and never produce 401/402/403 here; an invalid or foreign credential silently degrades to the public tier: - **Public** (anonymous): restricted metadata keys (for category `batteries`: `detailedPerformance`, `lifecycleAndInUse`, `circularityAndDisassembly` — masked only when present) and the owner-only key `facilityDetails` (present-as-placeholder in every non-owner response, even when the underlying metadata never contained it) carry the literal placeholder `[REDACTED - Privileged Access Required]`. Each masked key that exists in the sealed metadata keeps its true Merkle leaf hash in `proof.redactedLeaves`, so the seal stays offline-verifiable after redaction; a placeholder-valued key with no `redactedLeaves` entry was never in the sealed metadata and must be excluded when rebuilding the root. - **Legitimate interest / authority**: a capability grant token — `dpp_li_…` (tenant-issued) or `dpp_auth_…` (platform-issued, not tenant-revocable) — sent as `Authorization: Bearer <token>` or `?grant=<token>`, with TENANT or PASSPORT scope covering this passport, unlocks the restricted tier-2 keys. `facilityDetails`, the facility street address and DRAFT passports stay hidden. Grant-unlocked responses add `Cache-Control: private, no-store` and `Referrer-Policy: no-referrer`. - **Owner**: a tenant **API key** (`op_dpp_token_…`, shown once at creation) belonging to the owning tenant or to a tenant bound to the passport's economic operator — sent as a Bearer token. Only API keys are matched on the public resolvers: a Console JWT login session does **not** unlock owner tier (it silently resolves as public). Owners see everything, including DRAFT passports, owner-only metadata keys and the facility street address (`manufacturingFacility.streetAddress`/`city`/`postalCode`). In the AAS representation the owner credential's API-key role drives element filtering; a grant maps to the `legitimate_interest` filter tier, anonymous to `public`.  DRAFT passports are hidden from everyone but the owner (404 with a body identical to a true miss). Every resolution is recorded in the passport's access audit log with an anonymized IP.  **Rate limit:** 30 requests/min/IP; its 429 body is the two-field public error shape (no `success` field). This limiter adds no headers of its own — the `x-ratelimit-*` headers still present on responses (including these 429s) belong to the global platform limit (100 req/min/IP, 600/min for known crawler user agents), which applies on top.

        :param id: The passport's server-assigned UUID (returned as `id` on creation and embedded as AI-21 in the SKU-level Digital Link URI). (required)
        :type id: str
        :param grant: Capability grant token (`dpp_li_…` legitimate-interest, `dpp_auth_…` authority) — the inspection-link path for QR-scanning inspectors who cannot set headers. Equivalent to sending the token as `Authorization: Bearer`. Tokens minted by the platform are the prefix followed by 32 hex characters, but the server matches any prefixed token against its stored hashes (the demo workspace's sample tokens use a different suffix), so the pattern here is deliberately loose. Treat as a secret: responses unlocked this way carry `Cache-Control: private, no-store` + `Referrer-Policy: no-referrer`, and the server log redacts the parameter.
        :type grant: str
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

        _param = self._resolve_public_passport_serialize(
            id=id,
            grant=grant,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "Error",
            '404': "Error",
            '406': "Error",
            '429': "Error",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _resolve_public_passport_serialize(
        self,
        id,
        grant,
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
        if grant is not None:
            
            _query_params.append(('grant', grant))
            
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
            resource_path='/passport/{id}',
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


