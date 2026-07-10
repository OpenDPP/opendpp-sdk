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
 * Embedded manufacturing-facility JSON-LD node — the GS1 GLN-backed Unique Facility Identifier (UFI, EN 18219). The five listed fields are public; &#x60;streetAddress&#x60;/&#x60;city&#x60;/&#x60;postalCode&#x60; appear ONLY in owner-tier responses (never via legitimate-interest grants).
 */
@JsonPropertyOrder({
  PublicFacilityNode.JSON_PROPERTY_AT_TYPE,
  PublicFacilityNode.JSON_PROPERTY_ID,
  PublicFacilityNode.JSON_PROPERTY_GLN,
  PublicFacilityNode.JSON_PROPERTY_NAME,
  PublicFacilityNode.JSON_PROPERTY_ACTIVITY,
  PublicFacilityNode.JSON_PROPERTY_COUNTRY,
  PublicFacilityNode.JSON_PROPERTY_STREET_ADDRESS,
  PublicFacilityNode.JSON_PROPERTY_CITY,
  PublicFacilityNode.JSON_PROPERTY_POSTAL_CODE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PublicFacilityNode {
  /**
   * Gets or Sets atType
   */
  public enum AtTypeEnum {
    FACILITY(String.valueOf("Facility"));

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

  public static final String JSON_PROPERTY_GLN = "gln";
  @jakarta.annotation.Nonnull
  private String gln;

  public static final String JSON_PROPERTY_NAME = "name";
  @jakarta.annotation.Nonnull
  private String name;

  public static final String JSON_PROPERTY_ACTIVITY = "activity";
  @jakarta.annotation.Nullable
  private String activity;

  public static final String JSON_PROPERTY_COUNTRY = "country";
  @jakarta.annotation.Nonnull
  private String country;

  public static final String JSON_PROPERTY_STREET_ADDRESS = "streetAddress";
  private JsonNullable<String> streetAddress = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_CITY = "city";
  private JsonNullable<String> city = JsonNullable.<String>undefined();

  public static final String JSON_PROPERTY_POSTAL_CODE = "postalCode";
  private JsonNullable<String> postalCode = JsonNullable.<String>undefined();

  public PublicFacilityNode() { 
  }

  public PublicFacilityNode atType(@jakarta.annotation.Nonnull AtTypeEnum atType) {
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


  public PublicFacilityNode id(@jakarta.annotation.Nonnull String id) {
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


  public PublicFacilityNode gln(@jakarta.annotation.Nonnull String gln) {
    this.gln = gln;
    return this;
  }

  /**
   * GS1 GLN-13 with a valid modulo-10 check digit.
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


  public PublicFacilityNode name(@jakarta.annotation.Nonnull String name) {
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


  public PublicFacilityNode activity(@jakarta.annotation.Nullable String activity) {
    this.activity = activity;
    return this;
  }

  /**
   * What the facility does in the chain, e.g. &#x60;cell assembly&#x60;.
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


  public PublicFacilityNode country(@jakarta.annotation.Nonnull String country) {
    this.country = country;
    return this;
  }

  /**
   * ISO 3166-1 alpha-2 country code.
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


  public PublicFacilityNode streetAddress(@jakarta.annotation.Nullable String streetAddress) {
    this.streetAddress = JsonNullable.<String>of(streetAddress);
    return this;
  }

  /**
   * Owner tier only — omitted from public and grant-tier responses.
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


  public PublicFacilityNode city(@jakarta.annotation.Nullable String city) {
    this.city = JsonNullable.<String>of(city);
    return this;
  }

  /**
   * Owner tier only.
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


  public PublicFacilityNode postalCode(@jakarta.annotation.Nullable String postalCode) {
    this.postalCode = JsonNullable.<String>of(postalCode);
    return this;
  }

  /**
   * Owner tier only.
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


  /**
   * Return true if this PublicFacilityNode object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicFacilityNode publicFacilityNode = (PublicFacilityNode) o;
    return Objects.equals(this.atType, publicFacilityNode.atType) &&
        Objects.equals(this.id, publicFacilityNode.id) &&
        Objects.equals(this.gln, publicFacilityNode.gln) &&
        Objects.equals(this.name, publicFacilityNode.name) &&
        Objects.equals(this.activity, publicFacilityNode.activity) &&
        Objects.equals(this.country, publicFacilityNode.country) &&
        equalsNullable(this.streetAddress, publicFacilityNode.streetAddress) &&
        equalsNullable(this.city, publicFacilityNode.city) &&
        equalsNullable(this.postalCode, publicFacilityNode.postalCode);
  }

  private static <T> boolean equalsNullable(JsonNullable<T> a, JsonNullable<T> b) {
    return a == b || (a != null && b != null && a.isPresent() && b.isPresent() && Objects.deepEquals(a.get(), b.get()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(atType, id, gln, name, activity, country, hashCodeNullable(streetAddress), hashCodeNullable(city), hashCodeNullable(postalCode));
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
    sb.append("class PublicFacilityNode {\n");
    sb.append("    atType: ").append(toIndentedString(atType)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    gln: ").append(toIndentedString(gln)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    activity: ").append(toIndentedString(activity)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    streetAddress: ").append(toIndentedString(streetAddress)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
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

    // add `country` to the URL query string
    if (getCountry() != null) {
      joiner.add(String.format("%scountry%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCountry()))));
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

    return joiner.toString();
  }
}

