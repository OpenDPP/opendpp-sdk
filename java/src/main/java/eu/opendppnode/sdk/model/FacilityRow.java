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
 * A facility (GS1 GLN) master-data row, exactly as stored. Returned in full to the owning tenant. Public exposure in passport documents differs by format: the *JSON-LD* document exposes &#x60;id&#x60;, &#x60;gln&#x60;, &#x60;name&#x60;, &#x60;activity&#x60; and &#x60;country&#x60; of a linked facility; the *AAS* export emits only the GLN, name and country (&#x60;manufacturingFacilityGln&#x60;/&#x60;Name&#x60;/&#x60;Country&#x60;). &#x60;streetAddress&#x60;, &#x60;city&#x60; and &#x60;postalCode&#x60; are emitted only to the owning/bound tenant in both formats.
 */
@JsonPropertyOrder({
  FacilityRow.JSON_PROPERTY_ID,
  FacilityRow.JSON_PROPERTY_GLN,
  FacilityRow.JSON_PROPERTY_NAME,
  FacilityRow.JSON_PROPERTY_ACTIVITY,
  FacilityRow.JSON_PROPERTY_STREET_ADDRESS,
  FacilityRow.JSON_PROPERTY_CITY,
  FacilityRow.JSON_PROPERTY_POSTAL_CODE,
  FacilityRow.JSON_PROPERTY_COUNTRY,
  FacilityRow.JSON_PROPERTY_OPERATOR_ID,
  FacilityRow.JSON_PROPERTY_TENANT_ID,
  FacilityRow.JSON_PROPERTY_CREATED_AT,
  FacilityRow.JSON_PROPERTY_UPDATED_AT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class FacilityRow {
  public static final String JSON_PROPERTY_ID = "id";
  @jakarta.annotation.Nonnull
  private String id;

  public static final String JSON_PROPERTY_GLN = "gln";
  @jakarta.annotation.Nonnull
  private String gln;

  public static final String JSON_PROPERTY_NAME = "name";
  @jakarta.annotation.Nonnull
  private String name;

  public static final String JSON_PROPERTY_ACTIVITY = "activity";
  @jakarta.annotation.Nullable
  private String activity;

  public static final String JSON_PROPERTY_STREET_ADDRESS = "streetAddress";
  @jakarta.annotation.Nullable
  private String streetAddress;

  public static final String JSON_PROPERTY_CITY = "city";
  @jakarta.annotation.Nullable
  private String city;

  public static final String JSON_PROPERTY_POSTAL_CODE = "postalCode";
  @jakarta.annotation.Nullable
  private String postalCode;

  public static final String JSON_PROPERTY_COUNTRY = "country";
  @jakarta.annotation.Nonnull
  private String country;

  public static final String JSON_PROPERTY_OPERATOR_ID = "operatorId";
  @jakarta.annotation.Nullable
  private String operatorId;

  public static final String JSON_PROPERTY_TENANT_ID = "tenantId";
  @jakarta.annotation.Nonnull
  private String tenantId;

  public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime createdAt;

  public static final String JSON_PROPERTY_UPDATED_AT = "updatedAt";
  @jakarta.annotation.Nonnull
  private OffsetDateTime updatedAt;

  public FacilityRow() { 
  }

  public FacilityRow id(@jakarta.annotation.Nonnull String id) {
    this.id = id;
    return this;
  }

  /**
   * Facility id (UUID).
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


  public FacilityRow gln(@jakarta.annotation.Nonnull String gln) {
    this.gln = gln;
    return this;
  }

  /**
   * GS1 Global Location Number — 13 digits with a valid GS1 modulo-10 check digit. Unique platform-wide. Immutable after registration (it is the resolvable UFI).
   * @return gln
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_GLN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getGln() {
    return gln;
  }


  @JsonProperty(JSON_PROPERTY_GLN)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setGln(@jakarta.annotation.Nonnull String gln) {
    this.gln = gln;
  }


  public FacilityRow name(@jakarta.annotation.Nonnull String name) {
    this.name = name;
    return this;
  }

  /**
   * Facility display name (trimmed, non-empty).
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


  public FacilityRow activity(@jakarta.annotation.Nullable String activity) {
    this.activity = activity;
    return this;
  }

  /**
   * Free-text activity, e.g. \&quot;Cell assembly\&quot;, \&quot;Final manufacturing\&quot;, \&quot;Recycling\&quot;. Public in JSON-LD; not emitted in the AAS export.
   * @return activity
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ACTIVITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getActivity() {
    return activity;
  }


  @JsonProperty(JSON_PROPERTY_ACTIVITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setActivity(@jakarta.annotation.Nullable String activity) {
    this.activity = activity;
  }


  public FacilityRow streetAddress(@jakarta.annotation.Nullable String streetAddress) {
    this.streetAddress = streetAddress;
    return this;
  }

  /**
   * Street address. Owner-only: redacted from public JSON-LD and never emitted in AAS.
   * @return streetAddress
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_STREET_ADDRESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getStreetAddress() {
    return streetAddress;
  }


  @JsonProperty(JSON_PROPERTY_STREET_ADDRESS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setStreetAddress(@jakarta.annotation.Nullable String streetAddress) {
    this.streetAddress = streetAddress;
  }


  public FacilityRow city(@jakarta.annotation.Nullable String city) {
    this.city = city;
    return this;
  }

  /**
   * City. Owner-only: redacted from public JSON-LD and never emitted in AAS.
   * @return city
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getCity() {
    return city;
  }


  @JsonProperty(JSON_PROPERTY_CITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCity(@jakarta.annotation.Nullable String city) {
    this.city = city;
  }


  public FacilityRow postalCode(@jakarta.annotation.Nullable String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  /**
   * Postal code. Owner-only: redacted from public JSON-LD and never emitted in AAS.
   * @return postalCode
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_POSTAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPostalCode() {
    return postalCode;
  }


  @JsonProperty(JSON_PROPERTY_POSTAL_CODE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPostalCode(@jakarta.annotation.Nullable String postalCode) {
    this.postalCode = postalCode;
  }


  public FacilityRow country(@jakarta.annotation.Nonnull String country) {
    this.country = country;
    return this;
  }

  /**
   * 2-letter ISO 3166-1 alpha-2 country code, stored uppercase. Public in both JSON-LD and AAS.
   * @return country
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_COUNTRY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getCountry() {
    return country;
  }


  @JsonProperty(JSON_PROPERTY_COUNTRY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCountry(@jakarta.annotation.Nonnull String country) {
    this.country = country;
  }


  public FacilityRow operatorId(@jakarta.annotation.Nullable String operatorId) {
    this.operatorId = operatorId;
    return this;
  }

  /**
   * Id of the owning Economic Operator, or null for a tenant-level facility. Set at creation; not updatable via PUT.
   * @return operatorId
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_OPERATOR_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getOperatorId() {
    return operatorId;
  }


  @JsonProperty(JSON_PROPERTY_OPERATOR_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOperatorId(@jakarta.annotation.Nullable String operatorId) {
    this.operatorId = operatorId;
  }


  public FacilityRow tenantId(@jakarta.annotation.Nonnull String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  /**
   * Owning tenant workspace id.
   * @return tenantId
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TENANT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTenantId() {
    return tenantId;
  }


  @JsonProperty(JSON_PROPERTY_TENANT_ID)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTenantId(@jakarta.annotation.Nonnull String tenantId) {
    this.tenantId = tenantId;
  }


  public FacilityRow createdAt(@jakarta.annotation.Nonnull OffsetDateTime createdAt) {
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


  public FacilityRow updatedAt(@jakarta.annotation.Nonnull OffsetDateTime updatedAt) {
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
   * Return true if this FacilityRow object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FacilityRow facilityRow = (FacilityRow) o;
    return Objects.equals(this.id, facilityRow.id) &&
        Objects.equals(this.gln, facilityRow.gln) &&
        Objects.equals(this.name, facilityRow.name) &&
        Objects.equals(this.activity, facilityRow.activity) &&
        Objects.equals(this.streetAddress, facilityRow.streetAddress) &&
        Objects.equals(this.city, facilityRow.city) &&
        Objects.equals(this.postalCode, facilityRow.postalCode) &&
        Objects.equals(this.country, facilityRow.country) &&
        Objects.equals(this.operatorId, facilityRow.operatorId) &&
        Objects.equals(this.tenantId, facilityRow.tenantId) &&
        Objects.equals(this.createdAt, facilityRow.createdAt) &&
        Objects.equals(this.updatedAt, facilityRow.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, gln, name, activity, streetAddress, city, postalCode, country, operatorId, tenantId, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FacilityRow {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    gln: ").append(toIndentedString(gln)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    activity: ").append(toIndentedString(activity)).append("\n");
    sb.append("    streetAddress: ").append(toIndentedString(streetAddress)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    operatorId: ").append(toIndentedString(operatorId)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
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

    // add `id` to the URL query string
    if (getId() != null) {
      joiner.add(String.format("%sid%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
    }

    // add `gln` to the URL query string
    if (getGln() != null) {
      joiner.add(String.format("%sgln%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getGln()))));
    }

    // add `name` to the URL query string
    if (getName() != null) {
      joiner.add(String.format("%sname%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getName()))));
    }

    // add `activity` to the URL query string
    if (getActivity() != null) {
      joiner.add(String.format("%sactivity%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getActivity()))));
    }

    // add `streetAddress` to the URL query string
    if (getStreetAddress() != null) {
      joiner.add(String.format("%sstreetAddress%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getStreetAddress()))));
    }

    // add `city` to the URL query string
    if (getCity() != null) {
      joiner.add(String.format("%scity%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCity()))));
    }

    // add `postalCode` to the URL query string
    if (getPostalCode() != null) {
      joiner.add(String.format("%spostalCode%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getPostalCode()))));
    }

    // add `country` to the URL query string
    if (getCountry() != null) {
      joiner.add(String.format("%scountry%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCountry()))));
    }

    // add `operatorId` to the URL query string
    if (getOperatorId() != null) {
      joiner.add(String.format("%soperatorId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getOperatorId()))));
    }

    // add `tenantId` to the URL query string
    if (getTenantId() != null) {
      joiner.add(String.format("%stenantId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getTenantId()))));
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

