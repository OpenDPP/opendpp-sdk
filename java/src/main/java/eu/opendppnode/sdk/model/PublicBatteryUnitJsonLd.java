/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR (Regulation (EU) 2024/1781) data requirements and the EU Battery Regulation (Regulation (EU) 2023/1542). This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations return Fastify's default `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Global limit: **100 requests/min per IP** (higher for verified crawlers), with `x-ratelimit-*` response headers. Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**. Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window.  ## Sealing & verification Passport seals are **eIDAS advanced electronic seals** (ECDSA P-256 over a Merkle root of the passport content, optional RFC 3161 timestamp). Anyone can verify a seal — no account required. `POST /api/v1/audit/verify` recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.11.0
 * Contact: support@opendpp-node.eu
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package eu.opendppnode.sdk.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.opendppnode.sdk.model.BatteryUnitCurrentState;
import eu.opendppnode.sdk.model.BatteryUnitEventNode;
import eu.opendppnode.sdk.model.BatteryUnitLineageRef;
import eu.opendppnode.sdk.model.BatteryUnitRestrictedDataNotice;
import eu.opendppnode.sdk.model.PublicPassportJsonLd;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Public JSON-LD document for one individual serialised battery unit (Reg. (EU) 2023/1542 Art. 77(2)). The listed required keys are always present. EXACTLY ONE of two tier-dependent groups is added: anonymous (public) responses carry &#x60;restrictedData&#x60; (Annex XIII(2)-(4) notice) and OMIT &#x60;currentState&#x60;/&#x60;dynamicData&#x60; entirely; owner/grant (privileged) responses carry &#x60;currentState&#x60; (latest measurement or &#x60;null&#x60;) and &#x60;dynamicData&#x60; (up to 500 events, newest first) and omit &#x60;restrictedData&#x60;. The embedded &#x60;ofModel&#x60; passport is masked by the caller&#39;s tier like &#x60;GET /passport/{id}&#x60;.
 */
