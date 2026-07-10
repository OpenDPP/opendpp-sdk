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

import eu.opendppnode.sdk.model.EpcisCaptureResponse;
import eu.opendppnode.sdk.model.EpcisDocument;
import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.PassportQuotaError;
import eu.opendppnode.sdk.model.SealVerifyRequest;
import eu.opendppnode.sdk.model.SealVerifyResponse;
import eu.opendppnode.sdk.model.TraceComplianceAuditResponse;
import eu.opendppnode.sdk.model.TraceEventRegistered;
import eu.opendppnode.sdk.model.TraceLineageResponse;
import eu.opendppnode.sdk.model.UntpEventCredential;
import eu.opendppnode.sdk.model.VerifyPassportSeal400Response;

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
public class TraceabilityAuditApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public TraceabilityAuditApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TraceabilityAuditApi(ApiClient apiClient) {
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
   * Run heuristic UFLPA/EUDR compliance screening over an event&#39;s lineage
   * Walks the same upstream lineage DAG as &#x60;GET /api/v1/events/{id}/lineage&#x60; and screens every node&#39;s location data against two heuristic rules:  - **UFLPA** — flags any node whose &#x60;bizLocation&#x60; starts with &#x60;CN-65&#x60; (ISO 3166-2 Xinjiang), contains the keyword &#x60;XINJIANG&#x60; (case-insensitive), or whose &#x60;readPoint&#x60; contains the coordinate pair &#x60;43.8256,87.6168&#x60;. - **EUDR** — flags any node whose &#x60;readPoint&#x60; parses as &#x60;geo:&lt;lat&gt;,&lt;lng&gt;&#x60; (or bare &#x60;&lt;lat&gt;,&lt;lng&gt;&#x60;) coordinates inside the sample deforestation polygon lat −5.0…−3.0, lng −65.0…−60.0.  These are geographic screening heuristics evaluated against the data registered on this node — not a legal compliance determination.  **Permission:** &#x60;passport:read&#x60; (a read permission despite the POST verb — no subscription gating). Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header; Bearer clients are exempt. Tenant scoping and the &#x60;SUPER_ADMIN&#x60; bypass are identical to the lineage endpoint. **No request body is read** — send an empty body (an empty or absent JSON body is accepted).  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  When zero violations are found, the response embeds a &#x60;TraceabilityComplianceCertificate&#x60; object (status &#x60;SCREENED_NO_MATCHES&#x60;, screens &#x60;OpenDPP-EUDR-heuristic&#x60; / &#x60;OpenDPP-UFLPA-screen&#x60;); otherwise &#x60;certificate&#x60; is &#x60;null&#x60; and &#x60;errors&#x60; lists each violation as a human-readable string. ANY failure — unknown event id, other-tenant id, or even a circular lineage graph — is reported as the same generic 404 body.
   * @param id EPCIS event id — the server-generated UUID returned as &#x60;eventId&#x60; by &#x60;POST /api/v1/events&#x60;. Used as the root of the audited lineage DAG. (required)
   * @return TraceComplianceAuditResponse
   * @throws ApiException if fails to make API call
   */
  public TraceComplianceAuditResponse auditEventLineage(String id) throws ApiException {
    ApiResponse<TraceComplianceAuditResponse> localVarResponse = auditEventLineageWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Run heuristic UFLPA/EUDR compliance screening over an event&#39;s lineage
   * Walks the same upstream lineage DAG as &#x60;GET /api/v1/events/{id}/lineage&#x60; and screens every node&#39;s location data against two heuristic rules:  - **UFLPA** — flags any node whose &#x60;bizLocation&#x60; starts with &#x60;CN-65&#x60; (ISO 3166-2 Xinjiang), contains the keyword &#x60;XINJIANG&#x60; (case-insensitive), or whose &#x60;readPoint&#x60; contains the coordinate pair &#x60;43.8256,87.6168&#x60;. - **EUDR** — flags any node whose &#x60;readPoint&#x60; parses as &#x60;geo:&lt;lat&gt;,&lt;lng&gt;&#x60; (or bare &#x60;&lt;lat&gt;,&lt;lng&gt;&#x60;) coordinates inside the sample deforestation polygon lat −5.0…−3.0, lng −65.0…−60.0.  These are geographic screening heuristics evaluated against the data registered on this node — not a legal compliance determination.  **Permission:** &#x60;passport:read&#x60; (a read permission despite the POST verb — no subscription gating). Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header; Bearer clients are exempt. Tenant scoping and the &#x60;SUPER_ADMIN&#x60; bypass are identical to the lineage endpoint. **No request body is read** — send an empty body (an empty or absent JSON body is accepted).  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  When zero violations are found, the response embeds a &#x60;TraceabilityComplianceCertificate&#x60; object (status &#x60;SCREENED_NO_MATCHES&#x60;, screens &#x60;OpenDPP-EUDR-heuristic&#x60; / &#x60;OpenDPP-UFLPA-screen&#x60;); otherwise &#x60;certificate&#x60; is &#x60;null&#x60; and &#x60;errors&#x60; lists each violation as a human-readable string. ANY failure — unknown event id, other-tenant id, or even a circular lineage graph — is reported as the same generic 404 body.
   * @param id EPCIS event id — the server-generated UUID returned as &#x60;eventId&#x60; by &#x60;POST /api/v1/events&#x60;. Used as the root of the audited lineage DAG. (required)
   * @return ApiResponse&lt;TraceComplianceAuditResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<TraceComplianceAuditResponse> auditEventLineageWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = auditEventLineageRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("auditEventLineage", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<TraceComplianceAuditResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<TraceComplianceAuditResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<TraceComplianceAuditResponse>() {})
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

  private HttpRequest.Builder auditEventLineageRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling auditEventLineage");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/events/{id}/audit"
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
   * Capture a native GS1 EPCIS 2.0 document (JSON/JSON-LD)
   * Captures a native **GS1 EPCIS 2.0 document** — the standard&#39;s own JSON/JSON-LD interchange format — and persists each supported event as an EPCIS event row scoped to your tenant, alongside the VC-shaped &#x60;POST /api/v1/events&#x60; path. Send the document exactly as your EPCIS infrastructure produces it.  **Permission:** &#x60;passport:update&#x60; (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy &#x60;REQUIRED&#x60;, or &#x60;DEFAULT&#x60; with the workspace&#39;s MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header (double-submit with the &#x60;opendpp_csrf&#x60; cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  **Validation:** the WHOLE document is validated against the official GS1 EPCIS **2.0.1** JSON Schema (vendored and pinned on the node) before any event is stored — a non-conformant document is rejected 400 with the first few schema violations under &#x60;errors[]&#x60;. Notable rules the OFFICIAL schema enforces: &#x60;@context&#x60; and &#x60;creationDate&#x60; are required; &#x60;bizStep&#x60;/&#x60;disposition&#x60; must use the CBV **short names** (e.g. &#x60;commissioning&#x60;, &#x60;in_transit&#x60;) or a custom (non-CBV) URI — the legacy &#x60;urn:epcglobal:cbv:*&#x60; URN form is REJECTED by the standard&#39;s schema; &#x60;action&#x60; is forbidden on &#x60;TransformationEvent&#x60;; &#x60;readPoint&#x60;/&#x60;bizLocation&#x60; carry &#x60;{id: &lt;uri&gt;}&#x60;. Only &#x60;type: \&quot;EPCISDocument\&quot;&#x60; is accepted (no &#x60;EPCISQueryDocument&#x60;, no bare events), and &#x60;epcisBody.eventList&#x60; must be non-empty.  **Per-event capture (partial success):** events are processed independently and the 201 response reports &#x60;results[]&#x60; (captured) and &#x60;errors[]&#x60; (rejected) by &#x60;index&#x60;. An event is rejected — never silently dropped — when its type is outside this node&#39;s traceability model (&#x60;ObjectEvent&#x60;, &#x60;AggregationEvent&#x60;, &#x60;TransformationEvent&#x60;, &#x60;AssociationEvent&#x60; are supported; &#x60;TransactionEvent&#x60; is not) or when it identifies stock ONLY by quantity lists (no &#x60;epcList&#x60;/&#x60;parentID&#x60;/&#x60;childEPCs&#x60;/&#x60;inputEPCList&#x60;/&#x60;outputEPCList&#x60; — nothing EPC-identified would remain to trace). If EVERY event is rejected the response is 400 &#x60;No Events Captured&#x60; with the same &#x60;errors[]&#x60;.  **Fidelity disclosure:** recognized EPCIS fields the node does not persist (&#x60;eventID&#x60;, quantity lists, &#x60;sensorElementList&#x60;, &#x60;bizTransactionList&#x60;, &#x60;sourceList&#x60;/&#x60;destinationList&#x60;, &#x60;persistentDisposition&#x60;, &#x60;errorDeclaration&#x60;, &#x60;ilmd&#x60;, custom extension fields, …) are listed per event under &#x60;results[].ignoredFields&#x60; instead of being silently discarded.  **Persistence:** row ids are ALWAYS server-generated (UUID) — a client-supplied &#x60;eventID&#x60; is never adopted as the primary key (it is disclosed under &#x60;ignoredFields&#x60;); CBV short names are normalized to the node&#39;s stored URN form (&#x60;urn:epcglobal:cbv:bizstep:*&#x60; / &#x60;urn:epcglobal:cbv:disp:*&#x60;, the same form the VC-shaped path stores and the lineage projection reads); defaults when absent: &#x60;bizStep&#x60; → &#x60;receiving&#x60;, &#x60;disposition&#x60; → &#x60;in_progress&#x60;. Rows captured on this path carry **no per-event credential** and are stored with &#x60;isUntpCompliant: false&#x60; (API-key provenance only) — they are never presented as UNTP-verified. This endpoint does not create lineage edges between events.
   * @param epcisDocument  (required)
   * @return EpcisCaptureResponse
   * @throws ApiException if fails to make API call
   */
  public EpcisCaptureResponse captureEpcisDocument(EpcisDocument epcisDocument) throws ApiException {
    ApiResponse<EpcisCaptureResponse> localVarResponse = captureEpcisDocumentWithHttpInfo(epcisDocument);
    return localVarResponse.getData();
  }

  /**
   * Capture a native GS1 EPCIS 2.0 document (JSON/JSON-LD)
   * Captures a native **GS1 EPCIS 2.0 document** — the standard&#39;s own JSON/JSON-LD interchange format — and persists each supported event as an EPCIS event row scoped to your tenant, alongside the VC-shaped &#x60;POST /api/v1/events&#x60; path. Send the document exactly as your EPCIS infrastructure produces it.  **Permission:** &#x60;passport:update&#x60; (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy &#x60;REQUIRED&#x60;, or &#x60;DEFAULT&#x60; with the workspace&#39;s MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header (double-submit with the &#x60;opendpp_csrf&#x60; cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  **Validation:** the WHOLE document is validated against the official GS1 EPCIS **2.0.1** JSON Schema (vendored and pinned on the node) before any event is stored — a non-conformant document is rejected 400 with the first few schema violations under &#x60;errors[]&#x60;. Notable rules the OFFICIAL schema enforces: &#x60;@context&#x60; and &#x60;creationDate&#x60; are required; &#x60;bizStep&#x60;/&#x60;disposition&#x60; must use the CBV **short names** (e.g. &#x60;commissioning&#x60;, &#x60;in_transit&#x60;) or a custom (non-CBV) URI — the legacy &#x60;urn:epcglobal:cbv:*&#x60; URN form is REJECTED by the standard&#39;s schema; &#x60;action&#x60; is forbidden on &#x60;TransformationEvent&#x60;; &#x60;readPoint&#x60;/&#x60;bizLocation&#x60; carry &#x60;{id: &lt;uri&gt;}&#x60;. Only &#x60;type: \&quot;EPCISDocument\&quot;&#x60; is accepted (no &#x60;EPCISQueryDocument&#x60;, no bare events), and &#x60;epcisBody.eventList&#x60; must be non-empty.  **Per-event capture (partial success):** events are processed independently and the 201 response reports &#x60;results[]&#x60; (captured) and &#x60;errors[]&#x60; (rejected) by &#x60;index&#x60;. An event is rejected — never silently dropped — when its type is outside this node&#39;s traceability model (&#x60;ObjectEvent&#x60;, &#x60;AggregationEvent&#x60;, &#x60;TransformationEvent&#x60;, &#x60;AssociationEvent&#x60; are supported; &#x60;TransactionEvent&#x60; is not) or when it identifies stock ONLY by quantity lists (no &#x60;epcList&#x60;/&#x60;parentID&#x60;/&#x60;childEPCs&#x60;/&#x60;inputEPCList&#x60;/&#x60;outputEPCList&#x60; — nothing EPC-identified would remain to trace). If EVERY event is rejected the response is 400 &#x60;No Events Captured&#x60; with the same &#x60;errors[]&#x60;.  **Fidelity disclosure:** recognized EPCIS fields the node does not persist (&#x60;eventID&#x60;, quantity lists, &#x60;sensorElementList&#x60;, &#x60;bizTransactionList&#x60;, &#x60;sourceList&#x60;/&#x60;destinationList&#x60;, &#x60;persistentDisposition&#x60;, &#x60;errorDeclaration&#x60;, &#x60;ilmd&#x60;, custom extension fields, …) are listed per event under &#x60;results[].ignoredFields&#x60; instead of being silently discarded.  **Persistence:** row ids are ALWAYS server-generated (UUID) — a client-supplied &#x60;eventID&#x60; is never adopted as the primary key (it is disclosed under &#x60;ignoredFields&#x60;); CBV short names are normalized to the node&#39;s stored URN form (&#x60;urn:epcglobal:cbv:bizstep:*&#x60; / &#x60;urn:epcglobal:cbv:disp:*&#x60;, the same form the VC-shaped path stores and the lineage projection reads); defaults when absent: &#x60;bizStep&#x60; → &#x60;receiving&#x60;, &#x60;disposition&#x60; → &#x60;in_progress&#x60;. Rows captured on this path carry **no per-event credential** and are stored with &#x60;isUntpCompliant: false&#x60; (API-key provenance only) — they are never presented as UNTP-verified. This endpoint does not create lineage edges between events.
   * @param epcisDocument  (required)
   * @return ApiResponse&lt;EpcisCaptureResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<EpcisCaptureResponse> captureEpcisDocumentWithHttpInfo(EpcisDocument epcisDocument) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = captureEpcisDocumentRequestBuilder(epcisDocument);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("captureEpcisDocument", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<EpcisCaptureResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<EpcisCaptureResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<EpcisCaptureResponse>() {})
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

  private HttpRequest.Builder captureEpcisDocumentRequestBuilder(EpcisDocument epcisDocument) throws ApiException {
    // verify the required parameter 'epcisDocument' is set
    if (epcisDocument == null) {
      throw new ApiException(400, "Missing the required parameter 'epcisDocument' when calling captureEpcisDocument");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/events/epcis";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(epcisDocument);
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
   * Retrieve the upstream pedigree of an event as a recursive lineage DAG
   * Returns the full upstream pedigree of a traceability event as a recursive Directed Acyclic Graph: the root event plus, in &#x60;parents&#x60;, every event linked upstream through lineage relations registered on the node, walked transitively (parents of parents). A shared ancestor reached through multiple downstream paths is repeated under EACH path — the DAG is expanded into a tree in the response, not deduplicated; only a true cycle aborts the walk (400).  **Permission:** &#x60;passport:read&#x60;. Every node in the walk — the root AND each upstream parent — is scoped to the caller&#39;s tenant; an event belonging to another tenant is invisible and the request fails with 404 (no cross-tenant pedigree reads). Sessions with the &#x60;SUPER_ADMIN&#x60; role are exempt from tenant scoping.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  **Caveats:** if the lineage graph contains a circular reference the walk aborts with 400. Any other failure (unknown id, other-tenant id, missing parent) is reported as the same deliberately generic 404 body. &#x60;eventTime&#x60; is serialized as ISO 8601 UTC; &#x60;epcs&#x60; is parsed from the stored EPC list (a non-array value degrades to &#x60;[]&#x60;); &#x60;location&#x60; mirrors the stored &#x60;bizLocation&#x60;.  **Content negotiation (EPCIS projection):** &#x60;Accept: application/ld+json&#x60; returns the SAME tenant-scoped lineage as a native **GS1 EPCIS 2.0 document** (&#x60;EPCISDocument&#x60; with the walk&#39;s events under &#x60;epcisBody.eventList&#x60;, ordered by &#x60;eventTime&#x60;, bounded to 500 events) instead of the recursive JSON tree — another projection of the same canonical rows, mirroring the AAS/VC Accept-header pattern on the passport resolution routes. Emitted documents use the official CBV short names (stored &#x60;urn:epcglobal:cbv:*&#x60; values are mapped back), expose row ids as &#x60;eventID&#x60; URNs (&#x60;urn:uuid:*&#x60;, or &#x60;urn:opendpp:event:*&#x60; for non-UUID ids), and wrap non-URI stored locations as &#x60;urn:opendpp:location:*&#x60;.
   * @param id EPCIS event id — the server-generated UUID returned as &#x60;eventId&#x60; by &#x60;POST /api/v1/events&#x60;. (required)
   * @return TraceLineageResponse
   * @throws ApiException if fails to make API call
   */
  public TraceLineageResponse getEventLineage(String id) throws ApiException {
    ApiResponse<TraceLineageResponse> localVarResponse = getEventLineageWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Retrieve the upstream pedigree of an event as a recursive lineage DAG
   * Returns the full upstream pedigree of a traceability event as a recursive Directed Acyclic Graph: the root event plus, in &#x60;parents&#x60;, every event linked upstream through lineage relations registered on the node, walked transitively (parents of parents). A shared ancestor reached through multiple downstream paths is repeated under EACH path — the DAG is expanded into a tree in the response, not deduplicated; only a true cycle aborts the walk (400).  **Permission:** &#x60;passport:read&#x60;. Every node in the walk — the root AND each upstream parent — is scoped to the caller&#39;s tenant; an event belonging to another tenant is invisible and the request fails with 404 (no cross-tenant pedigree reads). Sessions with the &#x60;SUPER_ADMIN&#x60; role are exempt from tenant scoping.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  **Caveats:** if the lineage graph contains a circular reference the walk aborts with 400. Any other failure (unknown id, other-tenant id, missing parent) is reported as the same deliberately generic 404 body. &#x60;eventTime&#x60; is serialized as ISO 8601 UTC; &#x60;epcs&#x60; is parsed from the stored EPC list (a non-array value degrades to &#x60;[]&#x60;); &#x60;location&#x60; mirrors the stored &#x60;bizLocation&#x60;.  **Content negotiation (EPCIS projection):** &#x60;Accept: application/ld+json&#x60; returns the SAME tenant-scoped lineage as a native **GS1 EPCIS 2.0 document** (&#x60;EPCISDocument&#x60; with the walk&#39;s events under &#x60;epcisBody.eventList&#x60;, ordered by &#x60;eventTime&#x60;, bounded to 500 events) instead of the recursive JSON tree — another projection of the same canonical rows, mirroring the AAS/VC Accept-header pattern on the passport resolution routes. Emitted documents use the official CBV short names (stored &#x60;urn:epcglobal:cbv:*&#x60; values are mapped back), expose row ids as &#x60;eventID&#x60; URNs (&#x60;urn:uuid:*&#x60;, or &#x60;urn:opendpp:event:*&#x60; for non-UUID ids), and wrap non-URI stored locations as &#x60;urn:opendpp:location:*&#x60;.
   * @param id EPCIS event id — the server-generated UUID returned as &#x60;eventId&#x60; by &#x60;POST /api/v1/events&#x60;. (required)
   * @return ApiResponse&lt;TraceLineageResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<TraceLineageResponse> getEventLineageWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getEventLineageRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getEventLineage", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<TraceLineageResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<TraceLineageResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<TraceLineageResponse>() {})
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

  private HttpRequest.Builder getEventLineageRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getEventLineage");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/events/{id}/lineage"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
   * Register a UNTP/EPCIS 2.0 traceability event (VC-shaped)
   * Registers a supply-chain traceability event carried as a VC-shaped UNTP credential and persists it as an EPCIS 2.0 event row scoped to your tenant.  **Permission:** &#x60;passport:update&#x60; (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy &#x60;REQUIRED&#x60;, or &#x60;DEFAULT&#x60; with the workspace&#39;s MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header (double-submit with the &#x60;opendpp_csrf&#x60; cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  **Validation pipeline (in order):** 1. *Structural* — the body must be an object containing &#x60;credentialSubject&#x60;, otherwise 400 &#x60;Bad Request&#x60;. 2. *EPCIS rule* — &#x60;action&#x60; is strictly forbidden on &#x60;TransformationEvent&#x60; (any non-null value → 400 &#x60;Schema Validation Error&#x60;). 3. *Cryptographic* — the credential&#39;s &#x60;proof&#x60; MUST be a conformant W3C &#x60;DataIntegrityProof&#x60; with &#x60;cryptosuite: \&quot;ecdsa-jcs-2019\&quot;&#x60; and a multibase base58btc (&#x60;z…&#x60;) &#x60;proofValue&#x60;; any other proof shape (e.g. the legacy key-sorted &#x60;MerkleTreeAttestationProof&#x60;) is rejected. The ECDSA P-256 signature is verified per that cryptosuite over &#x60;sha256(JCS(proof options)) ‖ sha256(JCS(credential without proof))&#x60; — RFC 8785 JCS canonicalization, IEEE-P1363 raw r‖s — a conformant, interoperable Data Integrity suite, which is what makes the persisted &#x60;isUntpCompliant: true&#x60; honest. The verification key is resolved in trust order: (a) an embedded &#x60;proof.verificationMethod.x5c&#x60; chain, accepted ONLY when the node has eIDAS trust anchors configured, the chain validates against them, every certificate is currently valid, and the leaf attests the issuer; (b) ALL of the authoritative vault keys (current + retired, so a pre-rotation credential still verifies) of the tenant whose UNIQUE subdomain EXACTLY equals the trailing &#x60;:&#x60;-segment of the issuer DID. If no key resolves or the signature does not verify → 400 &#x60;Cryptographic Verification Failed&#x60;. 4. *Operator scoping* — if your API key is scoped to an Economic Operator, the credential&#39;s declared operator DID — the &#x60;issuer&#x60; DID, or &#x60;credentialSubject.responsibleOperatorDid&#x60; only when &#x60;issuer&#x60; is absent — must contain the bound operator&#39;s registration id (e.g. &#x60;EU-DEFAULT-001&#x60;), otherwise 403 with &#x60;message: \&quot;Your access is restricted to Economic Operator: &lt;operatorId&gt; (&lt;regId&gt;)\&quot;&#x60;.  **Persistence:** the stored event id is ALWAYS server-generated (UUID) — the credential&#39;s own &#x60;id&#x60; is never used as the primary key (prevents cross-tenant id squatting); the issuer DID is retained as &#x60;issuerDid&#x60;. Defaults applied on write: &#x60;bizStep&#x60; → &#x60;urn:epcglobal:cbv:bizstep:receiving&#x60;; &#x60;disposition&#x60; → &#x60;urn:epcglobal:cbv:disp:in_progress&#x60;; &#x60;readPoint&#x60; → &#x60;geo:&lt;latitude&gt;,&lt;longitude&gt;&#x60; derived from &#x60;credentialSubject.originLocation&#x60; when present; &#x60;bizLocation&#x60; → &#x60;responsibleOperatorDid&#x60;; &#x60;eventTime&#x60; → &#x60;issuanceDate&#x60;, else the server clock; &#x60;epcList&#x60; → &#x60;[credentialSubject.id]&#x60; when not supplied as an array (or &#x60;[]&#x60;). The row is stored with &#x60;isUntpCompliant: true&#x60; and the &#x60;proof.proofValue&#x60; retained.  **Caveats:** &#x60;credentialSubject.eventType&#x60; must be one of the documented event-type values and &#x60;action&#x60; (when present) one of &#x60;ADD&#x60;/&#x60;OBSERVE&#x60;/&#x60;DELETE&#x60; — both map to server-side enums, and a missing or unknown value is only rejected at the persistence layer and surfaces as the 500 &#x60;Database Persistence Failed&#x60; body, not as a 400. Note the 201 envelope is &#x60;{status: \&quot;success\&quot;, ...}&#x60;, NOT the usual &#x60;{success: true, ...}&#x60; shape. This endpoint does not create lineage edges between events; the lineage DAG read by &#x60;GET /api/v1/events/{id}/lineage&#x60; is built from lineage relations maintained separately on the node.
   * @param untpEventCredential  (required)
   * @return TraceEventRegistered
   * @throws ApiException if fails to make API call
   */
  public TraceEventRegistered registerTraceabilityEvent(UntpEventCredential untpEventCredential) throws ApiException {
    ApiResponse<TraceEventRegistered> localVarResponse = registerTraceabilityEventWithHttpInfo(untpEventCredential);
    return localVarResponse.getData();
  }

  /**
   * Register a UNTP/EPCIS 2.0 traceability event (VC-shaped)
   * Registers a supply-chain traceability event carried as a VC-shaped UNTP credential and persists it as an EPCIS 2.0 event row scoped to your tenant.  **Permission:** &#x60;passport:update&#x60; (write operation — subscription gating applies, see 402). When the node operator enforces MFA, writes from user-backed sessions (cookie or Bearer JWT) whose MFA policy requires a second factor (user policy &#x60;REQUIRED&#x60;, or &#x60;DEFAULT&#x60; with the workspace&#39;s MFA-by-default setting, which is on by default) receive 403 without one; API-key clients are exempt. Cookie-session clients must send the &#x60;X-CSRF-Token&#x60; header (double-submit with the &#x60;opendpp_csrf&#x60; cookie); Bearer JWT / API-key clients are exempt.  **Rate limit:** global limiter, 100 requests/min per IP (standard &#x60;x-ratelimit-*&#x60; headers).  **Validation pipeline (in order):** 1. *Structural* — the body must be an object containing &#x60;credentialSubject&#x60;, otherwise 400 &#x60;Bad Request&#x60;. 2. *EPCIS rule* — &#x60;action&#x60; is strictly forbidden on &#x60;TransformationEvent&#x60; (any non-null value → 400 &#x60;Schema Validation Error&#x60;). 3. *Cryptographic* — the credential&#39;s &#x60;proof&#x60; MUST be a conformant W3C &#x60;DataIntegrityProof&#x60; with &#x60;cryptosuite: \&quot;ecdsa-jcs-2019\&quot;&#x60; and a multibase base58btc (&#x60;z…&#x60;) &#x60;proofValue&#x60;; any other proof shape (e.g. the legacy key-sorted &#x60;MerkleTreeAttestationProof&#x60;) is rejected. The ECDSA P-256 signature is verified per that cryptosuite over &#x60;sha256(JCS(proof options)) ‖ sha256(JCS(credential without proof))&#x60; — RFC 8785 JCS canonicalization, IEEE-P1363 raw r‖s — a conformant, interoperable Data Integrity suite, which is what makes the persisted &#x60;isUntpCompliant: true&#x60; honest. The verification key is resolved in trust order: (a) an embedded &#x60;proof.verificationMethod.x5c&#x60; chain, accepted ONLY when the node has eIDAS trust anchors configured, the chain validates against them, every certificate is currently valid, and the leaf attests the issuer; (b) ALL of the authoritative vault keys (current + retired, so a pre-rotation credential still verifies) of the tenant whose UNIQUE subdomain EXACTLY equals the trailing &#x60;:&#x60;-segment of the issuer DID. If no key resolves or the signature does not verify → 400 &#x60;Cryptographic Verification Failed&#x60;. 4. *Operator scoping* — if your API key is scoped to an Economic Operator, the credential&#39;s declared operator DID — the &#x60;issuer&#x60; DID, or &#x60;credentialSubject.responsibleOperatorDid&#x60; only when &#x60;issuer&#x60; is absent — must contain the bound operator&#39;s registration id (e.g. &#x60;EU-DEFAULT-001&#x60;), otherwise 403 with &#x60;message: \&quot;Your access is restricted to Economic Operator: &lt;operatorId&gt; (&lt;regId&gt;)\&quot;&#x60;.  **Persistence:** the stored event id is ALWAYS server-generated (UUID) — the credential&#39;s own &#x60;id&#x60; is never used as the primary key (prevents cross-tenant id squatting); the issuer DID is retained as &#x60;issuerDid&#x60;. Defaults applied on write: &#x60;bizStep&#x60; → &#x60;urn:epcglobal:cbv:bizstep:receiving&#x60;; &#x60;disposition&#x60; → &#x60;urn:epcglobal:cbv:disp:in_progress&#x60;; &#x60;readPoint&#x60; → &#x60;geo:&lt;latitude&gt;,&lt;longitude&gt;&#x60; derived from &#x60;credentialSubject.originLocation&#x60; when present; &#x60;bizLocation&#x60; → &#x60;responsibleOperatorDid&#x60;; &#x60;eventTime&#x60; → &#x60;issuanceDate&#x60;, else the server clock; &#x60;epcList&#x60; → &#x60;[credentialSubject.id]&#x60; when not supplied as an array (or &#x60;[]&#x60;). The row is stored with &#x60;isUntpCompliant: true&#x60; and the &#x60;proof.proofValue&#x60; retained.  **Caveats:** &#x60;credentialSubject.eventType&#x60; must be one of the documented event-type values and &#x60;action&#x60; (when present) one of &#x60;ADD&#x60;/&#x60;OBSERVE&#x60;/&#x60;DELETE&#x60; — both map to server-side enums, and a missing or unknown value is only rejected at the persistence layer and surfaces as the 500 &#x60;Database Persistence Failed&#x60; body, not as a 400. Note the 201 envelope is &#x60;{status: \&quot;success\&quot;, ...}&#x60;, NOT the usual &#x60;{success: true, ...}&#x60; shape. This endpoint does not create lineage edges between events; the lineage DAG read by &#x60;GET /api/v1/events/{id}/lineage&#x60; is built from lineage relations maintained separately on the node.
   * @param untpEventCredential  (required)
   * @return ApiResponse&lt;TraceEventRegistered&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<TraceEventRegistered> registerTraceabilityEventWithHttpInfo(UntpEventCredential untpEventCredential) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = registerTraceabilityEventRequestBuilder(untpEventCredential);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("registerTraceabilityEvent", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<TraceEventRegistered>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<TraceEventRegistered>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<TraceEventRegistered>() {})
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

  private HttpRequest.Builder registerTraceabilityEventRequestBuilder(UntpEventCredential untpEventCredential) throws ApiException {
    // verify the required parameter 'untpEventCredential' is set
    if (untpEventCredential == null) {
      throw new ApiException(400, "Missing the required parameter 'untpEventCredential' when calling registerTraceabilityEvent");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/events";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(untpEventCredential);
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
   * Publicly verify a passport&#39;s eIDAS seal, certificate chain and timestamp
   * **Public seal-verification API** — cryptographically verifies that a Digital Product Passport document was sealed by an economic-operator tenant registered on this node and has not been tampered with. No authentication required.  **Rate limit:** custom in-memory token bucket, **30 requests/min per IP** (per app instance). This bucket emits no rate-limit headers of its own — any &#x60;x-ratelimit-*&#x60; headers on responses (including this 429) come from the global 100 req/min limiter and describe that budget, not the 30/min one. The 429 body is the two-field &#x60;{\&quot;error\&quot;: \&quot;Too Many Requests\&quot;, \&quot;message\&quot;: \&quot;Rate limit exceeded.\&quot;}&#x60;.  **Input resolution.** &#x60;payload&#x60; is required. &#x60;signature&#x60; and &#x60;publicKey&#x60; may be supplied top-level, or are extracted from the document&#39;s embedded proof block: &#x60;signature&#x60; ← &#x60;payload.proof.proofValue&#x60; (else &#x60;payload.proof.signatureValue&#x60;); &#x60;publicKey&#x60; ← &#x60;payload.proof.publicKeyPem&#x60;, else the leaf certificate&#39;s SPKI when &#x60;payload.proof.x5c&#x60; is present. If, after extraction, any of the three is still missing → 400. The public key is CRLF-normalized and trimmed before matching.  **Verification pipeline (in order):** 1. **Certificate-chain report (optional).** If &#x60;payload.proof.x5c&#x60; is a non-empty array of base64-DER certificates (leaf first), the chain is parsed and a &#x60;certificate&#x60; report is built: the leaf&#39;s &#x60;subject&#x60; / &#x60;issuer&#x60; / &#x60;validFrom&#x60; / &#x60;validTo&#x60; (X.509 textual dates such as &#x60;Jan 10 00:00:00 2026 GMT&#x60; — NOT ISO 8601), &#x60;chainValid&#x60; (every link signature-verifies against the next certificate, every certificate is inside its validity window, and the top of the chain is anchored to this node&#39;s seal CA — SHA-256 fingerprint match or signature under the CA key; the CA is published at &#x60;GET /.well-known/opendpp-seal-ca.pem&#x60;), and &#x60;keyMatchesProof&#x60; (the leaf SPKI equals the supplied &#x60;publicKey&#x60;, whitespace-insensitive; always &#x60;true&#x60; when no explicit key was supplied). An unparseable chain yields &#x60;{\&quot;chainValid\&quot;: false, \&quot;error\&quot;: \&quot;Unparseable x5c certificate chain\&quot;}&#x60; and does NOT fail the request. This reports the CERTIFIED identity of the seal creator (eIDAS Art. 36(1)(b)). SECURITY: the report is attached ONLY on a &#x60;verified: true&#x60; outcome whose chain is TRUSTED (&#x60;chainValid&#x60; AND &#x60;keyMatchesProof&#x60; both true) — an untrusted/self-signed chain, one outside its validity window or not anchored to this node&#39;s seal CA, or one whose leaf key does not match the verifying key, is never surfaced as a &#x60;certificate&#x60; block (it must not present an unverified identity as authoritative). The two policy-failure responses omit it too. 2. **Key-registration gate.** The &#x60;publicKey&#x60; must exactly match the registered eIDAS public key of a tenant on this node (trailing-newline tolerant) — otherwise HTTP **200** with &#x60;verified: false&#x60; and an explanatory &#x60;message&#x60;. Verification-policy failures are reported in-band, never as HTTP errors. 3. **Operator-binding gate (fail-closed).** If the payload declares an operator registration id (&#x60;payload.operator.regId&#x60;, else &#x60;payload.economicOperator.regId&#x60;), that id MUST resolve to an Economic Operator registered on this node AND that operator MUST be bound to the signing tenant (a workspace–operator binding registered on this node). A declared operator that is unregistered, or registered but not bound to the key-owning tenant, → 200 &#x60;verified: false&#x60; with an explanatory &#x60;message&#x60;. Payloads that declare no operator id skip this gate. 4. **Signature verification (two phases).** *Phase 1 — Merkle seal:* when &#x60;payload.metadata&#x60; is an object (or, when the &#x60;metadata&#x60; key is entirely absent, the whole &#x60;payload&#x60; is treated as the metadata), the SHA-256 Merkle tree over the metadata&#39;s top-level properties is rebuilt and the base64 ECDSA (P-256 / SHA-256) &#x60;signature&#x60; is verified against the recomputed root. Every leaf is recomputed from the actual values — caller-supplied redacted-leaf hashes are NOT accepted (they would let a tampered field be smuggled past verification), so a publicly redacted document will not pass the Merkle phase: verify the unredacted, privileged document. *Phase 2 — fallback:* if the Merkle phase does not verify, the signature is verified over the deterministic key-sorted canonicalization of the entire &#x60;payload&#x60;. 5. **RFC 3161 timestamp report (optional).** When &#x60;payload.proof.rfc3161.token&#x60; is a non-empty base64-DER TimeStampToken, the response includes &#x60;timestamp&#x60; with the TSA-asserted &#x60;genTime&#x60; parsed from the token&#39;s TSTInfo (or &#x60;genTime: null&#x60; plus a &#x60;note&#x60; when the token cannot be parsed). When the node has a TSA CA configured (&#x60;TSA_CA_PEM&#x60;), the report also carries &#x60;timeAuthenticated&#x60; — the node&#39;s own verification of the token&#39;s CMS SignedData signature over its TSTInfo PLUS full RFC 3161 trust-path validation of the signer to that anchor (a critical &#x60;id-kp-timeStamping&#x60; EKU, validity at the asserted &#x60;genTime&#x60;, CA-constrained intermediates) (&#x60;false&#x60;, and the asserted time unauthenticated, when no CA is configured, the signature fails, or the path is not policy-valid); a verifier may still run its own &#x60;openssl ts -verify&#x60;. Like &#x60;certificate&#x60;, it appears only on the final verification outcome.  **Outcome.** A processed verification ALWAYS returns HTTP 200 with &#x60;verified: true|false&#x60;; 400 is reserved for missing parameters or an exception thrown while verifying (e.g. an undecodable public key). &#x60;timestamp&#x60; is attached only when verification proceeds past the key-registration and operator-binding gates; &#x60;certificate&#x60; is attached only on a &#x60;verified: true&#x60; outcome whose chain is trusted (&#x60;chainValid&#x60; AND &#x60;keyMatchesProof&#x60;) — the two policy &#x60;verified: false&#x60; responses (and any untrusted-chain outcome) omit &#x60;certificate&#x60;, even when an x5c chain and/or an RFC 3161 token were supplied. The 400 bodies on this public endpoint are &#x60;{\&quot;success\&quot;: false, \&quot;message\&quot;: \&quot;...\&quot;}&#x60; — they include &#x60;success&#x60; but OMIT the &#x60;error&#x60; field. (A syntactically malformed JSON body is rejected earlier by the framework with its default &#x60;{statusCode, error, message}&#x60; body; a POST with no body at all — no &#x60;Content-Type&#x60; — fails before processing with a framework-default 500, so send at least &#x60;{}&#x60;. An empty &#x60;application/json&#x60; body is treated as &#x60;{}&#x60; and yields the documented 400.)
   * @param sealVerifyRequest  (required)
   * @return SealVerifyResponse
   * @throws ApiException if fails to make API call
   */
  public SealVerifyResponse verifyPassportSeal(SealVerifyRequest sealVerifyRequest) throws ApiException {
    ApiResponse<SealVerifyResponse> localVarResponse = verifyPassportSealWithHttpInfo(sealVerifyRequest);
    return localVarResponse.getData();
  }

  /**
   * Publicly verify a passport&#39;s eIDAS seal, certificate chain and timestamp
   * **Public seal-verification API** — cryptographically verifies that a Digital Product Passport document was sealed by an economic-operator tenant registered on this node and has not been tampered with. No authentication required.  **Rate limit:** custom in-memory token bucket, **30 requests/min per IP** (per app instance). This bucket emits no rate-limit headers of its own — any &#x60;x-ratelimit-*&#x60; headers on responses (including this 429) come from the global 100 req/min limiter and describe that budget, not the 30/min one. The 429 body is the two-field &#x60;{\&quot;error\&quot;: \&quot;Too Many Requests\&quot;, \&quot;message\&quot;: \&quot;Rate limit exceeded.\&quot;}&#x60;.  **Input resolution.** &#x60;payload&#x60; is required. &#x60;signature&#x60; and &#x60;publicKey&#x60; may be supplied top-level, or are extracted from the document&#39;s embedded proof block: &#x60;signature&#x60; ← &#x60;payload.proof.proofValue&#x60; (else &#x60;payload.proof.signatureValue&#x60;); &#x60;publicKey&#x60; ← &#x60;payload.proof.publicKeyPem&#x60;, else the leaf certificate&#39;s SPKI when &#x60;payload.proof.x5c&#x60; is present. If, after extraction, any of the three is still missing → 400. The public key is CRLF-normalized and trimmed before matching.  **Verification pipeline (in order):** 1. **Certificate-chain report (optional).** If &#x60;payload.proof.x5c&#x60; is a non-empty array of base64-DER certificates (leaf first), the chain is parsed and a &#x60;certificate&#x60; report is built: the leaf&#39;s &#x60;subject&#x60; / &#x60;issuer&#x60; / &#x60;validFrom&#x60; / &#x60;validTo&#x60; (X.509 textual dates such as &#x60;Jan 10 00:00:00 2026 GMT&#x60; — NOT ISO 8601), &#x60;chainValid&#x60; (every link signature-verifies against the next certificate, every certificate is inside its validity window, and the top of the chain is anchored to this node&#39;s seal CA — SHA-256 fingerprint match or signature under the CA key; the CA is published at &#x60;GET /.well-known/opendpp-seal-ca.pem&#x60;), and &#x60;keyMatchesProof&#x60; (the leaf SPKI equals the supplied &#x60;publicKey&#x60;, whitespace-insensitive; always &#x60;true&#x60; when no explicit key was supplied). An unparseable chain yields &#x60;{\&quot;chainValid\&quot;: false, \&quot;error\&quot;: \&quot;Unparseable x5c certificate chain\&quot;}&#x60; and does NOT fail the request. This reports the CERTIFIED identity of the seal creator (eIDAS Art. 36(1)(b)). SECURITY: the report is attached ONLY on a &#x60;verified: true&#x60; outcome whose chain is TRUSTED (&#x60;chainValid&#x60; AND &#x60;keyMatchesProof&#x60; both true) — an untrusted/self-signed chain, one outside its validity window or not anchored to this node&#39;s seal CA, or one whose leaf key does not match the verifying key, is never surfaced as a &#x60;certificate&#x60; block (it must not present an unverified identity as authoritative). The two policy-failure responses omit it too. 2. **Key-registration gate.** The &#x60;publicKey&#x60; must exactly match the registered eIDAS public key of a tenant on this node (trailing-newline tolerant) — otherwise HTTP **200** with &#x60;verified: false&#x60; and an explanatory &#x60;message&#x60;. Verification-policy failures are reported in-band, never as HTTP errors. 3. **Operator-binding gate (fail-closed).** If the payload declares an operator registration id (&#x60;payload.operator.regId&#x60;, else &#x60;payload.economicOperator.regId&#x60;), that id MUST resolve to an Economic Operator registered on this node AND that operator MUST be bound to the signing tenant (a workspace–operator binding registered on this node). A declared operator that is unregistered, or registered but not bound to the key-owning tenant, → 200 &#x60;verified: false&#x60; with an explanatory &#x60;message&#x60;. Payloads that declare no operator id skip this gate. 4. **Signature verification (two phases).** *Phase 1 — Merkle seal:* when &#x60;payload.metadata&#x60; is an object (or, when the &#x60;metadata&#x60; key is entirely absent, the whole &#x60;payload&#x60; is treated as the metadata), the SHA-256 Merkle tree over the metadata&#39;s top-level properties is rebuilt and the base64 ECDSA (P-256 / SHA-256) &#x60;signature&#x60; is verified against the recomputed root. Every leaf is recomputed from the actual values — caller-supplied redacted-leaf hashes are NOT accepted (they would let a tampered field be smuggled past verification), so a publicly redacted document will not pass the Merkle phase: verify the unredacted, privileged document. *Phase 2 — fallback:* if the Merkle phase does not verify, the signature is verified over the deterministic key-sorted canonicalization of the entire &#x60;payload&#x60;. 5. **RFC 3161 timestamp report (optional).** When &#x60;payload.proof.rfc3161.token&#x60; is a non-empty base64-DER TimeStampToken, the response includes &#x60;timestamp&#x60; with the TSA-asserted &#x60;genTime&#x60; parsed from the token&#39;s TSTInfo (or &#x60;genTime: null&#x60; plus a &#x60;note&#x60; when the token cannot be parsed). When the node has a TSA CA configured (&#x60;TSA_CA_PEM&#x60;), the report also carries &#x60;timeAuthenticated&#x60; — the node&#39;s own verification of the token&#39;s CMS SignedData signature over its TSTInfo PLUS full RFC 3161 trust-path validation of the signer to that anchor (a critical &#x60;id-kp-timeStamping&#x60; EKU, validity at the asserted &#x60;genTime&#x60;, CA-constrained intermediates) (&#x60;false&#x60;, and the asserted time unauthenticated, when no CA is configured, the signature fails, or the path is not policy-valid); a verifier may still run its own &#x60;openssl ts -verify&#x60;. Like &#x60;certificate&#x60;, it appears only on the final verification outcome.  **Outcome.** A processed verification ALWAYS returns HTTP 200 with &#x60;verified: true|false&#x60;; 400 is reserved for missing parameters or an exception thrown while verifying (e.g. an undecodable public key). &#x60;timestamp&#x60; is attached only when verification proceeds past the key-registration and operator-binding gates; &#x60;certificate&#x60; is attached only on a &#x60;verified: true&#x60; outcome whose chain is trusted (&#x60;chainValid&#x60; AND &#x60;keyMatchesProof&#x60;) — the two policy &#x60;verified: false&#x60; responses (and any untrusted-chain outcome) omit &#x60;certificate&#x60;, even when an x5c chain and/or an RFC 3161 token were supplied. The 400 bodies on this public endpoint are &#x60;{\&quot;success\&quot;: false, \&quot;message\&quot;: \&quot;...\&quot;}&#x60; — they include &#x60;success&#x60; but OMIT the &#x60;error&#x60; field. (A syntactically malformed JSON body is rejected earlier by the framework with its default &#x60;{statusCode, error, message}&#x60; body; a POST with no body at all — no &#x60;Content-Type&#x60; — fails before processing with a framework-default 500, so send at least &#x60;{}&#x60;. An empty &#x60;application/json&#x60; body is treated as &#x60;{}&#x60; and yields the documented 400.)
   * @param sealVerifyRequest  (required)
   * @return ApiResponse&lt;SealVerifyResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SealVerifyResponse> verifyPassportSealWithHttpInfo(SealVerifyRequest sealVerifyRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = verifyPassportSealRequestBuilder(sealVerifyRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("verifyPassportSeal", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<SealVerifyResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<SealVerifyResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<SealVerifyResponse>() {})
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

  private HttpRequest.Builder verifyPassportSealRequestBuilder(SealVerifyRequest sealVerifyRequest) throws ApiException {
    // verify the required parameter 'sealVerifyRequest' is set
    if (sealVerifyRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'sealVerifyRequest' when calling verifyPassportSeal");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/audit/verify";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(sealVerifyRequest);
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
