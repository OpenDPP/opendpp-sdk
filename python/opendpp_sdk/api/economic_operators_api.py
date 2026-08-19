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
from opendpp_sdk.models.delete_operator_response import DeleteOperatorResponse
from opendpp_sdk.models.operator_get_response import OperatorGetResponse
from opendpp_sdk.models.operator_list_response import OperatorListResponse
from opendpp_sdk.models.register_operator_request import RegisterOperatorRequest
from opendpp_sdk.models.register_operator_response import RegisterOperatorResponse
from opendpp_sdk.models.restore_operator_response import RestoreOperatorResponse
from opendpp_sdk.models.update_operator_request import UpdateOperatorRequest
from opendpp_sdk.models.update_operator_response import UpdateOperatorResponse

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class EconomicOperatorsApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def delete_operator(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
    ) -> DeleteOperatorResponse:
        """Remove an operator (archives if it has passports, else hard-deletes)

        Removes an operator, choosing automatically between two outcomes (ESPR passport-persistence compliance — an operator that still has passports must never be hard-deleted):  - **Archive (soft delete)** — if the operator has one or more passports, it is archived instead of deleted: `archivedAt` is set on the operator and every active passport of the operator is archived with a `retentionUntil` deadline set to a platform-configured retention period from now (default 15 years). Archived passports remain **publicly resolvable** (the persistence duty) but are excluded from active management lists. Response: `{success: true, archived: true, archivedPassports: <n>}`. Fully reversible via `POST /api/v1/operators/{id}/restore`. - **Hard delete** — if the operator has no passports it is permanently deleted (tenant bindings cascade-delete; user/facility/API-key references are set to null). Response: `{success: true, archived: false}` — no `archivedPassports` field. - **Fallback** — if the hard delete fails on a residual foreign-key reference, the operator is archived instead and the response is `{success: true, archived: true}` **without** `archivedPassports`. If even the fallback archive fails, `409` is returned.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt. The operator must be bound to your workspace (`404` otherwise).  **Tenant-scoped:** this affects only **your** workspace's operator and passports — operators are not shared across workspaces.  Side effects: an `operator.archived` or `operator.deleted` audit event plus an in-app notification — on the primary archive and hard-delete paths only; the foreign-key fallback archive writes **no** audit event or notification. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._delete_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DeleteOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
            '409': "OperatorMinimalError",
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
    def delete_operator_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
    ) -> ApiResponse[DeleteOperatorResponse]:
        """Remove an operator (archives if it has passports, else hard-deletes)

        Removes an operator, choosing automatically between two outcomes (ESPR passport-persistence compliance — an operator that still has passports must never be hard-deleted):  - **Archive (soft delete)** — if the operator has one or more passports, it is archived instead of deleted: `archivedAt` is set on the operator and every active passport of the operator is archived with a `retentionUntil` deadline set to a platform-configured retention period from now (default 15 years). Archived passports remain **publicly resolvable** (the persistence duty) but are excluded from active management lists. Response: `{success: true, archived: true, archivedPassports: <n>}`. Fully reversible via `POST /api/v1/operators/{id}/restore`. - **Hard delete** — if the operator has no passports it is permanently deleted (tenant bindings cascade-delete; user/facility/API-key references are set to null). Response: `{success: true, archived: false}` — no `archivedPassports` field. - **Fallback** — if the hard delete fails on a residual foreign-key reference, the operator is archived instead and the response is `{success: true, archived: true}` **without** `archivedPassports`. If even the fallback archive fails, `409` is returned.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt. The operator must be bound to your workspace (`404` otherwise).  **Tenant-scoped:** this affects only **your** workspace's operator and passports — operators are not shared across workspaces.  Side effects: an `operator.archived` or `operator.deleted` audit event plus an in-app notification — on the primary archive and hard-delete paths only; the foreign-key fallback archive writes **no** audit event or notification. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._delete_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DeleteOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
            '409': "OperatorMinimalError",
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
    def delete_operator_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
        """Remove an operator (archives if it has passports, else hard-deletes)

        Removes an operator, choosing automatically between two outcomes (ESPR passport-persistence compliance — an operator that still has passports must never be hard-deleted):  - **Archive (soft delete)** — if the operator has one or more passports, it is archived instead of deleted: `archivedAt` is set on the operator and every active passport of the operator is archived with a `retentionUntil` deadline set to a platform-configured retention period from now (default 15 years). Archived passports remain **publicly resolvable** (the persistence duty) but are excluded from active management lists. Response: `{success: true, archived: true, archivedPassports: <n>}`. Fully reversible via `POST /api/v1/operators/{id}/restore`. - **Hard delete** — if the operator has no passports it is permanently deleted (tenant bindings cascade-delete; user/facility/API-key references are set to null). Response: `{success: true, archived: false}` — no `archivedPassports` field. - **Fallback** — if the hard delete fails on a residual foreign-key reference, the operator is archived instead and the response is `{success: true, archived: true}` **without** `archivedPassports`. If even the fallback archive fails, `409` is returned.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt. The operator must be bound to your workspace (`404` otherwise).  **Tenant-scoped:** this affects only **your** workspace's operator and passports — operators are not shared across workspaces.  Side effects: an `operator.archived` or `operator.deleted` audit event plus an in-app notification — on the primary archive and hard-delete paths only; the foreign-key fallback archive writes **no** audit event or notification. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._delete_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DeleteOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
            '409': "OperatorMinimalError",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _delete_operator_serialize(
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
            resource_path='/api/v1/operators/{id}',
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
    def get_operator(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
    ) -> OperatorGetResponse:
        """Fetch a single bound economic operator

        Fetches one economic operator by UUID, scoped to your workspace (`404` if no operator with that id exists in your workspace).  **Permission:** `operator:read`. An **operator-scoped API key** may only fetch its own operator (`403` otherwise).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._get_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "OperatorGetResponse",
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
    def get_operator_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
    ) -> ApiResponse[OperatorGetResponse]:
        """Fetch a single bound economic operator

        Fetches one economic operator by UUID, scoped to your workspace (`404` if no operator with that id exists in your workspace).  **Permission:** `operator:read`. An **operator-scoped API key** may only fetch its own operator (`403` otherwise).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._get_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "OperatorGetResponse",
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
    def get_operator_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
        """Fetch a single bound economic operator

        Fetches one economic operator by UUID, scoped to your workspace (`404` if no operator with that id exists in your workspace).  **Permission:** `operator:read`. An **operator-scoped API key** may only fetch its own operator (`403` otherwise).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._get_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "OperatorGetResponse",
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


    def _get_operator_serialize(
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
            resource_path='/api/v1/operators/{id}',
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
    def list_operators(
        self,
        archived: Annotated[Optional[StrictStr], Field(description="Set `true` to include archived (off-boarded) operators. Default returns active operators only.")] = None,
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
    ) -> OperatorListResponse:
        """List economic operators bound to your workspace

        Returns the economic operators bound to your workspace, ordered by name. Active operators only unless `?archived=true` is passed (archived operators are off-boarded but their passports are retained and still publicly resolvable).  Each entry is the same `OperatorRow` shape returned by `POST`/`PATCH /api/v1/operators` — use the `id` to attribute a passport (`operatorId` on `POST /api/v1/passports`) or to address `PATCH`/`DELETE`.  **Permission:** `operator:read`. Requests authenticated with an **operator-scoped API key** see only their own operator.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param archived: Set `true` to include archived (off-boarded) operators. Default returns active operators only.
        :type archived: str
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

        _param = self._list_operators_serialize(
            archived=archived,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "OperatorListResponse",
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
    def list_operators_with_http_info(
        self,
        archived: Annotated[Optional[StrictStr], Field(description="Set `true` to include archived (off-boarded) operators. Default returns active operators only.")] = None,
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
    ) -> ApiResponse[OperatorListResponse]:
        """List economic operators bound to your workspace

        Returns the economic operators bound to your workspace, ordered by name. Active operators only unless `?archived=true` is passed (archived operators are off-boarded but their passports are retained and still publicly resolvable).  Each entry is the same `OperatorRow` shape returned by `POST`/`PATCH /api/v1/operators` — use the `id` to attribute a passport (`operatorId` on `POST /api/v1/passports`) or to address `PATCH`/`DELETE`.  **Permission:** `operator:read`. Requests authenticated with an **operator-scoped API key** see only their own operator.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param archived: Set `true` to include archived (off-boarded) operators. Default returns active operators only.
        :type archived: str
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

        _param = self._list_operators_serialize(
            archived=archived,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "OperatorListResponse",
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
    def list_operators_without_preload_content(
        self,
        archived: Annotated[Optional[StrictStr], Field(description="Set `true` to include archived (off-boarded) operators. Default returns active operators only.")] = None,
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
        """List economic operators bound to your workspace

        Returns the economic operators bound to your workspace, ordered by name. Active operators only unless `?archived=true` is passed (archived operators are off-boarded but their passports are retained and still publicly resolvable).  Each entry is the same `OperatorRow` shape returned by `POST`/`PATCH /api/v1/operators` — use the `id` to attribute a passport (`operatorId` on `POST /api/v1/passports`) or to address `PATCH`/`DELETE`.  **Permission:** `operator:read`. Requests authenticated with an **operator-scoped API key** see only their own operator.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param archived: Set `true` to include archived (off-boarded) operators. Default returns active operators only.
        :type archived: str
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

        _param = self._list_operators_serialize(
            archived=archived,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "OperatorListResponse",
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


    def _list_operators_serialize(
        self,
        archived,
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
        if archived is not None:
            
            _query_params.append(('archived', archived))
            
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
            resource_path='/api/v1/operators',
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
    def register_operator(
        self,
        register_operator_request: RegisterOperatorRequest,
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
    ) -> RegisterOperatorResponse:
        """Register an economic operator and bind it to your workspace

        Registers an economic operator (manufacturer, importer, supplier, …) and binds it to your workspace.  **Permission:** `operator:create`. Cookie-session clients must send the `X-CSRF-Token` header (double-submit); Bearer clients (API key / JWT) are exempt.  **Deduplication (per workspace):** operators are scoped to your workspace — `regId` is unique *within* your workspace, not across the platform. If **your workspace** already has an operator with the submitted `regId`, that existing record is returned and the submitted `name`, `role` and `regIdScheme` are **ignored**. A `regId` already used by *another* workspace is irrelevant — you always get **your own** operator row (so one workspace can never bind to, rename, or archive another's operator). The call is idempotent: re-registering an already-bound operator succeeds with `201` again. The per-workspace match includes **archived** operators: if your workspace's operator for that `regId` is archived, the archived record is returned as-is (`archivedAt` non-null) with `201` — registration does not un-archive it; use `POST /api/v1/operators/{id}/restore` to reactivate it.  **Registration-id integrity:** fabricated `EORI-MOCK…` ids are rejected on every path. When `regIdScheme` is `EORI`, `regId` must match `^[A-Z]{2}[A-Za-z0-9]{1,15}$` (2-letter ISO 3166 country prefix followed by up to 15 alphanumerics, e.g. `DE1234567890`). Validation is syntax-only by default. When the node operator enables the OPT-IN EORI existence check, a declared `EORI` `regId` is additionally checked for EXISTENCE against the EU Commission EOS validation service and, if not found, a NON-BLOCKING advisory is added to the 201 `warnings[]` — the operator is still registered (best-effort, fail-open: a network error, or a freshly-issued / GB EORI, never blocks registration). With the check off, `warnings` is `[]`.  Side effects: an `operator.created` audit event and an in-app notification are recorded.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param register_operator_request: (required)
        :type register_operator_request: RegisterOperatorRequest
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

        _param = self._register_operator_serialize(
            register_operator_request=register_operator_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "RegisterOperatorResponse",
            '400': "OperatorMinimalErrorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '429': "InlineObject",
            '500': "OperatorMinimalErrorResponse",
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
    def register_operator_with_http_info(
        self,
        register_operator_request: RegisterOperatorRequest,
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
    ) -> ApiResponse[RegisterOperatorResponse]:
        """Register an economic operator and bind it to your workspace

        Registers an economic operator (manufacturer, importer, supplier, …) and binds it to your workspace.  **Permission:** `operator:create`. Cookie-session clients must send the `X-CSRF-Token` header (double-submit); Bearer clients (API key / JWT) are exempt.  **Deduplication (per workspace):** operators are scoped to your workspace — `regId` is unique *within* your workspace, not across the platform. If **your workspace** already has an operator with the submitted `regId`, that existing record is returned and the submitted `name`, `role` and `regIdScheme` are **ignored**. A `regId` already used by *another* workspace is irrelevant — you always get **your own** operator row (so one workspace can never bind to, rename, or archive another's operator). The call is idempotent: re-registering an already-bound operator succeeds with `201` again. The per-workspace match includes **archived** operators: if your workspace's operator for that `regId` is archived, the archived record is returned as-is (`archivedAt` non-null) with `201` — registration does not un-archive it; use `POST /api/v1/operators/{id}/restore` to reactivate it.  **Registration-id integrity:** fabricated `EORI-MOCK…` ids are rejected on every path. When `regIdScheme` is `EORI`, `regId` must match `^[A-Z]{2}[A-Za-z0-9]{1,15}$` (2-letter ISO 3166 country prefix followed by up to 15 alphanumerics, e.g. `DE1234567890`). Validation is syntax-only by default. When the node operator enables the OPT-IN EORI existence check, a declared `EORI` `regId` is additionally checked for EXISTENCE against the EU Commission EOS validation service and, if not found, a NON-BLOCKING advisory is added to the 201 `warnings[]` — the operator is still registered (best-effort, fail-open: a network error, or a freshly-issued / GB EORI, never blocks registration). With the check off, `warnings` is `[]`.  Side effects: an `operator.created` audit event and an in-app notification are recorded.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param register_operator_request: (required)
        :type register_operator_request: RegisterOperatorRequest
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

        _param = self._register_operator_serialize(
            register_operator_request=register_operator_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "RegisterOperatorResponse",
            '400': "OperatorMinimalErrorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '429': "InlineObject",
            '500': "OperatorMinimalErrorResponse",
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
    def register_operator_without_preload_content(
        self,
        register_operator_request: RegisterOperatorRequest,
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
        """Register an economic operator and bind it to your workspace

        Registers an economic operator (manufacturer, importer, supplier, …) and binds it to your workspace.  **Permission:** `operator:create`. Cookie-session clients must send the `X-CSRF-Token` header (double-submit); Bearer clients (API key / JWT) are exempt.  **Deduplication (per workspace):** operators are scoped to your workspace — `regId` is unique *within* your workspace, not across the platform. If **your workspace** already has an operator with the submitted `regId`, that existing record is returned and the submitted `name`, `role` and `regIdScheme` are **ignored**. A `regId` already used by *another* workspace is irrelevant — you always get **your own** operator row (so one workspace can never bind to, rename, or archive another's operator). The call is idempotent: re-registering an already-bound operator succeeds with `201` again. The per-workspace match includes **archived** operators: if your workspace's operator for that `regId` is archived, the archived record is returned as-is (`archivedAt` non-null) with `201` — registration does not un-archive it; use `POST /api/v1/operators/{id}/restore` to reactivate it.  **Registration-id integrity:** fabricated `EORI-MOCK…` ids are rejected on every path. When `regIdScheme` is `EORI`, `regId` must match `^[A-Z]{2}[A-Za-z0-9]{1,15}$` (2-letter ISO 3166 country prefix followed by up to 15 alphanumerics, e.g. `DE1234567890`). Validation is syntax-only by default. When the node operator enables the OPT-IN EORI existence check, a declared `EORI` `regId` is additionally checked for EXISTENCE against the EU Commission EOS validation service and, if not found, a NON-BLOCKING advisory is added to the 201 `warnings[]` — the operator is still registered (best-effort, fail-open: a network error, or a freshly-issued / GB EORI, never blocks registration). With the check off, `warnings` is `[]`.  Side effects: an `operator.created` audit event and an in-app notification are recorded.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param register_operator_request: (required)
        :type register_operator_request: RegisterOperatorRequest
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

        _param = self._register_operator_serialize(
            register_operator_request=register_operator_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '201': "RegisterOperatorResponse",
            '400': "OperatorMinimalErrorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '429': "InlineObject",
            '500': "OperatorMinimalErrorResponse",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _register_operator_serialize(
        self,
        register_operator_request,
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
        if register_operator_request is not None:
            _body_params = register_operator_request


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
            resource_path='/api/v1/operators',
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
    def restore_operator(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
    ) -> RestoreOperatorResponse:
        """Restore an archived operator and its archived passports

        Un-archives an operator that was soft-deleted by `DELETE /api/v1/operators/{id}` and brings its archived passports back into the active catalogue: clears the operator's `archivedAt`, then clears `archivedAt` and `retentionUntil` on every archived passport of the operator **except** passports that were independently `DECOMMISSIONED` (those keep their own retention clock and stay archived).  Safe to call on a non-archived operator — it simply restores any archived passports the operator may have (`restoredPassports` may be `0`). No request body.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt. `404` if the operator is not bound to your workspace.  Side effects: an `operator.restored` audit event and an in-app notification. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._restore_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "RestoreOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
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
    def restore_operator_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
    ) -> ApiResponse[RestoreOperatorResponse]:
        """Restore an archived operator and its archived passports

        Un-archives an operator that was soft-deleted by `DELETE /api/v1/operators/{id}` and brings its archived passports back into the active catalogue: clears the operator's `archivedAt`, then clears `archivedAt` and `retentionUntil` on every archived passport of the operator **except** passports that were independently `DECOMMISSIONED` (those keep their own retention clock and stay archived).  Safe to call on a non-archived operator — it simply restores any archived passports the operator may have (`restoredPassports` may be `0`). No request body.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt. `404` if the operator is not bound to your workspace.  Side effects: an `operator.restored` audit event and an in-app notification. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._restore_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "RestoreOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
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
    def restore_operator_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
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
        """Restore an archived operator and its archived passports

        Un-archives an operator that was soft-deleted by `DELETE /api/v1/operators/{id}` and brings its archived passports back into the active catalogue: clears the operator's `archivedAt`, then clears `archivedAt` and `retentionUntil` on every archived passport of the operator **except** passports that were independently `DECOMMISSIONED` (those keep their own retention clock and stay archived).  Safe to call on a non-archived operator — it simply restores any archived passports the operator may have (`restoredPassports` may be `0`). No request body.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt. `404` if the operator is not bound to your workspace.  Side effects: an `operator.restored` audit event and an in-app notification. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
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

        _param = self._restore_operator_serialize(
            id=id,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "RestoreOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _restore_operator_serialize(
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
            resource_path='/api/v1/operators/{id}/restore',
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
    def update_operator(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
        update_operator_request: Optional[UpdateOperatorRequest] = None,
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
    ) -> UpdateOperatorResponse:
        """Update an operator's name or role (regId is immutable)

        Edits an operator bound to your workspace. Only `name` and `role` are editable; `regId` is the legal registry identifier and is **intentionally immutable** here (register the operator again under the correct id instead). Non-string or whitespace-only values are silently ignored; submitted values are trimmed. If no usable field is supplied (every field missing, non-string, or whitespace-only — including an empty object `{}` or an omitted body), the current operator row is returned unchanged with `200` and no audit event is written. The handler does not diff against current values: supplying a value identical to the current one still performs an update and writes an audit event.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt.  **Tenant-scoped:** operators are scoped to your workspace — `regId` is not globally unique. Registering a `regId` that another workspace also uses creates **your own** operator row; `name`/`role` edits never affect another workspace.  When a change is applied, an `operator.updated` audit event is recorded. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
        :type id: str
        :param update_operator_request:
        :type update_operator_request: UpdateOperatorRequest
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

        _param = self._update_operator_serialize(
            id=id,
            update_operator_request=update_operator_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "UpdateOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
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
    def update_operator_with_http_info(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
        update_operator_request: Optional[UpdateOperatorRequest] = None,
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
    ) -> ApiResponse[UpdateOperatorResponse]:
        """Update an operator's name or role (regId is immutable)

        Edits an operator bound to your workspace. Only `name` and `role` are editable; `regId` is the legal registry identifier and is **intentionally immutable** here (register the operator again under the correct id instead). Non-string or whitespace-only values are silently ignored; submitted values are trimmed. If no usable field is supplied (every field missing, non-string, or whitespace-only — including an empty object `{}` or an omitted body), the current operator row is returned unchanged with `200` and no audit event is written. The handler does not diff against current values: supplying a value identical to the current one still performs an update and writes an audit event.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt.  **Tenant-scoped:** operators are scoped to your workspace — `regId` is not globally unique. Registering a `regId` that another workspace also uses creates **your own** operator row; `name`/`role` edits never affect another workspace.  When a change is applied, an `operator.updated` audit event is recorded. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
        :type id: str
        :param update_operator_request:
        :type update_operator_request: UpdateOperatorRequest
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

        _param = self._update_operator_serialize(
            id=id,
            update_operator_request=update_operator_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "UpdateOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
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
    def update_operator_without_preload_content(
        self,
        id: Annotated[StrictStr, Field(description="Operator UUID (`EconomicOperator.id`). Must be bound to your workspace.")],
        update_operator_request: Optional[UpdateOperatorRequest] = None,
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
        """Update an operator's name or role (regId is immutable)

        Edits an operator bound to your workspace. Only `name` and `role` are editable; `regId` is the legal registry identifier and is **intentionally immutable** here (register the operator again under the correct id instead). Non-string or whitespace-only values are silently ignored; submitted values are trimmed. If no usable field is supplied (every field missing, non-string, or whitespace-only — including an empty object `{}` or an omitted body), the current operator row is returned unchanged with `200` and no audit event is written. The handler does not diff against current values: supplying a value identical to the current one still performs an update and writes an audit event.  **Permission:** `operator:write`. Cookie-session clients must send `X-CSRF-Token`; Bearer clients are exempt.  **Tenant-scoped:** operators are scoped to your workspace — `regId` is not globally unique. Registering a `regId` that another workspace also uses creates **your own** operator row; `name`/`role` edits never affect another workspace.  When a change is applied, an `operator.updated` audit event is recorded. Unhandled database errors are normalized by the global error handler to the standard `{success: false, error, message}` envelope with a generic message (details are logged server-side).  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param id: Operator UUID (`EconomicOperator.id`). Must be bound to your workspace. (required)
        :type id: str
        :param update_operator_request:
        :type update_operator_request: UpdateOperatorRequest
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

        _param = self._update_operator_serialize(
            id=id,
            update_operator_request=update_operator_request,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "UpdateOperatorResponse",
            '401': "Error",
            '402': "PassportQuotaError",
            '403': "Error",
            '404': "OperatorMinimalError",
            '429': "InlineObject",
            '500': "Error",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _update_operator_serialize(
        self,
        id,
        update_operator_request,
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
        if update_operator_request is not None:
            _body_params = update_operator_request


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
            resource_path='/api/v1/operators/{id}',
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


