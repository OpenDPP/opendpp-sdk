/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) `POST /api/v1/audit/verify` is public and unauthenticated, and verifies seals issued on this node — the signing key must be registered to a tenant here, so a seal from another node is declined without cryptographic evaluation. It recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.15.0
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

import eu.opendppnode.sdk.model.BatteryUnitEventBadRequest;
import eu.opendppnode.sdk.model.BatteryUnitEventListResponse;
import eu.opendppnode.sdk.model.BatteryUnitJsonLd;
import eu.opendppnode.sdk.model.BatteryUnitListResponse;
import eu.opendppnode.sdk.model.BatteryUnitSerialiseBadRequest;
import eu.opendppnode.sdk.model.BulkBatteryUnitEventsRequest;
import eu.opendppnode.sdk.model.BulkBatteryUnitEventsResponse;
import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.PassportQuotaError;
import eu.opendppnode.sdk.model.RecordBatteryUnitEventRequest;
import eu.opendppnode.sdk.model.RecordBatteryUnitEventResponse;
import eu.opendppnode.sdk.model.SerializeBatteryUnitsRequest;
import eu.opendppnode.sdk.model.SerializeBatteryUnitsResponse;
import eu.opendppnode.sdk.model.ValidateBatteryUnits200Response;

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
public class BatteryUnitsApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public BatteryUnitsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public BatteryUnitsApi(ApiClient apiClient) {
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
   * Append a batch of telemetry events to a battery unit
   * Appends up to **500** append-only dynamic-data records to one unit in a single request — the batch venue for backfilling a fleet&#39;s telemetry. **Telemetry only:** a record carrying &#x60;status&#x60; is refused per-item; a status transition is a lifecycle decision and goes through &#x60;POST /api/v1/units/{id}/events&#x60; one event at a time.  **Permission:** &#x60;battery:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403). A terminal (&#x60;RECYCLED&#x60;) unit refuses the whole batch (400 &#x60;Terminal Unit Status&#x60;).  **Per-record validation (collected as &#x60;[index]&#x60;-prefixed strings in &#x60;errors&#x60;, not a rejection of the whole batch):** the same field checks as the single-event endpoint — &#x60;eventType&#x60; required and valid, numeric ranges, Date-parseable &#x60;recordedAt&#x60; (defaults to server time when omitted) — plus the physics consistency checks, judged in chronological order across the batch and the unit&#39;s recorded history, so a reading that only conflicts with another batch member is caught too.  **Partial success:** the response is **201 when at least one record was accepted**; skipped items are listed in &#x60;errors&#x60;. If *every* item failed you get **400** &#x60;Bulk Event Ingest Failed&#x60; with the same string array.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @param bulkBatteryUnitEventsRequest  (required)
   * @param idempotencyKey Optional client idempotency key (≤255 characters, no control characters). Retrying this batch with the same &#x60;Idempotency-Key&#x60; replays the ORIGINAL result — same status and body, plus an &#x60;idempotent-replayed: true&#x60; response header — instead of re-inserting the batch. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally and readings may then be recorded twice. (optional)
   * @return BulkBatteryUnitEventsResponse
   * @throws ApiException if fails to make API call
   */
  public BulkBatteryUnitEventsResponse bulkRecordBatteryUnitEvents(String id, BulkBatteryUnitEventsRequest bulkBatteryUnitEventsRequest, String idempotencyKey) throws ApiException {
    ApiResponse<BulkBatteryUnitEventsResponse> localVarResponse = bulkRecordBatteryUnitEventsWithHttpInfo(id, bulkBatteryUnitEventsRequest, idempotencyKey);
    return localVarResponse.getData();
  }

