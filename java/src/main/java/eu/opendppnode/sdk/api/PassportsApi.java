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

import eu.opendppnode.sdk.model.AasEnvironmentInput;
import eu.opendppnode.sdk.model.AasIngestCreated;
import eu.opendppnode.sdk.model.BulkIngestPassports400Response;
import eu.opendppnode.sdk.model.CreatePassport400Response;
import eu.opendppnode.sdk.model.CreatePassport413Response;
import eu.opendppnode.sdk.model.DeleteDraftPassport200Response;
import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.GetPassport404Response;
import eu.opendppnode.sdk.model.GetPassport429Response;
import eu.opendppnode.sdk.model.IngestPassportFromAas400Response;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.ListPassports400Response;
import eu.opendppnode.sdk.model.PassportAasEnvironment;
import eu.opendppnode.sdk.model.PassportBulkRequest;
import eu.opendppnode.sdk.model.PassportBulkResult;
import eu.opendppnode.sdk.model.PassportCreateRequest;
import eu.opendppnode.sdk.model.PassportIngestCreated;
import eu.opendppnode.sdk.model.PassportListResponse;
import eu.opendppnode.sdk.model.PassportQuotaError;
import eu.opendppnode.sdk.model.PassportSealResponse;
import eu.opendppnode.sdk.model.PassportStatusUpdateRequest;
import eu.opendppnode.sdk.model.PassportStatusUpdateResponse;
import eu.opendppnode.sdk.model.PassportUpdateRequest;
import eu.opendppnode.sdk.model.PassportUpdateResponse;
import eu.opendppnode.sdk.model.PassportValidateOnlyError;
import eu.opendppnode.sdk.model.PassportValidateOnlyRequest;
import eu.opendppnode.sdk.model.PassportValidateOnlyResult;
import eu.opendppnode.sdk.model.PassportVcReadinessReport200Response;
import eu.opendppnode.sdk.model.PublicPassportJsonLd;
import eu.opendppnode.sdk.model.UpdatePassport400Response;
import eu.opendppnode.sdk.model.UpdatePassportStatus500Response;
import eu.opendppnode.sdk.model.ValidatePassportPublic429Response;

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
public class PassportsApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public PassportsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public PassportsApi(ApiClient apiClient) {
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
   * Bulk-ingest up to 200 passports with per-row error reporting
   * Ingests up to **200** passports in one request with **partial-success semantics**: each row is validated and inserted independently; failed rows are skipped and reported as human-readable strings in &#x60;errors[]&#x60;. Returns **201** as long as at least one row was inserted (even with row errors); returns **400 Bulk Ingestion Failed** only when **every** row failed.  **Permission:** &#x60;passport:create&#x60; (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** global 100 requests/min per IP. **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that; in practice the &#x60;maxItems: 200&#x60; envelope cap is the effective bound for typical rows. Envelope violations — empty array, more than 200 items, missing &#x60;passports&#x60; — are rejected before any row is processed, with the full default validation error body (&#x60;{statusCode, code, error, message}&#x60;).  **Per-row behavior (differences from &#x60;POST /api/v1/passports&#x60;):** - Rows are validated with the ESPR category engine only — the **EPCIS traceability audit is NOT run** for bulk rows. - No &#x60;draft&#x60; support: every inserted row is created with &#x60;status: \&quot;ACTIVE\&quot;&#x60;. No &#x60;enrichment&#x60; support. - A valid GTIN-14/GRAI &#x60;productId&#x60; is **not** auto-copied into &#x60;metadata.gtin&#x60;/&#x60;metadata.grai&#x60; (unlike single ingestion). - Operator resolution per row: explicit &#x60;operatorId&#x60; must be bound to your workspace; otherwise the workspace&#39;s first bound operator is used; operator-scoped API keys force their operator. Lookups are cached within the request. - Duplicate &#x60;(productId, operatorId)&#x60; rows, unknown facilities, and per-row DB failures become &#x60;errors[]&#x60; strings (prefixed &#x60;[SKU: &lt;productId&gt;]&#x60;), never a request-level failure. - Each successfully inserted row **transactionally enqueues a &#x60;passport.ingested&#x60; webhook event** (public redacted JSON-LD payload). - Row validation messages use the localized &#x60;friendlyMessage&#x60; where the engine provides one (&#x60;?lang&#x3D;&#x60; / &#x60;Accept-Language&#x60;); category-validity errors fall back to the technical &#x60;path: message&#x60; form.  Note the 400 &#x60;Bulk Ingestion Failed&#x60; body has **no &#x60;message&#x60; field**, and &#x60;errors&#x60; is an array of **strings** (not objects).
   * @param passportBulkRequest  (required)
   * @param idempotencyKey Optional client idempotency key (≤255 characters, no control characters). Retrying a real (non-&#x60;dryRun&#x60;) import with the same &#x60;Idempotency-Key&#x60; replays the ORIGINAL result — same status and body, plus an &#x60;idempotent-replayed: true&#x60; response header — instead of re-inserting the batch. A &#x60;dryRun&#x60; preview is never captured or replayed. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally (the &#x60;(productId, operator)&#x60; unique constraint still prevents duplicate rows). (optional)
   * @param lang Locale for the localized validation text inside per-row &#x60;errors[]&#x60; strings (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return PassportBulkResult
   * @throws ApiException if fails to make API call
   */
  public PassportBulkResult bulkIngestPassports(PassportBulkRequest passportBulkRequest, String idempotencyKey, String lang) throws ApiException {
    ApiResponse<PassportBulkResult> localVarResponse = bulkIngestPassportsWithHttpInfo(passportBulkRequest, idempotencyKey, lang);
    return localVarResponse.getData();
  }

  /**
   * Bulk-ingest up to 200 passports with per-row error reporting
   * Ingests up to **200** passports in one request with **partial-success semantics**: each row is validated and inserted independently; failed rows are skipped and reported as human-readable strings in &#x60;errors[]&#x60;. Returns **201** as long as at least one row was inserted (even with row errors); returns **400 Bulk Ingestion Failed** only when **every** row failed.  **Permission:** &#x60;passport:create&#x60; (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** global 100 requests/min per IP. **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that; in practice the &#x60;maxItems: 200&#x60; envelope cap is the effective bound for typical rows. Envelope violations — empty array, more than 200 items, missing &#x60;passports&#x60; — are rejected before any row is processed, with the full default validation error body (&#x60;{statusCode, code, error, message}&#x60;).  **Per-row behavior (differences from &#x60;POST /api/v1/passports&#x60;):** - Rows are validated with the ESPR category engine only — the **EPCIS traceability audit is NOT run** for bulk rows. - No &#x60;draft&#x60; support: every inserted row is created with &#x60;status: \&quot;ACTIVE\&quot;&#x60;. No &#x60;enrichment&#x60; support. - A valid GTIN-14/GRAI &#x60;productId&#x60; is **not** auto-copied into &#x60;metadata.gtin&#x60;/&#x60;metadata.grai&#x60; (unlike single ingestion). - Operator resolution per row: explicit &#x60;operatorId&#x60; must be bound to your workspace; otherwise the workspace&#39;s first bound operator is used; operator-scoped API keys force their operator. Lookups are cached within the request. - Duplicate &#x60;(productId, operatorId)&#x60; rows, unknown facilities, and per-row DB failures become &#x60;errors[]&#x60; strings (prefixed &#x60;[SKU: &lt;productId&gt;]&#x60;), never a request-level failure. - Each successfully inserted row **transactionally enqueues a &#x60;passport.ingested&#x60; webhook event** (public redacted JSON-LD payload). - Row validation messages use the localized &#x60;friendlyMessage&#x60; where the engine provides one (&#x60;?lang&#x3D;&#x60; / &#x60;Accept-Language&#x60;); category-validity errors fall back to the technical &#x60;path: message&#x60; form.  Note the 400 &#x60;Bulk Ingestion Failed&#x60; body has **no &#x60;message&#x60; field**, and &#x60;errors&#x60; is an array of **strings** (not objects).
   * @param passportBulkRequest  (required)
   * @param idempotencyKey Optional client idempotency key (≤255 characters, no control characters). Retrying a real (non-&#x60;dryRun&#x60;) import with the same &#x60;Idempotency-Key&#x60; replays the ORIGINAL result — same status and body, plus an &#x60;idempotent-replayed: true&#x60; response header — instead of re-inserting the batch. A &#x60;dryRun&#x60; preview is never captured or replayed. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally (the &#x60;(productId, operator)&#x60; unique constraint still prevents duplicate rows). (optional)
   * @param lang Locale for the localized validation text inside per-row &#x60;errors[]&#x60; strings (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return ApiResponse&lt;PassportBulkResult&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportBulkResult> bulkIngestPassportsWithHttpInfo(PassportBulkRequest passportBulkRequest, String idempotencyKey, String lang) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = bulkIngestPassportsRequestBuilder(passportBulkRequest, idempotencyKey, lang);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("bulkIngestPassports", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportBulkResult>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportBulkResult>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportBulkResult>() {})
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

  private HttpRequest.Builder bulkIngestPassportsRequestBuilder(PassportBulkRequest passportBulkRequest, String idempotencyKey, String lang) throws ApiException {
    // verify the required parameter 'passportBulkRequest' is set
    if (passportBulkRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'passportBulkRequest' when calling bulkIngestPassports");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/bulk";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "lang";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("lang", lang));

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

    if (idempotencyKey != null) {
      localVarRequestBuilder.header("Idempotency-Key", idempotencyKey.toString());
    }
    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(passportBulkRequest);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Create (ingest) a Digital Product Passport
   * Creates a SKU/type-level Digital Product Passport.  **Permission:** &#x60;passport:create&#x60; (Bearer &#x60;op_dpp_token_…&#x60; API key or session JWT; cookie sessions must also send the &#x60;X-CSRF-Token&#x60; double-submit header). Write operations are subject to subscription gating (**402**) and, where the workspace enforces it, MFA (**403**).  **Rate limit:** global 100 requests/min per IP (&#x60;x-ratelimit-*&#x60; headers). **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that.  **Validation.** Unless &#x60;draft: true&#x60;, &#x60;metadata&#x60; is validated against the ESPR category rules for &#x60;metadata.category&#x60; plus cross-field rules (e.g. &#x60;materialComposition&#x60; percentages must sum to 100 ±0.1, &#x60;originCountry&#x60; must be a real ISO 3166-1 alpha-2 code), and the product&#39;s EPCIS traceability lineage is audited. For five categories (textiles, batteries, electronics, chemicals, construction) the authoritative per-category JSON Schema is served live at &#x60;GET /api/v1/schemas/{category}&#x60;; the other four (cosmetics, toys, iron-steel, aluminium) are validated by built-in server-side rules and &#x60;GET /api/v1/schemas/{category}&#x60; returns **404** for them. Failure returns the **400 Validation Failed** body with per-field &#x60;errors[]&#x60; (plus &#x60;warnings[]&#x60; when any exist — the key is omitted entirely when there are none). A passing payload may still produce non-blocking &#x60;warnings[]&#x60;, echoed in the 201 — including a **privacy-by-design advisory** (#400) when the metadata *looks* like it carries personal data (a clearly-personal field name such as &#x60;email&#x60;/&#x60;firstName&#x60;, or an email-shaped value; scanned one level deep, at most one such advisory). A DPP should carry PRODUCT data, not PII (ESPR FAQ Q16); this advisory never blocks the save. &#x60;friendlyMessage&#x60; texts are localized via &#x60;?lang&#x3D;&#x60; or &#x60;Accept-Language&#x60; (default &#x60;en&#x60;); category-validity errors (&#x60;metadata.category&#x60; missing or unknown) carry no &#x60;friendlyMessage&#x60;.  **Drafts.** &#x60;draft: true&#x60; skips ALL validation, stores the passport with &#x60;status: \&quot;DRAFT\&quot;&#x60; (not publicly resolvable), returns &#x60;message: \&quot;Draft passport saved\&quot;&#x60; with &#x60;warnings: []&#x60;, and does **not** emit a webhook.  **Identifier handling.** &#x60;productId&#x60; may be a GTIN-14 (14 digits, GS1 mod-10 check digit), a GRAI (14-digit numeric asset id + up to 16 alphanumeric serial chars), or a free-form SKU. A 14-digit &#x60;productId&#x60; whose GS1 mod-10 check digit is invalid is rejected with **400** (a typo&#39;d GTIN is never silently downgraded to a SKU); a non-numeric or non-14-digit &#x60;productId&#x60; is accepted as a non-GS1 SKU and carries a non-blocking &#x60;warnings[]&#x60; advisory that it resolves via &#x60;/passport/{id}&#x60; with no scannable GS1 QR. A valid GTIN-14 is auto-copied into &#x60;metadata.gtin&#x60; (a GRAI into &#x60;metadata.grai&#x60;) before storage. The server mints a UUID passport id and a GS1 Digital Link URI &#x60;https://opendpp-node.eu/{01|8003}/{productId}&#x60;.  **Operator binding.** With &#x60;operatorId&#x60; omitted, the passport is attributed to the first economic operator bound to your workspace; if no operator is bound at all the request fails **400** (the API never fabricates an operator identity — register one via &#x60;POST /api/v1/operators&#x60;). An &#x60;operatorId&#x60; not bound to your workspace → **403**. Operator-scoped API keys force their own operator and **403** on mismatch. The &#x60;(productId, operatorId)&#x60; pair is unique → **409** on duplicates. An optional &#x60;facilityId&#x60; must reference a Facility in your workspace (**400** otherwise).  **Webhook:** non-draft creation transactionally enqueues a &#x60;passport.ingested&#x60; event whose payload is the public redacted JSON-LD passport document (same masking as the 201 &#x60;passport&#x60; field). Drafts emit nothing.  **Response caveats:** the 201 &#x60;passport&#x60; field is the **public, redacted** JSON-LD representation — even for the creator. The owner-only metadata key &#x60;facilityDetails&#x60; is replaced with the literal placeholder &#x60;\&quot;[REDACTED - Privileged Access Required]\&quot;&#x60; (it appears as the placeholder even when you did not supply it), and for &#x60;category: \&quot;batteries\&quot;&#x60; the restricted legitimate-interest keys &#x60;detailedPerformance&#x60;, &#x60;lifecycleAndInUse&#x60; and &#x60;circularityAndDisassembly&#x60; (Battery Reg. Annex XIII parts 2-4) are masked the same way when present. &#x60;enrichment&#x60; is stored outside the validated metadata and Merkle seal and never appears in the JSON-LD document. The 201 body&#39;s top-level fields are &#x60;success&#x60;, &#x60;message&#x60;, &#x60;passport&#x60;, &#x60;warnings&#x60;, and the &#x60;vcReady&#x60;/&#x60;vcReadyReason&#x60; UNTP Verifiable-Credential readiness signal (#247).  **Other 400 bodies:** non-validation failures (whitespace-only &#x60;productId&#x60;, no bound operator, unknown &#x60;facilityId&#x60;) reuse status 400 with the plain &#x60;{\&quot;success\&quot;: false, \&quot;error\&quot;: \&quot;Bad Request\&quot;, \&quot;message\&quot;: …}&#x60; triple and **no** &#x60;errors&#x60;/&#x60;warnings&#x60; arrays. Requests rejected before the handler runs — request-body schema violations (e.g. missing &#x60;productId&#x60;) and malformed JSON — come back as just &#x60;{\&quot;error\&quot;: \&quot;Bad Request\&quot;, \&quot;message\&quot;: …}&#x60;.
   * @param passportCreateRequest  (required)
   * @param idempotencyKey Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same &#x60;Idempotency-Key&#x60; replays the ORIGINAL response — same status and body, plus an &#x60;idempotent-replayed: true&#x60; response header — instead of creating a duplicate passport or returning **409**. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry falls back to normal processing — never a double write, but you may then see the normal **409** instead of a replay. (optional)
   * @param lang Locale for localized &#x60;friendlyMessage&#x60; validation texts. One of: en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr. Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return PassportIngestCreated
   * @throws ApiException if fails to make API call
   */
  public PassportIngestCreated createPassport(PassportCreateRequest passportCreateRequest, String idempotencyKey, String lang) throws ApiException {
    ApiResponse<PassportIngestCreated> localVarResponse = createPassportWithHttpInfo(passportCreateRequest, idempotencyKey, lang);
    return localVarResponse.getData();
  }

  /**
   * Create (ingest) a Digital Product Passport
   * Creates a SKU/type-level Digital Product Passport.  **Permission:** &#x60;passport:create&#x60; (Bearer &#x60;op_dpp_token_…&#x60; API key or session JWT; cookie sessions must also send the &#x60;X-CSRF-Token&#x60; double-submit header). Write operations are subject to subscription gating (**402**) and, where the workspace enforces it, MFA (**403**).  **Rate limit:** global 100 requests/min per IP (&#x60;x-ratelimit-*&#x60; headers). **Body limit: 1 MiB (1,048,576 bytes)** → **413** beyond that.  **Validation.** Unless &#x60;draft: true&#x60;, &#x60;metadata&#x60; is validated against the ESPR category rules for &#x60;metadata.category&#x60; plus cross-field rules (e.g. &#x60;materialComposition&#x60; percentages must sum to 100 ±0.1, &#x60;originCountry&#x60; must be a real ISO 3166-1 alpha-2 code), and the product&#39;s EPCIS traceability lineage is audited. For five categories (textiles, batteries, electronics, chemicals, construction) the authoritative per-category JSON Schema is served live at &#x60;GET /api/v1/schemas/{category}&#x60;; the other four (cosmetics, toys, iron-steel, aluminium) are validated by built-in server-side rules and &#x60;GET /api/v1/schemas/{category}&#x60; returns **404** for them. Failure returns the **400 Validation Failed** body with per-field &#x60;errors[]&#x60; (plus &#x60;warnings[]&#x60; when any exist — the key is omitted entirely when there are none). A passing payload may still produce non-blocking &#x60;warnings[]&#x60;, echoed in the 201 — including a **privacy-by-design advisory** (#400) when the metadata *looks* like it carries personal data (a clearly-personal field name such as &#x60;email&#x60;/&#x60;firstName&#x60;, or an email-shaped value; scanned one level deep, at most one such advisory). A DPP should carry PRODUCT data, not PII (ESPR FAQ Q16); this advisory never blocks the save. &#x60;friendlyMessage&#x60; texts are localized via &#x60;?lang&#x3D;&#x60; or &#x60;Accept-Language&#x60; (default &#x60;en&#x60;); category-validity errors (&#x60;metadata.category&#x60; missing or unknown) carry no &#x60;friendlyMessage&#x60;.  **Drafts.** &#x60;draft: true&#x60; skips ALL validation, stores the passport with &#x60;status: \&quot;DRAFT\&quot;&#x60; (not publicly resolvable), returns &#x60;message: \&quot;Draft passport saved\&quot;&#x60; with &#x60;warnings: []&#x60;, and does **not** emit a webhook.  **Identifier handling.** &#x60;productId&#x60; may be a GTIN-14 (14 digits, GS1 mod-10 check digit), a GRAI (14-digit numeric asset id + up to 16 alphanumeric serial chars), or a free-form SKU. A 14-digit &#x60;productId&#x60; whose GS1 mod-10 check digit is invalid is rejected with **400** (a typo&#39;d GTIN is never silently downgraded to a SKU); a non-numeric or non-14-digit &#x60;productId&#x60; is accepted as a non-GS1 SKU and carries a non-blocking &#x60;warnings[]&#x60; advisory that it resolves via &#x60;/passport/{id}&#x60; with no scannable GS1 QR. A valid GTIN-14 is auto-copied into &#x60;metadata.gtin&#x60; (a GRAI into &#x60;metadata.grai&#x60;) before storage. The server mints a UUID passport id and a GS1 Digital Link URI &#x60;https://opendpp-node.eu/{01|8003}/{productId}&#x60;.  **Operator binding.** With &#x60;operatorId&#x60; omitted, the passport is attributed to the first economic operator bound to your workspace; if no operator is bound at all the request fails **400** (the API never fabricates an operator identity — register one via &#x60;POST /api/v1/operators&#x60;). An &#x60;operatorId&#x60; not bound to your workspace → **403**. Operator-scoped API keys force their own operator and **403** on mismatch. The &#x60;(productId, operatorId)&#x60; pair is unique → **409** on duplicates. An optional &#x60;facilityId&#x60; must reference a Facility in your workspace (**400** otherwise).  **Webhook:** non-draft creation transactionally enqueues a &#x60;passport.ingested&#x60; event whose payload is the public redacted JSON-LD passport document (same masking as the 201 &#x60;passport&#x60; field). Drafts emit nothing.  **Response caveats:** the 201 &#x60;passport&#x60; field is the **public, redacted** JSON-LD representation — even for the creator. The owner-only metadata key &#x60;facilityDetails&#x60; is replaced with the literal placeholder &#x60;\&quot;[REDACTED - Privileged Access Required]\&quot;&#x60; (it appears as the placeholder even when you did not supply it), and for &#x60;category: \&quot;batteries\&quot;&#x60; the restricted legitimate-interest keys &#x60;detailedPerformance&#x60;, &#x60;lifecycleAndInUse&#x60; and &#x60;circularityAndDisassembly&#x60; (Battery Reg. Annex XIII parts 2-4) are masked the same way when present. &#x60;enrichment&#x60; is stored outside the validated metadata and Merkle seal and never appears in the JSON-LD document. The 201 body&#39;s top-level fields are &#x60;success&#x60;, &#x60;message&#x60;, &#x60;passport&#x60;, &#x60;warnings&#x60;, and the &#x60;vcReady&#x60;/&#x60;vcReadyReason&#x60; UNTP Verifiable-Credential readiness signal (#247).  **Other 400 bodies:** non-validation failures (whitespace-only &#x60;productId&#x60;, no bound operator, unknown &#x60;facilityId&#x60;) reuse status 400 with the plain &#x60;{\&quot;success\&quot;: false, \&quot;error\&quot;: \&quot;Bad Request\&quot;, \&quot;message\&quot;: …}&#x60; triple and **no** &#x60;errors&#x60;/&#x60;warnings&#x60; arrays. Requests rejected before the handler runs — request-body schema violations (e.g. missing &#x60;productId&#x60;) and malformed JSON — come back as just &#x60;{\&quot;error\&quot;: \&quot;Bad Request\&quot;, \&quot;message\&quot;: …}&#x60;.
   * @param passportCreateRequest  (required)
   * @param idempotencyKey Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same &#x60;Idempotency-Key&#x60; replays the ORIGINAL response — same status and body, plus an &#x60;idempotent-replayed: true&#x60; response header — instead of creating a duplicate passport or returning **409**. Scoped per (workspace, endpoint, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry falls back to normal processing — never a double write, but you may then see the normal **409** instead of a replay. (optional)
   * @param lang Locale for localized &#x60;friendlyMessage&#x60; validation texts. One of: en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr. Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return ApiResponse&lt;PassportIngestCreated&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportIngestCreated> createPassportWithHttpInfo(PassportCreateRequest passportCreateRequest, String idempotencyKey, String lang) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = createPassportRequestBuilder(passportCreateRequest, idempotencyKey, lang);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("createPassport", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportIngestCreated>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportIngestCreated>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportIngestCreated>() {})
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

  private HttpRequest.Builder createPassportRequestBuilder(PassportCreateRequest passportCreateRequest, String idempotencyKey, String lang) throws ApiException {
    // verify the required parameter 'passportCreateRequest' is set
    if (passportCreateRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'passportCreateRequest' when calling createPassport");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "lang";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("lang", lang));

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

    if (idempotencyKey != null) {
      localVarRequestBuilder.header("Idempotency-Key", idempotencyKey.toString());
    }
    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(passportCreateRequest);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Permanently delete a DRAFT passport
   * Hard-deletes a passport **only while it is a DRAFT** (never published, not publicly resolvable, no retention duty). Children (history, access logs, battery units) cascade on delete.  Published passports (ACTIVE/RECALLED/DECOMMISSIONED) are refused with **409** — they must be decommissioned/archived through the status lifecycle (&#x60;PUT /api/v1/passports/{id}/status&#x60;) to satisfy the ESPR Art. 9(2) persistence duty.  **Permission:** &#x60;passport:update&#x60; (write — subscription gating applies, see 402). Cookie sessions must send &#x60;X-CSRF-Token&#x60;; Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** (no &#x60;productId&#x60; aliasing) and only within the passport&#39;s **owning tenant** — an operator-binding alone is not sufficient, unlike PUT.  **Rate limits:** global limiter, 100 req/min/IP.
   * @param id Passport UUID. &#x60;productId&#x60; aliasing is NOT supported here. (required)
   * @return DeleteDraftPassport200Response
   * @throws ApiException if fails to make API call
   */
  public DeleteDraftPassport200Response deleteDraftPassport(String id) throws ApiException {
    ApiResponse<DeleteDraftPassport200Response> localVarResponse = deleteDraftPassportWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Permanently delete a DRAFT passport
   * Hard-deletes a passport **only while it is a DRAFT** (never published, not publicly resolvable, no retention duty). Children (history, access logs, battery units) cascade on delete.  Published passports (ACTIVE/RECALLED/DECOMMISSIONED) are refused with **409** — they must be decommissioned/archived through the status lifecycle (&#x60;PUT /api/v1/passports/{id}/status&#x60;) to satisfy the ESPR Art. 9(2) persistence duty.  **Permission:** &#x60;passport:update&#x60; (write — subscription gating applies, see 402). Cookie sessions must send &#x60;X-CSRF-Token&#x60;; Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** (no &#x60;productId&#x60; aliasing) and only within the passport&#39;s **owning tenant** — an operator-binding alone is not sufficient, unlike PUT.  **Rate limits:** global limiter, 100 req/min/IP.
   * @param id Passport UUID. &#x60;productId&#x60; aliasing is NOT supported here. (required)
   * @return ApiResponse&lt;DeleteDraftPassport200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<DeleteDraftPassport200Response> deleteDraftPassportWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = deleteDraftPassportRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("deleteDraftPassport", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<DeleteDraftPassport200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<DeleteDraftPassport200Response>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<DeleteDraftPassport200Response>() {})
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

  private HttpRequest.Builder deleteDraftPassportRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteDraftPassport");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Fetch a single passport (content-negotiated JSON-LD / AAS / HTML)
   * Owner-side alias of the public resolver. Accepts either the passport **UUID** or its caller-supplied **&#x60;productId&#x60;** (GTIN-14 / GRAI / SKU), scoped to operators bound to your workspace. After the scoped lookup the request is **re-dispatched internally to &#x60;GET /passport/{uuid}&#x60;**, forwarding all request headers, and the inner response (status, content type, body) is returned as-is.  **Permission:** &#x60;passport:read&#x60; (read-only — no subscription/402 gate).  **Content negotiation** (substring match on &#x60;Accept&#x60;): &#x60;application/aas+json&#x60; → role-filtered AAS environment; &#x60;application/vc+jwt&#x60; → enveloping UNTP Verifiable Credential; &#x60;application/vc+ld+json&#x60; → the same credential with an embedded &#x60;ecdsa-jcs-2019&#x60; W3C Data Integrity proof; &#x60;application/dc+sd-jwt&#x60; (legacy &#x60;vc+sd-jwt&#x60; accepted) → SD-JWT-VC selective disclosure (these three return &#x60;406 Not Acceptable&#x60; when the passport has no manufacturing facility with a country of production); &#x60;text/html&#x60; → SSR passport page; anything else (including &#x60;application/json&#x60;, &#x60;*_/_*&#x60;, or no header) → JSON-LD with &#x60;Content-Type: application/ld+json&#x60; (the default). The VC and SD-JWT representations are forwarded verbatim from &#x60;GET /passport/{id}&#x60; — see that operation for the full credential semantics.  **Access-tier caveat (privilege is resolved from the *forwarded* headers, not the already-authenticated context):** only **database API keys** (&#x60;Authorization: Bearer op_dpp_token_…&#x60;) of the owning or operator-bound tenant are recognized as owner by the inner resolver. Those callers get the **owner-tier** document: &#x60;facilityDetails&#x60; and battery restricted keys unmasked, &#x60;manufacturingFacility&#x60; includes &#x60;streetAddress&#x60;/&#x60;city&#x60;/&#x60;postalCode&#x60;, and DRAFT passports are visible. Callers authenticated with a **JWT session** (login cookie or bearer JWT) receive the **public-redacted** tier instead, and DRAFT passports answer 404 with the forwarded public body (no &#x60;success&#x60; field).  Every successful resolution records an anonymized-IP access audit entry.  **Rate limits:** global limiter 100 req/min/IP with &#x60;x-ratelimit-*&#x60; headers, **plus** the forwarded public resolver&#39;s own limiter (30 req/min/IP, no headers) — both 429 shapes are possible (see 429).
   * @param id Passport UUID **or** caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). UUID is tried first, then &#x60;productId&#x60;. (required)
   * @return PublicPassportJsonLd
   * @throws ApiException if fails to make API call
   */
  public PublicPassportJsonLd getPassport(String id) throws ApiException {
    ApiResponse<PublicPassportJsonLd> localVarResponse = getPassportWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Fetch a single passport (content-negotiated JSON-LD / AAS / HTML)
   * Owner-side alias of the public resolver. Accepts either the passport **UUID** or its caller-supplied **&#x60;productId&#x60;** (GTIN-14 / GRAI / SKU), scoped to operators bound to your workspace. After the scoped lookup the request is **re-dispatched internally to &#x60;GET /passport/{uuid}&#x60;**, forwarding all request headers, and the inner response (status, content type, body) is returned as-is.  **Permission:** &#x60;passport:read&#x60; (read-only — no subscription/402 gate).  **Content negotiation** (substring match on &#x60;Accept&#x60;): &#x60;application/aas+json&#x60; → role-filtered AAS environment; &#x60;application/vc+jwt&#x60; → enveloping UNTP Verifiable Credential; &#x60;application/vc+ld+json&#x60; → the same credential with an embedded &#x60;ecdsa-jcs-2019&#x60; W3C Data Integrity proof; &#x60;application/dc+sd-jwt&#x60; (legacy &#x60;vc+sd-jwt&#x60; accepted) → SD-JWT-VC selective disclosure (these three return &#x60;406 Not Acceptable&#x60; when the passport has no manufacturing facility with a country of production); &#x60;text/html&#x60; → SSR passport page; anything else (including &#x60;application/json&#x60;, &#x60;*_/_*&#x60;, or no header) → JSON-LD with &#x60;Content-Type: application/ld+json&#x60; (the default). The VC and SD-JWT representations are forwarded verbatim from &#x60;GET /passport/{id}&#x60; — see that operation for the full credential semantics.  **Access-tier caveat (privilege is resolved from the *forwarded* headers, not the already-authenticated context):** only **database API keys** (&#x60;Authorization: Bearer op_dpp_token_…&#x60;) of the owning or operator-bound tenant are recognized as owner by the inner resolver. Those callers get the **owner-tier** document: &#x60;facilityDetails&#x60; and battery restricted keys unmasked, &#x60;manufacturingFacility&#x60; includes &#x60;streetAddress&#x60;/&#x60;city&#x60;/&#x60;postalCode&#x60;, and DRAFT passports are visible. Callers authenticated with a **JWT session** (login cookie or bearer JWT) receive the **public-redacted** tier instead, and DRAFT passports answer 404 with the forwarded public body (no &#x60;success&#x60; field).  Every successful resolution records an anonymized-IP access audit entry.  **Rate limits:** global limiter 100 req/min/IP with &#x60;x-ratelimit-*&#x60; headers, **plus** the forwarded public resolver&#39;s own limiter (30 req/min/IP, no headers) — both 429 shapes are possible (see 429).
   * @param id Passport UUID **or** caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). UUID is tried first, then &#x60;productId&#x60;. (required)
   * @return ApiResponse&lt;PublicPassportJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PublicPassportJsonLd> getPassportWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getPassportRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getPassport", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PublicPassportJsonLd>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PublicPassportJsonLd>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PublicPassportJsonLd>() {})
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

  private HttpRequest.Builder getPassportRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getPassport");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/ld+json, application/aas+json, text/html, application/vc+jwt, application/vc+ld+json, application/dc+sd-jwt, application/json");

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
   * Ingest a passport from an AAS JSON Environment (seal-verified)
   * Ingests (creates **or updates**) a Digital Product Passport from an Industry-4.0 **Asset Administration Shell (AAS) JSON Environment** — the same format produced by OpenDPP&#39;s own AAS export.  **Permission:** &#x60;passport:create&#x60; (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** global 100 requests/min per IP. **Body limit: 262,144 bytes (256 KiB)** → **413**.  **Parsing.** The environment must contain a &#x60;submodels&#x60; array including a submodel with &#x60;idShort: \&quot;ComplianceMetadata\&quot;&#x60;, whose &#x60;submodelElements&#x60; are parsed back into the metadata object; missing it fails 400 (&#x60;Ingestion Failed&#x60;). &#x60;productId&#x60; is resolved from &#x60;metadata.gtin&#x60; || &#x60;metadata.grai&#x60; || &#x60;metadata.productId&#x60; || the first shell&#39;s &#x60;assetInformation.specificAssetIds&#x60; entry named &#x60;productId&#x60; — unresolvable → 400 &#x60;Bad Request&#x60;. The parsed metadata then passes the full ESPR category validation **plus the EPCIS traceability audit** (400 &#x60;Validation Failed&#x60; with &#x60;errors[]&#x60;).  **eIDAS seal verification.** If the environment embeds an &#x60;eidasVerificationSeal&#x60; submodel (&#x60;digitalSealHash&#x60; / &#x60;cryptographicSignature&#x60; / &#x60;pemPublicKey&#x60; elements), the seal is verified against **your tenant&#39;s server-held eIDAS public key** — never the key embedded in the request (self-signing is rejected by design). An embedded seal that fails verification → **400 &#x60;Signature Verification Failed&#x60;**; this includes the case where your workspace holds no matching key. &#x60;isSealed&#x60;/&#x60;signatureVerified&#x60; in the 201 echo the outcome (both &#x60;false&#x60; for unsealed documents).  **Upsert semantics.** If a passport already exists for the resolved &#x60;(productId, operator)&#x60; pair: a **sealed** existing passport refuses re-ingestion (**403** — re-seal explicitly after changes); an unsealed one has its metadata, Merkle tree and seal fields **replaced**, still answering **201**. Operator resolution: operator-scoped API keys use their own operator and **403** when that operator is not bound to your workspace; otherwise the workspace&#39;s first bound operator is used; none bound → 400.  **Caveats:** - **NO webhook is emitted** — unlike &#x60;POST /api/v1/passports&#x60; and &#x60;/bulk&#x60;, AAS ingestion never enqueues &#x60;passport.ingested&#x60; (or any other event). - The catch-all error path returns **400 &#x60;Ingestion Failed&#x60;** with the underlying parse/processing message — even for internal failures (this handler does not emit its own 500). - Validation &#x60;friendlyMessage&#x60; localization via &#x60;?lang&#x3D;&#x60; / &#x60;Accept-Language&#x60;; category-validity errors carry no &#x60;friendlyMessage&#x60;.
   * @param aasEnvironmentInput  (required)
   * @param lang Locale for localized &#x60;friendlyMessage&#x60; validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return AasIngestCreated
   * @throws ApiException if fails to make API call
   */
  public AasIngestCreated ingestPassportFromAas(AasEnvironmentInput aasEnvironmentInput, String lang) throws ApiException {
    ApiResponse<AasIngestCreated> localVarResponse = ingestPassportFromAasWithHttpInfo(aasEnvironmentInput, lang);
    return localVarResponse.getData();
  }

  /**
   * Ingest a passport from an AAS JSON Environment (seal-verified)
   * Ingests (creates **or updates**) a Digital Product Passport from an Industry-4.0 **Asset Administration Shell (AAS) JSON Environment** — the same format produced by OpenDPP&#39;s own AAS export.  **Permission:** &#x60;passport:create&#x60; (Bearer API key or session JWT + CSRF for cookie sessions; subscription gating → **402**).  **Rate limit:** global 100 requests/min per IP. **Body limit: 262,144 bytes (256 KiB)** → **413**.  **Parsing.** The environment must contain a &#x60;submodels&#x60; array including a submodel with &#x60;idShort: \&quot;ComplianceMetadata\&quot;&#x60;, whose &#x60;submodelElements&#x60; are parsed back into the metadata object; missing it fails 400 (&#x60;Ingestion Failed&#x60;). &#x60;productId&#x60; is resolved from &#x60;metadata.gtin&#x60; || &#x60;metadata.grai&#x60; || &#x60;metadata.productId&#x60; || the first shell&#39;s &#x60;assetInformation.specificAssetIds&#x60; entry named &#x60;productId&#x60; — unresolvable → 400 &#x60;Bad Request&#x60;. The parsed metadata then passes the full ESPR category validation **plus the EPCIS traceability audit** (400 &#x60;Validation Failed&#x60; with &#x60;errors[]&#x60;).  **eIDAS seal verification.** If the environment embeds an &#x60;eidasVerificationSeal&#x60; submodel (&#x60;digitalSealHash&#x60; / &#x60;cryptographicSignature&#x60; / &#x60;pemPublicKey&#x60; elements), the seal is verified against **your tenant&#39;s server-held eIDAS public key** — never the key embedded in the request (self-signing is rejected by design). An embedded seal that fails verification → **400 &#x60;Signature Verification Failed&#x60;**; this includes the case where your workspace holds no matching key. &#x60;isSealed&#x60;/&#x60;signatureVerified&#x60; in the 201 echo the outcome (both &#x60;false&#x60; for unsealed documents).  **Upsert semantics.** If a passport already exists for the resolved &#x60;(productId, operator)&#x60; pair: a **sealed** existing passport refuses re-ingestion (**403** — re-seal explicitly after changes); an unsealed one has its metadata, Merkle tree and seal fields **replaced**, still answering **201**. Operator resolution: operator-scoped API keys use their own operator and **403** when that operator is not bound to your workspace; otherwise the workspace&#39;s first bound operator is used; none bound → 400.  **Caveats:** - **NO webhook is emitted** — unlike &#x60;POST /api/v1/passports&#x60; and &#x60;/bulk&#x60;, AAS ingestion never enqueues &#x60;passport.ingested&#x60; (or any other event). - The catch-all error path returns **400 &#x60;Ingestion Failed&#x60;** with the underlying parse/processing message — even for internal failures (this handler does not emit its own 500). - Validation &#x60;friendlyMessage&#x60; localization via &#x60;?lang&#x3D;&#x60; / &#x60;Accept-Language&#x60;; category-validity errors carry no &#x60;friendlyMessage&#x60;.
   * @param aasEnvironmentInput  (required)
   * @param lang Locale for localized &#x60;friendlyMessage&#x60; validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return ApiResponse&lt;AasIngestCreated&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AasIngestCreated> ingestPassportFromAasWithHttpInfo(AasEnvironmentInput aasEnvironmentInput, String lang) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = ingestPassportFromAasRequestBuilder(aasEnvironmentInput, lang);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("ingestPassportFromAas", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<AasIngestCreated>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<AasIngestCreated>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<AasIngestCreated>() {})
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

  private HttpRequest.Builder ingestPassportFromAasRequestBuilder(AasEnvironmentInput aasEnvironmentInput, String lang) throws ApiException {
    // verify the required parameter 'aasEnvironmentInput' is set
    if (aasEnvironmentInput == null) {
      throw new ApiException(400, "Missing the required parameter 'aasEnvironmentInput' when calling ingestPassportFromAas");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/aas/ingest";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "lang";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("lang", lang));

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

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(aasEnvironmentInput);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * List passports in your workspace (paginated JSON-LD)
   * Returns the **non-archived** passports of every economic operator bound to your workspace, newest first (&#x60;createdAt DESC&#x60;). Operator-scoped API keys only see passports of their bound operator.  **Permission:** &#x60;passport:read&#x60; (read-only — no subscription/402 gate).  **Filtering:** &#x60;category&#x60; and &#x60;originCountry&#x60; are exact-match filters on the top-level &#x60;metadata&#x60; keys of the same name. Known &#x60;metadata.category&#x60; values: &#x60;textiles&#x60;, &#x60;batteries&#x60;, &#x60;electronics&#x60;, &#x60;cosmetics&#x60;, &#x60;toys&#x60;, &#x60;iron-steel&#x60;, &#x60;aluminium&#x60;, &#x60;chemicals&#x60;, &#x60;construction&#x60;; &#x60;originCountry&#x60; is ISO 3166-1 alpha-2.  **Pagination:** &#x60;page&#x60; (default 1) and &#x60;limit&#x60; (default 10) are numeric strings matching &#x60;^[0-9]+$&#x60; — any other value is rejected with the framework&#39;s default 400 validation body (see 400). Parsed values are clamped server-side to &#x60;page &gt;&#x3D; 1&#x60; and &#x60;1 &lt;&#x3D; limit &lt;&#x3D; 100&#x60;. There is **no &#x60;total&#x60; count**; page until you receive fewer than &#x60;limit&#x60; items.  **Serialization caveats:** - The redaction tier of each item depends on the credential&#39;s **role**: only &#x60;BRAND_OPERATOR&#x60; credentials receive the unredacted owner-tier document. Every other role — including &#x60;TENANT_ADMIN&#x60; — receives the public tier: &#x60;facilityDetails&#x60; (and, for &#x60;batteries&#x60;, &#x60;detailedPerformance&#x60; / &#x60;lifecycleAndInUse&#x60; / &#x60;circularityAndDisassembly&#x60;) are masked to the literal string &#x60;\&quot;[REDACTED - Privileged Access Required]\&quot;&#x60;. - &#x60;economicOperator.role&#x60; is **absent** from list items and &#x60;manufacturingFacility&#x60; is always &#x60;null&#x60; here — fetch a single passport (&#x60;GET /api/v1/passports/{id}&#x60;) for the facility node and operator role. - The response passes through a declared response schema: top-level keys other than &#x60;success&#x60;, &#x60;page&#x60;, &#x60;limit&#x60;, &#x60;passports&#x60; are stripped. Passport items allow additional properties, so undeclared item keys (&#x60;status&#x60;, &#x60;archivedAt&#x60;, &#x60;retentionUntil&#x60;, &#x60;manufacturingFacility&#x60;, the flattened metadata keys) pass through intact — but two **declared** item keys are mangled by their subschemas: the &#x60;@context&#x60; term-map object (second array element) is always emptied to &#x60;{}&#x60;, and &#x60;proof&#x60; is emptied to &#x60;{}&#x60; on sealed items (&#x60;null&#x60; on unsealed) — &#x60;signatureValue&#x60;, &#x60;merkleRoot&#x60;, &#x60;redactedLeaves&#x60;, &#x60;x5c&#x60; and &#x60;rfc3161&#x60; are all stripped from list output. Fetch a single passport (&#x60;GET /api/v1/passports/{id}&#x60;) or the public resolver for the verifiable proof block.  **Rate limits:** global limiter, 100 requests/min per IP (600/min for known crawler user agents); 429 carries &#x60;x-ratelimit-*&#x60; headers.
   * @param page 1-based page number (digits only). (optional, default to 1)
   * @param limit Page size. (optional, default to 10)
   * @param category Exact-match filter on &#x60;metadata.category&#x60;. Known values: textiles, batteries, electronics, cosmetics, toys, iron-steel, aluminium, chemicals, construction. (optional)
   * @param originCountry Exact-match filter on &#x60;metadata.originCountry&#x60; (ISO 3166-1 alpha-2, e.g. &#x60;PT&#x60;). (optional)
   * @return PassportListResponse
   * @throws ApiException if fails to make API call
   */
  public PassportListResponse listPassports(Integer page, Integer limit, String category, String originCountry) throws ApiException {
    ApiResponse<PassportListResponse> localVarResponse = listPassportsWithHttpInfo(page, limit, category, originCountry);
    return localVarResponse.getData();
  }

  /**
   * List passports in your workspace (paginated JSON-LD)
   * Returns the **non-archived** passports of every economic operator bound to your workspace, newest first (&#x60;createdAt DESC&#x60;). Operator-scoped API keys only see passports of their bound operator.  **Permission:** &#x60;passport:read&#x60; (read-only — no subscription/402 gate).  **Filtering:** &#x60;category&#x60; and &#x60;originCountry&#x60; are exact-match filters on the top-level &#x60;metadata&#x60; keys of the same name. Known &#x60;metadata.category&#x60; values: &#x60;textiles&#x60;, &#x60;batteries&#x60;, &#x60;electronics&#x60;, &#x60;cosmetics&#x60;, &#x60;toys&#x60;, &#x60;iron-steel&#x60;, &#x60;aluminium&#x60;, &#x60;chemicals&#x60;, &#x60;construction&#x60;; &#x60;originCountry&#x60; is ISO 3166-1 alpha-2.  **Pagination:** &#x60;page&#x60; (default 1) and &#x60;limit&#x60; (default 10) are numeric strings matching &#x60;^[0-9]+$&#x60; — any other value is rejected with the framework&#39;s default 400 validation body (see 400). Parsed values are clamped server-side to &#x60;page &gt;&#x3D; 1&#x60; and &#x60;1 &lt;&#x3D; limit &lt;&#x3D; 100&#x60;. There is **no &#x60;total&#x60; count**; page until you receive fewer than &#x60;limit&#x60; items.  **Serialization caveats:** - The redaction tier of each item depends on the credential&#39;s **role**: only &#x60;BRAND_OPERATOR&#x60; credentials receive the unredacted owner-tier document. Every other role — including &#x60;TENANT_ADMIN&#x60; — receives the public tier: &#x60;facilityDetails&#x60; (and, for &#x60;batteries&#x60;, &#x60;detailedPerformance&#x60; / &#x60;lifecycleAndInUse&#x60; / &#x60;circularityAndDisassembly&#x60;) are masked to the literal string &#x60;\&quot;[REDACTED - Privileged Access Required]\&quot;&#x60;. - &#x60;economicOperator.role&#x60; is **absent** from list items and &#x60;manufacturingFacility&#x60; is always &#x60;null&#x60; here — fetch a single passport (&#x60;GET /api/v1/passports/{id}&#x60;) for the facility node and operator role. - The response passes through a declared response schema: top-level keys other than &#x60;success&#x60;, &#x60;page&#x60;, &#x60;limit&#x60;, &#x60;passports&#x60; are stripped. Passport items allow additional properties, so undeclared item keys (&#x60;status&#x60;, &#x60;archivedAt&#x60;, &#x60;retentionUntil&#x60;, &#x60;manufacturingFacility&#x60;, the flattened metadata keys) pass through intact — but two **declared** item keys are mangled by their subschemas: the &#x60;@context&#x60; term-map object (second array element) is always emptied to &#x60;{}&#x60;, and &#x60;proof&#x60; is emptied to &#x60;{}&#x60; on sealed items (&#x60;null&#x60; on unsealed) — &#x60;signatureValue&#x60;, &#x60;merkleRoot&#x60;, &#x60;redactedLeaves&#x60;, &#x60;x5c&#x60; and &#x60;rfc3161&#x60; are all stripped from list output. Fetch a single passport (&#x60;GET /api/v1/passports/{id}&#x60;) or the public resolver for the verifiable proof block.  **Rate limits:** global limiter, 100 requests/min per IP (600/min for known crawler user agents); 429 carries &#x60;x-ratelimit-*&#x60; headers.
   * @param page 1-based page number (digits only). (optional, default to 1)
   * @param limit Page size. (optional, default to 10)
   * @param category Exact-match filter on &#x60;metadata.category&#x60;. Known values: textiles, batteries, electronics, cosmetics, toys, iron-steel, aluminium, chemicals, construction. (optional)
   * @param originCountry Exact-match filter on &#x60;metadata.originCountry&#x60; (ISO 3166-1 alpha-2, e.g. &#x60;PT&#x60;). (optional)
   * @return ApiResponse&lt;PassportListResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportListResponse> listPassportsWithHttpInfo(Integer page, Integer limit, String category, String originCountry) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listPassportsRequestBuilder(page, limit, category, originCountry);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listPassports", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportListResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportListResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportListResponse>() {})
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

  private HttpRequest.Builder listPassportsRequestBuilder(Integer page, Integer limit, String category, String originCountry) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "page";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("page", page));
    localVarQueryParameterBaseName = "limit";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
    localVarQueryParameterBaseName = "category";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("category", category));
    localVarQueryParameterBaseName = "originCountry";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("originCountry", originCountry));

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

  /**
   * Catalog-wide UNTP Verifiable-Credential readiness report
   * A read-only, tenant-scoped report of which SKUs in your catalog can / can&#39;t emit a UNTP **Verifiable Credential**, and why — so you can fix a whole catalog before relying on VCs, instead of probing passports one at a time. It **aggregates the same per-passport &#x60;vcReady&#x60; signal** (#247) returned on every ingest response: a passport is VC-ready only when it links a manufacturing &#x60;Facility&#x60; with a country of production.  **Permission:** &#x60;passport:read&#x60; (read-only — no subscription/402 gate). Operator scoping + the non-archived filter match the passports list.  **Shape:** each &#x60;results[]&#x60; row is &#x60;{ id, productId, vcReady, blockers[] }&#x60; — &#x60;blockers[]&#x60; reuses the SAME actionable reason the single-passport signal exposes (empty when ready). The top-level &#x60;ready&#x60; / &#x60;notReady&#x60; rollup is **catalog-wide** (counts every non-archived passport), while &#x60;results&#x60; is **paginated** — &#x60;page&#x60; (default 1) + &#x60;limit&#x60; (default 100, max 200), with &#x60;total&#x60; / &#x60;totalPages&#x60;. NOTE: because the rollup is catalog-wide but &#x60;results&#x60; is one page, &#x60;ready&#x60; is generally NOT the count of &#x60;vcReady:true&#x60; rows on the current page — page through all &#x60;totalPages&#x60; to enumerate every SKU.  **Rate limits:** global limiter, 100 requests/min per IP.
   * @param page 1-based page number (digits only). (optional, default to 1)
   * @param limit Page size. (optional, default to 10)
   * @return PassportVcReadinessReport200Response
   * @throws ApiException if fails to make API call
   */
  public PassportVcReadinessReport200Response passportVcReadinessReport(Integer page, Integer limit) throws ApiException {
    ApiResponse<PassportVcReadinessReport200Response> localVarResponse = passportVcReadinessReportWithHttpInfo(page, limit);
    return localVarResponse.getData();
  }

  /**
   * Catalog-wide UNTP Verifiable-Credential readiness report
   * A read-only, tenant-scoped report of which SKUs in your catalog can / can&#39;t emit a UNTP **Verifiable Credential**, and why — so you can fix a whole catalog before relying on VCs, instead of probing passports one at a time. It **aggregates the same per-passport &#x60;vcReady&#x60; signal** (#247) returned on every ingest response: a passport is VC-ready only when it links a manufacturing &#x60;Facility&#x60; with a country of production.  **Permission:** &#x60;passport:read&#x60; (read-only — no subscription/402 gate). Operator scoping + the non-archived filter match the passports list.  **Shape:** each &#x60;results[]&#x60; row is &#x60;{ id, productId, vcReady, blockers[] }&#x60; — &#x60;blockers[]&#x60; reuses the SAME actionable reason the single-passport signal exposes (empty when ready). The top-level &#x60;ready&#x60; / &#x60;notReady&#x60; rollup is **catalog-wide** (counts every non-archived passport), while &#x60;results&#x60; is **paginated** — &#x60;page&#x60; (default 1) + &#x60;limit&#x60; (default 100, max 200), with &#x60;total&#x60; / &#x60;totalPages&#x60;. NOTE: because the rollup is catalog-wide but &#x60;results&#x60; is one page, &#x60;ready&#x60; is generally NOT the count of &#x60;vcReady:true&#x60; rows on the current page — page through all &#x60;totalPages&#x60; to enumerate every SKU.  **Rate limits:** global limiter, 100 requests/min per IP.
   * @param page 1-based page number (digits only). (optional, default to 1)
   * @param limit Page size. (optional, default to 10)
   * @return ApiResponse&lt;PassportVcReadinessReport200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportVcReadinessReport200Response> passportVcReadinessReportWithHttpInfo(Integer page, Integer limit) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = passportVcReadinessReportRequestBuilder(page, limit);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("passportVcReadinessReport", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportVcReadinessReport200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportVcReadinessReport200Response>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportVcReadinessReport200Response>() {})
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

  private HttpRequest.Builder passportVcReadinessReportRequestBuilder(Integer page, Integer limit) throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/vc-readiness";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "page";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("page", page));
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

  /**
   * Apply the tenant&#39;s eIDAS advanced electronic seal
   * Signs the passport&#39;s Merkle root (SHA-256 tree over the key-sorted top-level &#x60;metadata&#x60; entries) with the tenant&#39;s vault-held **ECDSA P-256 (prime256v1)** private key, producing an eIDAS **advanced** electronic seal (this is a local cryptographic seal — NOT a Commission/EU-registry registration, and NOT a qualified seal). The base64 signature is stored as &#x60;digitalSeal&#x60; together with the signing public key (PEM), the X.509 chain binding the key to the tenant&#39;s legal identity (surfaced as &#x60;proof.x5c&#x60;, leaf first, base64 DER), and — **best-effort, opt-in** — an RFC 3161 trusted timestamp over SHA-256(merkleRoot) (&#x60;proof.rfc3161&#x60;; a TSA outage or missing configuration never blocks sealing, the field is simply absent).  A &#x60;passport.sealed&#x60; webhook is enqueued transactionally with the update (payload: the public-redacted JSON-LD document including the full &#x60;proof&#x60; block).  **Permission:** &#x60;passport:seal&#x60; (write — subscription gating applies, see 402). Cookie sessions must send &#x60;X-CSRF-Token&#x60;; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or &#x60;productId&#x60;** (UUID tried first), restricted to the passport&#39;s **owning tenant**.  **Behavioral caveats:** - The route does **not** modify the passport&#39;s &#x60;status&#x60; — despite the success message&#39;s \&quot;and published\&quot; wording, a DRAFT stays a DRAFT after sealing. Publish via &#x60;PUT /api/v1/passports/{id}&#x60; (validated save) instead. - Re-sealing an already-sealed passport is allowed and **overwrites** the previous seal/timestamp. - Once sealed, in-place metadata edits are refused (403 on &#x60;PUT /api/v1/passports/{id}&#x60;). - Requires the tenant&#39;s eIDAS key pair to exist — otherwise 400. - The returned &#x60;passport&#x60; document is serialized at the **public** redaction tier (masked keys keep their true leaf hashes in &#x60;proof.redactedLeaves&#x60;, so the seal stays offline-verifiable after redaction).  **Rate limits:** global limiter, 100 req/min/IP.
   * @param id Passport UUID **or** caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
   * @return PassportSealResponse
   * @throws ApiException if fails to make API call
   */
  public PassportSealResponse sealPassport(String id) throws ApiException {
    ApiResponse<PassportSealResponse> localVarResponse = sealPassportWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Apply the tenant&#39;s eIDAS advanced electronic seal
   * Signs the passport&#39;s Merkle root (SHA-256 tree over the key-sorted top-level &#x60;metadata&#x60; entries) with the tenant&#39;s vault-held **ECDSA P-256 (prime256v1)** private key, producing an eIDAS **advanced** electronic seal (this is a local cryptographic seal — NOT a Commission/EU-registry registration, and NOT a qualified seal). The base64 signature is stored as &#x60;digitalSeal&#x60; together with the signing public key (PEM), the X.509 chain binding the key to the tenant&#39;s legal identity (surfaced as &#x60;proof.x5c&#x60;, leaf first, base64 DER), and — **best-effort, opt-in** — an RFC 3161 trusted timestamp over SHA-256(merkleRoot) (&#x60;proof.rfc3161&#x60;; a TSA outage or missing configuration never blocks sealing, the field is simply absent).  A &#x60;passport.sealed&#x60; webhook is enqueued transactionally with the update (payload: the public-redacted JSON-LD document including the full &#x60;proof&#x60; block).  **Permission:** &#x60;passport:seal&#x60; (write — subscription gating applies, see 402). Cookie sessions must send &#x60;X-CSRF-Token&#x60;; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or &#x60;productId&#x60;** (UUID tried first), restricted to the passport&#39;s **owning tenant**.  **Behavioral caveats:** - The route does **not** modify the passport&#39;s &#x60;status&#x60; — despite the success message&#39;s \&quot;and published\&quot; wording, a DRAFT stays a DRAFT after sealing. Publish via &#x60;PUT /api/v1/passports/{id}&#x60; (validated save) instead. - Re-sealing an already-sealed passport is allowed and **overwrites** the previous seal/timestamp. - Once sealed, in-place metadata edits are refused (403 on &#x60;PUT /api/v1/passports/{id}&#x60;). - Requires the tenant&#39;s eIDAS key pair to exist — otherwise 400. - The returned &#x60;passport&#x60; document is serialized at the **public** redaction tier (masked keys keep their true leaf hashes in &#x60;proof.redactedLeaves&#x60;, so the seal stays offline-verifiable after redaction).  **Rate limits:** global limiter, 100 req/min/IP.
   * @param id Passport UUID **or** caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
   * @return ApiResponse&lt;PassportSealResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportSealResponse> sealPassportWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = sealPassportRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("sealPassport", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportSealResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportSealResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportSealResponse>() {})
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

  private HttpRequest.Builder sealPassportRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling sealPassport");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{id}/seal"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Update passport metadata (versioned to history)
   * Replaces the passport&#39;s &#x60;metadata&#x60; (the Merkle root and leaf hashes are recomputed) and snapshots the **previous** metadata into the passport&#39;s version history (version &#x3D; count + 1, &#x60;changedBy&#x60; &#x3D; user email or &#x60;api-key:&lt;id&gt;&#x60;, &#x60;changeReason&#x60; defaults to &#x60;\&quot;API Update\&quot;&#x60;).  **Permission:** &#x60;passport:update&#x60; (write — subscription gating applies, see 402). Cookie sessions must send &#x60;X-CSRF-Token&#x60; (double-submit); Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** — &#x60;productId&#x60; aliasing is NOT supported on this endpoint. The passport must belong to an operator bound to your workspace.  **Draft semantics (&#x60;draft&#x60; flag):** - &#x60;draft: true&#x60; **skips ESPR validation entirely** and forces &#x60;status: \&quot;DRAFT\&quot;&#x60; — note this also demotes an already-published (ACTIVE/RECALLED/DECOMMISSIONED) passport back to DRAFT. - &#x60;draft&#x60; absent/false: &#x60;metadata&#x60; is validated against the ESPR category rules (400 on failure — see below). If the passport was a DRAFT it is **published**: status becomes &#x60;ACTIVE&#x60;, a &#x60;passport.ingested&#x60; webhook is enqueued transactionally (public-redacted JSON-LD payload) and an in-app notification is created best-effort afterwards. Editing an already-published (live) passport leaves its status untouched and enqueues a &#x60;passport.updated&#x60; webhook instead (same public-redacted JSON-LD payload).  **Validation divergence:** the 400 validation body here contains &#x60;errors&#x60; but — unlike &#x60;POST /api/v1/passports&#x60; — **never a &#x60;warnings&#x60; array**. &#x60;friendlyMessage&#x60; is localized via the &#x60;lang&#x60; query parameter or &#x60;Accept-Language&#x60; (28 languages, default &#x60;en&#x60;; unsupported values silently fall back).  **Sealed passports are immutable in place:** if &#x60;digitalSeal&#x60; is set the update is refused with **403** (message: \&quot;This passport is sealed and cannot be edited in place — editing would invalidate the eIDAS advanced electronic seal. Re-seal explicitly after any change.\&quot;).  **Facility:** omit &#x60;facilityId&#x60; to leave it unchanged; pass &#x60;null&#x60; or &#x60;\&quot;\&quot;&#x60; to detach; pass a facility UUID owned by your tenant to attach (400 if not found in your workspace).  **Enrichment:** include the &#x60;enrichment&#x60; key (even as &#x60;null&#x60;/&#x60;{}&#x60;) to overwrite the presentational marketing block; omit it to leave it unchanged. Values are sanitized server-side (truncated/sliced, http(s) URLs only), never rejected.  **Response caveat:** the returned &#x60;passport&#x60; document is serialized at the **public** redaction tier — &#x60;facilityDetails&#x60; (and battery restricted keys) appear as &#x60;\&quot;[REDACTED - Privileged Access Required]\&quot;&#x60; even though you are the owner.  **Rate limits:** global limiter, 100 req/min/IP.
   * @param id Passport UUID. &#x60;productId&#x60; aliasing is NOT supported here. (required)
   * @param passportUpdateRequest  (required)
   * @param lang Locale for &#x60;friendlyMessage&#x60; localization in validation errors. Falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. Unsupported values are ignored (no error). (optional)
   * @return PassportUpdateResponse
   * @throws ApiException if fails to make API call
   */
  public PassportUpdateResponse updatePassport(String id, PassportUpdateRequest passportUpdateRequest, String lang) throws ApiException {
    ApiResponse<PassportUpdateResponse> localVarResponse = updatePassportWithHttpInfo(id, passportUpdateRequest, lang);
    return localVarResponse.getData();
  }

  /**
   * Update passport metadata (versioned to history)
   * Replaces the passport&#39;s &#x60;metadata&#x60; (the Merkle root and leaf hashes are recomputed) and snapshots the **previous** metadata into the passport&#39;s version history (version &#x3D; count + 1, &#x60;changedBy&#x60; &#x3D; user email or &#x60;api-key:&lt;id&gt;&#x60;, &#x60;changeReason&#x60; defaults to &#x60;\&quot;API Update\&quot;&#x60;).  **Permission:** &#x60;passport:update&#x60; (write — subscription gating applies, see 402). Cookie sessions must send &#x60;X-CSRF-Token&#x60; (double-submit); Bearer/API-key clients are exempt.  **Lookup:** by passport **UUID only** — &#x60;productId&#x60; aliasing is NOT supported on this endpoint. The passport must belong to an operator bound to your workspace.  **Draft semantics (&#x60;draft&#x60; flag):** - &#x60;draft: true&#x60; **skips ESPR validation entirely** and forces &#x60;status: \&quot;DRAFT\&quot;&#x60; — note this also demotes an already-published (ACTIVE/RECALLED/DECOMMISSIONED) passport back to DRAFT. - &#x60;draft&#x60; absent/false: &#x60;metadata&#x60; is validated against the ESPR category rules (400 on failure — see below). If the passport was a DRAFT it is **published**: status becomes &#x60;ACTIVE&#x60;, a &#x60;passport.ingested&#x60; webhook is enqueued transactionally (public-redacted JSON-LD payload) and an in-app notification is created best-effort afterwards. Editing an already-published (live) passport leaves its status untouched and enqueues a &#x60;passport.updated&#x60; webhook instead (same public-redacted JSON-LD payload).  **Validation divergence:** the 400 validation body here contains &#x60;errors&#x60; but — unlike &#x60;POST /api/v1/passports&#x60; — **never a &#x60;warnings&#x60; array**. &#x60;friendlyMessage&#x60; is localized via the &#x60;lang&#x60; query parameter or &#x60;Accept-Language&#x60; (28 languages, default &#x60;en&#x60;; unsupported values silently fall back).  **Sealed passports are immutable in place:** if &#x60;digitalSeal&#x60; is set the update is refused with **403** (message: \&quot;This passport is sealed and cannot be edited in place — editing would invalidate the eIDAS advanced electronic seal. Re-seal explicitly after any change.\&quot;).  **Facility:** omit &#x60;facilityId&#x60; to leave it unchanged; pass &#x60;null&#x60; or &#x60;\&quot;\&quot;&#x60; to detach; pass a facility UUID owned by your tenant to attach (400 if not found in your workspace).  **Enrichment:** include the &#x60;enrichment&#x60; key (even as &#x60;null&#x60;/&#x60;{}&#x60;) to overwrite the presentational marketing block; omit it to leave it unchanged. Values are sanitized server-side (truncated/sliced, http(s) URLs only), never rejected.  **Response caveat:** the returned &#x60;passport&#x60; document is serialized at the **public** redaction tier — &#x60;facilityDetails&#x60; (and battery restricted keys) appear as &#x60;\&quot;[REDACTED - Privileged Access Required]\&quot;&#x60; even though you are the owner.  **Rate limits:** global limiter, 100 req/min/IP.
   * @param id Passport UUID. &#x60;productId&#x60; aliasing is NOT supported here. (required)
   * @param passportUpdateRequest  (required)
   * @param lang Locale for &#x60;friendlyMessage&#x60; localization in validation errors. Falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. Unsupported values are ignored (no error). (optional)
   * @return ApiResponse&lt;PassportUpdateResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportUpdateResponse> updatePassportWithHttpInfo(String id, PassportUpdateRequest passportUpdateRequest, String lang) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = updatePassportRequestBuilder(id, passportUpdateRequest, lang);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("updatePassport", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportUpdateResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportUpdateResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportUpdateResponse>() {})
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

  private HttpRequest.Builder updatePassportRequestBuilder(String id, PassportUpdateRequest passportUpdateRequest, String lang) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling updatePassport");
    }
    // verify the required parameter 'passportUpdateRequest' is set
    if (passportUpdateRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'passportUpdateRequest' when calling updatePassport");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "lang";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("lang", lang));

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

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(passportUpdateRequest);
      localVarRequestBuilder.method("PUT", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Transition passport lifecycle status (recall / decommission / reactivate)
   * Transitions a **published** passport between live lifecycle states. The request body carries only &#x60;status&#x60; (any other keys are ignored — there is no &#x60;reason&#x60; field; the history entry&#39;s change reason is auto-generated as &#x60;Status changed: &lt;from&gt; → &lt;to&gt;&#x60;).  **Permission:** &#x60;passport:update&#x60; (write — subscription gating applies, see 402). Cookie sessions must send &#x60;X-CSRF-Token&#x60;; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or &#x60;productId&#x60;** (UUID tried first), scoped to operators bound to your workspace.  **Effects:** - &#x60;DECOMMISSIONED&#x60; — sets &#x60;retentionUntil &#x3D; now + the configured retention period&#x60; (default 15 years), starting the minimum-availability retention clock. The passport stays publicly resolvable. - &#x60;ACTIVE&#x60; (reactivation) — clears &#x60;retentionUntil&#x60; **and** &#x60;archivedAt&#x60;. - &#x60;RECALLED&#x60; — marks the product recalled. - The status change, the version-history entry (who/when/what) and the webhook enqueue are **transactional**; an in-app notification is created **best-effort after the transaction commits** (a notification failure never affects the response).  **Webhooks:** &#x60;RECALLED&#x60; enqueues &#x60;passport.recalled&#x60;; any other transition (&#x60;DECOMMISSIONED&#x60;, reactivate-to-&#x60;ACTIVE&#x60;) enqueues &#x60;passport.status_updated&#x60; — note that &#x60;passport.status_updated&#x60; is **not** an explicitly subscribable event filter, so only wildcard (&#x60;\&quot;*\&quot;&#x60;) webhook subscriptions receive it. Payloads are the public-redacted JSON-LD document.  **Caveats:** DRAFT passports are refused with 409 (publish first via a validated &#x60;PUT /api/v1/passports/{id}&#x60;). Sealed passports CAN change status — &#x60;status&#x60; is stored alongside the document, not inside the sealed metadata Merkle tree. The returned &#x60;passport&#x60; document is serialized at the **public** redaction tier.  **Rate limits:** global limiter, 100 req/min/IP.
   * @param id Passport UUID **or** caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
   * @param passportStatusUpdateRequest  (required)
   * @return PassportStatusUpdateResponse
   * @throws ApiException if fails to make API call
   */
  public PassportStatusUpdateResponse updatePassportStatus(String id, PassportStatusUpdateRequest passportStatusUpdateRequest) throws ApiException {
    ApiResponse<PassportStatusUpdateResponse> localVarResponse = updatePassportStatusWithHttpInfo(id, passportStatusUpdateRequest);
    return localVarResponse.getData();
  }

  /**
   * Transition passport lifecycle status (recall / decommission / reactivate)
   * Transitions a **published** passport between live lifecycle states. The request body carries only &#x60;status&#x60; (any other keys are ignored — there is no &#x60;reason&#x60; field; the history entry&#39;s change reason is auto-generated as &#x60;Status changed: &lt;from&gt; → &lt;to&gt;&#x60;).  **Permission:** &#x60;passport:update&#x60; (write — subscription gating applies, see 402). Cookie sessions must send &#x60;X-CSRF-Token&#x60;; Bearer/API-key clients are exempt.  **Lookup:** passport **UUID or &#x60;productId&#x60;** (UUID tried first), scoped to operators bound to your workspace.  **Effects:** - &#x60;DECOMMISSIONED&#x60; — sets &#x60;retentionUntil &#x3D; now + the configured retention period&#x60; (default 15 years), starting the minimum-availability retention clock. The passport stays publicly resolvable. - &#x60;ACTIVE&#x60; (reactivation) — clears &#x60;retentionUntil&#x60; **and** &#x60;archivedAt&#x60;. - &#x60;RECALLED&#x60; — marks the product recalled. - The status change, the version-history entry (who/when/what) and the webhook enqueue are **transactional**; an in-app notification is created **best-effort after the transaction commits** (a notification failure never affects the response).  **Webhooks:** &#x60;RECALLED&#x60; enqueues &#x60;passport.recalled&#x60;; any other transition (&#x60;DECOMMISSIONED&#x60;, reactivate-to-&#x60;ACTIVE&#x60;) enqueues &#x60;passport.status_updated&#x60; — note that &#x60;passport.status_updated&#x60; is **not** an explicitly subscribable event filter, so only wildcard (&#x60;\&quot;*\&quot;&#x60;) webhook subscriptions receive it. Payloads are the public-redacted JSON-LD document.  **Caveats:** DRAFT passports are refused with 409 (publish first via a validated &#x60;PUT /api/v1/passports/{id}&#x60;). Sealed passports CAN change status — &#x60;status&#x60; is stored alongside the document, not inside the sealed metadata Merkle tree. The returned &#x60;passport&#x60; document is serialized at the **public** redaction tier.  **Rate limits:** global limiter, 100 req/min/IP.
   * @param id Passport UUID **or** caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). UUID is tried first. (required)
   * @param passportStatusUpdateRequest  (required)
   * @return ApiResponse&lt;PassportStatusUpdateResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportStatusUpdateResponse> updatePassportStatusWithHttpInfo(String id, PassportStatusUpdateRequest passportStatusUpdateRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = updatePassportStatusRequestBuilder(id, passportStatusUpdateRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("updatePassportStatus", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportStatusUpdateResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportStatusUpdateResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportStatusUpdateResponse>() {})
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

  private HttpRequest.Builder updatePassportStatusRequestBuilder(String id, PassportStatusUpdateRequest passportStatusUpdateRequest) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling updatePassportStatus");
    }
    // verify the required parameter 'passportStatusUpdateRequest' is set
    if (passportStatusUpdateRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'passportStatusUpdateRequest' when calling updatePassportStatus");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{id}/status"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(passportStatusUpdateRequest);
      localVarRequestBuilder.method("PUT", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Dry-run ESPR validation of passport metadata (nothing is stored)
   * Runs the full ESPR category schema validation on a metadata payload **without persisting anything** — intended for pre-flight checks in integration pipelines.  **Permission:** &#x60;passport:create&#x60; (Bearer API key or session JWT + CSRF for cookie sessions). Despite being read-only in effect, it is gated as a write permission, so subscription gating (**402**) applies.  **Rate limit:** global 100 requests/min per IP. **Body limit: 262,144 bytes (256 KiB)** → **413** beyond that.  **Behavioral caveats:** - The EPCIS **traceability lineage audit is NOT run** here (it only runs at real ingestion), so a payload can pass this dry-run and still fail &#x60;POST /api/v1/passports&#x60; on traceability errors. - &#x60;operatorId&#x60; is accepted by the body schema but **ignored** by the handler. - The 200 body always carries &#x60;errors: []&#x60;; &#x60;warnings&#x60; is **omitted entirely** when there are none (it is not an empty array). The same omission applies to &#x60;warnings&#x60; on the 400 Validation Failed body. - &#x60;friendlyMessage&#x60; localization via &#x60;?lang&#x3D;&#x60; / &#x60;Accept-Language&#x60; (28 languages, default &#x60;en&#x60;); category-validity errors (&#x60;metadata.category&#x60; missing or unknown) carry no &#x60;friendlyMessage&#x60;. - Structural rejections of the request body (e.g. missing &#x60;productId&#x60;, non-object &#x60;metadata&#x60;) and malformed JSON return just &#x60;{\&quot;error\&quot;: \&quot;Bad Request\&quot;, \&quot;message\&quot;: …}&#x60;; the structurally bad inputs that reach the handler are a whitespace-only &#x60;productId&#x60; and a malformed GTIN-14 &#x60;productId&#x60; (14 digits failing the GS1 mod-10 check), each answered with the fuller &#x60;Bad Request&#x60; body shown below.
   * @param passportValidateOnlyRequest  (required)
   * @param lang Locale for localized &#x60;friendlyMessage&#x60; validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return PassportValidateOnlyResult
   * @throws ApiException if fails to make API call
   */
  public PassportValidateOnlyResult validatePassport(PassportValidateOnlyRequest passportValidateOnlyRequest, String lang) throws ApiException {
    ApiResponse<PassportValidateOnlyResult> localVarResponse = validatePassportWithHttpInfo(passportValidateOnlyRequest, lang);
    return localVarResponse.getData();
  }

  /**
   * Dry-run ESPR validation of passport metadata (nothing is stored)
   * Runs the full ESPR category schema validation on a metadata payload **without persisting anything** — intended for pre-flight checks in integration pipelines.  **Permission:** &#x60;passport:create&#x60; (Bearer API key or session JWT + CSRF for cookie sessions). Despite being read-only in effect, it is gated as a write permission, so subscription gating (**402**) applies.  **Rate limit:** global 100 requests/min per IP. **Body limit: 262,144 bytes (256 KiB)** → **413** beyond that.  **Behavioral caveats:** - The EPCIS **traceability lineage audit is NOT run** here (it only runs at real ingestion), so a payload can pass this dry-run and still fail &#x60;POST /api/v1/passports&#x60; on traceability errors. - &#x60;operatorId&#x60; is accepted by the body schema but **ignored** by the handler. - The 200 body always carries &#x60;errors: []&#x60;; &#x60;warnings&#x60; is **omitted entirely** when there are none (it is not an empty array). The same omission applies to &#x60;warnings&#x60; on the 400 Validation Failed body. - &#x60;friendlyMessage&#x60; localization via &#x60;?lang&#x3D;&#x60; / &#x60;Accept-Language&#x60; (28 languages, default &#x60;en&#x60;); category-validity errors (&#x60;metadata.category&#x60; missing or unknown) carry no &#x60;friendlyMessage&#x60;. - Structural rejections of the request body (e.g. missing &#x60;productId&#x60;, non-object &#x60;metadata&#x60;) and malformed JSON return just &#x60;{\&quot;error\&quot;: \&quot;Bad Request\&quot;, \&quot;message\&quot;: …}&#x60;; the structurally bad inputs that reach the handler are a whitespace-only &#x60;productId&#x60; and a malformed GTIN-14 &#x60;productId&#x60; (14 digits failing the GS1 mod-10 check), each answered with the fuller &#x60;Bad Request&#x60; body shown below.
   * @param passportValidateOnlyRequest  (required)
   * @param lang Locale for localized &#x60;friendlyMessage&#x60; validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return ApiResponse&lt;PassportValidateOnlyResult&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportValidateOnlyResult> validatePassportWithHttpInfo(PassportValidateOnlyRequest passportValidateOnlyRequest, String lang) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = validatePassportRequestBuilder(passportValidateOnlyRequest, lang);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("validatePassport", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportValidateOnlyResult>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportValidateOnlyResult>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportValidateOnlyResult>() {})
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

  private HttpRequest.Builder validatePassportRequestBuilder(PassportValidateOnlyRequest passportValidateOnlyRequest, String lang) throws ApiException {
    // verify the required parameter 'passportValidateOnlyRequest' is set
    if (passportValidateOnlyRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'passportValidateOnlyRequest' when calling validatePassport");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/validate-only";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "lang";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("lang", lang));

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

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(passportValidateOnlyRequest);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

  /**
   * Public dry-run ESPR metadata validation (strictly rate-limited)
   * Identical validation semantics to &#x60;POST /api/v1/passports/validate-only&#x60;, but **fully public — no authentication of any kind**, intended for try-before-you-buy schema checks. Nothing is persisted.  **Rate limit: 10 requests/min per IP** — a strict per-route limit that **replaces** the global 100/min for this endpoint (emits &#x60;x-ratelimit-limit&#x60; / &#x60;x-ratelimit-remaining&#x60; / &#x60;x-ratelimit-reset&#x60; headers and &#x60;retry-after&#x60; on 429). **Body limit: 65,536 bytes (64 KiB)** → **413** beyond that. These caps exist because the endpoint runs the full validation engine unauthenticated (DoS mitigation).  **Behavioral caveats:** - No tenant context: the EPCIS traceability lineage audit is **not** run, and &#x60;operatorId&#x60; is accepted but ignored. - The 200 body always carries &#x60;errors: []&#x60;; &#x60;warnings&#x60; is omitted entirely when there are none (same omission on the 400 Validation Failed body). - Error/warning &#x60;friendlyMessage&#x60; localization via &#x60;?lang&#x3D;&#x60; / &#x60;Accept-Language&#x60; (28 languages, default &#x60;en&#x60;); category-validity errors carry no &#x60;friendlyMessage&#x60;. - Structural rejections of the request body (e.g. missing &#x60;productId&#x60;) and malformed JSON return just &#x60;{\&quot;error\&quot;: \&quot;Bad Request\&quot;, \&quot;message\&quot;: …}&#x60;; a whitespace-only &#x60;productId&#x60; or a malformed GTIN-14 &#x60;productId&#x60; (14 digits failing the GS1 mod-10 check) gets the fuller &#x60;Bad Request&#x60; body shown below.
   * @param passportValidateOnlyRequest  (required)
   * @param lang Locale for localized &#x60;friendlyMessage&#x60; validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return PassportValidateOnlyResult
   * @throws ApiException if fails to make API call
   */
  public PassportValidateOnlyResult validatePassportPublic(PassportValidateOnlyRequest passportValidateOnlyRequest, String lang) throws ApiException {
    ApiResponse<PassportValidateOnlyResult> localVarResponse = validatePassportPublicWithHttpInfo(passportValidateOnlyRequest, lang);
    return localVarResponse.getData();
  }

  /**
   * Public dry-run ESPR metadata validation (strictly rate-limited)
   * Identical validation semantics to &#x60;POST /api/v1/passports/validate-only&#x60;, but **fully public — no authentication of any kind**, intended for try-before-you-buy schema checks. Nothing is persisted.  **Rate limit: 10 requests/min per IP** — a strict per-route limit that **replaces** the global 100/min for this endpoint (emits &#x60;x-ratelimit-limit&#x60; / &#x60;x-ratelimit-remaining&#x60; / &#x60;x-ratelimit-reset&#x60; headers and &#x60;retry-after&#x60; on 429). **Body limit: 65,536 bytes (64 KiB)** → **413** beyond that. These caps exist because the endpoint runs the full validation engine unauthenticated (DoS mitigation).  **Behavioral caveats:** - No tenant context: the EPCIS traceability lineage audit is **not** run, and &#x60;operatorId&#x60; is accepted but ignored. - The 200 body always carries &#x60;errors: []&#x60;; &#x60;warnings&#x60; is omitted entirely when there are none (same omission on the 400 Validation Failed body). - Error/warning &#x60;friendlyMessage&#x60; localization via &#x60;?lang&#x3D;&#x60; / &#x60;Accept-Language&#x60; (28 languages, default &#x60;en&#x60;); category-validity errors carry no &#x60;friendlyMessage&#x60;. - Structural rejections of the request body (e.g. missing &#x60;productId&#x60;) and malformed JSON return just &#x60;{\&quot;error\&quot;: \&quot;Bad Request\&quot;, \&quot;message\&quot;: …}&#x60;; a whitespace-only &#x60;productId&#x60; or a malformed GTIN-14 &#x60;productId&#x60; (14 digits failing the GS1 mod-10 check) gets the fuller &#x60;Bad Request&#x60; body shown below.
   * @param passportValidateOnlyRequest  (required)
   * @param lang Locale for localized &#x60;friendlyMessage&#x60; validation texts (en, bg, hr, cs, da, nl, et, fi, fr, de, el, hu, ga, it, lv, lt, mt, pl, pt, ro, sk, sl, es, sv, no, is, uk, tr). Unknown values are ignored; falls back to &#x60;Accept-Language&#x60;, then &#x60;en&#x60;. (optional)
   * @return ApiResponse&lt;PassportValidateOnlyResult&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PassportValidateOnlyResult> validatePassportPublicWithHttpInfo(PassportValidateOnlyRequest passportValidateOnlyRequest, String lang) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = validatePassportPublicRequestBuilder(passportValidateOnlyRequest, lang);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("validatePassportPublic", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<PassportValidateOnlyResult>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<PassportValidateOnlyResult>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<PassportValidateOnlyResult>() {})
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

  private HttpRequest.Builder validatePassportPublicRequestBuilder(PassportValidateOnlyRequest passportValidateOnlyRequest, String lang) throws ApiException {
    // verify the required parameter 'passportValidateOnlyRequest' is set
    if (passportValidateOnlyRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'passportValidateOnlyRequest' when calling validatePassportPublic");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/validate-only-public";

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "lang";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("lang", lang));

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

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(passportValidateOnlyRequest);
      localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }

}
