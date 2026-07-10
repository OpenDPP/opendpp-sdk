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
 * Present only for x5c-carrying proofs on a &#x60;verified: true&#x60; outcome whose chain is TRUSTED — &#x60;chainValid&#x60; AND &#x60;keyMatchesProof&#x60; both true (the two policy &#x60;verified: false&#x60; responses AND any untrusted-chain outcome omit it): the certified legal identity of the seal creator (eIDAS Art. 36(1)(b)). An untrusted chain is never surfaced, so an emitted report always has &#x60;chainValid: true&#x60;.
 */
@JsonPropertyOrder({
  SealCertificateReport.JSON_PROPERTY_SUBJECT,
  SealCertificateReport.JSON_PROPERTY_ISSUER,
  SealCertificateReport.JSON_PROPERTY_VALID_FROM,
  SealCertificateReport.JSON_PROPERTY_VALID_TO,
  SealCertificateReport.JSON_PROPERTY_CHAIN_VALID,
  SealCertificateReport.JSON_PROPERTY_KEY_MATCHES_PROOF,
  SealCertificateReport.JSON_PROPERTY_ERROR
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class SealCertificateReport {
  public static final String JSON_PROPERTY_SUBJECT = "subject";
  @jakarta.annotation.Nullable
  private String subject;

  public static final String JSON_PROPERTY_ISSUER = "issuer";
  @jakarta.annotation.Nullable
  private String issuer;

  public static final String JSON_PROPERTY_VALID_FROM = "validFrom";
  @jakarta.annotation.Nullable
  private String validFrom;

  public static final String JSON_PROPERTY_VALID_TO = "validTo";
  @jakarta.annotation.Nullable
  private String validTo;

  public static final String JSON_PROPERTY_CHAIN_VALID = "chainValid";
  @jakarta.annotation.Nonnull
  private Boolean chainValid;

  public static final String JSON_PROPERTY_KEY_MATCHES_PROOF = "keyMatchesProof";
  @jakarta.annotation.Nullable
  private Boolean keyMatchesProof;

  /**
   * Present only when the chain could not be parsed.
   */
  public enum ErrorEnum {
    UNPARSEABLE_X5C_CERTIFICATE_CHAIN(String.valueOf("Unparseable x5c certificate chain"));

    private String value;

    ErrorEnum(String value) {
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
    public static ErrorEnum fromValue(String value) {
      for (ErrorEnum b : ErrorEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_ERROR = "error";
  @jakarta.annotation.Nullable
  private ErrorEnum error;

  public SealCertificateReport() { 
  }

  public SealCertificateReport subject(@jakarta.annotation.Nullable String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Leaf-certificate subject (multi-line RDN string as produced by Node&#39;s X509Certificate, e.g. &#x60;CN&#x3D;OpenDPP Demo Eco Industries Seal&#x60;).
   * @return subject
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SUBJECT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getSubject() {
    return subject;
  }


  @JsonProperty(JSON_PROPERTY_SUBJECT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSubject(@jakarta.annotation.Nullable String subject) {
    this.subject = subject;
  }


  public SealCertificateReport issuer(@jakarta.annotation.Nullable String issuer) {
    this.issuer = issuer;
    return this;
  }

  /**
   * Leaf-certificate issuer RDN string.
   * @return issuer
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ISSUER)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getIssuer() {
    return issuer;
  }


  @JsonProperty(JSON_PROPERTY_ISSUER)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIssuer(@jakarta.annotation.Nullable String issuer) {
    this.issuer = issuer;
  }


  public SealCertificateReport validFrom(@jakarta.annotation.Nullable String validFrom) {
    this.validFrom = validFrom;
    return this;
  }

  /**
   * X.509 textual date, e.g. &#x60;Jan 10 00:00:00 2026 GMT&#x60; — NOT ISO 8601.
   * @return validFrom
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_VALID_FROM)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getValidFrom() {
    return validFrom;
  }


  @JsonProperty(JSON_PROPERTY_VALID_FROM)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setValidFrom(@jakarta.annotation.Nullable String validFrom) {
    this.validFrom = validFrom;
  }


  public SealCertificateReport validTo(@jakarta.annotation.Nullable String validTo) {
    this.validTo = validTo;
    return this;
  }

  /**
   * X.509 textual date — NOT ISO 8601.
   * @return validTo
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_VALID_TO)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getValidTo() {
    return validTo;
  }


  @JsonProperty(JSON_PROPERTY_VALID_TO)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setValidTo(@jakarta.annotation.Nullable String validTo) {
    this.validTo = validTo;
  }


  public SealCertificateReport chainValid(@jakarta.annotation.Nonnull Boolean chainValid) {
    this.chainValid = chainValid;
    return this;
  }

  /**
   * True only when every chain link signature-verifies, every certificate is within its validity window, AND the top of the chain is anchored to this node&#39;s seal CA (fingerprint match or signature under the CA key).
   * @return chainValid
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CHAIN_VALID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getChainValid() {
    return chainValid;
  }


  @JsonProperty(JSON_PROPERTY_CHAIN_VALID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setChainValid(@jakarta.annotation.Nonnull Boolean chainValid) {
    this.chainValid = chainValid;
  }


  public SealCertificateReport keyMatchesProof(@jakarta.annotation.Nullable Boolean keyMatchesProof) {
    this.keyMatchesProof = keyMatchesProof;
    return this;
  }

  /**
   * True when the leaf SPKI equals the supplied &#x60;publicKey&#x60; (whitespace-insensitive), or when no explicit public key was supplied (the leaf key was used).
   * @return keyMatchesProof
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_KEY_MATCHES_PROOF)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getKeyMatchesProof() {
    return keyMatchesProof;
  }


  @JsonProperty(JSON_PROPERTY_KEY_MATCHES_PROOF)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setKeyMatchesProof(@jakarta.annotation.Nullable Boolean keyMatchesProof) {
    this.keyMatchesProof = keyMatchesProof;
  }


  public SealCertificateReport error(@jakarta.annotation.Nullable ErrorEnum error) {
    this.error = error;
    return this;
  }

  /**
   * Present only when the chain could not be parsed.
   * @return error
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ERROR)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public ErrorEnum getError() {
    return error;
  }


  @JsonProperty(JSON_PROPERTY_ERROR)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setError(@jakarta.annotation.Nullable ErrorEnum error) {
    this.error = error;
  }


  /**
   * Return true if this SealCertificateReport object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SealCertificateReport sealCertificateReport = (SealCertificateReport) o;
    return Objects.equals(this.subject, sealCertificateReport.subject) &&
        Objects.equals(this.issuer, sealCertificateReport.issuer) &&
        Objects.equals(this.validFrom, sealCertificateReport.validFrom) &&
        Objects.equals(this.validTo, sealCertificateReport.validTo) &&
        Objects.equals(this.chainValid, sealCertificateReport.chainValid) &&
        Objects.equals(this.keyMatchesProof, sealCertificateReport.keyMatchesProof) &&
        Objects.equals(this.error, sealCertificateReport.error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, issuer, validFrom, validTo, chainValid, keyMatchesProof, error);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SealCertificateReport {\n");
    sb.append("    subject: ").append(toIndentedString(subject)).append("\n");
    sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
    sb.append("    validFrom: ").append(toIndentedString(validFrom)).append("\n");
    sb.append("    validTo: ").append(toIndentedString(validTo)).append("\n");
    sb.append("    chainValid: ").append(toIndentedString(chainValid)).append("\n");
    sb.append("    keyMatchesProof: ").append(toIndentedString(keyMatchesProof)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
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

    // add `subject` to the URL query string
    if (getSubject() != null) {
      joiner.add(String.format("%ssubject%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSubject()))));
    }

    // add `issuer` to the URL query string
    if (getIssuer() != null) {
      joiner.add(String.format("%sissuer%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIssuer()))));
    }

    // add `validFrom` to the URL query string
    if (getValidFrom() != null) {
      joiner.add(String.format("%svalidFrom%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getValidFrom()))));
    }

    // add `validTo` to the URL query string
    if (getValidTo() != null) {
      joiner.add(String.format("%svalidTo%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getValidTo()))));
    }

    // add `chainValid` to the URL query string
    if (getChainValid() != null) {
      joiner.add(String.format("%schainValid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getChainValid()))));
    }

    // add `keyMatchesProof` to the URL query string
    if (getKeyMatchesProof() != null) {
      joiner.add(String.format("%skeyMatchesProof%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getKeyMatchesProof()))));
    }

    // add `error` to the URL query string
    if (getError() != null) {
      joiner.add(String.format("%serror%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getError()))));
    }

    return joiner.toString();
  }
}

