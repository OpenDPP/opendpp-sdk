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
 * Embedded economic-operator JSON-LD node (public in all tiers).
 */
@JsonPropertyOrder({
  EconomicOperatorNode.JSON_PROPERTY_AT_TYPE,
  EconomicOperatorNode.JSON_PROPERTY_ID,
  EconomicOperatorNode.JSON_PROPERTY_NAME,
  EconomicOperatorNode.JSON_PROPERTY_REG_ID,
  EconomicOperatorNode.JSON_PROPERTY_ROLE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class EconomicOperatorNode {
  /**
   * Gets or Sets atType
   */
  public enum AtTypeEnum {
    ECONOMIC_OPERATOR(String.valueOf("EconomicOperator"));

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
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_AT_TYPE = "@type";
  @jakarta.annotation.Nonnull
  private AtTypeEnum atType;

  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_NAME = "name";
  @jakarta.annotation.Nonnull
  private String name;

  public static final String JSON_PROPERTY_REG_ID = "regId";
  @jakarta.annotation.Nonnull
  private String regId;

  public static final String JSON_PROPERTY_ROLE = "role";
  @jakarta.annotation.Nullable
  private String role;

  public EconomicOperatorNode() { 
  }

  public EconomicOperatorNode atType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
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


  public EconomicOperatorNode id(@jakarta.annotation.Nonnull String id) {
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


  public EconomicOperatorNode name(@jakarta.annotation.Nonnull String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
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


  public EconomicOperatorNode regId(@jakarta.annotation.Nonnull String regId) {
    this.regId = regId;
    return this;
  }

  /**
   * EORI number or official business-registry identifier (unique platform-wide), e.g. &#x60;EU-DEFAULT-001&#x60;.
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


  public EconomicOperatorNode role(@jakarta.annotation.Nullable String role) {
    this.role = role;
    return this;
  }

  /**
   * Operator role in the supply chain, e.g. &#x60;MANUFACTURER&#x60;, &#x60;IMPORTER&#x60;, &#x60;RETAILER&#x60;. Always present in detail/resolution responses; absent from &#x60;GET /api/v1/passports&#x60; list items.
   * @return role
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ROLE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getRole() {
    return role;
  }


  @JsonProperty(JSON_PROPERTY_ROLE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRole(@jakarta.annotation.Nullable String role) {
    this.role = role;
  }


  /**
   * Return true if this EconomicOperatorNode object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EconomicOperatorNode economicOperatorNode = (EconomicOperatorNode) o;
    return Objects.equals(this.atType, economicOperatorNode.atType) &&
        Objects.equals(this.id, economicOperatorNode.id) &&
        Objects.equals(this.name, economicOperatorNode.name) &&
        Objects.equals(this.regId, economicOperatorNode.regId) &&
        Objects.equals(this.role, economicOperatorNode.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atType, id, name, regId, role);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EconomicOperatorNode {\n");
    sb.append("    atType: ").append(toIndentedString(atType)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    regId: ").append(toIndentedString(regId)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
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

    // add `@type` to the URL query string
    if (getAtType() != null) {
      joiner.add(String.format("%s@type%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getAtType()))));
    }

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

    // add `role` to the URL query string
    if (getRole() != null) {
      joiner.add(String.format("%srole%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getRole()))));
    }

    return joiner.toString();
  }
}

