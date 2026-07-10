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
import eu.opendppnode.sdk.model.MerkleTreeAttestationProofRfc3161;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import eu.opendppnode.sdk.invoker.ApiClient;
/**
 * OpenDPP&#39;s own proof type — an eIDAS ADVANCED electronic seal: an ECDSA prime256v1 signature over a SHA-256 Merkle root of the key-sorted metadata (one leaf per top-level metadata key). Deliberately NOT a W3C DataIntegrityProof / &#x60;ecdsa-jcs-2019&#x60; Verifiable Credential (no RFC 8785 JCS canonicalization). Verifiable offline: rebuild the Merkle root from &#x60;metadata&#x60; — substituting each &#x60;redactedLeaves&#x60; hash for its placeholder-masked key, and EXCLUDING any placeholder-masked key that has no &#x60;redactedLeaves&#x60; entry (such a key was never present in the sealed metadata; the serializer injects the owner-only placeholder unconditionally) — then verify &#x60;signatureValue&#x60; with &#x60;publicKeyPem&#x60;; the &#x60;x5c&#x60; chain validates against the platform seal CA (&#x60;GET /.well-known/opendpp-seal-ca.pem&#x60;) and the &#x60;rfc3161&#x60; token via &#x60;openssl ts -verify&#x60;.
 */
