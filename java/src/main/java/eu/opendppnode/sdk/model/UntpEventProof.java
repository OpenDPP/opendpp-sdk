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
import eu.opendppnode.sdk.model.UntpEventProofVerificationMethod;
import java.time.OffsetDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Credential proof. MUST be a conformant W3C &#x60;DataIntegrityProof&#x60; with &#x60;cryptosuite: \&quot;ecdsa-jcs-2019\&quot;&#x60; and a multibase base58btc (&#x60;z…&#x60;) &#x60;proofValue&#x60;. Verified (ECDSA P-256, IEEE-P1363 raw r‖s) over &#x60;sha256(JCS(proof options)) ‖ sha256(JCS(credential without proof))&#x60; — RFC 8785 JCS canonicalization, a conformant W3C Data Integrity suite.
 */
@JsonPropertyOrder({
  UntpEventProof.JSON_PROPERTY_TYPE,
  UntpEventProof.JSON_PROPERTY_CRYPTOSUITE,
  UntpEventProof.JSON_PROPERTY_CREATED,
  UntpEventProof.JSON_PROPERTY_PROOF_PURPOSE,
  UntpEventProof.JSON_PROPERTY_VERIFICATION_METHOD,
  UntpEventProof.JSON_PROPERTY_PROOF_VALUE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class UntpEventProof {
  /**
   * MUST be &#x60;DataIntegrityProof&#x60;.
   */
  public enum TypeEnum {
    DATA_INTEGRITY_PROOF(String.valueOf("DataIntegrityProof"));

    private String value;

    TypeEnum(String value) {
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
    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_TYPE = "type";
  @jakarta.annotation.Nonnull
  private TypeEnum type;

  /**
   * MUST be &#x60;ecdsa-jcs-2019&#x60; (RFC 8785 JCS).
   */
  public enum CryptosuiteEnum {
    ECDSA_JCS_2019(String.valueOf("ecdsa-jcs-2019"));

    private String value;

    CryptosuiteEnum(String value) {
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
    public static CryptosuiteEnum fromValue(String value) {
      for (CryptosuiteEnum b : CryptosuiteEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_CRYPTOSUITE = "cryptosuite";
  @jakarta.annotation.Nonnull
  private CryptosuiteEnum cryptosuite;

  public static final String JSON_PROPERTY_CREATED = "created";
  @jakarta.annotation.Nullable
  private OffsetDateTime created;

  public static final String JSON_PROPERTY_PROOF_PURPOSE = "proofPurpose";
  @jakarta.annotation.Nullable
  private String proofPurpose;

  public static final String JSON_PROPERTY_VERIFICATION_METHOD = "verificationMethod";
  @jakarta.annotation.Nullable
  private UntpEventProofVerificationMethod verificationMethod;

  public static final String JSON_PROPERTY_PROOF_VALUE = "proofValue";
  @jakarta.annotation.Nonnull
  private String proofValue;

  public UntpEventProof() { 
  }

  public UntpEventProof type(@jakarta.annotation.Nonnull TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * MUST be &#x60;DataIntegrityProof&#x60;.
   * @return type
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public TypeEnum getType() {
    return type;
  }


  @JsonProperty(JSON_PROPERTY_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setType(@jakarta.annotation.Nonnull TypeEnum type) {
    this.type = type;
  }


  public UntpEventProof cryptosuite(@jakarta.annotation.Nonnull CryptosuiteEnum cryptosuite) {
    this.cryptosuite = cryptosuite;
    return this;
  }

  /**
   * MUST be &#x60;ecdsa-jcs-2019&#x60; (RFC 8785 JCS).
   * @return cryptosuite
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CRYPTOSUITE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public CryptosuiteEnum getCryptosuite() {
    return cryptosuite;
  }


  @JsonProperty(JSON_PROPERTY_CRYPTOSUITE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCryptosuite(@jakarta.annotation.Nonnull CryptosuiteEnum cryptosuite) {
    this.cryptosuite = cryptosuite;
  }


  public UntpEventProof created(@jakarta.annotation.Nullable OffsetDateTime created) {
    this.created = created;
    return this;
  }

  /**
   * Get created
   * @return created
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CREATED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public OffsetDateTime getCreated() {
    return created;
  }


  @JsonProperty(JSON_PROPERTY_CREATED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCreated(@jakarta.annotation.Nullable OffsetDateTime created) {
    this.created = created;
  }


  public UntpEventProof proofPurpose(@jakarta.annotation.Nullable String proofPurpose) {
    this.proofPurpose = proofPurpose;
    return this;
  }

  /**
   * e.g. &#x60;assertionMethod&#x60;.
   * @return proofPurpose
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PROOF_PURPOSE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getProofPurpose() {
    return proofPurpose;
  }


  @JsonProperty(JSON_PROPERTY_PROOF_PURPOSE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProofPurpose(@jakarta.annotation.Nullable String proofPurpose) {
    this.proofPurpose = proofPurpose;
  }


  public UntpEventProof verificationMethod(@jakarta.annotation.Nullable UntpEventProofVerificationMethod verificationMethod) {
    this.verificationMethod = verificationMethod;
    return this;
  }

  /**
   * Get verificationMethod
   * @return verificationMethod
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_VERIFICATION_METHOD)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public UntpEventProofVerificationMethod getVerificationMethod() {
    return verificationMethod;
  }


  @JsonProperty(JSON_PROPERTY_VERIFICATION_METHOD)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVerificationMethod(@jakarta.annotation.Nullable UntpEventProofVerificationMethod verificationMethod) {
    this.verificationMethod = verificationMethod;
  }


  public UntpEventProof proofValue(@jakarta.annotation.Nonnull String proofValue) {
    this.proofValue = proofValue;
    return this;
  }

  /**
   * Multibase base58btc (&#x60;z…&#x60;) ecdsa-jcs-2019 signature. Stored verbatim with the event.
   * @return proofValue
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PROOF_VALUE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getProofValue() {
    return proofValue;
  }


  @JsonProperty(JSON_PROPERTY_PROOF_VALUE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProofValue(@jakarta.annotation.Nonnull String proofValue) {
    this.proofValue = proofValue;
  }


  /**
   * Return true if this UntpEventProof object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UntpEventProof untpEventProof = (UntpEventProof) o;
    return Objects.equals(this.type, untpEventProof.type) &&
        Objects.equals(this.cryptosuite, untpEventProof.cryptosuite) &&
        Objects.equals(this.created, untpEventProof.created) &&
        Objects.equals(this.proofPurpose, untpEventProof.proofPurpose) &&
        Objects.equals(this.verificationMethod, untpEventProof.verificationMethod) &&
        Objects.equals(this.proofValue, untpEventProof.proofValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, cryptosuite, created, proofPurpose, verificationMethod, proofValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UntpEventProof {\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    cryptosuite: ").append(toIndentedString(cryptosuite)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
    sb.append("    proofPurpose: ").append(toIndentedString(proofPurpose)).append("\n");
    sb.append("    verificationMethod: ").append(toIndentedString(verificationMethod)).append("\n");
    sb.append("    proofValue: ").append(toIndentedString(proofValue)).append("\n");
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

    // add `type` to the URL query string
    if (getType() != null) {
      joiner.add(String.format("%stype%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getType()))));
    }

    // add `cryptosuite` to the URL query string
    if (getCryptosuite() != null) {
      joiner.add(String.format("%scryptosuite%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCryptosuite()))));
    }

    // add `created` to the URL query string
    if (getCreated() != null) {
      joiner.add(String.format("%screated%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreated()))));
    }

    // add `proofPurpose` to the URL query string
    if (getProofPurpose() != null) {
      joiner.add(String.format("%sproofPurpose%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProofPurpose()))));
    }

    // add `verificationMethod` to the URL query string
    if (getVerificationMethod() != null) {
      joiner.add(getVerificationMethod().toUrlQueryString(prefix + "verificationMethod" + suffix));
    }

    // add `proofValue` to the URL query string
    if (getProofValue() != null) {
      joiner.add(String.format("%sproofValue%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProofValue()))));
    }

    return joiner.toString();
  }
}

