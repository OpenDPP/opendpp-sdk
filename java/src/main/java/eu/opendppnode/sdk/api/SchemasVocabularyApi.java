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

package eu.opendppnode.sdk.api;

import eu.opendppnode.sdk.invoker.ApiClient;
import eu.opendppnode.sdk.invoker.ApiException;
import eu.opendppnode.sdk.invoker.ApiResponse;
import eu.opendppnode.sdk.invoker.Configuration;
import eu.opendppnode.sdk.invoker.Pair;

import eu.opendppnode.sdk.model.DppJsonLdContextDocument;
import eu.opendppnode.sdk.model.DppVocabContextDocument;
import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.MaterialVocabularyListResponse;
import eu.opendppnode.sdk.model.SectorJsonSchemaDocument;
import eu.opendppnode.sdk.model.SectorVocabularyContext;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class SchemasVocabularyApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public SchemasVocabularyApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SchemasVocabularyApi(ApiClient apiClient) {
    memberVarHttpClient = apiClient.getHttpClient();
    memberVarObjectMapper = apiClient.getObjectMapper();
    memberVarBaseUri = apiClient.getBaseUri();
    memberVarInterceptor = apiClient.getRequestInterceptor();
    memberVarReadTimeout = apiClient.getReadTimeout();
    memberVarResponseInterceptor = apiClient.getResponseInterceptor();
    memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
  }

  protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
    String body = response.body() == null ? null : new String(response.body().readAllBytes());
    String message = formatExceptionMessage(operationId, response.statusCode(), body);
    return new ApiException(response.statusCode(), message, response.headers(), body);
  }

  private String formatExceptionMessage(String operationId, int statusCode, String body) {
    if (body == null || body.isEmpty()) {
      body = "[no body]";
    }
    return operationId + " call failed with: " + statusCode + " - " + body;
  }

  /**
   * Canonical resolvable JSON-LD context for passport &amp; unit documents
   * Serves the stable, resolvable W3C JSON-LD &#x60;@context&#x60; (&#x60;application/ld+json&#x60;) that **every** public passport and battery-unit JSON-LD document references in its &#x60;@context&#x60; array — this is the context to dereference when expanding OpenDPP JSON-LD. It declares &#x60;@vocab: https://opendpp-node.eu/ns/dpp#&#x60; (so even dynamic metadata keys expand under the OpenDPP namespace and are never silently dropped by a strict JSON-LD processor) plus explicit term mappings for the core DPP/unit vocabulary (&#x60;DigitalProductPassport&#x60;, &#x60;BatteryUnit&#x60;, &#x60;economicOperator&#x60;, &#x60;manufacturingFacility&#x60;, &#x60;metadata&#x60;, &#x60;digitalSeal&#x60;, &#x60;signingPublicKey&#x60;, &#x60;proof&#x60;, &#x60;status&#x60;, &#x60;MerkleTreeAttestationProof&#x60;). Cacheable (&#x60;Cache-Control: public, max-age&#x3D;86400&#x60;).  (The separate &#x60;/context/v1&#x60; serves an older, fixed term-only list and is **not** the context emitted documents point to.)  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard &#x60;x-ratelimit-*&#x60; headers).
   * @return DppVocabContextDocument
   * @throws ApiException if fails to make API call
   */
  public DppVocabContextDocument getDppJsonLdContext() throws ApiException {
    ApiResponse<DppVocabContextDocument> localVarResponse = getDppJsonLdContextWithHttpInfo();
    return localVarResponse.getData();
  }

  /**
   * Canonical resolvable JSON-LD context for passport &amp; unit documents
   * Serves the stable, resolvable W3C JSON-LD &#x60;@context&#x60; (&#x60;application/ld+json&#x60;) that **every** public passport and battery-unit JSON-LD document references in its &#x60;@context&#x60; array — this is the context to dereference when expanding OpenDPP JSON-LD. It declares &#x60;@vocab: https://opendpp-node.eu/ns/dpp#&#x60; (so even dynamic metadata keys expand under the OpenDPP namespace and are never silently dropped by a strict JSON-LD processor) plus explicit term mappings for the core DPP/unit vocabulary (&#x60;DigitalProductPassport&#x60;, &#x60;BatteryUnit&#x60;, &#x60;economicOperator&#x60;, &#x60;manufacturingFacility&#x60;, &#x60;metadata&#x60;, &#x60;digitalSeal&#x60;, &#x60;signingPublicKey&#x60;, &#x60;proof&#x60;, &#x60;status&#x60;, &#x60;MerkleTreeAttestationProof&#x60;). Cacheable (&#x60;Cache-Control: public, max-age&#x3D;86400&#x60;).  (The separate &#x60;/context/v1&#x60; serves an older, fixed term-only list and is **not** the context emitted documents point to.)  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard &#x60;x-ratelimit-*&#x60; headers).
   * @return ApiResponse&lt;DppVocabContextDocument&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DppVocabContextDocument> getDppJsonLdContextWithHttpInfo() throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getDppJsonLdContextRequestBuilder();
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getDppJsonLdContext", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<DppVocabContextDocument>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<DppVocabContextDocument>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<DppVocabContextDocument>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder getDppJsonLdContextRequestBuilder() throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/contexts/dpp/v1";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/ld+json, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * W3C JSON-LD context document for passport terms (secondary, fixed term list)
   * Serves a static W3C JSON-LD &#x60;@context&#x60; document (&#x60;application/ld+json&#x60;) for the core Digital Product Passport term vocabulary: maps the DPP terms to &#x60;https://opendpp-node.eu/ns/dpp#…&#x60; IRIs and &#x60;createdAt&#x60;/&#x60;updatedAt&#x60; to schema.org &#x60;dateCreated&#x60;/&#x60;dateModified&#x60;. This is a **secondary** fixed term list — the context that public passport/unit JSON-LD documents actually reference is the &#x60;@vocab&#x60;-based one at &#x60;GET /contexts/dpp/v1&#x60;; dereference that when expanding OpenDPP JSON-LD.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard &#x60;x-ratelimit-*&#x60; headers). Like every documented path except &#x60;/health&#x60;, a request on an unknown tenant workspace host receives a platform-level JSON 404 before this handler runs.
   * @return DppJsonLdContextDocument
   * @throws ApiException if fails to make API call
   */
  public DppJsonLdContextDocument getJsonLdContext() throws ApiException {
    ApiResponse<DppJsonLdContextDocument> localVarResponse = getJsonLdContextWithHttpInfo();
    return localVarResponse.getData();
  }

  /**
   * W3C JSON-LD context document for passport terms (secondary, fixed term list)
   * Serves a static W3C JSON-LD &#x60;@context&#x60; document (&#x60;application/ld+json&#x60;) for the core Digital Product Passport term vocabulary: maps the DPP terms to &#x60;https://opendpp-node.eu/ns/dpp#…&#x60; IRIs and &#x60;createdAt&#x60;/&#x60;updatedAt&#x60; to schema.org &#x60;dateCreated&#x60;/&#x60;dateModified&#x60;. This is a **secondary** fixed term list — the context that public passport/unit JSON-LD documents actually reference is the &#x60;@vocab&#x60;-based one at &#x60;GET /contexts/dpp/v1&#x60;; dereference that when expanding OpenDPP JSON-LD.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard &#x60;x-ratelimit-*&#x60; headers). Like every documented path except &#x60;/health&#x60;, a request on an unknown tenant workspace host receives a platform-level JSON 404 before this handler runs.
   * @return ApiResponse&lt;DppJsonLdContextDocument&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DppJsonLdContextDocument> getJsonLdContextWithHttpInfo() throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getJsonLdContextRequestBuilder();
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getJsonLdContext", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<DppJsonLdContextDocument>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<DppJsonLdContextDocument>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<DppJsonLdContextDocument>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder getJsonLdContextRequestBuilder() throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/context/v1";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/ld+json, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Get the ESPR metadata schema for a product category
   * Returns the machine-readable ESPR &#x60;metadata&#x60; schema for a product category.  **Default representation** (any &#x60;Accept&#x60; NOT containing &#x60;application/ld+json&#x60;): the category&#39;s JSON Schema **draft-07** document, served as &#x60;application/schema+json&#x60;, with each known field annotated server-side with a plain-English &#x60;description&#x60; (the annotations are AJV-ignored — validation behavior is unchanged). **With &#x60;Accept: application/ld+json&#x60;:** a small JSON-LD &#x60;@context&#x60; for the category vocabulary instead. Note: the route does not set &#x60;Vary: Accept&#x60;.  The category path segment is lower-cased before lookup. Machine-readable schemas exist for **5** of the 9 ESPR categories: &#x60;textiles&#x60;, &#x60;batteries&#x60;, &#x60;electronics&#x60;, &#x60;chemicals&#x60;, &#x60;construction&#x60;. The remaining 4 categories accepted by passport metadata validation (&#x60;cosmetics&#x60;, &#x60;toys&#x60;, &#x60;iron-steel&#x60;, &#x60;aluminium&#x60;) are validated by built-in server rules and currently return &#x60;404&#x60; from this endpoint.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard &#x60;x-ratelimit-*&#x60; headers). Like every documented path except &#x60;/health&#x60;, a request on an unknown tenant workspace host receives a platform-level JSON 404 (&#x60;No tenant company found for subdomain: …&#x60;, no &#x60;success&#x60; field) before this handler runs.
   * @param category ESPR product category (case-insensitive). Only &#x60;textiles&#x60;, &#x60;batteries&#x60;, &#x60;electronics&#x60;, &#x60;chemicals&#x60; and &#x60;construction&#x60; have published JSON Schemas; &#x60;cosmetics&#x60;, &#x60;toys&#x60;, &#x60;iron-steel&#x60; and &#x60;aluminium&#x60; return 404 here (their validation rules are built into the server). (required)
   * @return SectorJsonSchemaDocument
   * @throws ApiException if fails to make API call
   */
  public SectorJsonSchemaDocument getSectorSchema(String category) throws ApiException {
    ApiResponse<SectorJsonSchemaDocument> localVarResponse = getSectorSchemaWithHttpInfo(category);
    return localVarResponse.getData();
  }

  /**
   * Get the ESPR metadata schema for a product category
   * Returns the machine-readable ESPR &#x60;metadata&#x60; schema for a product category.  **Default representation** (any &#x60;Accept&#x60; NOT containing &#x60;application/ld+json&#x60;): the category&#39;s JSON Schema **draft-07** document, served as &#x60;application/schema+json&#x60;, with each known field annotated server-side with a plain-English &#x60;description&#x60; (the annotations are AJV-ignored — validation behavior is unchanged). **With &#x60;Accept: application/ld+json&#x60;:** a small JSON-LD &#x60;@context&#x60; for the category vocabulary instead. Note: the route does not set &#x60;Vary: Accept&#x60;.  The category path segment is lower-cased before lookup. Machine-readable schemas exist for **5** of the 9 ESPR categories: &#x60;textiles&#x60;, &#x60;batteries&#x60;, &#x60;electronics&#x60;, &#x60;chemicals&#x60;, &#x60;construction&#x60;. The remaining 4 categories accepted by passport metadata validation (&#x60;cosmetics&#x60;, &#x60;toys&#x60;, &#x60;iron-steel&#x60;, &#x60;aluminium&#x60;) are validated by built-in server rules and currently return &#x60;404&#x60; from this endpoint.  No authentication, no permission (public endpoint). No custom rate limiter — only the global platform limit applies (100 req/min/IP, standard &#x60;x-ratelimit-*&#x60; headers). Like every documented path except &#x60;/health&#x60;, a request on an unknown tenant workspace host receives a platform-level JSON 404 (&#x60;No tenant company found for subdomain: …&#x60;, no &#x60;success&#x60; field) before this handler runs.
   * @param category ESPR product category (case-insensitive). Only &#x60;textiles&#x60;, &#x60;batteries&#x60;, &#x60;electronics&#x60;, &#x60;chemicals&#x60; and &#x60;construction&#x60; have published JSON Schemas; &#x60;cosmetics&#x60;, &#x60;toys&#x60;, &#x60;iron-steel&#x60; and &#x60;aluminium&#x60; return 404 here (their validation rules are built into the server). (required)
   * @return ApiResponse&lt;SectorJsonSchemaDocument&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SectorJsonSchemaDocument> getSectorSchemaWithHttpInfo(String category) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getSectorSchemaRequestBuilder(category);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getSectorSchema", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<SectorJsonSchemaDocument>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<SectorJsonSchemaDocument>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<SectorJsonSchemaDocument>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder getSectorSchemaRequestBuilder(String category) throws ApiException {
    // verify the required parameter 'category' is set
    if (category == null) {
      throw new ApiException(400, "Missing the required parameter 'category' when calling getSectorSchema");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/schemas/{category}"
        .replace("{category}", ApiClient.urlEncode(category.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/schema+json, application/ld+json, application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * List the platform-curated material vocabulary
   * Lists active entries from the platform-global material vocabulary that powers the searchable material/fiber/chemistry pickers in the passport form. This is shared reference data, deliberately **not** tenant-scoped, so DPP data stays comparable across tenants.  **Auth:** any authenticated session — Bearer API key, Bearer JWT, or &#x60;opendpp_session&#x60; cookie. **No specific permission string is required** and subscription status is not checked, so this endpoint never returns 402. On a tenant-subdomain host, credentials belonging to a different tenant receive **403** with message &#x60;Cross-tenant access blocked.&#x60;.  **Filtering &amp; ordering:** &#x60;kind&#x60; filters by vocabulary kind — an unrecognized value is **silently ignored** (the filter simply isn&#39;t applied; no 400). &#x60;search&#x60; is a trimmed, case-insensitive substring match on &#x60;name&#x60; (blank values ignored). Only active entries are returned, ordered by &#x60;kind&#x60; ascending then &#x60;name&#x60; ascending. &#x60;limit&#x60; is clamped to 1–1000 (default 1000); there is no pagination.  **Envelope caveat:** the 200 body is &#x60;{ \&quot;materials\&quot;: [...] }&#x60; — there is **no &#x60;success&#x60; field** on this endpoint.  **Curation:** this vocabulary is curated by the platform operator — the API is read-only for tenant credentials. Free-text material values in passport metadata remain allowed but are never auto-added to this vocabulary.  **Rate limit:** global limiter only — 100 requests/min/IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param kind Filter by vocabulary kind. Unrecognized values are silently ignored (no error; the filter is not applied). (optional)
   * @param search Case-insensitive substring match on the entry &#x60;name&#x60; (value is trimmed; blank values ignored). (optional)
   * @param limit Maximum number of entries to return. Clamped to 1–1000 (out-of-range numeric values are clamped, not rejected); a non-numeric value falls back to the default 1000. (optional, default to 1000)
   * @return MaterialVocabularyListResponse
   * @throws ApiException if fails to make API call
   */
  public MaterialVocabularyListResponse listMaterials(String kind, String search, Integer limit) throws ApiException {
    ApiResponse<MaterialVocabularyListResponse> localVarResponse = listMaterialsWithHttpInfo(kind, search, limit);
    return localVarResponse.getData();
  }

  /**
   * List the platform-curated material vocabulary
   * Lists active entries from the platform-global material vocabulary that powers the searchable material/fiber/chemistry pickers in the passport form. This is shared reference data, deliberately **not** tenant-scoped, so DPP data stays comparable across tenants.  **Auth:** any authenticated session — Bearer API key, Bearer JWT, or &#x60;opendpp_session&#x60; cookie. **No specific permission string is required** and subscription status is not checked, so this endpoint never returns 402. On a tenant-subdomain host, credentials belonging to a different tenant receive **403** with message &#x60;Cross-tenant access blocked.&#x60;.  **Filtering &amp; ordering:** &#x60;kind&#x60; filters by vocabulary kind — an unrecognized value is **silently ignored** (the filter simply isn&#39;t applied; no 400). &#x60;search&#x60; is a trimmed, case-insensitive substring match on &#x60;name&#x60; (blank values ignored). Only active entries are returned, ordered by &#x60;kind&#x60; ascending then &#x60;name&#x60; ascending. &#x60;limit&#x60; is clamped to 1–1000 (default 1000); there is no pagination.  **Envelope caveat:** the 200 body is &#x60;{ \&quot;materials\&quot;: [...] }&#x60; — there is **no &#x60;success&#x60; field** on this endpoint.  **Curation:** this vocabulary is curated by the platform operator — the API is read-only for tenant credentials. Free-text material values in passport metadata remain allowed but are never auto-added to this vocabulary.  **Rate limit:** global limiter only — 100 requests/min/IP (standard &#x60;x-ratelimit-*&#x60; headers).
   * @param kind Filter by vocabulary kind. Unrecognized values are silently ignored (no error; the filter is not applied). (optional)
   * @param search Case-insensitive substring match on the entry &#x60;name&#x60; (value is trimmed; blank values ignored). (optional)
   * @param limit Maximum number of entries to return. Clamped to 1–1000 (out-of-range numeric values are clamped, not rejected); a non-numeric value falls back to the default 1000. (optional, default to 1000)
   * @return ApiResponse&lt;MaterialVocabularyListResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<MaterialVocabularyListResponse> listMaterialsWithHttpInfo(String kind, String search, Integer limit) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listMaterialsRequestBuilder(kind, search, limit);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listMaterials", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<MaterialVocabularyListResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<MaterialVocabularyListResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<MaterialVocabularyListResponse>() {})
        );
      } finally {
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder listMaterialsRequestBuilder(String kind, String search, Integer limit) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/materials";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "kind";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("kind", kind));
    localVarQueryParameterBaseName = "search";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("search", search));
    localVarQueryParameterBaseName = "limit";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));

    if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
      StringJoiner queryJoiner = new StringJoiner("&");
      localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
      if (localVarQueryStringJoiner.length() != 0) {
        queryJoiner.add(localVarQueryStringJoiner.toString());
      }
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
    } else {
      localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
    }

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

}