  /**
   * Append a batch of telemetry events to a battery unit
   * Appends up to **500** append-only dynamic-data records to one unit in a single request — the batch venue for backfilling a fleet&#39;s telemetry. **Telemetry only:** a record carrying &#x60;status&#x60; is refused per-item; a status transition is a lifecycle decision and goes through &#x60;POST /api/v1/units/{id}/events&#x60; one event at a time.  **Permission:** &#x60;battery:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403). A terminal (&#x60;RECYCLED&#x60;) unit refuses the whole batch (400 &#x60;Terminal Unit Status&#x60;).  **Per-record validation (collected as &#x60;[index]&#x60;-prefixed strings in &#x60;errors&#x60;, not a rejection of the whole batch):** the same field checks as the single-event endpoint — &#x60;eventType&#x60; required and valid, numeric ranges, Date-parseable &#x60;recordedAt&#x60; (defaults to server time when omitted) — plus the physics consistency checks, judged in chronological order across the batch and the unit&#39;s recorded history, so a reading that only conflicts with another batch member is caught too.  **Partial success:** the response is **201 when at least one record was accepted**; skipped items are listed in &#x60;errors&#x60;. If *every* item failed you get **400** &#x60;Bulk Event Ingest Failed&#x60; with the same string array.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @param bulkBatteryUnitEventsRequest  (required)
   * @param idempotencyKey Optional client idempotency key (≤255 characters, no control characters). Retrying this batch with the same &#x60;Idempotency-Key&#x60; replays the ORIGINAL result — same status and body, plus an &#x60;idempotent-replayed: true&#x60; response header — instead of re-inserting the batch. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the result is recorded after the batch commits, so in the rare window between commit and recording (or across an instance restart) a retry re-processes the batch normally and readings may then be recorded twice. (optional)
   * @return ApiResponse&lt;BulkBatteryUnitEventsResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BulkBatteryUnitEventsResponse> bulkRecordBatteryUnitEventsWithHttpInfo(String id, BulkBatteryUnitEventsRequest bulkBatteryUnitEventsRequest, String idempotencyKey) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = bulkRecordBatteryUnitEventsRequestBuilder(id, bulkBatteryUnitEventsRequest, idempotencyKey);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("bulkRecordBatteryUnitEvents", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<BulkBatteryUnitEventsResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<BulkBatteryUnitEventsResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<BulkBatteryUnitEventsResponse>() {})
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

  private HttpRequest.Builder bulkRecordBatteryUnitEventsRequestBuilder(String id, BulkBatteryUnitEventsRequest bulkBatteryUnitEventsRequest, String idempotencyKey) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling bulkRecordBatteryUnitEvents");
    }
    // verify the required parameter 'bulkBatteryUnitEventsRequest' is set
    if (bulkBatteryUnitEventsRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'bulkBatteryUnitEventsRequest' when calling bulkRecordBatteryUnitEvents");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/units/{id}/events/bulk"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    if (idempotencyKey != null) {
      localVarRequestBuilder.header("Idempotency-Key", idempotencyKey.toString());
    }
    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(bulkBatteryUnitEventsRequest);
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
   * Not deletable: a serialised unit is a marketed physical item (always 409)
   * **Always refused with 409.** A serialised unit is an item-level battery passport for a physical battery placed on the market, so the record — including its append-only telemetry — is retained (EU Battery Regulation persistence, mirroring the passport-level archive model). End a unit&#39;s life through the lifecycle instead: append a &#x60;STATUS_CHANGE&#x60; event via &#x60;POST /api/v1/units/{id}/events&#x60; — &#x60;RECYCLED&#x60; ceases it (public 410 tombstone), &#x60;DECOMMISSIONED&#x60; retires it. Hard removal is reserved for the node&#39;s retention-gated purge, never an ad-hoc API delete.  **Permission:** &#x60;battery:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;. Operator-scoped credentials may only address units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteBatteryUnit(String id) throws ApiException {
    deleteBatteryUnitWithHttpInfo(id);
  }

  /**
   * Not deletable: a serialised unit is a marketed physical item (always 409)
   * **Always refused with 409.** A serialised unit is an item-level battery passport for a physical battery placed on the market, so the record — including its append-only telemetry — is retained (EU Battery Regulation persistence, mirroring the passport-level archive model). End a unit&#39;s life through the lifecycle instead: append a &#x60;STATUS_CHANGE&#x60; event via &#x60;POST /api/v1/units/{id}/events&#x60; — &#x60;RECYCLED&#x60; ceases it (public 410 tombstone), &#x60;DECOMMISSIONED&#x60; retires it. Hard removal is reserved for the node&#39;s retention-gated purge, never an ad-hoc API delete.  **Permission:** &#x60;battery:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;. Operator-scoped credentials may only address units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteBatteryUnitWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = deleteBatteryUnitRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("deleteBatteryUnit", localVarResponse);
        }
        return new ApiResponse<>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            null
        );
      } finally {
        // Drain the InputStream
        while (localVarResponse.body().read() != -1) {
          // Ignore
        }
        localVarResponse.body().close();
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder deleteBatteryUnitRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteBatteryUnit");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/units/{id}"
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
   * Get one battery unit as JSON-LD with its dynamic-data history
   * Returns the unit as a **JSON-LD document** (&#x60;Content-Type: application/ld+json&#x60;) in the **privileged tenant view**: &#x60;currentState&#x60; (the latest telemetry snapshot) and &#x60;dynamicData&#x60; (the **500 most recent** events, newest first by &#x60;recordedAt&#x60;) are included; the public &#x60;restrictedData&#x60; marker is absent. The embedded &#x60;ofModel&#x60; is the SKU/type passport document rendered in the **owner (unredacted) variant** — legitimate-interest-tier metadata and owner-only keys are NOT masked, unlike the anonymous public document.  **Caveat:** this authenticated endpoint does **not** load lineage relations, so &#x60;repurposedFrom&#x60; is always &#x60;null&#x60; and &#x60;successorUnits&#x60; is always &#x60;[]&#x60; here even when lineage exists; the public resolver view (&#x60;GET /unit/{id}&#x60;) does resolve them.  **Permission:** &#x60;battery:read&#x60;. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403).  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @return BatteryUnitJsonLd
   * @throws ApiException if fails to make API call
   */
  public BatteryUnitJsonLd getBatteryUnit(String id) throws ApiException {
    ApiResponse<BatteryUnitJsonLd> localVarResponse = getBatteryUnitWithHttpInfo(id);
    return localVarResponse.getData();
  }

