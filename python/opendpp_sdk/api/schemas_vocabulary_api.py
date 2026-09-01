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
from opendpp_sdk.models.dpp_json_ld_context_document import DppJsonLdContextDocument
from opendpp_sdk.models.dpp_vocab_context_document import DppVocabContextDocument
from opendpp_sdk.models.material_vocabulary_list_response import MaterialVocabularyListResponse
from opendpp_sdk.models.sector_vocabulary_context import SectorVocabularyContext

from opendpp_sdk.api_client import ApiClient, RequestSerialized
from opendpp_sdk.api_response import ApiResponse
from opendpp_sdk.rest import RESTResponseType


class SchemasVocabularyApi:
    """NOTE: This class is auto generated by OpenAPI Generator
    Ref: https://openapi-generator.tech

    Do not edit the class manually.
    """

    def __init__(self, api_client=None) -> None:
        if api_client is None:
            api_client = ApiClient.get_default()
        self.api_client = api_client


    @validate_call
    def get_dpp_json_ld_context(
        self,
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
    ) -> DppVocabContextDocument:
        """Canonical resolvable JSON-LD context for passport & unit documents

        Serves the stable, resolvable W3C JSON-LD `@context` (`application/ld+json`) that **every** public passport and battery-unit JSON-LD document references in its `@context` array — this is the context to dereference when expanding OpenDPP JSON-LD. It declares `@vocab: https://opendpp-node.eu/ns/dpp#` (so even dynamic metadata keys expand under the OpenDPP namespace and are never silently dropped by a strict JSON-LD processor) plus explicit term mappings for the core DPP/unit vocabulary (`DigitalProductPassport`, `BatteryUnit`, `economicOperator`, `manufacturingFacility`, `metadata`, `digitalSeal`, `signingPublicKey`, `proof`, `status`, `MerkleTreeAttestationProof`). Cacheable (`Cache-Control: public, max-age=86400`).  (The separate `/context/v1` serves an older, fixed term-only list and is **not** the context emitted documents point to.)  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers).

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

        _param = self._get_dpp_json_ld_context_serialize(
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DppVocabContextDocument",
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
    def get_dpp_json_ld_context_with_http_info(
        self,
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
    ) -> ApiResponse[DppVocabContextDocument]:
        """Canonical resolvable JSON-LD context for passport & unit documents

        Serves the stable, resolvable W3C JSON-LD `@context` (`application/ld+json`) that **every** public passport and battery-unit JSON-LD document references in its `@context` array — this is the context to dereference when expanding OpenDPP JSON-LD. It declares `@vocab: https://opendpp-node.eu/ns/dpp#` (so even dynamic metadata keys expand under the OpenDPP namespace and are never silently dropped by a strict JSON-LD processor) plus explicit term mappings for the core DPP/unit vocabulary (`DigitalProductPassport`, `BatteryUnit`, `economicOperator`, `manufacturingFacility`, `metadata`, `digitalSeal`, `signingPublicKey`, `proof`, `status`, `MerkleTreeAttestationProof`). Cacheable (`Cache-Control: public, max-age=86400`).  (The separate `/context/v1` serves an older, fixed term-only list and is **not** the context emitted documents point to.)  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers).

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

        _param = self._get_dpp_json_ld_context_serialize(
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DppVocabContextDocument",
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
    def get_dpp_json_ld_context_without_preload_content(
        self,
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
        """Canonical resolvable JSON-LD context for passport & unit documents

        Serves the stable, resolvable W3C JSON-LD `@context` (`application/ld+json`) that **every** public passport and battery-unit JSON-LD document references in its `@context` array — this is the context to dereference when expanding OpenDPP JSON-LD. It declares `@vocab: https://opendpp-node.eu/ns/dpp#` (so even dynamic metadata keys expand under the OpenDPP namespace and are never silently dropped by a strict JSON-LD processor) plus explicit term mappings for the core DPP/unit vocabulary (`DigitalProductPassport`, `BatteryUnit`, `economicOperator`, `manufacturingFacility`, `metadata`, `digitalSeal`, `signingPublicKey`, `proof`, `status`, `MerkleTreeAttestationProof`). Cacheable (`Cache-Control: public, max-age=86400`).  (The separate `/context/v1` serves an older, fixed term-only list and is **not** the context emitted documents point to.)  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers).

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

        _param = self._get_dpp_json_ld_context_serialize(
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DppVocabContextDocument",
            '429': "InlineObject",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _get_dpp_json_ld_context_serialize(
        self,
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
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/contexts/dpp/v1',
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
    def get_json_ld_context(
        self,
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
    ) -> DppJsonLdContextDocument:
        """W3C JSON-LD context document for passport terms (secondary, fixed term list)

        Serves a static W3C JSON-LD `@context` document (`application/ld+json`) for the core Digital Product Passport term vocabulary: maps the DPP terms to `https://opendpp-node.eu/ns/dpp#…` IRIs and `createdAt`/`updatedAt` to schema.org `dateCreated`/`dateModified`. This is a **secondary** fixed term list — the context that public passport/unit JSON-LD documents actually reference is the `@vocab`-based one at `GET /contexts/dpp/v1`; dereference that when expanding OpenDPP JSON-LD.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers). Like every documented path except `/health`, a request on an unknown tenant workspace host receives a platform-level JSON 404 before this handler runs.

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

        _param = self._get_json_ld_context_serialize(
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DppJsonLdContextDocument",
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
    def get_json_ld_context_with_http_info(
        self,
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
    ) -> ApiResponse[DppJsonLdContextDocument]:
        """W3C JSON-LD context document for passport terms (secondary, fixed term list)

        Serves a static W3C JSON-LD `@context` document (`application/ld+json`) for the core Digital Product Passport term vocabulary: maps the DPP terms to `https://opendpp-node.eu/ns/dpp#…` IRIs and `createdAt`/`updatedAt` to schema.org `dateCreated`/`dateModified`. This is a **secondary** fixed term list — the context that public passport/unit JSON-LD documents actually reference is the `@vocab`-based one at `GET /contexts/dpp/v1`; dereference that when expanding OpenDPP JSON-LD.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers). Like every documented path except `/health`, a request on an unknown tenant workspace host receives a platform-level JSON 404 before this handler runs.

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

        _param = self._get_json_ld_context_serialize(
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DppJsonLdContextDocument",
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
    def get_json_ld_context_without_preload_content(
        self,
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
        """W3C JSON-LD context document for passport terms (secondary, fixed term list)

        Serves a static W3C JSON-LD `@context` document (`application/ld+json`) for the core Digital Product Passport term vocabulary: maps the DPP terms to `https://opendpp-node.eu/ns/dpp#…` IRIs and `createdAt`/`updatedAt` to schema.org `dateCreated`/`dateModified`. This is a **secondary** fixed term list — the context that public passport/unit JSON-LD documents actually reference is the `@vocab`-based one at `GET /contexts/dpp/v1`; dereference that when expanding OpenDPP JSON-LD.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers). Like every documented path except `/health`, a request on an unknown tenant workspace host receives a platform-level JSON 404 before this handler runs.

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

        _param = self._get_json_ld_context_serialize(
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "DppJsonLdContextDocument",
            '429': "InlineObject",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _get_json_ld_context_serialize(
        self,
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
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/context/v1',
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
    def get_sector_schema(
        self,
        category: Annotated[StrictStr, Field(description="ESPR product category (case-insensitive). Every value in the enum has a published JSON Schema, including `cosmetics`, `toys`, `iron-steel` and `aluminium`; a category outside the enum returns 404.")],
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
    ) -> SectorVocabularyContext:
        """Get the ESPR metadata schema for a product category

        Returns the machine-readable ESPR `metadata` schema for a product category.  **Default representation** (any `Accept` NOT containing `application/ld+json`): the category's JSON Schema **draft-07** document, served as `application/schema+json`, with each known field annotated server-side with a plain-English `description` (the annotations are AJV-ignored — validation behavior is unchanged). **With `Accept: application/ld+json`:** a small JSON-LD `@context` for the category vocabulary instead. Note: the route does not set `Vary: Accept`.  The category path segment is lower-cased before lookup. Machine-readable schemas exist for **5** of the 9 ESPR categories: `textiles`, `batteries`, `electronics`, `chemicals`, `construction`. The remaining 4 categories accepted by passport metadata validation (`cosmetics`, `toys`, `iron-steel`, `aluminium`) are validated by built-in server rules and currently return `404` from this endpoint.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers). Like every documented path except `/health`, a request on an unknown tenant workspace host receives a platform-level JSON 404 (`No tenant company found for subdomain: …`, no `success` field) before this handler runs.

        :param category: ESPR product category (case-insensitive). Every value in the enum has a published JSON Schema, including `cosmetics`, `toys`, `iron-steel` and `aluminium`; a category outside the enum returns 404. (required)
        :type category: str
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

        _param = self._get_sector_schema_serialize(
            category=category,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "SectorVocabularyContext",
            '404': "Error",
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
    def get_sector_schema_with_http_info(
        self,
        category: Annotated[StrictStr, Field(description="ESPR product category (case-insensitive). Every value in the enum has a published JSON Schema, including `cosmetics`, `toys`, `iron-steel` and `aluminium`; a category outside the enum returns 404.")],
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
    ) -> ApiResponse[SectorVocabularyContext]:
        """Get the ESPR metadata schema for a product category

        Returns the machine-readable ESPR `metadata` schema for a product category.  **Default representation** (any `Accept` NOT containing `application/ld+json`): the category's JSON Schema **draft-07** document, served as `application/schema+json`, with each known field annotated server-side with a plain-English `description` (the annotations are AJV-ignored — validation behavior is unchanged). **With `Accept: application/ld+json`:** a small JSON-LD `@context` for the category vocabulary instead. Note: the route does not set `Vary: Accept`.  The category path segment is lower-cased before lookup. Machine-readable schemas exist for **5** of the 9 ESPR categories: `textiles`, `batteries`, `electronics`, `chemicals`, `construction`. The remaining 4 categories accepted by passport metadata validation (`cosmetics`, `toys`, `iron-steel`, `aluminium`) are validated by built-in server rules and currently return `404` from this endpoint.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers). Like every documented path except `/health`, a request on an unknown tenant workspace host receives a platform-level JSON 404 (`No tenant company found for subdomain: …`, no `success` field) before this handler runs.

        :param category: ESPR product category (case-insensitive). Every value in the enum has a published JSON Schema, including `cosmetics`, `toys`, `iron-steel` and `aluminium`; a category outside the enum returns 404. (required)
        :type category: str
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

        _param = self._get_sector_schema_serialize(
            category=category,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "SectorVocabularyContext",
            '404': "Error",
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
    def get_sector_schema_without_preload_content(
        self,
        category: Annotated[StrictStr, Field(description="ESPR product category (case-insensitive). Every value in the enum has a published JSON Schema, including `cosmetics`, `toys`, `iron-steel` and `aluminium`; a category outside the enum returns 404.")],
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
        """Get the ESPR metadata schema for a product category

        Returns the machine-readable ESPR `metadata` schema for a product category.  **Default representation** (any `Accept` NOT containing `application/ld+json`): the category's JSON Schema **draft-07** document, served as `application/schema+json`, with each known field annotated server-side with a plain-English `description` (the annotations are AJV-ignored — validation behavior is unchanged). **With `Accept: application/ld+json`:** a small JSON-LD `@context` for the category vocabulary instead. Note: the route does not set `Vary: Accept`.  The category path segment is lower-cased before lookup. Machine-readable schemas exist for **5** of the 9 ESPR categories: `textiles`, `batteries`, `electronics`, `chemicals`, `construction`. The remaining 4 categories accepted by passport metadata validation (`cosmetics`, `toys`, `iron-steel`, `aluminium`) are validated by built-in server rules and currently return `404` from this endpoint.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard `x-ratelimit-*` headers). Like every documented path except `/health`, a request on an unknown tenant workspace host receives a platform-level JSON 404 (`No tenant company found for subdomain: …`, no `success` field) before this handler runs.

        :param category: ESPR product category (case-insensitive). Every value in the enum has a published JSON Schema, including `cosmetics`, `toys`, `iron-steel` and `aluminium`; a category outside the enum returns 404. (required)
        :type category: str
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

        _param = self._get_sector_schema_serialize(
            category=category,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "SectorVocabularyContext",
            '404': "Error",
            '429': "InlineObject",
        }
        response_data = self.api_client.call_api(
            *_param,
            _request_timeout=_request_timeout
        )
        return response_data.response


    def _get_sector_schema_serialize(
        self,
        category,
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
        if category is not None:
            _path_params['category'] = category
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
        ]

        return self.api_client.param_serialize(
            method='GET',
            resource_path='/api/v1/schemas/{category}',
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
    def list_materials(
        self,
        kind: Annotated[Optional[StrictStr], Field(description="Filter by vocabulary kind. Unrecognized values are silently ignored (no error; the filter is not applied).")] = None,
        search: Annotated[Optional[StrictStr], Field(description="Case-insensitive substring match on the entry `name` (value is trimmed; blank values ignored).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=1000, strict=True, ge=1)]], Field(description="Maximum number of entries to return. Clamped to 1–1000 (out-of-range numeric values are clamped, not rejected); a non-numeric value falls back to the default 1000.")] = None,
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
    ) -> MaterialVocabularyListResponse:
        """List the platform-curated material vocabulary

        Lists active entries from the platform-global material vocabulary that powers the searchable material/fiber/chemistry pickers in the passport form. This is shared reference data, deliberately **not** tenant-scoped, so DPP data stays comparable across tenants.  **Auth:** any authenticated session — Bearer API key, Bearer JWT, or browser session. **No specific permission string is required** and subscription status is not checked, so this endpoint never returns 402. On a tenant-subdomain host, credentials belonging to a different tenant receive **403** with message `Cross-tenant access blocked.`.  **Filtering & ordering:** `kind` filters by vocabulary kind — an unrecognized value is **silently ignored** (the filter simply isn't applied; no 400). `search` is a trimmed, case-insensitive substring match on `name` (blank values ignored). Only active entries are returned, ordered by `kind` ascending then `name` ascending. `limit` is clamped to 1–1000 (default 1000); there is no pagination.  **Envelope caveat:** the 200 body is `{ \"materials\": [...] }` — there is **no `success` field** on this endpoint.  **Curation:** this vocabulary is curated by the platform operator — the API is read-only for tenant credentials. Free-text material values in passport metadata remain allowed but are never auto-added to this vocabulary.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param kind: Filter by vocabulary kind. Unrecognized values are silently ignored (no error; the filter is not applied).
        :type kind: str
        :param search: Case-insensitive substring match on the entry `name` (value is trimmed; blank values ignored).
        :type search: str
        :param limit: Maximum number of entries to return. Clamped to 1–1000 (out-of-range numeric values are clamped, not rejected); a non-numeric value falls back to the default 1000.
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

        _param = self._list_materials_serialize(
            kind=kind,
            search=search,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "MaterialVocabularyListResponse",
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
    def list_materials_with_http_info(
        self,
        kind: Annotated[Optional[StrictStr], Field(description="Filter by vocabulary kind. Unrecognized values are silently ignored (no error; the filter is not applied).")] = None,
        search: Annotated[Optional[StrictStr], Field(description="Case-insensitive substring match on the entry `name` (value is trimmed; blank values ignored).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=1000, strict=True, ge=1)]], Field(description="Maximum number of entries to return. Clamped to 1–1000 (out-of-range numeric values are clamped, not rejected); a non-numeric value falls back to the default 1000.")] = None,
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
    ) -> ApiResponse[MaterialVocabularyListResponse]:
        """List the platform-curated material vocabulary

        Lists active entries from the platform-global material vocabulary that powers the searchable material/fiber/chemistry pickers in the passport form. This is shared reference data, deliberately **not** tenant-scoped, so DPP data stays comparable across tenants.  **Auth:** any authenticated session — Bearer API key, Bearer JWT, or browser session. **No specific permission string is required** and subscription status is not checked, so this endpoint never returns 402. On a tenant-subdomain host, credentials belonging to a different tenant receive **403** with message `Cross-tenant access blocked.`.  **Filtering & ordering:** `kind` filters by vocabulary kind — an unrecognized value is **silently ignored** (the filter simply isn't applied; no 400). `search` is a trimmed, case-insensitive substring match on `name` (blank values ignored). Only active entries are returned, ordered by `kind` ascending then `name` ascending. `limit` is clamped to 1–1000 (default 1000); there is no pagination.  **Envelope caveat:** the 200 body is `{ \"materials\": [...] }` — there is **no `success` field** on this endpoint.  **Curation:** this vocabulary is curated by the platform operator — the API is read-only for tenant credentials. Free-text material values in passport metadata remain allowed but are never auto-added to this vocabulary.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param kind: Filter by vocabulary kind. Unrecognized values are silently ignored (no error; the filter is not applied).
        :type kind: str
        :param search: Case-insensitive substring match on the entry `name` (value is trimmed; blank values ignored).
        :type search: str
        :param limit: Maximum number of entries to return. Clamped to 1–1000 (out-of-range numeric values are clamped, not rejected); a non-numeric value falls back to the default 1000.
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

        _param = self._list_materials_serialize(
            kind=kind,
            search=search,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "MaterialVocabularyListResponse",
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
    def list_materials_without_preload_content(
        self,
        kind: Annotated[Optional[StrictStr], Field(description="Filter by vocabulary kind. Unrecognized values are silently ignored (no error; the filter is not applied).")] = None,
        search: Annotated[Optional[StrictStr], Field(description="Case-insensitive substring match on the entry `name` (value is trimmed; blank values ignored).")] = None,
        limit: Annotated[Optional[Annotated[int, Field(le=1000, strict=True, ge=1)]], Field(description="Maximum number of entries to return. Clamped to 1–1000 (out-of-range numeric values are clamped, not rejected); a non-numeric value falls back to the default 1000.")] = None,
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
        """List the platform-curated material vocabulary

        Lists active entries from the platform-global material vocabulary that powers the searchable material/fiber/chemistry pickers in the passport form. This is shared reference data, deliberately **not** tenant-scoped, so DPP data stays comparable across tenants.  **Auth:** any authenticated session — Bearer API key, Bearer JWT, or browser session. **No specific permission string is required** and subscription status is not checked, so this endpoint never returns 402. On a tenant-subdomain host, credentials belonging to a different tenant receive **403** with message `Cross-tenant access blocked.`.  **Filtering & ordering:** `kind` filters by vocabulary kind — an unrecognized value is **silently ignored** (the filter simply isn't applied; no 400). `search` is a trimmed, case-insensitive substring match on `name` (blank values ignored). Only active entries are returned, ordered by `kind` ascending then `name` ascending. `limit` is clamped to 1–1000 (default 1000); there is no pagination.  **Envelope caveat:** the 200 body is `{ \"materials\": [...] }` — there is **no `success` field** on this endpoint.  **Curation:** this vocabulary is curated by the platform operator — the API is read-only for tenant credentials. Free-text material values in passport metadata remain allowed but are never auto-added to this vocabulary.  **Rate limit:** your plan's per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace's keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard `x-ratelimit-*` headers; **429** carries `Retry-After`.

        :param kind: Filter by vocabulary kind. Unrecognized values are silently ignored (no error; the filter is not applied).
        :type kind: str
        :param search: Case-insensitive substring match on the entry `name` (value is trimmed; blank values ignored).
        :type search: str
        :param limit: Maximum number of entries to return. Clamped to 1–1000 (out-of-range numeric values are clamped, not rejected); a non-numeric value falls back to the default 1000.
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

        _param = self._list_materials_serialize(
            kind=kind,
            search=search,
            limit=limit,
            _request_auth=_request_auth,
            _content_type=_content_type,
            _headers=_headers,
            _host_index=_host_index
        )

        _response_types_map: Dict[str, Optional[str]] = {
            '200': "MaterialVocabularyListResponse",
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


    def _list_materials_serialize(
        self,
        kind,
        search,
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
        if kind is not None:
            
            _query_params.append(('kind', kind))
            
        if search is not None:
            
            _query_params.append(('search', search))
            
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
            resource_path='/api/v1/materials',
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


