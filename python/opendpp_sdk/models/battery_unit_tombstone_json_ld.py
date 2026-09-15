# coding: utf-8

"""
    OpenDPP Integration API

    OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `EORI_NOT_FOUND`, `CARRIER_SYMBOLOGY_NOT_RENDERED`, `CATEGORY_GRANULARITY_UNEXPECTED`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Methods  A request whose path exists but whose method this API does not serve returns **`405 Method Not Allowed`** with an `Allow` header listing the methods that path does serve (RFC 9110 §15.5.6); `HEAD` is listed wherever `GET` is, and is served. A path no route matches returns `404`, as does a path whose method IS allowed but whose resource does not exist — so a `405` always means the verb, and never the identifier. `405` is not listed per operation below because it is not a property of any operation: it is the answer to a method for which no operation exists.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Role in the data exchange This node is **not a DPP registry**. It hosts passports on behalf of the economic operators that create them and provides no registration service, so the registry methods of EN 18222:2026 clause 5 (Table 17, `registerDPP`) are outside this API's scope. Which service-provider role the node holds for a given passport is a property of the agreement with that operator rather than of this document, so it is not asserted here.  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.

    The version of the OpenAPI document: 1.16.0
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
from typing import Optional, Set
from typing_extensions import Self

class BatteryUnitTombstoneJsonLd(BaseModel):
    """
    Tombstone (HTTP 410): once a battery is recycled its passport has ceased to exist. This minimal record confirms the unit existed, that it was recycled and when, plus the (still living) model-passport link. Grants and owner credentials do not override the tombstone on the public URL; the underlying data is retained internally for the statutory retention window.
    """ # noqa: E501
    digital_product_passport_id: StrictStr = Field(description="The identifier of this PASSPORT instance (EN 18223 Table 1 `digitalProductPassportId`): this node's own `/unit/{id}` URL for the individual serialised unit. It identifies the passport, **not** the product — the product's identifier is `uniqueProductIdentifier` below, and the two are never the same value. `@id` and `digitalLinkUri` carry the product's link, so this attribute differs from both.", alias="digitalProductPassportId")
    unique_product_identifier: StrictStr = Field(description="The identifier of the PRODUCT in its web-linkable form (EN 18219, EN 18223 Table 1 `uniqueProductIdentifier`): the GS1 Digital Link of the individual serialised unit, or an EN IEC 61406 Identification Link where the product carries no GS1 key. Distinct from `digitalProductPassportId` above, which identifies the passport.", alias="uniqueProductIdentifier")
    granularity: StrictStr = Field(description="Granularity level of the unique product identifier (EN 18223 4.1.2.2): an individually serialised unit is always `item`.")
    dpp_schema_version: StrictStr = Field(description="The reference standard whose schema the instance follows (EN 18223 Table 1): the dated designation of the standard the body's model and serialisation come from, so a consumer picks its parser by it. Not this node's API contract version — that is `GET /api/v1/version`, and it says nothing about the body's model.", alias="dppSchemaVersion")
    dpp_status: StrictStr = Field(description="Status of the DPP instance AS A DIGITAL RESOURCE (EN 18223 Table 1), not of the product. An OPEN vocabulary — Table 1's values are examples and a legal act may add more — so it is deliberately not an enum here. This node emits `active` for a published passport (`status` ACTIVE or RECALLED — a recalled product's passport is still maintained), `inactive` for a DRAFT, `archived` once DECOMMISSIONED or when the owner was off-boarded (`archivedAt`). A unit is `archived` once tombstoned (RECYCLED).", alias="dppStatus")
    last_updated: datetime = Field(description="Date and time of the latest update to the instance (ISO 8601, UTC) — the same instant as `updatedAt`. Never null: EN 18223 gives it cardinality 1, and the database maintains the column on every write.", alias="lastUpdated")
    economic_operator_id: StrictStr = Field(description="The responsible economic operator's identifier in EN 18219 form (EN 18223 Table 1): ISO/IEC 6523 `ICD:identifier` for the operator's clause 6 scheme — VAT `0223`, DUNS `0060`, LEI `0199`, GLN `0088` — e.g. `0223:LT000000000001`. The scheme is `economicOperator.regIdScheme`; only an operator that predates the scheme declaration (`UNDECLARED`) is carried as registered. Never null: EN 18223 gives it cardinality 1, the operator is a non-null foreign key, and every serialisation venue loads that relation.", alias="economicOperatorId")
    facility_id: Optional[StrictStr] = Field(default=None, description="The Unique Facility Identifier of the linked manufacturing facility as a GS1 Digital Link carrying the location GLN under AI 414 (`https://id.gs1.org/414/{gln}`). Optional in EN 18223 (cardinality 0..1): when no facility is linked the attribute is OMITTED — never `null`, never a placeholder — so it is not a `required` key and a reader tests for its presence.", alias="facilityId")
    content_specification_ids: Optional[List[StrictStr]] = Field(default=None, description="The content specification(s) the instance follows: the URL of the ESPR category schema this node validated the metadata against (`GET /api/v1/schemas/{category}`). Empty when the metadata names no category. Optional in EN 18223 (cardinality 0..*), so it is not a `required` key — this node always sends it.", alias="contentSpecificationIds")
    at_context: Optional[Any] = Field(alias="@context")
    at_type: StrictStr = Field(alias="@type")
    at_id: StrictStr = Field(alias="@id")
    id: StrictStr
    serial_number: StrictStr = Field(alias="serialNumber")
    status: StrictStr
    ceased_at: Optional[datetime] = Field(description="When the unit's passport ceased to exist (stamped when the status transitioned to RECYCLED).", alias="ceasedAt")
    notice: StrictStr
    of_model_url: Optional[StrictStr] = Field(description="Relative URL of the still-living SKU/type passport: `/passport/{passportId}`.", alias="ofModelUrl")
    additional_properties: Dict[str, Any] = {}
    __properties: ClassVar[List[str]] = ["digitalProductPassportId", "uniqueProductIdentifier", "granularity", "dppSchemaVersion", "dppStatus", "lastUpdated", "economicOperatorId", "facilityId", "contentSpecificationIds", "@context", "@type", "@id", "id", "serialNumber", "status", "ceasedAt", "notice", "ofModelUrl"]

    @field_validator('granularity')
    def granularity_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['item']):
            raise ValueError("must be one of enum values ('item')")
        return value

    @field_validator('dpp_schema_version')
    def dpp_schema_version_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['EN 18223:2026']):
            raise ValueError("must be one of enum values ('EN 18223:2026')")
        return value

    @field_validator('at_type')
    def at_type_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['BatteryUnit']):
            raise ValueError("must be one of enum values ('BatteryUnit')")
        return value

    @field_validator('status')
    def status_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['RECYCLED']):
            raise ValueError("must be one of enum values ('RECYCLED')")
        return value

    @field_validator('notice')
    def notice_validate_enum(cls, value):
        """Validates the enum"""
        if value not in set(['This battery has been recycled. Its battery passport has ceased to exist (Regulation (EU) 2023/1542, Art. 77(8)).']):
            raise ValueError("must be one of enum values ('This battery has been recycled. Its battery passport has ceased to exist (Regulation (EU) 2023/1542, Art. 77(8)).')")
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
        """Create an instance of BatteryUnitTombstoneJsonLd from a JSON string"""
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
        # puts key-value pairs in additional_properties in the top level
        if self.additional_properties is not None:
            for _key, _value in self.additional_properties.items():
                _dict[_key] = _value

        # set to None if at_context (nullable) is None
        # and model_fields_set contains the field
        if self.at_context is None and "at_context" in self.model_fields_set:
            _dict['@context'] = None

        # set to None if ceased_at (nullable) is None
        # and model_fields_set contains the field
        if self.ceased_at is None and "ceased_at" in self.model_fields_set:
            _dict['ceasedAt'] = None

        # set to None if of_model_url (nullable) is None
        # and model_fields_set contains the field
        if self.of_model_url is None and "of_model_url" in self.model_fields_set:
            _dict['ofModelUrl'] = None

        return _dict

    @classmethod
    def from_dict(cls, obj: Optional[Dict[str, Any]]) -> Optional[Self]:
        """Create an instance of BatteryUnitTombstoneJsonLd from a dict"""
        if obj is None:
            return None

        if not isinstance(obj, dict):
            return cls.model_validate(obj)

        _obj = cls.model_validate({
            "digitalProductPassportId": obj.get("digitalProductPassportId"),
            "uniqueProductIdentifier": obj.get("uniqueProductIdentifier"),
            "granularity": obj.get("granularity"),
            "dppSchemaVersion": obj.get("dppSchemaVersion"),
            "dppStatus": obj.get("dppStatus"),
            "lastUpdated": obj.get("lastUpdated"),
            "economicOperatorId": obj.get("economicOperatorId"),
            "facilityId": obj.get("facilityId"),
            "contentSpecificationIds": obj.get("contentSpecificationIds"),
            "@context": obj.get("@context"),
            "@type": obj.get("@type"),
            "@id": obj.get("@id"),
            "id": obj.get("id"),
            "serialNumber": obj.get("serialNumber"),
            "status": obj.get("status"),
            "ceasedAt": obj.get("ceasedAt"),
            "notice": obj.get("notice"),
            "ofModelUrl": obj.get("ofModelUrl")
        })
        # store additional fields in additional_properties
        for _key in obj.keys():
            if _key not in cls.__properties:
                _obj.additional_properties[_key] = obj.get(_key)

        return _obj