  /**
   * Get one battery unit as JSON-LD with its dynamic-data history
   * Returns the unit as a **JSON-LD document** (&#x60;Content-Type: application/ld+json&#x60;) in the **privileged tenant view**: &#x60;currentState&#x60; (the latest telemetry snapshot) and &#x60;dynamicData&#x60; (the **500 most recent** events, newest first by &#x60;recordedAt&#x60;) are included; the public &#x60;restrictedData&#x60; marker is absent. The embedded &#x60;ofModel&#x60; is the SKU/type passport document rendered in the **owner (unredacted) variant** — legitimate-interest-tier metadata and owner-only keys are NOT masked, unlike the anonymous public document.  **Caveat:** this authenticated endpoint does **not** load lineage relations, so &#x60;repurposedFrom&#x60; is always &#x60;null&#x60; and &#x60;successorUnits&#x60; is always &#x60;[]&#x60; here even when lineage exists; the public resolver view (&#x60;GET /unit/{id}&#x60;) does resolve them.  **Permission:** &#x60;battery:read&#x60;. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403).  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @return ApiResponse&lt;BatteryUnitJsonLd&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BatteryUnitJsonLd> getBatteryUnitWithHttpInfo(String id) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getBatteryUnitRequestBuilder(id);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getBatteryUnit", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<BatteryUnitJsonLd>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<BatteryUnitJsonLd>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<BatteryUnitJsonLd>() {})
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

  private HttpRequest.Builder getBatteryUnitRequestBuilder(String id) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getBatteryUnit");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/units/{id}"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

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
   * List a battery unit&#39;s telemetry history (newest first, cursor-paginated)
   * Returns one page of the unit&#39;s append-only dynamic-data history ordered by &#x60;recordedAt&#x60; DESC, ties broken by &#x60;id&#x60; DESC — so paging is deterministic. A page holds at most 500 events (&#x60;limit&#x60;, default 500); while older history remains the response carries a non-null &#x60;nextCursor&#x60; — pass it back as &#x60;cursor&#x60; to fetch the next (older) page, so the **full history is retrievable** however long it grows. The cursor is opaque; a malformed value returns **400**.  **Permission:** &#x60;battery:read&#x60;. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403). Events are returned as stored.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @param limit Page size (1–500). Out-of-range or non-integer values return **400**. (optional, default to 500)
   * @param cursor Opaque page cursor — the &#x60;nextCursor&#x60; value from the previous page. Omit for the first (newest) page; a malformed value returns **400**. (optional)
   * @return BatteryUnitEventListResponse
   * @throws ApiException if fails to make API call
   */
  public BatteryUnitEventListResponse listBatteryUnitEvents(String id, Integer limit, String cursor) throws ApiException {
    ApiResponse<BatteryUnitEventListResponse> localVarResponse = listBatteryUnitEventsWithHttpInfo(id, limit, cursor);
    return localVarResponse.getData();
  }

