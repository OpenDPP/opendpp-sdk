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
import eu.opendppnode.sdk.model.ValidationErrorItem;
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
 * 201 envelope of &#x60;POST /api/v1/passports/aas/ingest&#x60;. Returned for both newly created passports and in-place updates of existing UNSEALED passports. No webhook event is emitted by this endpoint. &#x60;vcReady&#x60;/&#x60;vcReadyReason&#x60; report UNTP Verifiable-Credential readiness (#247) and &#x60;warnings&#x60; carries the non-GS1 advisory (#249), for parity with &#x60;POST /api/v1/passports&#x60;.
 */
@JsonPropertyOrder({
  AasIngestCreated.JSON_PROPERTY_SUCCESS,
  AasIngestCreated.JSON_PROPERTY_MESSAGE,
  AasIngestCreated.JSON_PROPERTY_PASSPORT_ID,
  AasIngestCreated.JSON_PROPERTY_PRODUCT_ID,
  AasIngestCreated.JSON_PROPERTY_IS_SEALED,
  AasIngestCreated.JSON_PROPERTY_SIGNATURE_VERIFIED,
  AasIngestCreated.JSON_PROPERTY_VC_READY,
  AasIngestCreated.JSON_PROPERTY_VC_READY_REASON,
  AasIngestCreated.JSON_PROPERTY_WARNINGS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class AasIngestCreated {
  /**
   * Gets or Sets success
   */
  public enum SuccessEnum {
    TRUE(Boolean.valueOf("true"));

    private Boolean value;

    SuccessEnum(Boolean value) {
      this.value = value;
    }

    @JsonValue
    public Boolean getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SuccessEnum fromValue(Boolean value) {
      for (SuccessEnum b : SuccessEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_SUCCESS = "success";
  @jakarta.annotation.Nonnull
  private SuccessEnum success;

  /**
   * Gets or Sets message
   */
  public enum MessageEnum {
    DIGITAL_PRODUCT_PASSPORT_SUCCESSFULLY_INGESTED_FROM_AAS(String.valueOf("Digital Product Passport successfully ingested from AAS"));

    private String value;

    MessageEnum(String value) {
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
    public static MessageEnum fromValue(String value) {
      for (MessageEnum b : MessageEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_MESSAGE = "message";
  @jakarta.annotation.Nonnull
  private MessageEnum message;

  public static final String JSON_PROPERTY_PASSPORT_ID = "passportId";
  @jakarta.annotation.Nonnull
  private String passportId;

  public static final String JSON_PROPERTY_PRODUCT_ID = "productId";
  @jakarta.annotation.Nonnull
  private String productId;

  public static final String JSON_PROPERTY_IS_SEALED = "isSealed";
  @jakarta.annotation.Nonnull
  private Boolean isSealed;

  public static final String JSON_PROPERTY_SIGNATURE_VERIFIED = "signatureVerified";
  @jakarta.annotation.Nonnull
  private Boolean signatureVerified;

  public static final String JSON_PROPERTY_VC_READY = "vcReady";
  @jakarta.annotation.Nonnull
  private Boolean vcReady;

  public static final String JSON_PROPERTY_VC_READY_REASON = "vcReadyReason";
  private JsonNullable<String> vcReadyReason = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_WARNINGS = "warnings";
  @jakarta.annotation.Nonnull
  private List<ValidationErrorItem> warnings = new ArrayList<>();

  public AasIngestCreated() { 
  }

  public AasIngestCreated success(@jakarta.annotation.Nonnull SuccessEnum success) {
    this.success = success;
    return this;
  }

  /**
   * Get success
   * @return success
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public SuccessEnum getSuccess() {
    return success;
  }


  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSuccess(@jakarta.annotation.Nonnull SuccessEnum success) {
    this.success = success;
  }


  public AasIngestCreated message(@jakarta.annotation.Nonnull MessageEnum message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public MessageEnum getMessage() {
    return message;
  }


  @JsonProperty(JSON_PROPERTY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMessage(@jakarta.annotation.Nonnull MessageEnum message) {
    this.message = message;
  }


  public AasIngestCreated passportId(@jakarta.annotation.Nonnull String passportId) {
    this.passportId = passportId;
    return this;
  }

  /**
   * Get passportId
   * @return passportId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPassportId() {
    return passportId;
  }


  @JsonProperty(JSON_PROPERTY_PASSPORT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPassportId(@jakarta.annotation.Nonnull String passportId) {
    this.passportId = passportId;
  }


  public AasIngestCreated productId(@jakarta.annotation.Nonnull String productId) {
    this.productId = productId;
    return this;
  }

  /**
   * Get productId
   * @return productId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PRODUCT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getProductId() {
    return productId;
  }


  @JsonProperty(JSON_PROPERTY_PRODUCT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProductId(@jakarta.annotation.Nonnull String productId) {
    this.productId = productId;
  }


  public AasIngestCreated isSealed(@jakarta.annotation.Nonnull Boolean isSealed) {
    this.isSealed = isSealed;
    return this;
  }

  /**
   * True when the environment embedded an &#x60;eidasVerificationSeal&#x60; submodel (the seal is then stored on the passport).
   * @return isSealed
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_IS_SEALED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsSealed() {
    return isSealed;
  }


  @JsonProperty(JSON_PROPERTY_IS_SEALED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsSealed(@jakarta.annotation.Nonnull Boolean isSealed) {
    this.isSealed = isSealed;
  }


  public AasIngestCreated signatureVerified(@jakarta.annotation.Nonnull Boolean signatureVerified) {
    this.signatureVerified = signatureVerified;
    return this;
  }

  /**
   * True when the embedded seal verified against the tenant&#39;s server-held eIDAS public key. Always false for unsealed documents. (A sealed-but-unverified document never reaches 201 — it fails 400.)
   * @return signatureVerified
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SIGNATURE_VERIFIED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getSignatureVerified() {
    return signatureVerified;
  }


  @JsonProperty(JSON_PROPERTY_SIGNATURE_VERIFIED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSignatureVerified(@jakarta.annotation.Nonnull Boolean signatureVerified) {
    this.signatureVerified = signatureVerified;
  }


  public AasIngestCreated vcReady(@jakarta.annotation.Nonnull Boolean vcReady) {
    this.vcReady = vcReady;
    return this;
  }

  /**
   * #247: whether the ingested passport can emit a UNTP Verifiable Credential — true only when a manufacturing facility with a country of production is linked. AAS ingestion does not set a facility, so a newly created passport is false; an in-place update preserves whatever facility the existing passport had.
   * @return vcReady
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_VC_READY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getVcReady() {
    return vcReady;
  }


  @JsonProperty(JSON_PROPERTY_VC_READY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setVcReady(@jakarta.annotation.Nonnull Boolean vcReady) {
    this.vcReady = vcReady;
  }


  public AasIngestCreated vcReadyReason(@jakarta.annotation.Nullable String vcReadyReason) {
    this.vcReadyReason = JsonNullable.<String>of(vcReadyReason);
    return this;
  }

  /**
   * Null when &#x60;vcReady&#x60; is true; otherwise a short, actionable reason (link a facility with a country of production).
   * @return vcReadyReason
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public String getVcReadyReason() {
        return vcReadyReason.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_VC_READY_REASON)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getVcReadyReason_JsonNullable() {
    return vcReadyReason;
  }
  
  @JsonProperty(JSON_PROPERTY_VC_READY_REASON)
  public void setVcReadyReason_JsonNullable(JsonNullable<String> vcReadyReason) {
    this.vcReadyReason = vcReadyReason;
  }

  public void setVcReadyReason(@jakarta.annotation.Nullable String vcReadyReason) {
    this.vcReadyReason = JsonNullable.<String>of(vcReadyReason);
  }


  public AasIngestCreated warnings(@jakarta.annotation.Nonnull List<ValidationErrorItem> warnings) {
    this.warnings = warnings;
    return this;
  }

  public AasIngestCreated addWarningsItem(ValidationErrorItem warningsItem) {
    if (this.warnings == null) {
      this.warnings = new ArrayList<>();
    }
    this.warnings.add(warningsItem);
    return this;
  }

  /**
   * #249: non-blocking advisories. Always present (empty array when none); carries the non-GS1 advisory when the resolved &#x60;productId&#x60; is not a GS1 GTIN-14/GRAI.
   * @return warnings
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_WARNINGS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<ValidationErrorItem> getWarnings() {
    return warnings;
  }


  @JsonProperty(JSON_PROPERTY_WARNINGS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setWarnings(@jakarta.annotation.Nonnull List<ValidationErrorItem> warnings) {
    this.warnings = warnings;
  }


  /**
   * Return true if this AasIngestCreated object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AasIngestCreated aasIngestCreated = (AasIngestCreated) o;
    return Objects.equals(this.success, aasIngestCreated.success) &&
        Objects.equals(this.message, aasIngestCreated.message) &&
        Objects.equals(this.passportId, aasIngestCreated.passportId) &&
        Objects.equals(this.productId, aasIngestCreated.productId) &&
        Objects.equals(this.isSealed, aasIngestCreated.isSealed) &&
        Objects.equals(this.signatureVerified, aasIngestCreated.signatureVerified) &&
        Objects.equals(this.vcReady, aasIngestCreated.vcReady) &&
        equalsNullable(this.vcReadyReason, aasIngestCreated.vcReadyReason) &&
        Objects.equals(this.warnings, aasIngestCreated.warnings);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, message, passportId, productId, isSealed, signatureVerified, vcReady, hashCodeNullable(vcReadyReason), warnings);
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
    sb.append("class AasIngestCreated {\n");
    sb.append("    success: ").append(toIndentedString(success)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    passportId: ").append(toIndentedString(passportId)).append("\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("    isSealed: ").append(toIndentedString(isSealed)).append("\n");
    sb.append("    signatureVerified: ").append(toIndentedString(signatureVerified)).append("\n");
    sb.append("    vcReady: ").append(toIndentedString(vcReady)).append("\n");
    sb.append("    vcReadyReason: ").append(toIndentedString(vcReadyReason)).append("\n");
    sb.append("    warnings: ").append(toIndentedString(warnings)).append("\n");
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

    // add `success` to the URL query string
    if (getSuccess() != null) {
      joiner.add(String.format("%ssuccess%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSuccess()))));
    }

    // add `message` to the URL query string
    if (getMessage() != null) {
      joiner.add(String.format("%smessage%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMessage()))));
    }

    // add `passportId` to the URL query string
    if (getPassportId() != null) {
      joiner.add(String.format("%spassportId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPassportId()))));
    }

    // add `productId` to the URL query string
    if (getProductId() != null) {
      joiner.add(String.format("%sproductId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProductId()))));
    }

    // add `isSealed` to the URL query string
    if (getIsSealed() != null) {
      joiner.add(String.format("%sisSealed%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIsSealed()))));
    }

    // add `signatureVerified` to the URL query string
    if (getSignatureVerified() != null) {
      joiner.add(String.format("%ssignatureVerified%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSignatureVerified()))));
    }

    // add `vcReady` to the URL query string
    if (getVcReady() != null) {
      joiner.add(String.format("%svcReady%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVcReady()))));
    }

    // add `vcReadyReason` to the URL query string
    if (getVcReadyReason() != null) {
      joiner.add(String.format("%svcReadyReason%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVcReadyReason()))));
    }

    // add `warnings` to the URL query string
    if (getWarnings() != null) {
      for (int i = 0; i < getWarnings().size(); i++) {
        if (getWarnings().get(i) != null) {
          joiner.add(getWarnings().get(i).toUrlQueryString(String.format("%swarnings%s%s", prefix, suffix,
          "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
        }
      }
    }

    return joiner.toString();
  }
}

