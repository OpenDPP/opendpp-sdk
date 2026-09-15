/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `EORI_NOT_FOUND`, `CARRIER_SYMBOLOGY_NOT_RENDERED`, `CATEGORY_GRANULARITY_UNEXPECTED`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Methods  A request whose path exists but whose method this API does not serve returns **`405 Method Not Allowed`** with an `Allow` header listing the methods that path does serve (RFC 9110 §15.5.6); `HEAD` is listed wherever `GET` is, and is served. A path no route matches returns `404`, as does a path whose method IS allowed but whose resource does not exist — so a `405` always means the verb, and never the identifier. `405` is not listed per operation below because it is not a property of any operation: it is the answer to a method for which no operation exists.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Role in the data exchange This node is **not a DPP registry**. It hosts passports on behalf of the economic operators that create them and provides no registration service, so the registry methods of EN 18222:2026 clause 5 (Table 17, `registerDPP`) are outside this API's scope. Which service-provider role the node holds for a given passport is a property of the agreement with that operator rather than of this document, so it is not asserted here.  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.16.0
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
  OperatorRow.JSON_PROPERTY_EORI,
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
   * The EN 18219 clause 6 scheme &#x60;regId&#x60; is issued under. Each has an ISO/IEC 6523 ICD — VAT &#x60;0223&#x60;, DUNS &#x60;0060&#x60;, LEI &#x60;0199&#x60;, GLN &#x60;0088&#x60; — which is how the EN 18223 &#x60;economicOperatorId&#x60; is written. &#x60;UNDECLARED&#x60; appears only on an operator that predates the declaration and whose identifier the migration could not classify: it cannot be sent, and such an operator is declared once via &#x60;PATCH&#x60;.
   */
  public enum RegIdSchemeEnum {
    VAT(String.valueOf("VAT")),
    
    DUNS(String.valueOf("DUNS")),
    
    LEI(String.valueOf("LEI")),
    
    GLN(String.valueOf("GLN")),
    
    UNDECLARED(String.valueOf("UNDECLARED")),
    
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
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_REG_ID_SCHEME = "regIdScheme";
  @jakarta.annotation.Nonnull
  private RegIdSchemeEnum regIdScheme;

  public static final String JSON_PROPERTY_EORI = "eori";
  @jakarta.annotation.Nullable
  private String eori;

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
   * The identifier issued under &#x60;regIdScheme&#x60; — an EU VAT identification number, a D-U-N-S number, an LEI or a GS1 GLN. Unique within your workspace and immutable once declared.
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


  public OperatorRow regIdScheme(@jakarta.annotation.Nonnull RegIdSchemeEnum regIdScheme) {
    this.regIdScheme = regIdScheme;
    return this;
  }

  /**
   * The EN 18219 clause 6 scheme &#x60;regId&#x60; is issued under. Each has an ISO/IEC 6523 ICD — VAT &#x60;0223&#x60;, DUNS &#x60;0060&#x60;, LEI &#x60;0199&#x60;, GLN &#x60;0088&#x60; — which is how the EN 18223 &#x60;economicOperatorId&#x60; is written. &#x60;UNDECLARED&#x60; appears only on an operator that predates the declaration and whose identifier the migration could not classify: it cannot be sent, and such an operator is declared once via &#x60;PATCH&#x60;.
   * @return regIdScheme
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_REG_ID_SCHEME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public RegIdSchemeEnum getRegIdScheme() {
    return regIdScheme;
  }


  @JsonProperty(JSON_PROPERTY_REG_ID_SCHEME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRegIdScheme(@jakarta.annotation.Nonnull RegIdSchemeEnum regIdScheme) {
    this.regIdScheme = regIdScheme;
  }


  public OperatorRow eori(@jakarta.annotation.Nullable String eori) {
    this.eori = eori;
    return this;
  }

  /**
   * The operator&#39;s EU EORI (customs identifier), normalised, or &#x60;null&#x60;. Carried beside &#x60;regId&#x60;: it has no ISO/IEC 6523 ICD and is not what the EN 18223 header names.
   * @return eori
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_EORI)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getEori() {
    return eori;
  }


  @JsonProperty(JSON_PROPERTY_EORI)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEori(@jakarta.annotation.Nullable String eori) {
    this.eori = eori;
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
        Objects.equals(this.eori, operatorRow.eori) &&
        Objects.equals(this.role, operatorRow.role) &&
        Objects.equals(this.archivedAt, operatorRow.archivedAt) &&
        Objects.equals(this.createdAt, operatorRow.createdAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, regId, regIdScheme, eori, role, archivedAt, createdAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperatorRow {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    regId: ").append(toIndentedString(regId)).append("\n");
    sb.append("    regIdScheme: ").append(toIndentedString(regIdScheme)).append("\n");
    sb.append("    eori: ").append(toIndentedString(eori)).append("\n");
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

    // add `eori` to the URL query string
    if (getEori() != null) {
      joiner.add(String.format("%seori%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getEori()))));
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