  /**
   * List a battery unit&#39;s telemetry history (newest first, cursor-paginated)
   * Returns one page of the unit&#39;s append-only dynamic-data history ordered by &#x60;recordedAt&#x60; DESC, ties broken by &#x60;id&#x60; DESC — so paging is deterministic. A page holds at most 500 events (&#x60;limit&#x60;, default 500); while older history remains the response carries a non-null &#x60;nextCursor&#x60; — pass it back as &#x60;cursor&#x60; to fetch the next (older) page, so the **full history is retrievable** however long it grows. The cursor is opaque; a malformed value returns **400**.  **Permission:** &#x60;battery:read&#x60;. Operator-scoped credentials may only read units whose passport belongs to their Economic Operator (403). Events are returned as stored.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @param limit Page size (1–500). Out-of-range or non-integer values return **400**. (optional, default to 500)
   * @param cursor Opaque page cursor — the &#x60;nextCursor&#x60; value from the previous page. Omit for the first (newest) page; a malformed value returns **400**. (optional)
   * @return ApiResponse&lt;BatteryUnitEventListResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BatteryUnitEventListResponse> listBatteryUnitEventsWithHttpInfo(String id, Integer limit, String cursor) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listBatteryUnitEventsRequestBuilder(id, limit, cursor);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listBatteryUnitEvents", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<BatteryUnitEventListResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<BatteryUnitEventListResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<BatteryUnitEventListResponse>() {})
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

  private HttpRequest.Builder listBatteryUnitEventsRequestBuilder(String id, Integer limit, String cursor) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling listBatteryUnitEvents");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/units/{id}/events"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "limit";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
    localVarQueryParameterBaseName = "cursor";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("cursor", cursor));

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
   * List serialised battery units under a passport
   * Lists **all** serialised units of the passport, newest first (&#x60;createdAt&#x60; DESC). **Paginated** with &#x60;?page&#x60; (default 1) and &#x60;?limit&#x60; (default 100, max 200) — a SKU may carry many physical units; &#x60;count&#x60; is this page&#39;s size, &#x60;total&#x60;/&#x60;totalPages&#x60; describe the full set.  **Permission:** &#x60;battery:read&#x60;. Operator-scoped credentials may only read passports of their own Economic Operator (403). Units are returned as stored.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param passportId The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then &#x60;productId&#x60; — both scoped to your tenant. (required)
   * @param page 1-based page number (digits only; non-numeric falls back to 1). (optional, default to 1)
   * @param limit Page size. Clamped to 1–200; non-numeric falls back to the default 100. (optional, default to 100)
   * @return BatteryUnitListResponse
   * @throws ApiException if fails to make API call
   */
  public BatteryUnitListResponse listBatteryUnits(String passportId, Integer page, Integer limit) throws ApiException {
    ApiResponse<BatteryUnitListResponse> localVarResponse = listBatteryUnitsWithHttpInfo(passportId, page, limit);
    return localVarResponse.getData();
  }

  /**
   * List serialised battery units under a passport
   * Lists **all** serialised units of the passport, newest first (&#x60;createdAt&#x60; DESC). **Paginated** with &#x60;?page&#x60; (default 1) and &#x60;?limit&#x60; (default 100, max 200) — a SKU may carry many physical units; &#x60;count&#x60; is this page&#39;s size, &#x60;total&#x60;/&#x60;totalPages&#x60; describe the full set.  **Permission:** &#x60;battery:read&#x60;. Operator-scoped credentials may only read passports of their own Economic Operator (403). Units are returned as stored.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param passportId The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then &#x60;productId&#x60; — both scoped to your tenant. (required)
   * @param page 1-based page number (digits only; non-numeric falls back to 1). (optional, default to 1)
   * @param limit Page size. Clamped to 1–200; non-numeric falls back to the default 100. (optional, default to 100)
   * @return ApiResponse&lt;BatteryUnitListResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BatteryUnitListResponse> listBatteryUnitsWithHttpInfo(String passportId, Integer page, Integer limit) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = listBatteryUnitsRequestBuilder(passportId, page, limit);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("listBatteryUnits", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<BatteryUnitListResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<BatteryUnitListResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<BatteryUnitListResponse>() {})
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

  private HttpRequest.Builder listBatteryUnitsRequestBuilder(String passportId, Integer page, Integer limit) throws ApiException {
    // verify the required parameter 'passportId' is set
    if (passportId == null) {
      throw new ApiException(400, "Missing the required parameter 'passportId' when calling listBatteryUnits");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{passportId}/units"
        .replace("{passportId}", ApiClient.urlEncode(passportId.toString()));

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
   * Append an immutable telemetry event to a battery unit
   * Appends one **append-only** per-unit dynamic-data record (Annex XIII / Art. 77: SoH, cycle count, remaining capacity, temperature, negative events). History is immutable — there is **no update or delete path** for events.  **Permission:** &#x60;battery:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Validation (400 with the standard error triple):** &#x60;eventType&#x60; is required and must be one of &#x60;SOH_MEASUREMENT|CHARGE_CYCLE|STATUS_CHANGE|NEGATIVE_EVENT|OTHER&#x60;; &#x60;stateOfHealth&#x60; 0–100; &#x60;cycleCount&#x60; and &#x60;remainingCapacityAh&#x60; 0–9007199254740991; &#x60;temperatureC&#x60; −273.15–10000 (each may also be &#x60;null&#x60;/omitted); &#x60;status&#x60;, if present, must be a valid unit status; &#x60;recordedAt&#x60; must be Date-parseable (defaults to server time when omitted). &#x60;cycleCount&#x60; is truncated to an integer before persisting; a &#x60;payload&#x60; that is not an object or array is silently dropped (stored as &#x60;null&#x60;) — JSON **arrays** pass the server&#39;s &#x60;typeof&#x60; check and are persisted verbatim.  **Status transition:** when &#x60;status&#x60; is present and differs from the unit&#39;s current status, the unit is updated **in the same transaction** as the event — this works with *any* &#x60;eventType&#x60;, though &#x60;STATUS_CHANGE&#x60; is the conventional carrier. Transitioning to **&#x60;RECYCLED&#x60;** additionally stamps &#x60;ceasedAt&#x60; (if not already set; never cleared), after which the public unit view becomes a 410 tombstone and the unit can no longer gain successor units. &#x60;RECYCLED&#x60; is terminal: once the unit&#39;s status is &#x60;RECYCLED&#x60; this endpoint refuses **every** further event with **400** &#x60;Terminal Unit Status&#x60; — neither &#x60;status&#x60; nor telemetry can change again. A second life must be linked with &#x60;predecessorUnitId&#x60; **before** the predecessor is recycled — a terminal unit is itself refused as a &#x60;predecessorUnitId&#x60;, so there is no path back once it is recycled.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @param recordBatteryUnitEventRequest  (required)
   * @param idempotencyKey Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same &#x60;Idempotency-Key&#x60; replays the ORIGINAL response — same status and body, plus an &#x60;idempotent-replayed: true&#x60; response header — instead of appending a duplicate reading. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry appends normally — the reading may then be recorded twice. (optional)
   * @return RecordBatteryUnitEventResponse
   * @throws ApiException if fails to make API call
   */
  public RecordBatteryUnitEventResponse recordBatteryUnitEvent(String id, RecordBatteryUnitEventRequest recordBatteryUnitEventRequest, String idempotencyKey) throws ApiException {
    ApiResponse<RecordBatteryUnitEventResponse> localVarResponse = recordBatteryUnitEventWithHttpInfo(id, recordBatteryUnitEventRequest, idempotencyKey);
    return localVarResponse.getData();
  }