@JsonPropertyOrder({
  PublicBatteryUnitJsonLd.JSON_PROPERTY_AT_CONTEXT,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_AT_TYPE,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_AT_ID,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_ID,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_SERIAL_NUMBER,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_DIGITAL_LINK_URI,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_STATUS,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_MANUFACTURED_AT,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_REPURPOSED_FROM,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_SUCCESSOR_UNITS,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_OF_MODEL,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_RESTRICTED_DATA,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_CURRENT_STATE,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_DYNAMIC_DATA,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_CREATED_AT,
  PublicBatteryUnitJsonLd.JSON_PROPERTY_UPDATED_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PublicBatteryUnitJsonLd {
  public static final String JSON_PROPERTY_AT_CONTEXT = "@context";
  @jakarta.annotation.Nullable
  private Object atContext = null;

  /**
   * Gets or Sets atType
   */
  public enum AtTypeEnum {
    BATTERY_UNIT(String.valueOf("BatteryUnit")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    AtTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static AtTypeEnum fromValue(String value) {
      for (AtTypeEnum b : AtTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_AT_TYPE = "@type";
  @jakarta.annotation.Nonnull
  private AtTypeEnum atType;

  public static final String JSON_PROPERTY_AT_ID = "@id";
  @jakarta.annotation.Nonnull
  private URI atId;

  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_SERIAL_NUMBER = "serialNumber";
  @jakarta.annotation.Nonnull
  private String serialNumber;

  public static final String JSON_PROPERTY_DIGITAL_LINK_URI = "digitalLinkUri";
  @jakarta.annotation.Nonnull
  private URI digitalLinkUri;

  /**
   * Annex XIII battery-status vocabulary. A &#x60;RECYCLED&#x60; (or ceased) unit is never served as a 200 — its URL answers 410 with the tombstone document instead.
   */
  public enum StatusEnum {
    IN_SERVICE(String.valueOf("IN_SERVICE")),
    
    DECOMMISSIONED(String.valueOf("DECOMMISSIONED")),
    
    RECALLED(String.valueOf("RECALLED")),
    
    REPURPOSED(String.valueOf("REPURPOSED")),
    
    REMANUFACTURED(String.valueOf("REMANUFACTURED")),
    
    REUSED(String.valueOf("REUSED")),
    
    WASTE(String.valueOf("WASTE")),
    
    RECYCLED(String.valueOf("RECYCLED")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_STATUS = "status";
  @jakarta.annotation.Nonnull
  private StatusEnum status;

  public static final String JSON_PROPERTY_MANUFACTURED_AT = "manufacturedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime manufacturedAt;

  public static final String JSON_PROPERTY_REPURPOSED_FROM = "repurposedFrom";
  @jakarta.annotation.Nullable
  private BatteryUnitLineageRef repurposedFrom;

  public static final String JSON_PROPERTY_SUCCESSOR_UNITS = "successorUnits";
  @jakarta.annotation.Nonnull
  private List<BatteryUnitLineageRef> successorUnits = new ArrayList<>();

  public static final String JSON_PROPERTY_OF_MODEL = "ofModel";
  @jakarta.annotation.Nonnull
  private PublicPassportJsonLd ofModel;

  public static final String JSON_PROPERTY_RESTRICTED_DATA = "restrictedData";
  @jakarta.annotation.Nullable
  private BatteryUnitRestrictedDataNotice restrictedData;

  public static final String JSON_PROPERTY_CURRENT_STATE = "currentState";
  private JsonNullable<BatteryUnitCurrentState> currentState = JsonNullable.<BatteryUnitCurrentState>undefined();

  public static final String JSON_PROPERTY_DYNAMIC_DATA = "dynamicData";
  @jakarta.annotation.Nullable
  private List<BatteryUnitEventNode> dynamicData = new ArrayList<>();

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public static final String JSON_PROPERTY_UPDATED_AT = "updatedAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime updatedAt;

  public PublicBatteryUnitJsonLd() { 
  }

  public PublicBatteryUnitJsonLd atContext(@jakarta.annotation.Nullable Object atContext) {
    this.atContext = atContext;
    return this;
  }

  /**
   * Get atContext
   * @return atContext
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_AT_CONTEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getAtContext() {
    return atContext;
  }


  @JsonProperty(JSON_PROPERTY_AT_CONTEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtContext(@jakarta.annotation.Nullable Object atContext) {
    this.atContext = atContext;
  }


  public PublicBatteryUnitJsonLd atType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
    this.atType = atType;
    return this;
  }

  /**
   * Get atType
   * @return atType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public AtTypeEnum getAtType() {
    return atType;
  }


  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
    this.atType = atType;
  }


  public PublicBatteryUnitJsonLd atId(@jakarta.annotation.Nonnull URI atId) {
    this.atId = atId;
    return this;
  }

  /**
   * The unit&#39;s GS1 Digital Link URI (AI-21 &#x3D; the real physical serial).
   * @return atId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_AT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getAtId() {
    return atId;
  }


  @JsonProperty(JSON_PROPERTY_AT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtId(@jakarta.annotation.Nonnull URI atId) {
    this.atId = atId;
  }


  public PublicBatteryUnitJsonLd id(@jakarta.annotation.Nonnull String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getId() {
    return id;
  }


  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setId(@jakarta.annotation.Nonnull String id) {
    this.id = id;
  }


  public PublicBatteryUnitJsonLd serialNumber(@jakarta.annotation.Nonnull String serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

  /**
   * The physical battery serial (the real GS1 AI-21 value; unique within its SKU/type passport).
   * @return serialNumber
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SERIAL_NUMBER)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSerialNumber() {
    return serialNumber;
  }


  @JsonProperty(JSON_PROPERTY_SERIAL_NUMBER)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSerialNumber(@jakarta.annotation.Nonnull String serialNumber) {
    this.serialNumber = serialNumber;
  }


  public PublicBatteryUnitJsonLd digitalLinkUri(@jakarta.annotation.Nonnull URI digitalLinkUri) {
    this.digitalLinkUri = digitalLinkUri;
    return this;
  }

  /**
   * Get digitalLinkUri
   * @return digitalLinkUri
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DIGITAL_LINK_URI)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getDigitalLinkUri() {
    return digitalLinkUri;
  }


  @JsonProperty(JSON_PROPERTY_DIGITAL_LINK_URI)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDigitalLinkUri(@jakarta.annotation.Nonnull URI digitalLinkUri) {
    this.digitalLinkUri = digitalLinkUri;
  }


  public PublicBatteryUnitJsonLd status(@jakarta.annotation.Nonnull StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Annex XIII battery-status vocabulary. A &#x60;RECYCLED&#x60; (or ceased) unit is never served as a 200 — its URL answers 410 with the tombstone document instead.
   * @return status
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_STATUS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public StatusEnum getStatus() {
    return status;
  }


  @JsonProperty(JSON_PROPERTY_STATUS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setStatus(@jakarta.annotation.Nonnull StatusEnum status) {
    this.status = status;
  }


  public PublicBatteryUnitJsonLd manufacturedAt(@jakarta.annotation.Nullable OffsetDateTime manufacturedAt) {
    this.manufacturedAt = manufacturedAt;
    return this;
  }

  /**
   * Get manufacturedAt
   * @return manufacturedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_MANUFACTURED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getManufacturedAt() {
    return manufacturedAt;
  }


  @JsonProperty(JSON_PROPERTY_MANUFACTURED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setManufacturedAt(@jakarta.annotation.Nullable OffsetDateTime manufacturedAt) {
    this.manufacturedAt = manufacturedAt;
  }


  public PublicBatteryUnitJsonLd repurposedFrom(@jakarta.annotation.Nullable BatteryUnitLineageRef repurposedFrom) {
    this.repurposedFrom = repurposedFrom;
    return this;
  }

  /**
   * Get repurposedFrom
   * @return repurposedFrom
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_REPURPOSED_FROM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BatteryUnitLineageRef getRepurposedFrom() {
    return repurposedFrom;
  }


  @JsonProperty(JSON_PROPERTY_REPURPOSED_FROM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRepurposedFrom(@jakarta.annotation.Nullable BatteryUnitLineageRef repurposedFrom) {
    this.repurposedFrom = repurposedFrom;
  }


  public PublicBatteryUnitJsonLd successorUnits(@jakarta.annotation.Nonnull List<BatteryUnitLineageRef> successorUnits) {
    this.successorUnits = successorUnits;
    return this;
  }

  public PublicBatteryUnitJsonLd addSuccessorUnitsItem(BatteryUnitLineageRef successorUnitsItem) {
    if (this.successorUnits == null) {
      this.successorUnits = new ArrayList<>();
    }
    this.successorUnits.add(successorUnitsItem);
    return this;
  }

  /**
   * Units re-placed on the market under a new passport derived from this one (empty array when none).
   * @return successorUnits
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SUCCESSOR_UNITS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<BatteryUnitLineageRef> getSuccessorUnits() {
    return successorUnits;
  }


  @JsonProperty(JSON_PROPERTY_SUCCESSOR_UNITS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSuccessorUnits(@jakarta.annotation.Nonnull List<BatteryUnitLineageRef> successorUnits) {
    this.successorUnits = successorUnits;
  }


  public PublicBatteryUnitJsonLd ofModel(@jakarta.annotation.Nonnull PublicPassportJsonLd ofModel) {
    this.ofModel = ofModel;
    return this;
  }

  /**
   * The SKU/type-level passport this physical unit is an instance of, masked by the caller&#39;s tier.
   * @return ofModel
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_OF_MODEL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public PublicPassportJsonLd getOfModel() {
    return ofModel;
  }


  @JsonProperty(JSON_PROPERTY_OF_MODEL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOfModel(@jakarta.annotation.Nonnull PublicPassportJsonLd ofModel) {
    this.ofModel = ofModel;
  }


  public PublicBatteryUnitJsonLd restrictedData(@jakarta.annotation.Nullable BatteryUnitRestrictedDataNotice restrictedData) {
    this.restrictedData = restrictedData;
    return this;
  }

  /**
   * Present ONLY in anonymous (public-tier) responses.
   * @return restrictedData
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RESTRICTED_DATA)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public BatteryUnitRestrictedDataNotice getRestrictedData() {
    return restrictedData;
  }


  @JsonProperty(JSON_PROPERTY_RESTRICTED_DATA)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRestrictedData(@jakarta.annotation.Nullable BatteryUnitRestrictedDataNotice restrictedData) {
    this.restrictedData = restrictedData;
  }


  public PublicBatteryUnitJsonLd currentState(@jakarta.annotation.Nullable BatteryUnitCurrentState currentState) {
    this.currentState = JsonNullable.<BatteryUnitCurrentState>of(currentState);
    return this;
  }

  /**
   * Get currentState
   * @return currentState
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public BatteryUnitCurrentState getCurrentState() {
        return currentState.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_CURRENT_STATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<BatteryUnitCurrentState> getCurrentState_JsonNullable() {
    return currentState;
  }
  
  @JsonProperty(JSON_PROPERTY_CURRENT_STATE)
  public void setCurrentState_JsonNullable(JsonNullable<BatteryUnitCurrentState> currentState) {
    this.currentState = currentState;
  }

  public void setCurrentState(@jakarta.annotation.Nullable BatteryUnitCurrentState currentState) {
    this.currentState = JsonNullable.<BatteryUnitCurrentState>of(currentState);
  }


  public PublicBatteryUnitJsonLd dynamicData(@jakarta.annotation.Nullable List<BatteryUnitEventNode> dynamicData) {
    this.dynamicData = dynamicData;
    return this;
  }

  public PublicBatteryUnitJsonLd addDynamicDataItem(BatteryUnitEventNode dynamicDataItem) {
    if (this.dynamicData == null) {
      this.dynamicData = new ArrayList<>();
    }
    this.dynamicData.add(dynamicDataItem);
    return this;
  }

  /**
   * Present ONLY in owner/grant-tier responses: append-only telemetry history, newest first, capped at the 500 most recent events.
   * @return dynamicData
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DYNAMIC_DATA)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<BatteryUnitEventNode> getDynamicData() {
    return dynamicData;
  }


  @JsonProperty(JSON_PROPERTY_DYNAMIC_DATA)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDynamicData(@jakarta.annotation.Nullable List<BatteryUnitEventNode> dynamicData) {
    this.dynamicData = dynamicData;
  }


  public PublicBatteryUnitJsonLd createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   * Get createdAt
   * @return createdAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CREATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }


  @JsonProperty(JSON_PROPERTY_CREATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCreatedAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }


  public PublicBatteryUnitJsonLd updatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  /**
   * Get updatedAt
   * @return updatedAt
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }


  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUpdatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }


  /**
   * Return true if this PublicBatteryUnitJsonLd object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicBatteryUnitJsonLd publicBatteryUnitJsonLd = (PublicBatteryUnitJsonLd) o;
    return Objects.equals(this.atContext, publicBatteryUnitJsonLd.atContext) &&
        Objects.equals(this.atType, publicBatteryUnitJsonLd.atType) &&
        Objects.equals(this.atId, publicBatteryUnitJsonLd.atId) &&
        Objects.equals(this.id, publicBatteryUnitJsonLd.id) &&
        Objects.equals(this.serialNumber, publicBatteryUnitJsonLd.serialNumber) &&
        Objects.equals(this.digitalLinkUri, publicBatteryUnitJsonLd.digitalLinkUri) &&
        Objects.equals(this.status, publicBatteryUnitJsonLd.status) &&
        Objects.equals(this.manufacturedAt, publicBatteryUnitJsonLd.manufacturedAt) &&
        Objects.equals(this.repurposedFrom, publicBatteryUnitJsonLd.repurposedFrom) &&
        Objects.equals(this.successorUnits, publicBatteryUnitJsonLd.successorUnits) &&
        Objects.equals(this.ofModel, publicBatteryUnitJsonLd.ofModel) &&
        Objects.equals(this.restrictedData, publicBatteryUnitJsonLd.restrictedData) &&
        equalsNullable(this.currentState, publicBatteryUnitJsonLd.currentState) &&
        Objects.equals(this.dynamicData, publicBatteryUnitJsonLd.dynamicData) &&
        Objects.equals(this.createdAt, publicBatteryUnitJsonLd.createdAt) &&
        Objects.equals(this.updatedAt, publicBatteryUnitJsonLd.updatedAt);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(atContext, atType, atId, id, serialNumber, digitalLinkUri, status, manufacturedAt, repurposedFrom, successorUnits, ofModel, restrictedData, hashCodeNullable(currentState), dynamicData, createdAt, updatedAt);
  }

  private static <T> int hashCodeNullable(JsonNullable<T> a) {
    if (a == null) {
      return 1;
    }
    return a.isPresent() ? Arrays.deepHashCode(new Object[]{a.get()}) : 31;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PublicBatteryUnitJsonLd {\n");
    sb.append("    atContext: ").append(toIndentedString(atContext)).append("\n");
    sb.append("    atType: ").append(toIndentedString(atType)).append("\n");
    sb.append("    atId: ").append(toIndentedString(atId)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
    sb.append("    digitalLinkUri: ").append(toIndentedString(digitalLinkUri)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    manufacturedAt: ").append(toIndentedString(manufacturedAt)).append("\n");
    sb.append("    repurposedFrom: ").append(toIndentedString(repurposedFrom)).append("\n");
    sb.append("    successorUnits: ").append(toIndentedString(successorUnits)).append("\n");
    sb.append("    ofModel: ").append(toIndentedString(ofModel)).append("\n");
    sb.append("    restrictedData: ").append(toIndentedString(restrictedData)).append("\n");
    sb.append("    currentState: ").append(toIndentedString(currentState)).append("\n");
    sb.append("    dynamicData: ").append(toIndentedString(dynamicData)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  /**
   * Convert the instance into URL query string.
   *
   * @return URL query string
   */
  public String toUrlQueryString() {
    return toUrlQueryString(null);
  }

  /**
   * Convert the instance into URL query string.
   *
   * @param prefix prefix of the query string
   * @return URL query string
   */
  public String toUrlQueryString(String prefix) {
    String suffix = "";
    String containerSuffix = "";
    String containerPrefix = "";
    if (prefix == null) {
      // style=form, explode=true, e.g. /pet?name=cat&type=manx
      prefix = "";
    } else {
      // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
      prefix = prefix + "[";
      suffix = "]";
      containerSuffix = "]";
      containerPrefix = "[";
    }

    StringJoiner joiner = new StringJoiner("&");

    // add `@context` to the URL query string
    if (getAtContext() != null) {
      joiner.add(String.format("%s@context%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAtContext()))));
    }

    // add `@type` to the URL query string
    if (getAtType() != null) {
      joiner.add(String.format("%s@type%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAtType()))));
    }

    // add `@id` to the URL query string
    if (getAtId() != null) {
      joiner.add(String.format("%s@id%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAtId()))));
    }

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format("%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `serialNumber` to the URL query string
    if (getSerialNumber() != null) {
      joiner.add(String.format("%sserialNumber%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSerialNumber()))));
    }

    // add `digitalLinkUri` to the URL query string
    if (getDigitalLinkUri() != null) {
      joiner.add(String.format("%sdigitalLinkUri%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDigitalLinkUri()))));
    }

    // add `status` to the URL query string
    if (getStatus() != null) {
      joiner.add(String.format("%sstatus%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
    }

    // add `manufacturedAt` to the URL query string
    if (getManufacturedAt() != null) {
      joiner.add(String.format("%smanufacturedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getManufacturedAt()))));
    }

    // add `repurposedFrom` to the URL query string
    if (getRepurposedFrom() != null) {
      joiner.add(getRepurposedFrom().toUrlQueryString(prefix + "repurposedFrom" + suffix));
    }

    // add `successorUnits` to the URL query string
    if (getSuccessorUnits() != null) {
      for (int i = 0; i < getSuccessorUnits().size(); i++) {
        if (getSuccessorUnits().get(i) != null) {
          joiner.add(getSuccessorUnits().get(i).toUrlQueryString(String.format("%ssuccessorUnits%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `ofModel` to the URL query string
    if (getOfModel() != null) {
      joiner.add(getOfModel().toUrlQueryString(prefix + "ofModel" + suffix));
    }

    // add `restrictedData` to the URL query string
    if (getRestrictedData() != null) {
      joiner.add(getRestrictedData().toUrlQueryString(prefix + "restrictedData" + suffix));
    }

    // add `currentState` to the URL query string
    if (getCurrentState() != null) {
      joiner.add(getCurrentState().toUrlQueryString(prefix + "currentState" + suffix));
    }

    // add `dynamicData` to the URL query string
    if (getDynamicData() != null) {
      for (int i = 0; i < getDynamicData().size(); i++) {
        if (getDynamicData().get(i) != null) {
          joiner.add(getDynamicData().get(i).toUrlQueryString(String.format("%sdynamicData%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    // add `createdAt` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    // add `updatedAt` to the URL query string
    if (getUpdatedAt() != null) {
      joiner.add(String.format("%supdatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getUpdatedAt()))));
    }

    return joiner.toString();
  }
}

