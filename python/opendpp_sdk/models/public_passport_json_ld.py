# coding: utf-8

"""
    OpenDPP Integration API

    OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) Anyone can verify a seal — no account required. `POST /api/v1/audit/verify` recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.

    The version of the OpenAPI document: 1.13.0
    Contact: support@opendpp-node.eu
    Generated by OpenAPI Generator (https://openapi-generator.tech)

    Do not edit the class manually.
"""  # noqa: E501


from __future__ import annotations
import pprint
import re  # noqa: F401
import json

from datetime import datetime
from pydantic import BaseModel, ConfigDict, Field, StrictStr, field_validator
from typing import Any, ClassVar, Dict, List, Optional
from opendpp_sdk.models.economic_operator_node import EconomicOperatorNode
from opendpp_sdk.models.merkle_tree_attestation_proof import MerkleTreeAttestationProof
from opendpp_sdk.models.public_facility_node import PublicFacilityNode
from typing import Optional, Set
from typing_extensions import Self

class PublicPassportJsonLd(BaseModel):
    """
    The public, redacted JSON-LD Digital Product Passport document (`application/ld+json`). All listed top-level keys are ALWAYS present (`null` where not applicable). Additionally, every key of the (masked) `metadata` object — except the reserved document keys (`@context`, `@type`, `@id`, `id`, `productId`, `digitalLinkUri`, `digitalSeal`, `signingPublicKey`, `status`, `archivedAt`, `retentionUntil`, `proof`, `createdAt`, `updatedAt`, `economicOperator`, `manufacturingFacility`, `metadata`) — is ALSO flattened onto the document root for direct semantic-graph querying (hence `additionalProperties: true`); flattened values are identical to the corresponding `metadata` values, including redaction placeholders. Tier-masked metadata keys are replaced (in both places) with the literal string `[REDACTED - Privileged Access Required]`. Masking by tier: anonymous public callers lose the per-category restricted keys (category `batteries`: `detailedPerformance`, `lifecycleAndInUse`, `circularityAndDisassembly` — masked only when actually present) AND the owner-only key `facilityDetails`; legitimate-interest/authority grant holders lose only `facilityDetails`; owner-tier responses are unmasked and additionally include the facility street address fields. Note: `facilityDetails` is placeholder-masked in EVERY non-owner response, even when the underlying metadata never contained the key — in that case it has no entry in `proof.redactedLeaves`. Each masked key that exists in the sealed metadata keeps its true Merkle leaf hash in `proof.redactedLeaves`, so the seal stays verifiable offline after redaction (see `MerkleTreeAttestationProof` for the reconstruction rule).
    """ # noqa: E501
    at_context: Optional[Any] = Field(alias="@context")
    at_type: StrictStr = Field(alias="@type")
    at_id: StrictStr = Field(description="The passport's canonical GS1 Digital Link URI (same value as `digitalLinkUri`).", alias="@id")
    id: StrictStr = Field(description="Server-assigned passport UUID.")
    product_id: StrictStr = Field(description="Caller-supplied product identifier: a GTIN-14 (`^[0-9]{14}$` with valid GS1 modulo-10 check digit), a GRAI (`^[0-9]{14}[A-Za-z0-9]{0,16}$`), or a free-form SKU.", alias="productId")
    digital_link_uri: StrictStr = Field(description="SKU/type-level GS1 Digital Link URI: `{origin}/{01|8003}/{productId}` (AI-21 carries the passport UUID at SKU level; individual units carry their physical serial instead).", alias="digitalLinkUri")
    digital_seal: Optional[StrictStr] = Field(description="ADVANCED electronic seal: base64 ECDSA prime256v1 (P-256) signature over the Merkle root of the key-sorted metadata. `null` when the passport has not been sealed.", alias="digitalSeal")
    signing_public_key: Optional[StrictStr] = Field(description="PEM public key that verifies `digitalSeal`. `null` when unsealed.", alias="signingPublicKey")
    status: StrictStr = Field(description="Passport lifecycle status (serialized as `ACTIVE` when unset). `DRAFT` is only ever visible to owner-tier callers — public/grant resolution of a draft returns 404.")
    archived_at: Optional[datetime] = Field(description="Soft-delete marker (owner off-boarded / decommissioned). Archived passports remain publicly resolvable (ESPR persistence duty).", alias="archivedAt")
    retention_until: Optional[datetime] = Field(description="Minimum-availability deadline; the passport is never purged before this instant.", alias="retentionUntil")
    proof: Optional[MerkleTreeAttestationProof]
    created_at: datetime = Field(alias="createdAt")
    updated_at: datetime = Field(alias="updatedAt")
    economic_operator: Optional[EconomicOperatorNode] = Field(alias="economicOperator")
    manufacturing_facility: Optional[PublicFacilityNode] = Field(alias="manufacturingFacility")
    metadata: Dict[str, Any] = Field(description="The ESPR category metadata, tier-masked: keys above the caller's tier hold the literal string `[REDACTED - Privileged Access Required]` instead of their value.")
    additional_properties: Dict[str, Any] = {}
    __properties: ClassVar[List[str]] = ["@context", "@type", "@id", "id", "productId", "digitalLinkUri", "digitalSeal", "signingPublicKey", "status", "archivedAt", "retentionUntil", "proof", "createdAt", "updatedAt", "economicOperator", "manufacturingFacility", "metadata"]

    @field_validator('at_type')
    def at_type_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['DigitalProductPassport']):
            raise ValueError("must be one of enum values ('DigitalProductPassport')")
        return value

    @field_validator('status')
    def status_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['DRAFT', 'ACTIVE', 'RECALLED', 'DECOMMISSIONED']):
            raise ValueError("must be one of enum values ('DRAFT', 'ACTIVE', 'RECALLED', 'DECOMMISSIONED')")
        return value

    model_config = ConfigDict(
        populate_by_name=True,
        validate_assignment=True,
        protected_namespaces=(),
    )


    def to_str(self) -> str:
        """Returns the string representation of the model using alias"""
        return pprint.pformat(self.model_dump(by_alias=True))

    def to_json(self) -> str:
        """Returns the JSON representation of the model using alias"""
        # TODO: pydantic v2: use .model_dump_json(by_alias=True, exclude_unset=True) instead
        return json.dumps(self.to_dict())

    @classmethod
    def from_json(cls, json_str: str) -> Optional[Self]:
        """Create an instance of PublicPassportJsonLd from a JSON string"""
        return cls.from_dict(json.loads(json_str))

    def to_dict(self) -> Dict[str, Any]:
        """Return the dictionary representation of the model using alias.

        This has the following differences from calling pydantic's
        `self.model_dump(by_alias=True)`:

        * `None` is only added to the output dict for nullable fields that
          were set at model initialization. Other fields with value `None`
          are ignored.
        * Fields in `self.additional_properties` are added to the output dict.
        """
        excluded_fields: Set[str] = set([
            "additional_properties",
        ])

        _dict = self.model_dump(
            by_alias=True,
            exclude=excluded_fields,
            exclude_none=True,
        )
        # override the default output from pydantic by calling `to_dict()` of proof
        if self.proof:
            _dict['proof'] = self.proof.to_dict()
        # override the default output from pydantic by calling `to_dict()` of economic_operator
        if self.economic_operator:
            _dict['economicOperator'] = self.economic_operator.to_dict()
        # override the default output from pydantic by calling `to_dict()` of manufacturing_facility
        if self.manufacturing_facility:
            _dict['manufacturingFacility'] = self.manufacturing_facility.to_dict()
        # puts key-value pairs in additional_properties in the top level
        if self.additional_properties is not None:
            for _key, _value in self.additional_properties.items():
                _dict[_key] = _value

        # set to None if at_context (nullable) is None
        # and model_fields_set contains the field
        if self.at_context is None and "at_context" in self.model_fields_set:
            _dict['@context'] = None

        # set to None if digital_seal (nullable) is None
        # and model_fields_set contains the field
        if self.digital_seal is None and "digital_seal" in self.model_fields_set:
            _dict['digitalSeal'] = None

        # set to None if signing_public_key (nullable) is None
        # and model_fields_set contains the field
        if self.signing_public_key is None and "signing_public_key" in self.model_fields_set:
            _dict['signingPublicKey'] = None

        # set to None if archived_at (nullable) is None
        # and model_fields_set contains the field
        if self.archived_at is None and "archived_at" in self.model_fields_set:
            _dict['archivedAt'] = None

        # set to None if retention_until (nullable) is None
        # and model_fields_set contains the field
        if self.retention_until is None and "retention_until" in self.model_fields_set:
            _dict['retentionUntil'] = None

        # set to None if proof (nullable) is None
        # and model_fields_set contains the field
        if self.proof is None and "proof" in self.model_fields_set:
            _dict['proof'] = None

        # set to None if economic_operator (nullable) is None
        # and model_fields_set contains the field
        if self.economic_operator is None and "economic_operator" in self.model_fields_set:
            _dict['economicOperator'] = None

        # set to None if manufacturing_facility (nullable) is None
        # and model_fields_set contains the field
        if self.manufacturing_facility is None and "manufacturing_facility" in self.model_fields_set:
            _dict['manufacturingFacility'] = None

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of PublicPassportJsonLd from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "@context": obj.get("@context"),
            "@type": obj.get("@type"),
            "@id": obj.get("@id"),
            "id": obj.get("id"),
            "productId": obj.get("productId"),
            "digitalLinkUri": obj.get("digitalLinkUri"),
            "digitalSeal": obj.get("digitalSeal"),
            "signingPublicKey": obj.get("signingPublicKey"),
            "status": obj.get("status"),
            "archivedAt": obj.get("archivedAt"),
            "retentionUntil": obj.get("retentionUntil"),
            "proof": MerkleTreeAttestationProof.from_dict(obj["proof"]) if obj.get("proof") is not None else None,
            "createdAt": obj.get("createdAt"),
            "updatedAt": obj.get("updatedAt"),
            "economicOperator": EconomicOperatorNode.from_dict(obj["economicOperator"]) if obj.get("economicOperator") is not None else None,
            "manufacturingFacility": PublicFacilityNode.from_dict(obj["manufacturingFacility"]) if obj.get("manufacturingFacility") is not None else None,
            "metadata": obj.get("metadata")
        })
        # store additional fields in additional_properties
        for _key in obj.keys():
            if _key not in cls.__properties:
                _obj.additional_properties[_key] = obj.get(_key)

        return _obj


