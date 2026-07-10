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
import org.openapitools.jackson.nullable.JsonNullable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openapitools.jackson.nullable.JsonNullable;
import java.util.NoSuchElementException;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * Partial update. &#x60;gln&#x60; and &#x60;operatorId&#x60; are immutable — if present they are silently ignored, as is any unknown key. For &#x60;activity&#x60;/&#x60;streetAddress&#x60;/&#x60;city&#x60;/&#x60;postalCode&#x60; the *presence* of the key matters: the value is stringified and trimmed, and anything that trims to empty (null, \&quot;\&quot;, or a whitespace-only string) clears the field to null — the same normalization as POST.
 */
@JsonPropertyOrder({
  FacilityUpdateRequest.JSON_PROPERTY_NAME,
  FacilityUpdateRequest.JSON_PROPERTY_ACTIVITY,
  FacilityUpdateRequest.JSON_PROPERTY_STREET_ADDRESS,
  FacilityUpdateRequest.JSON_PROPERTY_CITY,
  FacilityUpdateRequest.JSON_PROPERTY_POSTAL_CODE,
  FacilityUpdateRequest.JSON_PROPERTY_COUNTRY
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class FacilityUpdateRequest {
  public static final String JSON_PROPERTY_NAME = "name";
  @jakarta.annotation.Nullable
  private String name;

  public static final String JSON_PROPERTY_ACTIVITY = "activity";
  private JsonNullable<String> activity = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_STREET_ADDRESS = "streetAddress";
  private JsonNullable<String> streetAddress = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_CITY = "city";
  private JsonNullable<String> city = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_POSTAL_CODE = "postalCode";
  private JsonNullable<String> postalCode = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_COUNTRY = "country";
  @jakarta.annotation.Nullable
  private String country;

  public FacilityUpdateRequest() { 
  }

  public FacilityUpdateRequest name(@jakarta.annotation.Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * New name. Applied only when a non-empty string; empty/whitespace or non-string values are silently ignored (the name can never be cleared).
   * @return name
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getName() {
    return name;
  }


  @JsonProperty(JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setName(@jakarta.annotation.Nullable String name) {
    this.name = name;
  }


  public FacilityUpdateRequest activity(@jakarta.annotation.Nullable String activity) {
    this.activity = JsonNullable.<String>of(activity);
    return this;
  }

  /**
   * New activity, or null/\&quot;\&quot; to clear. A whitespace-only string is stored as \&quot;\&quot; (see schema description).
   * @return activity
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public String getActivity() {
        return activity.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_ACTIVITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getActivity_JsonNullable() {
    return activity;
  }
  
  @JsonProperty(JSON_PROPERTY_ACTIVITY)
  public void setActivity_JsonNullable(JsonNullable<String> activity) {
    this.activity = activity;
  }

  public void setActivity(@jakarta.annotation.Nullable String activity) {
    this.activity = JsonNullable.<String>of(activity);
  }


  public FacilityUpdateRequest streetAddress(@jakarta.annotation.Nullable String streetAddress) {
    this.streetAddress = JsonNullable.<String>of(streetAddress);
    return this;
  }

  /**
   * New street address, or null/\&quot;\&quot; to clear. A whitespace-only string is stored as \&quot;\&quot; (see schema description).
   * @return streetAddress
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public String getStreetAddress() {
        return streetAddress.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_STREET_ADDRESS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getStreetAddress_JsonNullable() {
    return streetAddress;
  }
  
  @JsonProperty(JSON_PROPERTY_STREET_ADDRESS)
  public void setStreetAddress_JsonNullable(JsonNullable<String> streetAddress) {
    this.streetAddress = streetAddress;
  }

  public void setStreetAddress(@jakarta.annotation.Nullable String streetAddress) {
    this.streetAddress = JsonNullable.<String>of(streetAddress);
  }


  public FacilityUpdateRequest city(@jakarta.annotation.Nullable String city) {
    this.city = JsonNullable.<String>of(city);
    return this;
  }

  /**
   * New city, or null/\&quot;\&quot; to clear. A whitespace-only string is stored as \&quot;\&quot; (see schema description).
   * @return city
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public String getCity() {
        return city.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_CITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getCity_JsonNullable() {
    return city;
  }
  
  @JsonProperty(JSON_PROPERTY_CITY)
  public void setCity_JsonNullable(JsonNullable<String> city) {
    this.city = city;
  }

  public void setCity(@jakarta.annotation.Nullable String city) {
    this.city = JsonNullable.<String>of(city);
  }


  public FacilityUpdateRequest postalCode(@jakarta.annotation.Nullable String postalCode) {
    this.postalCode = JsonNullable.<String>of(postalCode);
    return this;
  }

  /**
   * New postal code, or null/\&quot;\&quot; to clear. A whitespace-only string is stored as \&quot;\&quot; (see schema description).
   * @return postalCode
   */
  @jakarta.annotation.Nullable
  @JsonIgnore
  public String getPostalCode() {
        return postalCode.orElse(null);
  }

  @JsonProperty(JSON_PROPERTY_POSTAL_CODE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public JsonNullable<String> getPostalCode_JsonNullable() {
    return postalCode;
  }
  
  @JsonProperty(JSON_PROPERTY_POSTAL_CODE)
  public void setPostalCode_JsonNullable(JsonNullable<String> postalCode) {
    this.postalCode = postalCode;
  }

  public void setPostalCode(@jakarta.annotation.Nullable String postalCode) {
    this.postalCode = JsonNullable.<String>of(postalCode);
  }


  public FacilityUpdateRequest country(@jakarta.annotation.Nullable String country) {
    this.country = country;
    return this;
  }

  /**
   * New 2-letter ISO country code (400 if a string that does not match; stored uppercase; non-string values are silently ignored).
   * @return country
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_COUNTRY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public String getCountry() {
    return country;
  }


  @JsonProperty(JSON_PROPERTY_COUNTRY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCountry(@jakarta.annotation.Nullable String country) {
    this.country = country;
  }


  /**
   * Return true if this FacilityUpdateRequest object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FacilityUpdateRequest facilityUpdateRequest = (FacilityUpdateRequest) o;
    return Objects.equals(this.name, facilityUpdateRequest.name) &&
        equalsNullable(this.activity, facilityUpdateRequest.activity) &&
        equalsNullable(this.streetAddress, facilityUpdateRequest.streetAddress) &&
        equalsNullable(this.city, facilityUpdateRequest.city) &&
        equalsNullable(this.postalCode, facilityUpdateRequest.postalCode) &&
        Objects.equals(this.country, facilityUpdateRequest.country);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, hashCodeNullable(activity), hashCodeNullable(streetAddress), hashCodeNullable(city), hashCodeNullable(postalCode), country);
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
    sb.append("class FacilityUpdateRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    activity: ").append(toIndentedString(activity)).append("\n");
    sb.append("    streetAddress: ").append(toIndentedString(streetAddress)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
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

    return joiner.toString();
  }
}

