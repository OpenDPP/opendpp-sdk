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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * An Asset Administration Shell (AAS) JSON Environment — the format produced by OpenDPP&#39;s AAS export of a passport. MUST contain a submodel with &#x60;idShort: \&quot;ComplianceMetadata\&quot;&#x60; whose &#x60;submodelElements&#x60; (AAS &#x60;Property&#x60; elements and &#x60;SubmodelElementCollection&#x60;s) are parsed back into the passport metadata object; absence fails 400 &#x60;Ingestion Failed&#x60;. MAY contain an &#x60;eidasVerificationSeal&#x60; submodel (elements &#x60;digitalSealHash&#x60;, &#x60;cryptographicSignature&#x60;, &#x60;pemPublicKey&#x60;) — when present, the seal is verified against the tenant&#39;s SERVER-HELD eIDAS public key (the embedded &#x60;pemPublicKey&#x60; is never trusted as the verification key). Body limit 256 KiB.
 */
@JsonPropertyOrder({
  AasEnvironmentInput.JSON_PROPERTY_ASSET_ADMINISTRATION_SHELLS,
  AasEnvironmentInput.JSON_PROPERTY_SUBMODELS,
  AasEnvironmentInput.JSON_PROPERTY_CONCEPT_DESCRIPTIONS
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class AasEnvironmentInput {
  public static final String JSON_PROPERTY_ASSET_ADMINISTRATION_SHELLS = "assetAdministrationShells";
  @jakarta.annotation.Nullable
  private List<Map<String, Object>> assetAdministrationShells = new ArrayList<>();

  public static final String JSON_PROPERTY_SUBMODELS = "submodels";
  @jakarta.annotation.Nonnull
  private List<Map<String, Object>> submodels = new ArrayList<>();

  public static final String JSON_PROPERTY_CONCEPT_DESCRIPTIONS = "conceptDescriptions";
  @jakarta.annotation.Nullable
  private List<Map<String, Object>> conceptDescriptions = new ArrayList<>();

  public AasEnvironmentInput() { 
  }

  public AasEnvironmentInput assetAdministrationShells(@jakarta.annotation.Nullable List<Map<String, Object>> assetAdministrationShells) {
    this.assetAdministrationShells = assetAdministrationShells;
    return this;
  }

  public AasEnvironmentInput addAssetAdministrationShellsItem(Map<String, Object> assetAdministrationShellsItem) {
    if (this.assetAdministrationShells == null) {
      this.assetAdministrationShells = new ArrayList<>();
    }
    this.assetAdministrationShells.add(assetAdministrationShellsItem);
    return this;
  }

  /**
   * AAS shells. The first shell&#39;s &#x60;assetInformation.specificAssetIds&#x60; entry with &#x60;name: \&quot;productId\&quot;&#x60; is the last-resort productId source (after &#x60;metadata.gtin&#x60;/&#x60;metadata.grai&#x60;/&#x60;metadata.productId&#x60;).
   * @return assetAdministrationShells
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ASSET_ADMINISTRATION_SHELLS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<Map<String, Object>> getAssetAdministrationShells() {
    return assetAdministrationShells;
  }


  @JsonProperty(JSON_PROPERTY_ASSET_ADMINISTRATION_SHELLS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAssetAdministrationShells(@jakarta.annotation.Nullable List<Map<String, Object>> assetAdministrationShells) {
    this.assetAdministrationShells = assetAdministrationShells;
  }


  public AasEnvironmentInput submodels(@jakarta.annotation.Nonnull List<Map<String, Object>> submodels) {
    this.submodels = submodels;
    return this;
  }

  public AasEnvironmentInput addSubmodelsItem(Map<String, Object> submodelsItem) {
    if (this.submodels == null) {
      this.submodels = new ArrayList<>();
    }
    this.submodels.add(submodelsItem);
    return this;
  }

  /**
   * Must include the &#x60;ComplianceMetadata&#x60; submodel; may include &#x60;eidasVerificationSeal&#x60;.
   * @return submodels
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SUBMODELS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<Map<String, Object>> getSubmodels() {
    return submodels;
  }


  @JsonProperty(JSON_PROPERTY_SUBMODELS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubmodels(@jakarta.annotation.Nonnull List<Map<String, Object>> submodels) {
    this.submodels = submodels;
  }


  public AasEnvironmentInput conceptDescriptions(@jakarta.annotation.Nullable List<Map<String, Object>> conceptDescriptions) {
    this.conceptDescriptions = conceptDescriptions;
    return this;
  }

  public AasEnvironmentInput addConceptDescriptionsItem(Map<String, Object> conceptDescriptionsItem) {
    if (this.conceptDescriptions == null) {
      this.conceptDescriptions = new ArrayList<>();
    }
    this.conceptDescriptions.add(conceptDescriptionsItem);
    return this;
  }

  /**
   * Get conceptDescriptions
   * @return conceptDescriptions
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CONCEPT_DESCRIPTIONS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public List<Map<String, Object>> getConceptDescriptions() {
    return conceptDescriptions;
  }


  @JsonProperty(JSON_PROPERTY_CONCEPT_DESCRIPTIONS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setConceptDescriptions(@jakarta.annotation.Nullable List<Map<String, Object>> conceptDescriptions) {
    this.conceptDescriptions = conceptDescriptions;
  }


  /**
   * Return true if this AasEnvironmentInput object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AasEnvironmentInput aasEnvironmentInput = (AasEnvironmentInput) o;
    return Objects.equals(this.assetAdministrationShells, aasEnvironmentInput.assetAdministrationShells) &&
        Objects.equals(this.submodels, aasEnvironmentInput.submodels) &&
        Objects.equals(this.conceptDescriptions, aasEnvironmentInput.conceptDescriptions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assetAdministrationShells, submodels, conceptDescriptions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AasEnvironmentInput {\n");
    sb.append("    assetAdministrationShells: ").append(toIndentedString(assetAdministrationShells)).append("\n");
    sb.append("    submodels: ").append(toIndentedString(submodels)).append("\n");
    sb.append("    conceptDescriptions: ").append(toIndentedString(conceptDescriptions)).append("\n");
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

    // add `assetAdministrationShells` to the URL query string
    if (getAssetAdministrationShells() != null) {
      for (int i = 0; i < getAssetAdministrationShells().size(); i++) {
        joiner.add(String.format("%sassetAdministrationShells%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getAssetAdministrationShells().get(i)))));
      }
    }

    // add `submodels` to the URL query string
    if (getSubmodels() != null) {
      for (int i = 0; i < getSubmodels().size(); i++) {
        joiner.add(String.format("%ssubmodels%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getSubmodels().get(i)))));
      }
    }

    // add `conceptDescriptions` to the URL query string
    if (getConceptDescriptions() != null) {
      for (int i = 0; i < getConceptDescriptions().size(); i++) {
        joiner.add(String.format("%sconceptDescriptions%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getConceptDescriptions().get(i)))));
      }
    }

    return joiner.toString();
  }
}

