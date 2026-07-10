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
import eu.opendppnode.sdk.model.UntpEventCredentialSubject;
import eu.opendppnode.sdk.model.UntpEventProof;
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
 * A UNTP/EPCIS 2.0 traceability event wrapped as a VC-shaped credential. The only hard structural requirement is &#x60;credentialSubject&#x60;; the &#x60;proof&#x60; MUST be a conformant W3C &#x60;DataIntegrityProof&#x60; (&#x60;cryptosuite: \&quot;ecdsa-jcs-2019\&quot;&#x60;) and a missing, non-conformant, or unverifiable proof is rejected with the 400 &#x60;Cryptographic Verification Failed&#x60; body. Extra properties are permitted — the signature covers &#x60;sha256(JCS(proof options)) ‖ sha256(JCS(credential without proof))&#x60; (RFC 8785 JCS canonicalization).
 */
@JsonPropertyOrder({
  UntpEventCredential.JSON_PROPERTY_AT_CONTEXT,
  UntpEventCredential.JSON_PROPERTY_ID,
  UntpEventCredential.JSON_PROPERTY_TYPE,
  UntpEventCredential.JSON_PROPERTY_ISSUER,
  UntpEventCredential.JSON_PROPERTY_ISSUANCE_DATE,
  UntpEventCredential.JSON_PROPERTY_CREDENTIAL_SUBJECT,
  UntpEventCredential.JSON_PROPERTY_PROOF
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class UntpEventCredential {
  public static final String JSON_PROPERTY_AT_CONTEXT = "@context";
  private JsonNullable<Object> atContext = JsonNullable.<Object>of(null);

  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nullable
  private String id;

  public static final String JSON_PROPERTY_TYPE = "type";
  @jakarta.annotation.Nullable
  private List<String> type = new ArrayList<>();

  public static final String JSON_PROPERTY_ISSUER = "issuer";
  @jakarta.annotation.Nullable
  private String issuer;

  public static final String JSON_PROPERTY_ISSUANCE_DATE = "issuanceDate";
  @jakarta.annotation.Nullable
  private OffsetDateTime issuanceDate;

  public static final String JSON_PROPERTY_CREDENTIAL_SUBJECT = "credentialSubject";
  @jakarta.annotation.Nonnull
  private UntpEventCredentialSubject credentialSubject;

  public static final String JSON_PROPERTY_PROOF = "proof";
  @jakarta.annotation.Nonnull
  private UntpEventProof proof;

  public UntpEventCredential() { 
  }

  public UntpEventCredential atContext(@jakarta.annotation.Nullable Object atContext) {
    this.atContext = JsonNullable.<Object>of(atContext);
    return this;
  }

  /**
   * Get atContext
   * @return atContext
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public Object getAtContext() {
        return atContext.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_AT_CONTEXT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<Object> getAtContext_JsonNullable() {
    return atContext;
  }
  
  @JsonProperty(JSON_PROPERTY_AT_CONTEXT)
  public void setAtContext_JsonNullable(JsonNullable<Object> atContext) {
    this.atContext = atContext;
  }

  public void setAtContext(@jakarta.annotation.Nullable Object atContext) {
    this.atContext = JsonNullable.<Object>of(atContext);
  }


  public UntpEventCredential id(@jakarta.annotation.Nullable String id) {
    this.id = id;
    return this;
  }

  /**
   * Credential id (e.g. &#x60;urn:uuid:...&#x60;). NOT used as the stored event id — the event primary key is always server-generated.
   * @return id
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getId() {
    return id;
  }


  @JsonProperty(JSON_PROPERTY_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setId(@jakarta.annotation.Nullable String id) {
    this.id = id;
  }


  public UntpEventCredential type(@jakarta.annotation.Nullable List<String> type) {
    this.type = type;
    return this;
  }

  public UntpEventCredential addTypeItem(String typeItem) {
    if (this.type == null) {
      this.type = new ArrayList<>();
    }
    this.type.add(typeItem);
    return this;
  }

  /**
   * Get type
   * @return type
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<String> getType() {
    return type;
  }


  @JsonProperty(JSON_PROPERTY_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setType(@jakarta.annotation.Nullable List<String> type) {
    this.type = type;
  }


  public UntpEventCredential issuer(@jakarta.annotation.Nullable String issuer) {
    this.issuer = issuer;
    return this;
  }

  /**
   * Issuer DID. Unless a trusted x5c chain is embedded, the verification key is resolved by EXACT match of the DID&#39;s trailing &#x60;:&#x60;-segment against registered tenant subdomains/company names — e.g. &#x60;did:web:opendpp-node.eu:demo&#x60; resolves the workspace with subdomain &#x60;demo&#x60;. For operator-scoped API keys the issuer DID must ALSO contain the bound operator&#39;s registration id somewhere in the string (the issuer is checked in preference to &#x60;credentialSubject.responsibleOperatorDid&#x60;), e.g. &#x60;did:web:opendpp-node.eu:EU-DEFAULT-001:demo&#x60;. Stored verbatim as the event&#39;s &#x60;issuerDid&#x60;.
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


  public UntpEventCredential issuanceDate(@jakarta.annotation.Nullable OffsetDateTime issuanceDate) {
    this.issuanceDate = issuanceDate;
    return this;
  }

  /**
   * Fallback for the stored &#x60;eventTime&#x60; when &#x60;credentialSubject.eventTime&#x60; is absent.
   * @return issuanceDate
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ISSUANCE_DATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public OffsetDateTime getIssuanceDate() {
    return issuanceDate;
  }


  @JsonProperty(JSON_PROPERTY_ISSUANCE_DATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIssuanceDate(@jakarta.annotation.Nullable OffsetDateTime issuanceDate) {
    this.issuanceDate = issuanceDate;
  }


  public UntpEventCredential credentialSubject(@jakarta.annotation.Nonnull UntpEventCredentialSubject credentialSubject) {
    this.credentialSubject = credentialSubject;
    return this;
  }

  /**
   * Get credentialSubject
   * @return credentialSubject
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CREDENTIAL_SUBJECT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public UntpEventCredentialSubject getCredentialSubject() {
    return credentialSubject;
  }


  @JsonProperty(JSON_PROPERTY_CREDENTIAL_SUBJECT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCredentialSubject(@jakarta.annotation.Nonnull UntpEventCredentialSubject credentialSubject) {
    this.credentialSubject = credentialSubject;
  }


  public UntpEventCredential proof(@jakarta.annotation.Nonnull UntpEventProof proof) {
    this.proof = proof;
    return this;
  }

  /**
   * Get proof
   * @return proof
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PROOF)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public UntpEventProof getProof() {
    return proof;
  }


  @JsonProperty(JSON_PROPERTY_PROOF)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProof(@jakarta.annotation.Nonnull UntpEventProof proof) {
    this.proof = proof;
  }


  /**
   * Return true if this UntpEventCredential object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UntpEventCredential untpEventCredential = (UntpEventCredential) o;
    return equalsNullable(this.atContext, untpEventCredential.atContext) &&
        Objects.equals(this.id, untpEventCredential.id) &&
        Objects.equals(this.type, untpEventCredential.type) &&
        Objects.equals(this.issuer, untpEventCredential.issuer) &&
        Objects.equals(this.issuanceDate, untpEventCredential.issuanceDate) &&
        Objects.equals(this.credentialSubject, untpEventCredential.credentialSubject) &&
        Objects.equals(this.proof, untpEventCredential.proof);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(hashCodeNullable(atContext), id, type, issuer, issuanceDate, credentialSubject, proof);
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
    sb.append("class UntpEventCredential {\n");
    sb.append("    atContext: ").append(toIndentedString(atContext)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    issuer: ").append(toIndentedString(issuer)).append("\n");
    sb.append("    issuanceDate: ").append(toIndentedString(issuanceDate)).append("\n");
    sb.append("    credentialSubject: ").append(toIndentedString(credentialSubject)).append("\n");
    sb.append("    proof: ").append(toIndentedString(proof)).append("\n");
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

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format("%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `type` to the URL query string
    if (getType() != null) {
      for (int i = 0; i < getType().size(); i++) {
        joiner.add(String.format("%stype%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getType().get(i)))));
      }
    }

    // add `issuer` to the URL query string
    if (getIssuer() != null) {
      joiner.add(String.format("%sissuer%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIssuer()))));
    }

    // add `issuanceDate` to the URL query string
    if (getIssuanceDate() != null) {
      joiner.add(String.format("%sissuanceDate%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getIssuanceDate()))));
    }

    // add `credentialSubject` to the URL query string
    if (getCredentialSubject() != null) {
      joiner.add(getCredentialSubject().toUrlQueryString(prefix + "credentialSubject" + suffix));
    }

    // add `proof` to the URL query string
    if (getProof() != null) {
      joiner.add(getProof().toUrlQueryString(prefix + "proof" + suffix));
    }

    return joiner.toString();
  }
}

