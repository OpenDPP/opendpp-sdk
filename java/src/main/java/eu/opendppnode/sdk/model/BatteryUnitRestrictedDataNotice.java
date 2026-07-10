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
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Marker replacing per-unit telemetry in anonymous (public-tier) responses, with a pointer for requesting legitimate-interest access (Reg. (EU) 2023/1542, Annex XIII(2)-(4)).
 */
@JsonPropertyOrder({
  BatteryUnitRestrictedDataNotice.JSON_PROPERTY_REASON,
  BatteryUnitRestrictedDataNotice.JSON_PROPERTY_REFERENCE,
  BatteryUnitRestrictedDataNotice.JSON_PROPERTY_DESCRIPTION,
  BatteryUnitRestrictedDataNotice.JSON_PROPERTY_HOW_TO_REQUEST
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class BatteryUnitRestrictedDataNotice {
  /**
   * Gets or Sets reason
   */
  public enum ReasonEnum {
    LEGITIMATE_INTEREST_REQUIRED(String.valueOf("LEGITIMATE_INTEREST_REQUIRED"));

    private String value;

    ReasonEnum(String value) {
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
    public static ReasonEnum fromValue(String value) {
      for (ReasonEnum b : ReasonEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_REASON = "reason";
  @jakarta.annotation.Nonnull
  private ReasonEnum reason;

  /**
   * Gets or Sets reference
   */
  public enum ReferenceEnum {
    REGULATION_EU_2023_1542_ANNEX_XIII_2_4_(String.valueOf("Regulation (EU) 2023/1542, Annex XIII(2)-(4)"));

    private String value;

    ReferenceEnum(String value) {
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
    public static ReferenceEnum fromValue(String value) {
      for (ReferenceEnum b : ReferenceEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_REFERENCE = "reference";
  @jakarta.annotation.Nonnull
  private ReferenceEnum reference;

  /**
   * Gets or Sets description
   */
  public enum DescriptionEnum {
    PER_UNIT_DYNAMIC_DATA_STATE_OF_HEALTH_CYCLE_COUNTS_NEGATIVE_EVENTS_TEMPERATURE_IS_ACCESSIBLE_ONLY_TO_PERSONS_WITH_A_LEGITIMATE_INTEREST_AND_TO_AUTHORITIES_(String.valueOf("Per-unit dynamic data (state of health, cycle counts, negative events, temperature) is accessible only to persons with a legitimate interest and to authorities."));

    private String value;

    DescriptionEnum(String value) {
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
    public static DescriptionEnum fromValue(String value) {
      for (DescriptionEnum b : DescriptionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_DESCRIPTION = "description";
  @jakarta.annotation.Nonnull
  private DescriptionEnum description;

  public static final String JSON_PROPERTY_HOW_TO_REQUEST = "howToRequest";
  @jakarta.annotation.Nonnull
  private String howToRequest;

  public BatteryUnitRestrictedDataNotice() { 
  }

  public BatteryUnitRestrictedDataNotice reason(@jakarta.annotation.Nonnull ReasonEnum reason) {
    this.reason = reason;
    return this;
  }

  /**
   * Get reason
   * @return reason
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_REASON)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ReasonEnum getReason() {
    return reason;
  }


  @JsonProperty(JSON_PROPERTY_REASON)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setReason(@jakarta.annotation.Nonnull ReasonEnum reason) {
    this.reason = reason;
  }


  public BatteryUnitRestrictedDataNotice reference(@jakarta.annotation.Nonnull ReferenceEnum reference) {
    this.reference = reference;
    return this;
  }

  /**
   * Get reference
   * @return reference
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_REFERENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ReferenceEnum getReference() {
    return reference;
  }


  @JsonProperty(JSON_PROPERTY_REFERENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setReference(@jakarta.annotation.Nonnull ReferenceEnum reference) {
    this.reference = reference;
  }


  public BatteryUnitRestrictedDataNotice description(@jakarta.annotation.Nonnull DescriptionEnum description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_DESCRIPTION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public DescriptionEnum getDescription() {
    return description;
  }


  @JsonProperty(JSON_PROPERTY_DESCRIPTION)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDescription(@jakarta.annotation.Nonnull DescriptionEnum description) {
    this.description = description;
  }


  public BatteryUnitRestrictedDataNotice howToRequest(@jakarta.annotation.Nonnull String howToRequest) {
    this.howToRequest = howToRequest;
    return this;
  }

  /**
   * Relative URL &#x60;/request-access?unit&#x3D;{unitId}&#x60; where a legitimate-interest grant can be requested.
   * @return howToRequest
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_HOW_TO_REQUEST)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getHowToRequest() {
    return howToRequest;
  }


  @JsonProperty(JSON_PROPERTY_HOW_TO_REQUEST)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setHowToRequest(@jakarta.annotation.Nonnull String howToRequest) {
    this.howToRequest = howToRequest;
  }


  /**
   * Return true if this BatteryUnitRestrictedDataNotice object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatteryUnitRestrictedDataNotice batteryUnitRestrictedDataNotice = (BatteryUnitRestrictedDataNotice) o;
    return Objects.equals(this.reason, batteryUnitRestrictedDataNotice.reason) &&
        Objects.equals(this.reference, batteryUnitRestrictedDataNotice.reference) &&
        Objects.equals(this.description, batteryUnitRestrictedDataNotice.description) &&
        Objects.equals(this.howToRequest, batteryUnitRestrictedDataNotice.howToRequest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reason, reference, description, howToRequest);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatteryUnitRestrictedDataNotice {\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    reference: ").append(toIndentedString(reference)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    howToRequest: ").append(toIndentedString(howToRequest)).append("\n");
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

    // add `reason` to the URL query string
    if (getReason() != null) {
      joiner.add(String.format("%sreason%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getReason()))));
    }

    // add `reference` to the URL query string
    if (getReference() != null) {
      joiner.add(String.format("%sreference%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getReference()))));
    }

    // add `description` to the URL query string
    if (getDescription() != null) {
      joiner.add(String.format("%sdescription%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getDescription()))));
    }

    // add `howToRequest` to the URL query string
    if (getHowToRequest() != null) {
      joiner.add(String.format("%showToRequest%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getHowToRequest()))));
    }

    return joiner.toString();
  }
}

