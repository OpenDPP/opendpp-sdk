# coding: utf-8

"""
    OpenDPP Integration API

    OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.

    The version of the OpenAPI document: 1.15.0
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
from typing_extensions import Annotated
from opendpp_sdk.models.merkle_tree_attestation_proof_rfc3161 import MerkleTreeAttestationProofRfc3161
from typing import Optional, Set
from typing_extensions import Self

class MerkleTreeAttestationProof(BaseModel):
    """
    OpenDPP's own proof type — an ADVANCED electronic seal: an ECDSA prime256v1 signature over a SHA-256 Merkle root of the key-sorted metadata (one leaf per top-level metadata key). Deliberately NOT a W3C DataIntegrityProof / `ecdsa-jcs-2019` Verifiable Credential (no RFC 8785 JCS canonicalization). Verifiable offline: rebuild the Merkle root from `metadata` — substituting each `redactedLeaves` hash for its placeholder-masked key, and EXCLUDING any placeholder-masked key that has no `redactedLeaves` entry (such a key was never present in the sealed metadata; the serializer injects the owner-only placeholder unconditionally) — then verify `signatureValue` with `publicKeyPem`; the `x5c` chain validates against the platform seal CA (`GET /.well-known/opendpp-seal-ca.pem`) and the `rfc3161` token via `openssl ts -verify`.
    """ # noqa: E501
    at_type: List[StrictStr] = Field(description="Always `[\"MerkleTreeAttestationProof\"]`.", alias="@type")
    type: StrictStr
    signature_algorithm: StrictStr = Field(alias="signatureAlgorithm")
    created: datetime = Field(description="Mirrors the passport's `updatedAt`.")
    proof_purpose: StrictStr = Field(alias="proofPurpose")
    verification_method: StrictStr = Field(description="`https://opendpp-node.eu/passport/{passportId}#key-1`.", alias="verificationMethod")
    signature_value: StrictStr = Field(description="Base64 ECDSA P-256/SHA-256 signature over the hex Merkle root string (same value as the document's `digitalSeal`).", alias="signatureValue")
    public_key_pem: StrictStr = Field(description="PEM public key for verification (same value as the document's `signingPublicKey`).", alias="publicKeyPem")
    x5c: Optional[List[StrictStr]] = Field(default=None, description="OPTIONAL (omitted when no chain was recorded at seal time). X.509 chain as base64 DER (no PEM armor), leaf first, binding the signing key to the tenant's legal identity; issued by the platform seal CA. Denormalised at seal time, so later key/cert rotations never retroactively change a proof.")
    rfc3161: Optional[MerkleTreeAttestationProofRfc3161] = None
    merkle_root: Annotated[str, Field(strict=True)] = Field(description="Hex SHA-256 Merkle root over the key-sorted metadata leaves.", alias="merkleRoot")
    redacted_leaves: Optional[Dict[str, Annotated[str, Field(strict=True)]]] = Field(default=None, description="OPTIONAL — present only when at least one masked key actually exists in the underlying sealed metadata. Maps each such metadata key to its TRUE hex leaf hash, so the Merkle root can be reconstructed from the redacted document. A masked key that was never present in the metadata (the owner-only key is placeholder-injected unconditionally for non-owner tiers) yields NO entry here — verifiers must exclude placeholder-valued keys without an entry when rebuilding the tree.", alias="redactedLeaves")
    additional_properties: Dict[str, Any] = {}
    __properties: ClassVar[List[str]] = ["@type", "type", "signatureAlgorithm", "created", "proofPurpose", "verificationMethod", "signatureValue", "publicKeyPem", "x5c", "rfc3161", "merkleRoot", "redactedLeaves"]

    @field_validator('at_type')
    def at_type_validate_enum(cls, value):
        """Validates the enum"""
        for i in value:
            if i not in set(['MerkleTreeAttestationProof']):
                raise ValueError("each list item must be one of ('MerkleTreeAttestationProof')")
        return value

    @field_validator('type')
    def type_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['MerkleTreeAttestationProof']):
            raise ValueError("must be one of enum values ('MerkleTreeAttestationProof')")
        return value

    @field_validator('signature_algorithm')
    def signature_algorithm_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['ECDSA-P256-SHA256-over-MerkleRoot']):
            raise ValueError("must be one of enum values ('ECDSA-P256-SHA256-over-MerkleRoot')")
        return value

    @field_validator('proof_purpose')
    def proof_purpose_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['assertionMethod']):
            raise ValueError("must be one of enum values ('assertionMethod')")
        return value

    @field_validator('merkle_root')
    def merkle_root_validate_regular_expression(cls, value):
        """Validates the regular expression"""
        if not re.match(r"^[0-9a-f]{64}$", value):
            raise ValueError(r"must validate the regular expression /^[0-9a-f]{64}$/")
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
        """Create an instance of MerkleTreeAttestationProof from a JSON string"""
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
        # override the default output from pydantic by calling `to_dict()` of rfc3161
        if self.rfc3161:
            _dict['rfc3161'] = self.rfc3161.to_dict()
        # puts key-value pairs in additional_properties in the top level
        if self.additional_properties is not None:
            for _key, _value in self.additional_properties.items():
                _dict[_key] = _value

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of MerkleTreeAttestationProof from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "@type": obj.get("@type"),
            "type": obj.get("type"),
            "signatureAlgorithm": obj.get("signatureAlgorithm"),
            "created": obj.get("created"),
            "proofPurpose": obj.get("proofPurpose"),
            "verificationMethod": obj.get("verificationMethod"),
            "signatureValue": obj.get("signatureValue"),
            "publicKeyPem": obj.get("publicKeyPem"),
            "x5c": obj.get("x5c"),
            "rfc3161": MerkleTreeAttestationProofRfc3161.from_dict(obj["rfc3161"]) if obj.get("rfc3161") is not None else None,
            "merkleRoot": obj.get("merkleRoot"),
            "redactedLeaves": obj.get("redactedLeaves")
        })
        # store additional fields in additional_properties
        for _key in obj.keys():
            if _key not in cls.__properties:
                _obj.additional_properties[_key] = obj.get(_key)

        return _obj


