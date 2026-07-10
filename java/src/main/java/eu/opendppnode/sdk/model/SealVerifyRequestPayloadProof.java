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
import eu.opendppnode.sdk.model.SealVerifyRequestPayloadProofRfc3161;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Embedded W3C-style proof block. Sources for the signature, public key, certificate chain and RFC 3161 token when the top-level fields are omitted.
 */
@JsonPropertyOrder({
  SealVerifyRequestPayloadProof.JSON_PROPERTY_TYPE,
  SealVerifyRequestPayloadProof.JSON_PROPERTY_PROOF_VALUE,
  SealVerifyRequestPayloadProof.JSON_PROPERTY_SIGNATURE_VALUE,
  SealVerifyRequestPayloadProof.JSON_PROPERTY_PUBLIC_KEY_PEM,
  SealVerifyRequestPayloadProof.JSON_PROPERTY_X5C,
  SealVerifyRequestPayloadProof.JSON_PROPERTY_RFC3161
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class SealVerifyRequestPayloadProof {
  public static final String JSON_PROPERTY_TYPE = "type";
  @jakarta.annotation.Nullable
  private String type;

  public static final String JSON_PROPERTY_PROOF_VALUE = "proofValue";
  @jakarta.annotation.Nullable
  private String proofValue;

  public static final String JSON_PROPERTY_SIGNATURE_VALUE = "signatureValue";
  @jakarta.annotation.Nullable
  private String signatureValue;

  public static final String JSON_PROPERTY_PUBLIC_KEY_PEM = "publicKeyPem";
  @jakarta.annotation.Nullable
  private String publicKeyPem;

  public static final String JSON_PROPERTY_X5C = "x5c";
  @jakarta.annotation.Nullable
  private List<String> x5c = new ArrayList<>();

  public static final String JSON_PROPERTY_RFC3161 = "rfc3161";
  @jakarta.annotation.Nullable
  private SealVerifyRequestPayloadProofRfc3161 rfc3161;

  public SealVerifyRequestPayloadProof() { 
  }

  public SealVerifyRequestPayloadProof type(@jakarta.annotation.Nullable String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getType() {
    return type;
  }


  @JsonProperty(JSON_PROPERTY_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setType(@jakarta.annotation.Nullable String type) {
    this.type = type;
  }


  public SealVerifyRequestPayloadProof proofValue(@jakarta.annotation.Nullable String proofValue) {
    this.proofValue = proofValue;
    return this;
  }

  /**
   * Base64 ECDSA seal — used as &#x60;signature&#x60; when the top-level field is absent.
   * @return proofValue
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PROOF_VALUE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getProofValue() {
    return proofValue;
  }


  @JsonProperty(JSON_PROPERTY_PROOF_VALUE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProofValue(@jakarta.annotation.Nullable String proofValue) {
    this.proofValue = proofValue;
  }


  public SealVerifyRequestPayloadProof signatureValue(@jakarta.annotation.Nullable String signatureValue) {
    this.signatureValue = signatureValue;
    return this;
  }

  /**
   * Legacy alias for &#x60;proofValue&#x60; (checked second).
   * @return signatureValue
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SIGNATURE_VALUE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getSignatureValue() {
    return signatureValue;
  }


  @JsonProperty(JSON_PROPERTY_SIGNATURE_VALUE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSignatureValue(@jakarta.annotation.Nullable String signatureValue) {
    this.signatureValue = signatureValue;
  }


  public SealVerifyRequestPayloadProof publicKeyPem(@jakarta.annotation.Nullable String publicKeyPem) {
    this.publicKeyPem = publicKeyPem;
    return this;
  }

  /**
   * PEM (SPKI) public key — used as &#x60;publicKey&#x60; when the top-level field is absent.
   * @return publicKeyPem
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PUBLIC_KEY_PEM)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getPublicKeyPem() {
    return publicKeyPem;
  }


  @JsonProperty(JSON_PROPERTY_PUBLIC_KEY_PEM)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPublicKeyPem(@jakarta.annotation.Nullable String publicKeyPem) {
    this.publicKeyPem = publicKeyPem;
  }


  public SealVerifyRequestPayloadProof x5c(@jakarta.annotation.Nullable List<String> x5c) {
    this.x5c = x5c;
    return this;
  }

  public SealVerifyRequestPayloadProof addX5cItem(String x5cItem) {
    if (this.x5c == null) {
      this.x5c = new ArrayList<>();
    }
    this.x5c.add(x5cItem);
    return this;
  }

  /**
   * X.509 certificate chain, base64 DER, LEAF FIRST. Enables the &#x60;certificate&#x60; report; the leaf&#39;s SPKI also serves as the public key when none is otherwise supplied.
   * @return x5c
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_X5C)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<String> getX5c() {
    return x5c;
  }


  @JsonProperty(JSON_PROPERTY_X5C)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setX5c(@jakarta.annotation.Nullable List<String> x5c) {
    this.x5c = x5c;
  }


  public SealVerifyRequestPayloadProof rfc3161(@jakarta.annotation.Nullable SealVerifyRequestPayloadProofRfc3161 rfc3161) {
    this.rfc3161 = rfc3161;
    return this;
  }

  /**
   * Get rfc3161
   * @return rfc3161
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RFC3161)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public SealVerifyRequestPayloadProofRfc3161 getRfc3161() {
    return rfc3161;
  }


  @JsonProperty(JSON_PROPERTY_RFC3161)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRfc3161(@jakarta.annotation.Nullable SealVerifyRequestPayloadProofRfc3161 rfc3161) {
    this.rfc3161 = rfc3161;
  }


  /**
   * Return true if this SealVerifyRequest_payload_proof object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SealVerifyRequestPayloadProof sealVerifyRequestPayloadProof = (SealVerifyRequestPayloadProof) o;
    return Objects.equals(this.type, sealVerifyRequestPayloadProof.type) &&
        Objects.equals(this.proofValue, sealVerifyRequestPayloadProof.proofValue) &&
        Objects.equals(this.signatureValue, sealVerifyRequestPayloadProof.signatureValue) &&
        Objects.equals(this.publicKeyPem, sealVerifyRequestPayloadProof.publicKeyPem) &&
        Objects.equals(this.x5c, sealVerifyRequestPayloadProof.x5c) &&
        Objects.equals(this.rfc3161, sealVerifyRequestPayloadProof.rfc3161);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, proofValue, signatureValue, publicKeyPem, x5c, rfc3161);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SealVerifyRequestPayloadProof {\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    proofValue: ").append(toIndentedString(proofValue)).append("\n");
    sb.append("    signatureValue: ").append(toIndentedString(signatureValue)).append("\n");
    sb.append("    publicKeyPem: ").append(toIndentedString(publicKeyPem)).append("\n");
    sb.append("    x5c: ").append(toIndentedString(x5c)).append("\n");
    sb.append("    rfc3161: ").append(toIndentedString(rfc3161)).append("\n");
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

    // add `proofValue` to the URL query string
    if (getProofValue() != null) {
      joiner.add(String.format("%sproofValue%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProofValue()))));
    }

    // add `signatureValue` to the URL query string
    if (getSignatureValue() != null) {
      joiner.add(String.format("%ssignatureValue%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSignatureValue()))));
    }

    // add `publicKeyPem` to the URL query string
    if (getPublicKeyPem() != null) {
      joiner.add(String.format("%spublicKeyPem%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPublicKeyPem()))));
    }

    // add `x5c` to the URL query string
    if (getX5c() != null) {
      for (int i = 0; i < getX5c().size(); i++) {
        joiner.add(String.format("%sx5c%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getX5c().get(i)))));
      }
    }

    // add `rfc3161` to the URL query string
    if (getRfc3161() != null) {
      joiner.add(getRfc3161().toUrlQueryString(prefix + "rfc3161" + suffix));
    }

    return joiner.toString();
  }
}

