# coding: utf-8

"""
    OpenDPP Integration API

    OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `EORI_NOT_FOUND`, `CARRIER_SYMBOLOGY_NOT_RENDERED`, `CATEGORY_GRANULARITY_UNEXPECTED`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Methods  A request whose path exists but whose method this API does not serve returns **`405 Method Not Allowed`** with an `Allow` header listing the methods that path does serve (RFC 9110 §15.5.6); `HEAD` is listed wherever `GET` is, and is served. A path no route matches returns `404`, as does a path whose method IS allowed but whose resource does not exist — so a `405` always means the verb, and never the identifier. `405` is not listed per operation below because it is not a property of any operation: it is the answer to a method for which no operation exists.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Role in the data exchange This node is **not a DPP registry**. It hosts passports on behalf of the economic operators that create them and provides no registration service, so the registry methods of EN 18222:2026 clause 5 (Table 17, `registerDPP`) are outside this API's scope. Which service-provider role the node holds for a given passport is a property of the agreement with that operator rather than of this document, so it is not asserted here.  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.

    The version of the OpenAPI document: 1.17.0
    Contact: support@opendpp-node.eu
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501

import warnings
from pydantic import validate_call, Field, StrictFloat, StrictStr, StrictInt
from typing import Any, Dict, List, Optional, Tuple, Union
from typing_extensions import Annotated

from datetime import datetime
from pydantic import Field, StrictStr, field_validator
from typing import Any, Dict, List, Optional
from typing_extensions import Annotated
from opendpp_sdk.models.en18222_product_id_query import En18222ProductIdQuery
from opendpp_sdk.models.public_passport_json_ld import PublicPassportJsonLd

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class EN18222LifeCycleAPIApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def create_dpp(
        self,
        body: Dict[str, Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.")] = None,
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
    ) -> str:
        """CreateDPP — create a new DPP and return its identifier

        **EN 18222:2026 clause 4.6, Table 5; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator. OpenDPP is declared the main provider and never the creator: the economic operator is always the creator, and this method is how a creator's passport comes to be hosted here.  Table 5's output is the **DPP ID**, not the passport, so a successful creation answers `201` (EN 18222 Table 15 `SuccessCreated`) with the new `digitalProductPassportId` as the body and a `Location` header pointing at its `ReadDPPById` path.  **Permission:** `passport:create`.

        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.
        :type representation: str
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

        _param = self._create_dpp_serialize(
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "str",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def create_dpp_with_http_info(
        self,
        body: Dict[str, Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.")] = None,
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
    ) -> ApiResponse[str]:
        """CreateDPP — create a new DPP and return its identifier

        **EN 18222:2026 clause 4.6, Table 5; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator. OpenDPP is declared the main provider and never the creator: the economic operator is always the creator, and this method is how a creator's passport comes to be hosted here.  Table 5's output is the **DPP ID**, not the passport, so a successful creation answers `201` (EN 18222 Table 15 `SuccessCreated`) with the new `digitalProductPassportId` as the body and a `Location` header pointing at its `ReadDPPById` path.  **Permission:** `passport:create`.

        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.
        :type representation: str
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

        _param = self._create_dpp_serialize(
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "str",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def create_dpp_without_preload_content(
        self,
        body: Dict[str, Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.")] = None,
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
        """CreateDPP — create a new DPP and return its identifier

        **EN 18222:2026 clause 4.6, Table 5; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator. OpenDPP is declared the main provider and never the creator: the economic operator is always the creator, and this method is how a creator's passport comes to be hosted here.  Table 5's output is the **DPP ID**, not the passport, so a successful creation answers `201` (EN 18222 Table 15 `SuccessCreated`) with the new `digitalProductPassportId` as the body and a `Location` header pointing at its `ReadDPPById` path.  **Permission:** `passport:create`.

        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.
        :type representation: str
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

        _param = self._create_dpp_serialize(
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "str",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _create_dpp_serialize(
        self,
        body,
        representation,
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
        if representation is not None:
            
            _query_params.append(('representation', representation))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if body is not None:
            _body_params = body


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
            resource_path='/v1/dpps',
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
    def delete_dppby_id(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
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
        """DeleteDPPById — remove the DPP with the specified DPP ID

        **EN 18222:2026 clause 4.8, Table 7; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator.  Table 7 declares no output payload, so a successful deletion answers `204` (EN 18222 Table 15 `SuccessNoContent`).  **Access rules control whether deletion is allowed**, and here they are strict: a passport that was ever published or sealed is **not** deletable, because ESPR Article 77(8) makes it persist. That refusal is reported, not softened — this method removes a passport that was never published.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
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

        _param = self._delete_dppby_id_serialize(
            dpp_id=dpp_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '204': None,
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def delete_dppby_id_with_http_info(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
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
        """DeleteDPPById — remove the DPP with the specified DPP ID

        **EN 18222:2026 clause 4.8, Table 7; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator.  Table 7 declares no output payload, so a successful deletion answers `204` (EN 18222 Table 15 `SuccessNoContent`).  **Access rules control whether deletion is allowed**, and here they are strict: a passport that was ever published or sealed is **not** deletable, because ESPR Article 77(8) makes it persist. That refusal is reported, not softened — this method removes a passport that was never published.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
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

        _param = self._delete_dppby_id_serialize(
            dpp_id=dpp_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '204': None,
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def delete_dppby_id_without_preload_content(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
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
        """DeleteDPPById — remove the DPP with the specified DPP ID

        **EN 18222:2026 clause 4.8, Table 7; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main DPP service provider and by a back-up provider — expressly not by the creator.  Table 7 declares no output payload, so a successful deletion answers `204` (EN 18222 Table 15 `SuccessNoContent`).  **Access rules control whether deletion is allowed**, and here they are strict: a passport that was ever published or sealed is **not** deletable, because ESPR Article 77(8) makes it persist. That refusal is reported, not softened — this method removes a passport that was never published.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
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

        _param = self._delete_dppby_id_serialize(
            dpp_id=dpp_id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '204': None,
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _delete_dppby_id_serialize(
        self,
        dpp_id,
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
        if dpp_id is not None:
            _path_params['dppId'] = dpp_id
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
            resource_path='/v1/dpps/{dppId}',
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
    def read_data_element(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        element_id_path: Annotated[str, Field(min_length=1, strict=True, description="The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
    ) -> object:
        """ReadDataElement — return one data element by its absolute path

        **EN 18222:2026 clause 6.2, Table 9; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available by the creator or their main provider, as well as by a back-up provider. Clause 6.1 itself is permissive (\"may be made available\"), so the obligation is the one clause 4.1 states.  The element is addressed within the passport document in the requested representation, so the access tier that governs `ReadDPPById` governs which elements are addressable here too.  **Permission:** `passport:read`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param element_id_path: The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
        :type element_id_path: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_data_element_serialize(
            dpp_id=dpp_id,
            element_id_path=element_id_path,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "object",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_data_element_with_http_info(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        element_id_path: Annotated[str, Field(min_length=1, strict=True, description="The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
    ) -> ApiResponse[object]:
        """ReadDataElement — return one data element by its absolute path

        **EN 18222:2026 clause 6.2, Table 9; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available by the creator or their main provider, as well as by a back-up provider. Clause 6.1 itself is permissive (\"may be made available\"), so the obligation is the one clause 4.1 states.  The element is addressed within the passport document in the requested representation, so the access tier that governs `ReadDPPById` governs which elements are addressable here too.  **Permission:** `passport:read`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param element_id_path: The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
        :type element_id_path: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_data_element_serialize(
            dpp_id=dpp_id,
            element_id_path=element_id_path,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "object",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_data_element_without_preload_content(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        element_id_path: Annotated[str, Field(min_length=1, strict=True, description="The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDataElement — return one data element by its absolute path

        **EN 18222:2026 clause 6.2, Table 9; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available by the creator or their main provider, as well as by a back-up provider. Clause 6.1 itself is permissive (\"may be made available\"), so the obligation is the one clause 4.1 states.  The element is addressed within the passport document in the requested representation, so the access tier that governs `ReadDPPById` governs which elements are addressable here too.  **Permission:** `passport:read`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param element_id_path: The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
        :type element_id_path: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_data_element_serialize(
            dpp_id=dpp_id,
            element_id_path=element_id_path,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "object",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _read_data_element_serialize(
        self,
        dpp_id,
        element_id_path,
        representation,
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
        if dpp_id is not None:
            _path_params['dppId'] = dpp_id
        if element_id_path is not None:
            _path_params['elementIdPath'] = element_id_path
        # process the query parameters
        if representation is not None:
            
            _query_params.append(('representation', representation))
            
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
            resource_path='/v1/dpps/{dppId}/elements/{elementIdPath}',
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
    def read_dpp_ids_by_product_ids(
        self,
        en18222_product_id_query: En18222ProductIdQuery,
        limit: Annotated[Optional[Annotated[int, Field(le=500, strict=True, ge=1)]], Field(description="Maximum identifiers per page, 1–500 (default 100). A value outside that range is refused, not clamped — a client that asked for more and silently received a short page cannot tell it from the last one.")] = None,
        cursor: Annotated[Optional[Annotated[str, Field(min_length=1, strict=True)]], Field(description="The continuation from a previous page's `Link: rel=\"next\"` header. EN 18222 clause 4.5: the value shall not be empty — omit the parameter to start from the first page.")] = None,
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
    ) -> List[str]:
        """ReadDPPIdsByProductIds — return the DPP identifiers matching a set of product identifiers

        **EN 18222:2026 clause 4.5, Table 4; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available, and it is the method EN 18222's own Annex ZA leans on hardest — five of Table ZA.1's six rows cite clause 4.5.  The result is the set of `digitalProductPassportId` values, not the passports; fetch each with `ReadDPPById`.  **Pagination.** Clause 4.5 lets the client set `limit` and `cursor`, and requires that \"the value of the cursor shall not be empty\" — a `cursor` present but empty is refused rather than treated as a restart, which would repeat rows the client already holds. Table 16 reports the result \"except status code **and pagination**\", so the continuation is carried out of band as an RFC 8288 `Link` header with `rel=\"next\"`; its absence means the last page.  **Permission:** `passport:read`.

        :param en18222_product_id_query: (required)
        :type en18222_product_id_query: En18222ProductIdQuery
        :param limit: Maximum identifiers per page, 1–500 (default 100). A value outside that range is refused, not clamped — a client that asked for more and silently received a short page cannot tell it from the last one.
        :type limit: int
        :param cursor: The continuation from a previous page's `Link: rel=\"next\"` header. EN 18222 clause 4.5: the value shall not be empty — omit the parameter to start from the first page.
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

        _param = self._read_dpp_ids_by_product_ids_serialize(
            en18222_product_id_query=en18222_product_id_query,
            limit=limit,
            cursor=cursor,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "List[str]",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_dpp_ids_by_product_ids_with_http_info(
        self,
        en18222_product_id_query: En18222ProductIdQuery,
        limit: Annotated[Optional[Annotated[int, Field(le=500, strict=True, ge=1)]], Field(description="Maximum identifiers per page, 1–500 (default 100). A value outside that range is refused, not clamped — a client that asked for more and silently received a short page cannot tell it from the last one.")] = None,
        cursor: Annotated[Optional[Annotated[str, Field(min_length=1, strict=True)]], Field(description="The continuation from a previous page's `Link: rel=\"next\"` header. EN 18222 clause 4.5: the value shall not be empty — omit the parameter to start from the first page.")] = None,
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
    ) -> ApiResponse[List[str]]:
        """ReadDPPIdsByProductIds — return the DPP identifiers matching a set of product identifiers

        **EN 18222:2026 clause 4.5, Table 4; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available, and it is the method EN 18222's own Annex ZA leans on hardest — five of Table ZA.1's six rows cite clause 4.5.  The result is the set of `digitalProductPassportId` values, not the passports; fetch each with `ReadDPPById`.  **Pagination.** Clause 4.5 lets the client set `limit` and `cursor`, and requires that \"the value of the cursor shall not be empty\" — a `cursor` present but empty is refused rather than treated as a restart, which would repeat rows the client already holds. Table 16 reports the result \"except status code **and pagination**\", so the continuation is carried out of band as an RFC 8288 `Link` header with `rel=\"next\"`; its absence means the last page.  **Permission:** `passport:read`.

        :param en18222_product_id_query: (required)
        :type en18222_product_id_query: En18222ProductIdQuery
        :param limit: Maximum identifiers per page, 1–500 (default 100). A value outside that range is refused, not clamped — a client that asked for more and silently received a short page cannot tell it from the last one.
        :type limit: int
        :param cursor: The continuation from a previous page's `Link: rel=\"next\"` header. EN 18222 clause 4.5: the value shall not be empty — omit the parameter to start from the first page.
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

        _param = self._read_dpp_ids_by_product_ids_serialize(
            en18222_product_id_query=en18222_product_id_query,
            limit=limit,
            cursor=cursor,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "List[str]",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_dpp_ids_by_product_ids_without_preload_content(
        self,
        en18222_product_id_query: En18222ProductIdQuery,
        limit: Annotated[Optional[Annotated[int, Field(le=500, strict=True, ge=1)]], Field(description="Maximum identifiers per page, 1–500 (default 100). A value outside that range is refused, not clamped — a client that asked for more and silently received a short page cannot tell it from the last one.")] = None,
        cursor: Annotated[Optional[Annotated[str, Field(min_length=1, strict=True)]], Field(description="The continuation from a previous page's `Link: rel=\"next\"` header. EN 18222 clause 4.5: the value shall not be empty — omit the parameter to start from the first page.")] = None,
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
        """ReadDPPIdsByProductIds — return the DPP identifiers matching a set of product identifiers

        **EN 18222:2026 clause 4.5, Table 4; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available, and it is the method EN 18222's own Annex ZA leans on hardest — five of Table ZA.1's six rows cite clause 4.5.  The result is the set of `digitalProductPassportId` values, not the passports; fetch each with `ReadDPPById`.  **Pagination.** Clause 4.5 lets the client set `limit` and `cursor`, and requires that \"the value of the cursor shall not be empty\" — a `cursor` present but empty is refused rather than treated as a restart, which would repeat rows the client already holds. Table 16 reports the result \"except status code **and pagination**\", so the continuation is carried out of band as an RFC 8288 `Link` header with `rel=\"next\"`; its absence means the last page.  **Permission:** `passport:read`.

        :param en18222_product_id_query: (required)
        :type en18222_product_id_query: En18222ProductIdQuery
        :param limit: Maximum identifiers per page, 1–500 (default 100). A value outside that range is refused, not clamped — a client that asked for more and silently received a short page cannot tell it from the last one.
        :type limit: int
        :param cursor: The continuation from a previous page's `Link: rel=\"next\"` header. EN 18222 clause 4.5: the value shall not be empty — omit the parameter to start from the first page.
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

        _param = self._read_dpp_ids_by_product_ids_serialize(
            en18222_product_id_query=en18222_product_id_query,
            limit=limit,
            cursor=cursor,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "List[str]",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _read_dpp_ids_by_product_ids_serialize(
        self,
        en18222_product_id_query,
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
        # process the query parameters
        if limit is not None:
            
            _query_params.append(('limit', limit))
            
        if cursor is not None:
            
            _query_params.append(('cursor', cursor))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if en18222_product_id_query is not None:
            _body_params = en18222_product_id_query


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
            resource_path='/v1/dppsByProductIds',
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
    def read_dpp_version_by_id_and_date(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        var_date: Annotated[datetime, Field(description="The instant the passport is requested for — a UTC-based timestamp in the ISO 8601-1 format EN 18223 uses, e.g. `2026-09-03T12:00:00Z`. A value without a UTC designator is refused rather than read in the server's local zone.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPVersionByIdAndDate — return the DPP version current at a given date

        **EN 18222:2026 clause 4.4, Table 3; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main provider and by a back-up provider — expressly not by the creator. Table 3 notes the method is optional for the economic operator.  The version returned is the one that was current at the instant given: the node archives a snapshot before each change, so the first snapshot recorded after the instant holds the state that was current at it.  **Permission:** `passport:read`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param var_date: The instant the passport is requested for — a UTC-based timestamp in the ISO 8601-1 format EN 18223 uses, e.g. `2026-09-03T12:00:00Z`. A value without a UTC designator is refused rather than read in the server's local zone. (required)
        :type var_date: datetime
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dpp_version_by_id_and_date_serialize(
            dpp_id=dpp_id,
            var_date=var_date,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_dpp_version_by_id_and_date_with_http_info(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        var_date: Annotated[datetime, Field(description="The instant the passport is requested for — a UTC-based timestamp in the ISO 8601-1 format EN 18223 uses, e.g. `2026-09-03T12:00:00Z`. A value without a UTC designator is refused rather than read in the server's local zone.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPVersionByIdAndDate — return the DPP version current at a given date

        **EN 18222:2026 clause 4.4, Table 3; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main provider and by a back-up provider — expressly not by the creator. Table 3 notes the method is optional for the economic operator.  The version returned is the one that was current at the instant given: the node archives a snapshot before each change, so the first snapshot recorded after the instant holds the state that was current at it.  **Permission:** `passport:read`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param var_date: The instant the passport is requested for — a UTC-based timestamp in the ISO 8601-1 format EN 18223 uses, e.g. `2026-09-03T12:00:00Z`. A value without a UTC designator is refused rather than read in the server's local zone. (required)
        :type var_date: datetime
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dpp_version_by_id_and_date_serialize(
            dpp_id=dpp_id,
            var_date=var_date,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_dpp_version_by_id_and_date_without_preload_content(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        var_date: Annotated[datetime, Field(description="The instant the passport is requested for — a UTC-based timestamp in the ISO 8601-1 format EN 18223 uses, e.g. `2026-09-03T12:00:00Z`. A value without a UTC designator is refused rather than read in the server's local zone.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPVersionByIdAndDate — return the DPP version current at a given date

        **EN 18222:2026 clause 4.4, Table 3; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **should** be made available by the main provider and by a back-up provider — expressly not by the creator. Table 3 notes the method is optional for the economic operator.  The version returned is the one that was current at the instant given: the node archives a snapshot before each change, so the first snapshot recorded after the instant holds the state that was current at it.  **Permission:** `passport:read`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param var_date: The instant the passport is requested for — a UTC-based timestamp in the ISO 8601-1 format EN 18223 uses, e.g. `2026-09-03T12:00:00Z`. A value without a UTC designator is refused rather than read in the server's local zone. (required)
        :type var_date: datetime
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dpp_version_by_id_and_date_serialize(
            dpp_id=dpp_id,
            var_date=var_date,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _read_dpp_version_by_id_and_date_serialize(
        self,
        dpp_id,
        var_date,
        representation,
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
        if dpp_id is not None:
            _path_params['dppId'] = dpp_id
        # process the query parameters
        if var_date is not None:
            if isinstance(var_date, datetime):
                _query_params.append(
                    (
                        'date',
                        var_date.strftime(
                            self.api_client.configuration.datetime_format
                        )
                    )
                )
            else:
                _query_params.append(('date', var_date))
            
        if representation is not None:
            
            _query_params.append(('representation', representation))
            
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
            resource_path='/v1/dppsByIdAndDate/{dppId}',
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
    def read_dppby_id(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPById — return the DPP with the specified DPP ID

        **EN 18222:2026 clause 4.2, Table 1; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available by the creator of the digital product passport or their main DPP service provider, as well as by a back-up DPP service provider. OpenDPP is declared the **main** provider.  The response is the passport itself: Table 16 reports the result \"except status code\", so the status code is the HTTP status (EN 18222 Table 15) and the body is the payload alone, with no envelope.  **Permission:** `passport:read`. Access rules decide which data elements the response carries — an owner key sees the owner tier, other credentials the public-redacted one.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dppby_id_serialize(
            dpp_id=dpp_id,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_dppby_id_with_http_info(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPById — return the DPP with the specified DPP ID

        **EN 18222:2026 clause 4.2, Table 1; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available by the creator of the digital product passport or their main DPP service provider, as well as by a back-up DPP service provider. OpenDPP is declared the **main** provider.  The response is the passport itself: Table 16 reports the result \"except status code\", so the status code is the HTTP status (EN 18222 Table 15) and the body is the payload alone, with no envelope.  **Permission:** `passport:read`. Access rules decide which data elements the response carries — an owner key sees the owner tier, other credentials the public-redacted one.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dppby_id_serialize(
            dpp_id=dpp_id,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_dppby_id_without_preload_content(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPById — return the DPP with the specified DPP ID

        **EN 18222:2026 clause 4.2, Table 1; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available by the creator of the digital product passport or their main DPP service provider, as well as by a back-up DPP service provider. OpenDPP is declared the **main** provider.  The response is the passport itself: Table 16 reports the result \"except status code\", so the status code is the HTTP status (EN 18222 Table 15) and the body is the payload alone, with no envelope.  **Permission:** `passport:read`. Access rules decide which data elements the response carries — an owner key sees the owner tier, other credentials the public-redacted one.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dppby_id_serialize(
            dpp_id=dpp_id,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _read_dppby_id_serialize(
        self,
        dpp_id,
        representation,
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
        if dpp_id is not None:
            _path_params['dppId'] = dpp_id
        # process the query parameters
        if representation is not None:
            
            _query_params.append(('representation', representation))
            
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
            resource_path='/v1/dpps/{dppId}',
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
    def read_dppby_product_id(
        self,
        product_id: Annotated[str, Field(min_length=1, strict=True, description="The unique product identifier as defined in EN 18219 (GTIN-14, GRAI or the operator's own SKU), percent-encoded as one path segment.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPByProductId — return the current active DPP for a product identifier

        **EN 18222:2026 clause 4.3, Table 2; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available.  Table 2 returns \"the current active DPP (latest version)\" for the unique product identifier as defined in EN 18219. This door reads the product identifier and nothing else — a `digitalProductPassportId` is not accepted, because EN 18222 gives that its own path.  **Permission:** `passport:read`.

        :param product_id: The unique product identifier as defined in EN 18219 (GTIN-14, GRAI or the operator's own SKU), percent-encoded as one path segment. (required)
        :type product_id: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dppby_product_id_serialize(
            product_id=product_id,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_dppby_product_id_with_http_info(
        self,
        product_id: Annotated[str, Field(min_length=1, strict=True, description="The unique product identifier as defined in EN 18219 (GTIN-14, GRAI or the operator's own SKU), percent-encoded as one path segment.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPByProductId — return the current active DPP for a product identifier

        **EN 18222:2026 clause 4.3, Table 2; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available.  Table 2 returns \"the current active DPP (latest version)\" for the unique product identifier as defined in EN 18219. This door reads the product identifier and nothing else — a `digitalProductPassportId` is not accepted, because EN 18222 gives that its own path.  **Permission:** `passport:read`.

        :param product_id: The unique product identifier as defined in EN 18219 (GTIN-14, GRAI or the operator's own SKU), percent-encoded as one path segment. (required)
        :type product_id: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dppby_product_id_serialize(
            product_id=product_id,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def read_dppby_product_id_without_preload_content(
        self,
        product_id: Annotated[str, Field(min_length=1, strict=True, description="The unique product identifier as defined in EN 18219 (GTIN-14, GRAI or the operator's own SKU), percent-encoded as one path segment.")],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """ReadDPPByProductId — return the current active DPP for a product identifier

        **EN 18222:2026 clause 4.3, Table 2; bound by clause 8.2, Table 16.** Clause 4.1 lists this among the methods that **shall** be made available.  Table 2 returns \"the current active DPP (latest version)\" for the unique product identifier as defined in EN 18219. This door reads the product identifier and nothing else — a `digitalProductPassportId` is not accepted, because EN 18222 gives that its own path.  **Permission:** `passport:read`.

        :param product_id: The unique product identifier as defined in EN 18219 (GTIN-14, GRAI or the operator's own SKU), percent-encoded as one path segment. (required)
        :type product_id: str
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._read_dppby_product_id_serialize(
            product_id=product_id,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _read_dppby_product_id_serialize(
        self,
        product_id,
        representation,
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
        if product_id is not None:
            _path_params['productId'] = product_id
        # process the query parameters
        if representation is not None:
            
            _query_params.append(('representation', representation))
            
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
            resource_path='/v1/dppsByProductId/{productId}',
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
    def update_data_element(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        element_id_path: Annotated[str, Field(min_length=1, strict=True, description="The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one.")],
        body: Optional[Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.")] = None,
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
    ) -> object:
        """UpdateDataElement — update, amend or remove one data element

        **EN 18222:2026 clause 6.3, Table 10; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available if authorized third parties have access rights to update parts of the DPP.  Clause 6.3: the method \"also allows to amend or remove data element information\". So a path whose parent exists but whose member does not is **created**, and an explicit `null` body **removes** the element — the RFC 7396 convention clause 8.1 points at. A removal has nothing to return and answers `204` (EN 18222 Table 15 `SuccessNoContent`); any other update answers `200` with the changed element (Table 10).  Elements this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status, the embedded operator and facility nodes — are refused with `403`: they are published in the document but are computed from the record and its seal, not written by the operator.  The write goes through the same passport-mutation seam as every other, so the EN 18221 archiving duty is discharged identically.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param element_id_path: The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
        :type element_id_path: str
        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.
        :type representation: str
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

        _param = self._update_data_element_serialize(
            dpp_id=dpp_id,
            element_id_path=element_id_path,
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "object",
            '204': None,
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def update_data_element_with_http_info(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        element_id_path: Annotated[str, Field(min_length=1, strict=True, description="The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one.")],
        body: Optional[Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.")] = None,
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
    ) -> ApiResponse[object]:
        """UpdateDataElement — update, amend or remove one data element

        **EN 18222:2026 clause 6.3, Table 10; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available if authorized third parties have access rights to update parts of the DPP.  Clause 6.3: the method \"also allows to amend or remove data element information\". So a path whose parent exists but whose member does not is **created**, and an explicit `null` body **removes** the element — the RFC 7396 convention clause 8.1 points at. A removal has nothing to return and answers `204` (EN 18222 Table 15 `SuccessNoContent`); any other update answers `200` with the changed element (Table 10).  Elements this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status, the embedded operator and facility nodes — are refused with `403`: they are published in the document but are computed from the record and its seal, not written by the operator.  The write goes through the same passport-mutation seam as every other, so the EN 18221 archiving duty is discharged identically.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param element_id_path: The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
        :type element_id_path: str
        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.
        :type representation: str
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

        _param = self._update_data_element_serialize(
            dpp_id=dpp_id,
            element_id_path=element_id_path,
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "object",
            '204': None,
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def update_data_element_without_preload_content(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        element_id_path: Annotated[str, Field(min_length=1, strict=True, description="The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one.")],
        body: Optional[Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.")] = None,
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
        """UpdateDataElement — update, amend or remove one data element

        **EN 18222:2026 clause 6.3, Table 10; bound by clause 8.4, Table 18.** Clause 4.1 lists this among the methods that **should** be made available if authorized third parties have access rights to update parts of the DPP.  Clause 6.3: the method \"also allows to amend or remove data element information\". So a path whose parent exists but whose member does not is **created**, and an explicit `null` body **removes** the element — the RFC 7396 convention clause 8.1 points at. A removal has nothing to return and answers `204` (EN 18222 Table 15 `SuccessNoContent`); any other update answers `200` with the changed element (Table 10).  Elements this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status, the embedded operator and facility nodes — are refused with `403`: they are published in the document but are computed from the record and its seal, not written by the operator.  The write goes through the same passport-mutation seam as every other, so the EN 18221 archiving duty is discharged identically.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param element_id_path: The absolute path to the data element, as an RFC 9535 JSONPath (EN 18222 clause 8.1), percent-encoded as one path segment.  **Declared subset.** This node resolves the deterministic single-node subset: the root identifier `$`, name selectors in both spellings (`$.name`, `$['name']`, `$[\"name\"]`) and the index selector including RFC 9535's negative form (`$.materials[0]`, `$.materials[-1]`). The wildcard, descendant (`..`), slice and filter selectors, and a comma union of several selectors, are **refused with `400`** naming the construct used. EN 18222 Table 9 and Table 10 both return a single `DataElement`, and RFC 9535's general grammar produces a nodelist of any length — the series states no rule for what a multi-node address means to a method that returns one element, so this node refuses rather than resolving the first match and presenting it as the addressed one. (required)
        :type element_id_path: str
        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 names this flag on GET, POST and PATCH, so it is accepted here — but **on this method it selects nothing**. The result is an identifier, not a passport document. A value outside `compressed` / `full` / `expanded` is nonetheless refused with `400`: the flag is honoured or refused, never ignored, and silently accepting a value the node did not read would tell you your request was understood when it was not.
        :type representation: str
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

        _param = self._update_data_element_serialize(
            dpp_id=dpp_id,
            element_id_path=element_id_path,
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "object",
            '204': None,
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _update_data_element_serialize(
        self,
        dpp_id,
        element_id_path,
        body,
        representation,
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
        if dpp_id is not None:
            _path_params['dppId'] = dpp_id
        if element_id_path is not None:
            _path_params['elementIdPath'] = element_id_path
        # process the query parameters
        if representation is not None:
            
            _query_params.append(('representation', representation))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if body is not None:
            _body_params = body


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
            method='PATCH',
            resource_path='/v1/dpps/{dppId}/elements/{elementIdPath}',
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
    def update_dppby_id(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        body: Dict[str, Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """UpdateDPPById — partial update of a DPP with a specified DPP ID

        **EN 18222:2026 clause 4.7, Table 6; bound by clause 8.2, Table 16.** Clause 4.1 makes this a **shall** for the creator or their main provider *if authorized third parties have access rights to update parts of the DPP*.  The body is a `partialDPP` — \"only contains data that need to be updated or are extended with new data\". It is applied as an RFC 7396 JSON Merge Patch, which clause 8.1 names for update operations: a member set to `null` is removed, an object member merges recursively, anything else replaces. Data elements may be named directly (they are the passport's own product data) or wrapped in `metadata`.  Clause 4.7 requires that \"if the update of some parts fails the complete update process will fail and there should be no changes adopted in the DPP\" — the merge is computed in full and written in one transaction, so a rejected member changes nothing. It also requires that \"all changes to the digital product passport shall be archived in accordance with EN 18221:2026\": the write goes through this node's single passport-mutation seam, which snapshots the passport as it was and chains a content hash over the previous version.  Members this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status and the embedded operator and facility nodes — are refused with `403` rather than silently ignored.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._update_dppby_id_serialize(
            dpp_id=dpp_id,
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def update_dppby_id_with_http_info(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        body: Dict[str, Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """UpdateDPPById — partial update of a DPP with a specified DPP ID

        **EN 18222:2026 clause 4.7, Table 6; bound by clause 8.2, Table 16.** Clause 4.1 makes this a **shall** for the creator or their main provider *if authorized third parties have access rights to update parts of the DPP*.  The body is a `partialDPP` — \"only contains data that need to be updated or are extended with new data\". It is applied as an RFC 7396 JSON Merge Patch, which clause 8.1 names for update operations: a member set to `null` is removed, an object member merges recursively, anything else replaces. Data elements may be named directly (they are the passport's own product data) or wrapped in `metadata`.  Clause 4.7 requires that \"if the update of some parts fails the complete update process will fail and there should be no changes adopted in the DPP\" — the merge is computed in full and written in one transaction, so a rejected member changes nothing. It also requires that \"all changes to the digital product passport shall be archived in accordance with EN 18221:2026\": the write goes through this node's single passport-mutation seam, which snapshots the passport as it was and chains a content hash over the previous version.  Members this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status and the embedded operator and facility nodes — are refused with `403` rather than silently ignored.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._update_dppby_id_serialize(
            dpp_id=dpp_id,
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
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
    def update_dppby_id_without_preload_content(
        self,
        dpp_id: Annotated[str, Field(min_length=1, strict=True, description="The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`.")],
        body: Dict[str, Any],
        representation: Annotated[Optional[StrictStr], Field(description="EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.")] = None,
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
        """UpdateDPPById — partial update of a DPP with a specified DPP ID

        **EN 18222:2026 clause 4.7, Table 6; bound by clause 8.2, Table 16.** Clause 4.1 makes this a **shall** for the creator or their main provider *if authorized third parties have access rights to update parts of the DPP*.  The body is a `partialDPP` — \"only contains data that need to be updated or are extended with new data\". It is applied as an RFC 7396 JSON Merge Patch, which clause 8.1 names for update operations: a member set to `null` is removed, an object member merges recursively, anything else replaces. Data elements may be named directly (they are the passport's own product data) or wrapped in `metadata`.  Clause 4.7 requires that \"if the update of some parts fails the complete update process will fail and there should be no changes adopted in the DPP\" — the merge is computed in full and written in one transaction, so a rejected member changes nothing. It also requires that \"all changes to the digital product passport shall be archived in accordance with EN 18221:2026\": the write goes through this node's single passport-mutation seam, which snapshots the passport as it was and chains a content hash over the previous version.  Members this node **derives** — the EN 18223 Table 1 header attributes, the seal, the lifecycle status and the embedded operator and facility nodes — are refused with `403` rather than silently ignored.  **Permission:** `passport:update`.

        :param dpp_id: The passport's `digitalProductPassportId` (EN 18223 clause 4.1.2.1) — either this node's resolvable instance URI `{base}/passport/{id}`, percent-encoded as one path segment per EN 18222 clause 8.1, or the bare record id inside it. A product identifier is **not** accepted here: EN 18222 gives that its own path, `/v1/dppsByProductId/{productId}`. (required)
        :type dpp_id: str
        :param body: (required)
        :type body: object
        :param representation: EN 18222 clause 8.1 — `compressed` (the default, EN 18223 clause 5.2) or `full` (EN 18223 Annex A). `expanded` is accepted as an alias of `full`, the word EN 18223 itself uses for the form. A value outside that set is refused, never silently downgraded.
        :type representation: str
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

        _param = self._update_dppby_id_serialize(
            dpp_id=dpp_id,
            body=body,
            representation=representation,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "PublicPassportJsonLd",
            '400': "En18222Result",
            '401': "En18222Result",
            '403': "En18222Result",
            '404': "En18222Result",
            '409': "En18222Result",
            '429': "En18222Result",
            '500': "En18222Result",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _update_dppby_id_serialize(
        self,
        dpp_id,
        body,
        representation,
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
        if dpp_id is not None:
            _path_params['dppId'] = dpp_id
        # process the query parameters
        if representation is not None:
            
            _query_params.append(('representation', representation))
            
        # process the header parameters
        # process the form parameters
        # process the body parameter
        if body is not None:
            _body_params = body


        # set the HTTP header `Accept`
        if 'Accept' not in _header_params:
            _header_params['Accept'] = self.api_client.select_header_accept(
                [
                    'application/ld+json', 
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
            method='PATCH',
            resource_path='/v1/dpps/{dppId}',
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


