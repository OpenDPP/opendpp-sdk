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
import java.time.OffsetDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * An economic-operator record (&#x60;EconomicOperator&#x60;). Operators are scoped to your workspace (each workspace keeps its own row for a given &#x60;regId&#x60;). Returned verbatim from the database (no field stripping); nullable fields are serialized as &#x60;null&#x60;.
 */
@JsonPropertyOrder({
  OperatorRow.JSON_PROPERTY_ID,
  OperatorRow.JSON_PROPERTY_NAME,
  OperatorRow.JSON_PROPERTY_REG_ID,
  OperatorRow.JSON_PROPERTY_REG_ID_SCHEME,
  OperatorRow.JSON_PROPERTY_ROLE,
  OperatorRow.JSON_PROPERTY_ARCHIVED_AT,
  OperatorRow.JSON_PROPERTY_CREATED_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class OperatorRow {
  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_NAME = "name";
  @jakarta.annotation.Nonnull
  private String name;

  public static final String JSON_PROPERTY_REG_ID = "regId";
  @jakarta.annotation.Nonnull
  private String regId;

  /**
   * Which kind of registration id &#x60;regId&#x60; is. &#x60;null&#x60; &#x3D; unspecified national/business id. When &#x60;EORI&#x60;, &#x60;regId&#x60; is guaranteed to satisfy the EORI syntax &#x60;^[A-Z]{2}[A-Za-z0-9]{1,15}$&#x60;.
   */
  public enum RegIdSchemeEnum {
    EORI(String.valueOf("EORI")),
    
    VAT(String.valueOf("VAT")),
    
    DUNS(String.valueOf("DUNS")),
    
    NATIONAL(String.valueOf("NATIONAL")),
    
    OTHER(String.valueOf("OTHER")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    RegIdSchemeEnum(String value) {
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
    public static RegIdSchemeEnum fromValue(String value) {
      for (RegIdSchemeEnum b : RegIdSchemeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return null;
    }
  }

  public static final String JSON_PROPERTY_REG_ID_SCHEME = "regIdScheme";
  @jakarta.annotation.Nullable
  private RegIdSchemeEnum regIdScheme;

  public static final String JSON_PROPERTY_ROLE = "role";
  @jakarta.annotation.Nonnull
  private String role;

  public static final String JSON_PROPERTY_ARCHIVED_AT = "archivedAt";
  @jakarta.annotation.Nullable
  private OffsetDateTime archivedAt;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public OperatorRow() { 
  }

  public OperatorRow id(@jakarta.annotation.Nonnull String id) {
    this.id = id;
    return this;
  }

  /**
   * Operator UUID.
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


  public OperatorRow name(@jakarta.annotation.Nonnull String name) {
    this.name = name;
    return this;
  }

  /**
   * Legal/display name of the operator.
   * @return name
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getName() {
    return name;
  }


  @JsonProperty(JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setName(@jakarta.annotation.Nonnull String name) {
    this.name = name;
  }


  public OperatorRow regId(@jakarta.annotation.Nonnull String regId) {
    this.regId = regId;
    return this;
  }

  /**
   * Official registration id (EORI number, VAT id, DUNS, or national business-registry id). Unique within your workspace and immutable after registration.
   * @return regId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_REG_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getRegId() {
    return regId;
  }


  @JsonProperty(JSON_PROPERTY_REG_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRegId(@jakarta.annotation.Nonnull String regId) {
    this.regId = regId;
  }


  public OperatorRow regIdScheme(@jakarta.annotation.Nullable RegIdSchemeEnum regIdScheme) {
    this.regIdScheme = regIdScheme;
    return this;
  }

  /**
   * Which kind of registration id &#x60;regId&#x60; is. &#x60;null&#x60; &#x3D; unspecified national/business id. When &#x60;EORI&#x60;, &#x60;regId&#x60; is guaranteed to satisfy the EORI syntax &#x60;^[A-Z]{2}[A-Za-z0-9]{1,15}$&#x60;.
   * @return regIdScheme
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_REG_ID_SCHEME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public RegIdSchemeEnum getRegIdScheme() {
    return regIdScheme;
  }


  @JsonProperty(JSON_PROPERTY_REG_ID_SCHEME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRegIdScheme(@jakarta.annotation.Nullable RegIdSchemeEnum regIdScheme) {
    this.regIdScheme = regIdScheme;
  }


  public OperatorRow role(@jakarta.annotation.Nonnull String role) {
    this.role = role;
    return this;
  }

  /**
   * Supply-chain role, free text — e.g. &#x60;\&quot;MANUFACTURER\&quot;&#x60;, &#x60;\&quot;IMPORTER\&quot;&#x60;, &#x60;\&quot;RETAILER\&quot;&#x60;. Defaults to &#x60;\&quot;MANUFACTURER\&quot;&#x60; at registration.
   * @return role
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_ROLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getRole() {
    return role;
  }


  @JsonProperty(JSON_PROPERTY_ROLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRole(@jakarta.annotation.Nonnull String role) {
    this.role = role;
  }


  public OperatorRow archivedAt(@jakarta.annotation.Nullable OffsetDateTime archivedAt) {
    this.archivedAt = archivedAt;
    return this;
  }

  /**
   * Soft-delete / cessation-of-trading marker. Non-null &#x3D; the operator is archived (its passports are retained and still publicly resolvable).
   * @return archivedAt
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ARCHIVED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getArchivedAt() {
    return archivedAt;
  }


  @JsonProperty(JSON_PROPERTY_ARCHIVED_AT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setArchivedAt(@jakarta.annotation.Nullable OffsetDateTime archivedAt) {
    this.archivedAt = archivedAt;
  }


  public OperatorRow createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
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


  /**
   * Return true if this OperatorRow object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperatorRow operatorRow = (OperatorRow) o;
    return Objects.equals(this.id, operatorRow.id) &&
        Objects.equals(this.name, operatorRow.name) &&
        Objects.equals(this.regId, operatorRow.regId) &&
        Objects.equals(this.regIdScheme, operatorRow.regIdScheme) &&
        Objects.equals(this.role, operatorRow.role) &&
        Objects.equals(this.archivedAt, operatorRow.archivedAt) &&
        Objects.equals(this.createdAt, operatorRow.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, regId, regIdScheme, role, archivedAt, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperatorRow {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    regId: ").append(toIndentedString(regId)).append("\n");
    sb.append("    regIdScheme: ").append(toIndentedString(regIdScheme)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    archivedAt: ").append(toIndentedString(archivedAt)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
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

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format("%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `name` to the URL query string
    if (getName() != null) {
      joiner.add(String.format("%sname%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getName()))));
    }

    // add `regId` to the URL query string
    if (getRegId() != null) {
      joiner.add(String.format("%sregId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRegId()))));
    }

    // add `regIdScheme` to the URL query string
    if (getRegIdScheme() != null) {
      joiner.add(String.format("%sregIdScheme%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRegIdScheme()))));
    }

    // add `role` to the URL query string
    if (getRole() != null) {
      joiner.add(String.format("%srole%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRole()))));
    }

    // add `archivedAt` to the URL query string
    if (getArchivedAt() != null) {
      joiner.add(String.format("%sarchivedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getArchivedAt()))));
    }

    // add `createdAt` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screatedAt%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreatedAt()))));
    }

    return joiner.toString();
  }
}