  /**
   * Append an immutable telemetry event to a battery unit
   * Appends one **append-only** per-unit dynamic-data record (Annex XIII / Art. 77: SoH, cycle count, remaining capacity, temperature, negative events). History is immutable — there is **no update or delete path** for events.  **Permission:** &#x60;battery:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;. Operator-scoped credentials may only write to units whose passport belongs to their Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Validation (400 with the standard error triple):** &#x60;eventType&#x60; is required and must be one of &#x60;SOH_MEASUREMENT|CHARGE_CYCLE|STATUS_CHANGE|NEGATIVE_EVENT|OTHER&#x60;; &#x60;stateOfHealth&#x60; 0–100; &#x60;cycleCount&#x60; and &#x60;remainingCapacityAh&#x60; 0–9007199254740991; &#x60;temperatureC&#x60; −273.15–10000 (each may also be &#x60;null&#x60;/omitted); &#x60;status&#x60;, if present, must be a valid unit status; &#x60;recordedAt&#x60; must be Date-parseable (defaults to server time when omitted). &#x60;cycleCount&#x60; is truncated to an integer before persisting; a &#x60;payload&#x60; that is not an object or array is silently dropped (stored as &#x60;null&#x60;) — JSON **arrays** pass the server&#39;s &#x60;typeof&#x60; check and are persisted verbatim.  **Status transition:** when &#x60;status&#x60; is present and differs from the unit&#39;s current status, the unit is updated **in the same transaction** as the event — this works with *any* &#x60;eventType&#x60;, though &#x60;STATUS_CHANGE&#x60; is the conventional carrier. Transitioning to **&#x60;RECYCLED&#x60;** additionally stamps &#x60;ceasedAt&#x60; (if not already set; never cleared), after which the public unit view becomes a 410 tombstone and the unit can no longer gain successor units. &#x60;RECYCLED&#x60; is terminal: once the unit&#39;s status is &#x60;RECYCLED&#x60; this endpoint refuses **every** further event with **400** &#x60;Terminal Unit Status&#x60; — neither &#x60;status&#x60; nor telemetry can change again. A second life must be linked with &#x60;predecessorUnitId&#x60; **before** the predecessor is recycled — a terminal unit is itself refused as a &#x60;predecessorUnitId&#x60;, so there is no path back once it is recycled.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Battery unit UUID (tenant-scoped). (required)
   * @param recordBatteryUnitEventRequest  (required)
   * @param idempotencyKey Optional client idempotency key (≤255 characters, no control characters). Retrying this request with the same &#x60;Idempotency-Key&#x60; replays the ORIGINAL response — same status and body, plus an &#x60;idempotent-replayed: true&#x60; response header — instead of appending a duplicate reading. Scoped per (workspace, unit, key) and consulted within a 24-hour window; a malformed key returns **400**. Best-effort: the replay is recorded after the write commits, so in the rare window between commit and recording (or across an instance restart) a retry appends normally — the reading may then be recorded twice. (optional)
   * @return ApiResponse&lt;RecordBatteryUnitEventResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RecordBatteryUnitEventResponse> recordBatteryUnitEventWithHttpInfo(String id, RecordBatteryUnitEventRequest recordBatteryUnitEventRequest, String idempotencyKey) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = recordBatteryUnitEventRequestBuilder(id, recordBatteryUnitEventRequest, idempotencyKey);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("recordBatteryUnitEvent", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<RecordBatteryUnitEventResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<RecordBatteryUnitEventResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<RecordBatteryUnitEventResponse>() {})
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

  private HttpRequest.Builder recordBatteryUnitEventRequestBuilder(String id, RecordBatteryUnitEventRequest recordBatteryUnitEventRequest, String idempotencyKey) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling recordBatteryUnitEvent");
    }
    // verify the required parameter 'recordBatteryUnitEventRequest' is set
    if (recordBatteryUnitEventRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'recordBatteryUnitEventRequest' when calling recordBatteryUnitEvent");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/units/{id}/events"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    if (idempotencyKey != null) {
      localVarRequestBuilder.header("Idempotency-Key", idempotencyKey.toString());
    }
    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(recordBatteryUnitEventRequest);
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
   * Serialise individual battery units under a passport (bulk, up to 200)
   * Creates one or many **individual physical battery units** (EU Battery Regulation) under a SKU/type-level passport. Send either a single unit object or &#x60;{\&quot;units\&quot;: [...]}&#x60; with **at most 200 items** (if &#x60;units&#x60; is present and an array it is used; otherwise the whole body is treated as one unit).  **Permission:** &#x60;battery:write&#x60;. Bearer API key (&#x60;op_dpp_token_…&#x60;) or session JWT; cookie-session clients must send &#x60;X-CSRF-Token&#x60;. Operator-scoped credentials may only serialise under passports of their own Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Per-item validation (collected as plain-string errors, not a rejection of the whole batch):** &#x60;serialNumber&#x60; is trimmed then must match &#x60;^[A-Za-z0-9._-]{1,20}$&#x60; (a URL-safe subset of GS1 AI-21 CSET 82, ≤ 20 chars) AND is validated to full AI-21 conformance by GS1&#39;s authoritative engine — a GTIN-keyed unit through its full Digital Link, a non-GTIN unit through its AI-21 serial value; &#x60;status&#x60; must be a valid unit status; &#x60;manufacturedAt&#x60; must be Date-parseable; duplicate &#x60;(passport, serialNumber)&#x60; pairs are skipped with *\&quot;A unit with this serial already exists for this passport\&quot;*. Each created unit gets a per-unit GS1 Digital Link URI &#x60;/{01|8003}/{productId}/21/{serialNumber}&#x60; carrying the **real physical serial** in AI-21.  **Predecessor linkage (repurpose/remanufacture):** &#x60;predecessorUnitId&#x60; must reference an existing unit **in your tenant** (any passport). A recycled predecessor (&#x60;ceasedAt&#x60; set) is refused — its passport has ceased to exist. (A unit *created* with status &#x60;RECYCLED&#x60; is ceased from birth — &#x60;ceasedAt&#x60; is stamped at creation — and is refused as a predecessor exactly like one recycled via the events route.) In one transaction the new unit is created, an append-only &#x60;STATUS_CHANGE&#x60; event (&#x60;{status, successorUnitId, successorSerial}&#x60; payload) is written to the predecessor, and the predecessor&#39;s status is set to &#x60;predecessorStatus&#x60; (default &#x60;REPURPOSED&#x60;; only &#x60;REPURPOSED|REMANUFACTURED|REUSED&#x60; allowed).  **Partial success:** the response is **201 when at least one unit was created**; skipped items are listed in &#x60;errors&#x60;. If *every* item failed you get **400 &#x60;Serialisation Failed&#x60;** with the same string array. A &#x60;batteryunit.created&#x60; audit event and a tenant notification are emitted on success.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param passportId The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then &#x60;productId&#x60; — both scoped to your tenant. (required)
   * @param serializeBatteryUnitsRequest  (required)
   * @return SerializeBatteryUnitsResponse
   * @throws ApiException if fails to make API call
   */
  public SerializeBatteryUnitsResponse serializeBatteryUnits(String passportId, SerializeBatteryUnitsRequest serializeBatteryUnitsRequest) throws ApiException {
    ApiResponse<SerializeBatteryUnitsResponse> localVarResponse = serializeBatteryUnitsWithHttpInfo(passportId, serializeBatteryUnitsRequest);
    return localVarResponse.getData();
  }

