/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.15.0
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
 * 201 envelope of &#x60;POST /api/v1/passports/aas/ingest&#x60;. Returned for both newly created passports and in-place updates of existing UNSEALED passports. No webhook event is emitted by this endpoint. &#x60;vcReady&#x60;/&#x60;vcReadyReason&#x60; report UNTP Verifiable-Credential readiness and &#x60;warnings&#x60; carries the non-GS1 advisory, for parity with &#x60;POST /api/v1/passports&#x60;.
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
  public static final String JSON_PROPERTY_SUCCESS = "success";
  @jakarta.annotation.Nonnull
  private Boolean success;

  /**
   * Gets or Sets message
   */
  public enum MessageEnum {
    DIGITAL_PRODUCT_PASSPORT_SUCCESSFULLY_INGESTED_FROM_AAS(String.valueOf("Digital Product Passport successfully ingested from AAS")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

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
      return UNKNOWN_DEFAULT_OPEN_API;
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

  public AasIngestCreated success(@jakarta.annotation.Nonnull Boolean success) {
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
  public Boolean getSuccess() {
    return success;
  }


  @JsonProperty(JSON_PROPERTY_SUCCESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSuccess(@jakarta.annotation.Nonnull Boolean success) {
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
   * True when the embedded seal verified against the tenant&#39;s server-held signing public key. Always false for unsealed documents. (A sealed-but-unverified document never reaches 201 — it fails 400.)
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
   * Whether the ingested passport can emit a UNTP Verifiable Credential — true only when a manufacturing facility with a country of production is linked. AAS ingestion does not set a facility, so a newly created passport is false; an in-place update preserves whatever facility the existing passport had.
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
   * Non-blocking advisories. Always present (empty array when none); carries the non-GS1 advisory when the resolved &#x60;productId&#x60; is not a GS1 GTIN-14/GRAI.
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