@JsonPropertyOrder({
  MerkleTreeAttestationProof.JSON_PROPERTY_AT_TYPE,
  MerkleTreeAttestationProof.JSON_PROPERTY_TYPE,
  MerkleTreeAttestationProof.JSON_PROPERTY_SIGNATURE_ALGORITHM,
  MerkleTreeAttestationProof.JSON_PROPERTY_CREATED,
  MerkleTreeAttestationProof.JSON_PROPERTY_PROOF_PURPOSE,
  MerkleTreeAttestationProof.JSON_PROPERTY_VERIFICATION_METHOD,
  MerkleTreeAttestationProof.JSON_PROPERTY_SIGNATURE_VALUE,
  MerkleTreeAttestationProof.JSON_PROPERTY_PUBLIC_KEY_PEM,
  MerkleTreeAttestationProof.JSON_PROPERTY_X5C,
  MerkleTreeAttestationProof.JSON_PROPERTY_RFC3161,
  MerkleTreeAttestationProof.JSON_PROPERTY_MERKLE_ROOT,
  MerkleTreeAttestationProof.JSON_PROPERTY_REDACTED_LEAVES
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class MerkleTreeAttestationProof {
  /**
   * Gets or Sets atType
   */
  public enum AtTypeEnum {
    MERKLE_TREE_ATTESTATION_PROOF(String.valueOf("MerkleTreeAttestationProof")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

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
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_AT_TYPE = "@type";
  @jakarta.annotation.Nonnull
  private List<AtTypeEnum> atType = new ArrayList<>();

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    MERKLE_TREE_ATTESTATION_PROOF(String.valueOf("MerkleTreeAttestationProof")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    TypeEnum(String value) {
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
    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_TYPE = "type";
  @jakarta.annotation.Nonnull
  private TypeEnum type;

  /**
   * Gets or Sets signatureAlgorithm
   */
  public enum SignatureAlgorithmEnum {
    ECDSA_P256_SHA256_OVER_MERKLE_ROOT(String.valueOf("ECDSA-P256-SHA256-over-MerkleRoot")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    SignatureAlgorithmEnum(String value) {
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
    public static SignatureAlgorithmEnum fromValue(String value) {
      for (SignatureAlgorithmEnum b : SignatureAlgorithmEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_SIGNATURE_ALGORITHM = "signatureAlgorithm";
  @jakarta.annotation.Nonnull
  private SignatureAlgorithmEnum signatureAlgorithm;

  public static final String JSON_PROPERTY_CREATED = "created";
  @jakarta.annotation.Nonnull
  private OffsetDateTime created;

  /**
   * Gets or Sets proofPurpose
   */
  public enum ProofPurposeEnum {
    ASSERTION_METHOD(String.valueOf("assertionMethod")),
    
    UNKNOWN_DEFAULT_OPEN_API(String.valueOf("unknown_default_open_api"));

    private String value;

    ProofPurposeEnum(String value) {
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
    public static ProofPurposeEnum fromValue(String value) {
      for (ProofPurposeEnum b : ProofPurposeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      return UNKNOWN_DEFAULT_OPEN_API;
    }
  }

  public static final String JSON_PROPERTY_PROOF_PURPOSE = "proofPurpose";
  @jakarta.annotation.Nonnull
  private ProofPurposeEnum proofPurpose;

  public static final String JSON_PROPERTY_VERIFICATION_METHOD = "verificationMethod";
  @jakarta.annotation.Nonnull
  private URI verificationMethod;

  public static final String JSON_PROPERTY_SIGNATURE_VALUE = "signatureValue";
  @jakarta.annotation.Nonnull
  private String signatureValue;

  public static final String JSON_PROPERTY_PUBLIC_KEY_PEM = "publicKeyPem";
  @jakarta.annotation.Nonnull
  private String publicKeyPem;

  public static final String JSON_PROPERTY_X5C = "x5c";
  @jakarta.annotation.Nullable
  private List<String> x5c = new ArrayList<>();

  public static final String JSON_PROPERTY_RFC3161 = "rfc3161";
  @jakarta.annotation.Nullable
  private MerkleTreeAttestationProofRfc3161 rfc3161;

  public static final String JSON_PROPERTY_MERKLE_ROOT = "merkleRoot";
  @jakarta.annotation.Nonnull
  private String merkleRoot;

  public static final String JSON_PROPERTY_REDACTED_LEAVES = "redactedLeaves";
  @jakarta.annotation.Nullable
  private Map<String, String> redactedLeaves = new HashMap<>();

  public MerkleTreeAttestationProof() { 
  }

  public MerkleTreeAttestationProof atType(@jakarta.annotation.Nonnull List<AtTypeEnum> atType) {
    this.atType = atType;
    return this;
  }

  public MerkleTreeAttestationProof addAtTypeItem(AtTypeEnum atTypeItem) {
    if (this.atType == null) {
      this.atType = new ArrayList<>();
    }
    this.atType.add(atTypeItem);
    return this;
  }

  /**
   * Always &#x60;[\&quot;MerkleTreeAttestationProof\&quot;]&#x60;.
   * @return atType
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<AtTypeEnum> getAtType() {
    return atType;
  }


  @JsonProperty(JSON_PROPERTY_AT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAtType(@jakarta.annotation.Nonnull List<AtTypeEnum> atType) {
    this.atType = atType;
  }


  public MerkleTreeAttestationProof type(@jakarta.annotation.Nonnull TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public TypeEnum getType() {
    return type;
  }


  @JsonProperty(JSON_PROPERTY_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setType(@jakarta.annotation.Nonnull TypeEnum type) {
    this.type = type;
  }


  public MerkleTreeAttestationProof signatureAlgorithm(@jakarta.annotation.Nonnull SignatureAlgorithmEnum signatureAlgorithm) {
    this.signatureAlgorithm = signatureAlgorithm;
    return this;
  }

  /**
   * Get signatureAlgorithm
   * @return signatureAlgorithm
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SIGNATURE_ALGORITHM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public SignatureAlgorithmEnum getSignatureAlgorithm() {
    return signatureAlgorithm;
  }


  @JsonProperty(JSON_PROPERTY_SIGNATURE_ALGORITHM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSignatureAlgorithm(@jakarta.annotation.Nonnull SignatureAlgorithmEnum signatureAlgorithm) {
    this.signatureAlgorithm = signatureAlgorithm;
  }


  public MerkleTreeAttestationProof created(@jakarta.annotation.Nonnull OffsetDateTime created) {
    this.created = created;
    return this;
  }

  /**
   * Mirrors the passport&#39;s &#x60;updatedAt&#x60;.
   * @return created
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CREATED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public OffsetDateTime getCreated() {
    return created;
  }


  @JsonProperty(JSON_PROPERTY_CREATED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCreated(@jakarta.annotation.Nonnull OffsetDateTime created) {
    this.created = created;
  }


  public MerkleTreeAttestationProof proofPurpose(@jakarta.annotation.Nonnull ProofPurposeEnum proofPurpose) {
    this.proofPurpose = proofPurpose;
    return this;
  }

  /**
   * Get proofPurpose
   * @return proofPurpose
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PROOF_PURPOSE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ProofPurposeEnum getProofPurpose() {
    return proofPurpose;
  }


  @JsonProperty(JSON_PROPERTY_PROOF_PURPOSE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProofPurpose(@jakarta.annotation.Nonnull ProofPurposeEnum proofPurpose) {
    this.proofPurpose = proofPurpose;
  }


  public MerkleTreeAttestationProof verificationMethod(@jakarta.annotation.Nonnull URI verificationMethod) {
    this.verificationMethod = verificationMethod;
    return this;
  }

  /**
   * &#x60;https://opendpp-node.eu/passport/{passportId}#key-1&#x60;.
   * @return verificationMethod
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_VERIFICATION_METHOD)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public URI getVerificationMethod() {
    return verificationMethod;
  }


  @JsonProperty(JSON_PROPERTY_VERIFICATION_METHOD)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setVerificationMethod(@jakarta.annotation.Nonnull URI verificationMethod) {
    this.verificationMethod = verificationMethod;
  }


  public MerkleTreeAttestationProof signatureValue(@jakarta.annotation.Nonnull String signatureValue) {
    this.signatureValue = signatureValue;
    return this;
  }

  /**
   * Base64 ECDSA P-256/SHA-256 signature over the hex Merkle root string (same value as the document&#39;s &#x60;digitalSeal&#x60;).
   * @return signatureValue
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SIGNATURE_VALUE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSignatureValue() {
    return signatureValue;
  }


  @JsonProperty(JSON_PROPERTY_SIGNATURE_VALUE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSignatureValue(@jakarta.annotation.Nonnull String signatureValue) {
    this.signatureValue = signatureValue;
  }


  public MerkleTreeAttestationProof publicKeyPem(@jakarta.annotation.Nonnull String publicKeyPem) {
    this.publicKeyPem = publicKeyPem;
    return this;
  }

  /**
   * PEM public key for verification (same value as the document&#39;s &#x60;signingPublicKey&#x60;).
   * @return publicKeyPem
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_PUBLIC_KEY_PEM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPublicKeyPem() {
    return publicKeyPem;
  }


  @JsonProperty(JSON_PROPERTY_PUBLIC_KEY_PEM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPublicKeyPem(@jakarta.annotation.Nonnull String publicKeyPem) {
    this.publicKeyPem = publicKeyPem;
  }


  public MerkleTreeAttestationProof x5c(@jakarta.annotation.Nullable List<String> x5c) {
    this.x5c = x5c;
    return this;
  }

  public MerkleTreeAttestationProof addX5cItem(String x5cItem) {
    if (this.x5c == null) {
      this.x5c = new ArrayList<>();
    }
    this.x5c.add(x5cItem);
    return this;
  }

  /**
   * OPTIONAL (omitted when no chain was recorded at seal time). X.509 chain as base64 DER (no PEM armor), leaf first, binding the signing key to the tenant&#39;s legal identity; issued by the platform seal CA. Denormalised at seal time, so later key/cert rotations never retroactively change a proof.
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


  public MerkleTreeAttestationProof rfc3161(@jakarta.annotation.Nullable MerkleTreeAttestationProofRfc3161 rfc3161) {
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
  public MerkleTreeAttestationProofRfc3161 getRfc3161() {
    return rfc3161;
  }


  @JsonProperty(JSON_PROPERTY_RFC3161)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRfc3161(@jakarta.annotation.Nullable MerkleTreeAttestationProofRfc3161 rfc3161) {
    this.rfc3161 = rfc3161;
  }


  public MerkleTreeAttestationProof merkleRoot(@jakarta.annotation.Nonnull String merkleRoot) {
    this.merkleRoot = merkleRoot;
    return this;
  }

  /**
   * Hex SHA-256 Merkle root over the key-sorted metadata leaves.
   * @return merkleRoot
   */
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_MERKLE_ROOT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getMerkleRoot() {
    return merkleRoot;
  }


  @JsonProperty(JSON_PROPERTY_MERKLE_ROOT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMerkleRoot(@jakarta.annotation.Nonnull String merkleRoot) {
    this.merkleRoot = merkleRoot;
  }


  public MerkleTreeAttestationProof redactedLeaves(@jakarta.annotation.Nullable Map<String, String> redactedLeaves) {
    this.redactedLeaves = redactedLeaves;
    return this;
  }

  public MerkleTreeAttestationProof putRedactedLeavesItem(String key, String redactedLeavesItem) {
    if (this.redactedLeaves == null) {
      this.redactedLeaves = new HashMap<>();
    }
    this.redactedLeaves.put(key, redactedLeavesItem);
    return this;
  }

  /**
   * OPTIONAL — present only when at least one masked key actually exists in the underlying sealed metadata. Maps each such metadata key to its TRUE hex leaf hash, so the Merkle root can be reconstructed from the redacted document. A masked key that was never present in the metadata (the owner-only key is placeholder-injected unconditionally for non-owner tiers) yields NO entry here — verifiers must exclude placeholder-valued keys without an entry when rebuilding the tree.
   * @return redactedLeaves
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_REDACTED_LEAVES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Map<String, String> getRedactedLeaves() {
    return redactedLeaves;
  }


  @JsonProperty(JSON_PROPERTY_REDACTED_LEAVES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRedactedLeaves(@jakarta.annotation.Nullable Map<String, String> redactedLeaves) {
    this.redactedLeaves = redactedLeaves;
  }


  /**
   * Return true if this MerkleTreeAttestationProof object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MerkleTreeAttestationProof merkleTreeAttestationProof = (MerkleTreeAttestationProof) o;
    return Objects.equals(this.atType, merkleTreeAttestationProof.atType) &&
        Objects.equals(this.type, merkleTreeAttestationProof.type) &&
        Objects.equals(this.signatureAlgorithm, merkleTreeAttestationProof.signatureAlgorithm) &&
        Objects.equals(this.created, merkleTreeAttestationProof.created) &&
        Objects.equals(this.proofPurpose, merkleTreeAttestationProof.proofPurpose) &&
        Objects.equals(this.verificationMethod, merkleTreeAttestationProof.verificationMethod) &&
        Objects.equals(this.signatureValue, merkleTreeAttestationProof.signatureValue) &&
        Objects.equals(this.publicKeyPem, merkleTreeAttestationProof.publicKeyPem) &&
        Objects.equals(this.x5c, merkleTreeAttestationProof.x5c) &&
        Objects.equals(this.rfc3161, merkleTreeAttestationProof.rfc3161) &&
        Objects.equals(this.merkleRoot, merkleTreeAttestationProof.merkleRoot) &&
        Objects.equals(this.redactedLeaves, merkleTreeAttestationProof.redactedLeaves);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atType, type, signatureAlgorithm, created, proofPurpose, verificationMethod, signatureValue, publicKeyPem, x5c, rfc3161, merkleRoot, redactedLeaves);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MerkleTreeAttestationProof {\n");
    sb.append("    atType: ").append(toIndentedString(atType)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    signatureAlgorithm: ").append(toIndentedString(signatureAlgorithm)).append("\n");
    sb.append("    created: ").append(toIndentedString(created)).append("\n");
    sb.append("    proofPurpose: ").append(toIndentedString(proofPurpose)).append("\n");
    sb.append("    verificationMethod: ").append(toIndentedString(verificationMethod)).append("\n");
    sb.append("    signatureValue: ").append(toIndentedString(signatureValue)).append("\n");
    sb.append("    publicKeyPem: ").append(toIndentedString(publicKeyPem)).append("\n");
    sb.append("    x5c: ").append(toIndentedString(x5c)).append("\n");
    sb.append("    rfc3161: ").append(toIndentedString(rfc3161)).append("\n");
    sb.append("    merkleRoot: ").append(toIndentedString(merkleRoot)).append("\n");
    sb.append("    redactedLeaves: ").append(toIndentedString(redactedLeaves)).append("\n");
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
      for (int i = 0; i < getAtType().size(); i++) {
        joiner.add(String.format("%s@type%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
            ApiClient.urlEncode(ApiClient.valueToString(getAtType().get(i)))));
      }
    }

    // add `type` to the URL query string
    if (getType() != null) {
      joiner.add(String.format("%stype%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getType()))));
    }

    // add `signatureAlgorithm` to the URL query string
    if (getSignatureAlgorithm() != null) {
      joiner.add(String.format("%ssignatureAlgorithm%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getSignatureAlgorithm()))));
    }

    // add `created` to the URL query string
    if (getCreated() != null) {
      joiner.add(String.format("%screated%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getCreated()))));
    }

    // add `proofPurpose` to the URL query string
    if (getProofPurpose() != null) {
      joiner.add(String.format("%sproofPurpose%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getProofPurpose()))));
    }

    // add `verificationMethod` to the URL query string
    if (getVerificationMethod() != null) {
      joiner.add(String.format("%sverificationMethod%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getVerificationMethod()))));
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

    // add `merkleRoot` to the URL query string
    if (getMerkleRoot() != null) {
      joiner.add(String.format("%smerkleRoot%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getMerkleRoot()))));
    }

    // add `redactedLeaves` to the URL query string
    if (getRedactedLeaves() != null) {
      for (String _key : getRedactedLeaves().keySet()) {
        joiner.add(String.format("%sredactedLeaves%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, _key, containerSuffix),
            getRedactedLeaves().get(_key), ApiClient.urlEncode(ApiClient.valueToString(getRedactedLeaves().get(_key)))));
      }
    }

    return joiner.toString();
  }
}