  /**
   * Serialise individual battery units under a passport (bulk, up to 200)
   * Creates one or many **individual physical battery units** (EU Battery Regulation) under a SKU/type-level passport. Send either a single unit object or &#x60;{\&quot;units\&quot;: [...]}&#x60; with **at most 200 items** (if &#x60;units&#x60; is present and an array it is used; otherwise the whole body is treated as one unit).  **Permission:** &#x60;battery:write&#x60;. Bearer API key (&#x60;op_dpp_token_…&#x60;) or session JWT; cookie-session clients must send &#x60;X-CSRF-Token&#x60;. Operator-scoped credentials may only serialise under passports of their own Economic Operator (403). Write operations pass subscription gating (402) and optional tenant MFA enforcement (403).  **Per-item validation (collected as plain-string errors, not a rejection of the whole batch):** &#x60;serialNumber&#x60; is trimmed then must match &#x60;^[A-Za-z0-9._-]{1,20}$&#x60; (a URL-safe subset of GS1 AI-21 CSET 82, ≤ 20 chars) AND is validated to full AI-21 conformance by GS1&#39;s authoritative engine — a GTIN-keyed unit through its full Digital Link, a non-GTIN unit through its AI-21 serial value; &#x60;status&#x60; must be a valid unit status; &#x60;manufacturedAt&#x60; must be Date-parseable; duplicate &#x60;(passport, serialNumber)&#x60; pairs are skipped with *\&quot;A unit with this serial already exists for this passport\&quot;*. Each created unit gets a per-unit GS1 Digital Link URI &#x60;/{01|8003}/{productId}/21/{serialNumber}&#x60; carrying the **real physical serial** in AI-21.  **Predecessor linkage (repurpose/remanufacture):** &#x60;predecessorUnitId&#x60; must reference an existing unit **in your tenant** (any passport). A recycled predecessor (&#x60;ceasedAt&#x60; set) is refused — its passport has ceased to exist. (A unit *created* with status &#x60;RECYCLED&#x60; is ceased from birth — &#x60;ceasedAt&#x60; is stamped at creation — and is refused as a predecessor exactly like one recycled via the events route.) In one transaction the new unit is created, an append-only &#x60;STATUS_CHANGE&#x60; event (&#x60;{status, successorUnitId, successorSerial}&#x60; payload) is written to the predecessor, and the predecessor&#39;s status is set to &#x60;predecessorStatus&#x60; (default &#x60;REPURPOSED&#x60;; only &#x60;REPURPOSED|REMANUFACTURED|REUSED&#x60; allowed).  **Partial success:** the response is **201 when at least one unit was created**; skipped items are listed in &#x60;errors&#x60;. If *every* item failed you get **400 &#x60;Serialisation Failed&#x60;** with the same string array. A &#x60;batteryunit.created&#x60; audit event and a tenant notification are emitted on success.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param passportId The SKU/type-level passport, addressed either by its UUID **or** by its caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU). The UUID lookup is tried first, then &#x60;productId&#x60; — both scoped to your tenant. (required)
   * @param serializeBatteryUnitsRequest  (required)
   * @return ApiResponse&lt;SerializeBatteryUnitsResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SerializeBatteryUnitsResponse> serializeBatteryUnitsWithHttpInfo(String passportId, SerializeBatteryUnitsRequest serializeBatteryUnitsRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = serializeBatteryUnitsRequestBuilder(passportId, serializeBatteryUnitsRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("serializeBatteryUnits", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<SerializeBatteryUnitsResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<SerializeBatteryUnitsResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<SerializeBatteryUnitsResponse>() {})
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

  private HttpRequest.Builder serializeBatteryUnitsRequestBuilder(String passportId, SerializeBatteryUnitsRequest serializeBatteryUnitsRequest) throws ApiException {
    // verify the required parameter 'passportId' is set
    if (passportId == null) {
      throw new ApiException(400, "Missing the required parameter 'passportId' when calling serializeBatteryUnits");
    }
    // verify the required parameter 'serializeBatteryUnitsRequest' is set
    if (serializeBatteryUnitsRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'serializeBatteryUnitsRequest' when calling serializeBatteryUnits");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{passportId}/units"
        .replace("{passportId}", ApiClient.urlEncode(passportId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(serializeBatteryUnitsRequest);
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
   * Pre-flight: validate battery-unit identifiers without persisting
   * NON-MUTATING pre-flight for bulk unit import. Runs the SAME engine-backed AI-21 / GS1 Digital Link conformance + field checks as &#x60;POST /api/v1/passports/{passportId}/units&#x60; and returns a per-item verdict — **persisting nothing**. Send a single unit or &#x60;{\&quot;units\&quot;: [...]}&#x60; (≤200). Lets a bulk importer ask \&quot;would these serials be GS1-conformant?\&quot; before committing a batch.  **Permission:** &#x60;battery:write&#x60; (gated as the write permission, like other validate-only checks; subscription gating → 402). **Validation:** &#x60;serialNumber&#x60; charset/length (&#x60;^[A-Za-z0-9._-]{1,20}$&#x60;, a URL-safe subset of GS1 AI-21 CSET 82) PLUS authoritative GS1-engine conformance for EVERY unit — a GTIN-keyed passport&#39;s unit Digital Link must parse cleanly through the engine, and a non-GTIN passport&#39;s AI-21 serial VALUE is validated through the same engine (CSET-82 charset + length); &#x60;status&#x60; must be a valid unit status; &#x60;manufacturedAt&#x60; must be Date-parseable. Predecessor linkage is NOT checked here (a persistence-time concern). The verdict order matches the input order.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param passportId The SKU/type-level passport, addressed by its UUID or caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU), scoped to your tenant. (required)
   * @param serializeBatteryUnitsRequest  (required)
   * @return ValidateBatteryUnits200Response
   * @throws ApiException if fails to make API call
   */
  public ValidateBatteryUnits200Response validateBatteryUnits(String passportId, SerializeBatteryUnitsRequest serializeBatteryUnitsRequest) throws ApiException {
    ApiResponse<ValidateBatteryUnits200Response> localVarResponse = validateBatteryUnitsWithHttpInfo(passportId, serializeBatteryUnitsRequest);
    return localVarResponse.getData();
  }

  /**
   * Pre-flight: validate battery-unit identifiers without persisting
   * NON-MUTATING pre-flight for bulk unit import. Runs the SAME engine-backed AI-21 / GS1 Digital Link conformance + field checks as &#x60;POST /api/v1/passports/{passportId}/units&#x60; and returns a per-item verdict — **persisting nothing**. Send a single unit or &#x60;{\&quot;units\&quot;: [...]}&#x60; (≤200). Lets a bulk importer ask \&quot;would these serials be GS1-conformant?\&quot; before committing a batch.  **Permission:** &#x60;battery:write&#x60; (gated as the write permission, like other validate-only checks; subscription gating → 402). **Validation:** &#x60;serialNumber&#x60; charset/length (&#x60;^[A-Za-z0-9._-]{1,20}$&#x60;, a URL-safe subset of GS1 AI-21 CSET 82) PLUS authoritative GS1-engine conformance for EVERY unit — a GTIN-keyed passport&#39;s unit Digital Link must parse cleanly through the engine, and a non-GTIN passport&#39;s AI-21 serial VALUE is validated through the same engine (CSET-82 charset + length); &#x60;status&#x60; must be a valid unit status; &#x60;manufacturedAt&#x60; must be Date-parseable. Predecessor linkage is NOT checked here (a persistence-time concern). The verdict order matches the input order.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param passportId The SKU/type-level passport, addressed by its UUID or caller-supplied &#x60;productId&#x60; (GTIN-14 / GRAI / SKU), scoped to your tenant. (required)
   * @param serializeBatteryUnitsRequest  (required)
   * @return ApiResponse&lt;ValidateBatteryUnits200Response&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ValidateBatteryUnits200Response> validateBatteryUnitsWithHttpInfo(String passportId, SerializeBatteryUnitsRequest serializeBatteryUnitsRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = validateBatteryUnitsRequestBuilder(passportId, serializeBatteryUnitsRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("validateBatteryUnits", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<ValidateBatteryUnits200Response>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<ValidateBatteryUnits200Response>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<ValidateBatteryUnits200Response>() {})
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

  private HttpRequest.Builder validateBatteryUnitsRequestBuilder(String passportId, SerializeBatteryUnitsRequest serializeBatteryUnitsRequest) throws ApiException {
    // verify the required parameter 'passportId' is set
    if (passportId == null) {
      throw new ApiException(400, "Missing the required parameter 'passportId' when calling validateBatteryUnits");
    }
    // verify the required parameter 'serializeBatteryUnitsRequest' is set
    if (serializeBatteryUnitsRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'serializeBatteryUnitsRequest' when calling validateBatteryUnits");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{passportId}/units/validate"
        .replace("{passportId}", ApiClient.urlEncode(passportId.toString()));

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(serializeBatteryUnitsRequest);
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
