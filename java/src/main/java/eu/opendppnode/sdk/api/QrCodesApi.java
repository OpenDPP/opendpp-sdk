/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) Anyone can verify a seal — no account required. `POST /api/v1/audit/verify` recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.13.0
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

import eu.opendppnode.sdk.model.BulkExportPassportLabelsRequest;
import eu.opendppnode.sdk.model.Error;
import java.io.File;
import eu.opendppnode.sdk.model.InlineObject;

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
public class QrCodesApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public QrCodesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public QrCodesApi(ApiClient apiClient) {
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
   * Bulk-export print-grade QR labels for many passports as a ZIP
   * Renders a GS1 Digital Link QR code for each of the supplied passports and returns them as a single &#x60;application/zip&#x60; download (&#x60;Content-Disposition: attachment; filename&#x3D;\&quot;labels.zip\&quot;&#x60;) — the export counterpart to the bulk import. One image entry per resolved passport, named &#x60;&lt;productId&gt;.&lt;png|svg&gt;&#x60; (characters outside &#x60;[A-Za-z0-9._-]&#x60; replaced by &#x60;_&#x60;, truncated to 80 chars; duplicate names get a &#x60;-2&#x60;, &#x60;-3&#x60;, … suffix), plus a &#x60;manifest.json&#x60; listing what was &#x60;included&#x60; and &#x60;skipped&#x60;.  **Permission:** &#x60;passport:read&#x60; (read-only — no subscription/402 gate, and NOT subject to the programmatic API-write entitlement).  **Partial success:** an id that is unknown, not owned by your tenant, or outside an operator-scoped key&#39;s bound operator is **skipped and reported** in &#x60;manifest.json&#x60; (&#x60;{ id, reason }&#x60;) — it never fails the whole batch. Only the caller&#39;s own passports resolve, so this cannot enumerate another tenant&#39;s catalog.  **Limits:** at most **200** ids per call (mirrors the bulk-import cap); more returns **400** pointing at the async export. &#x60;hri: true&#x60; requires &#x60;format: \&quot;svg\&quot;&#x60; (same constraint as the single QR). &#x60;size&#x60; is clamped to 128–2048.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param bulkExportPassportLabelsRequest  (required)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File bulkExportPassportLabels(BulkExportPassportLabelsRequest bulkExportPassportLabelsRequest) throws ApiException {
    ApiResponse<File> localVarResponse = bulkExportPassportLabelsWithHttpInfo(bulkExportPassportLabelsRequest);
    return localVarResponse.getData();
  }

  /**
   * Bulk-export print-grade QR labels for many passports as a ZIP
   * Renders a GS1 Digital Link QR code for each of the supplied passports and returns them as a single &#x60;application/zip&#x60; download (&#x60;Content-Disposition: attachment; filename&#x3D;\&quot;labels.zip\&quot;&#x60;) — the export counterpart to the bulk import. One image entry per resolved passport, named &#x60;&lt;productId&gt;.&lt;png|svg&gt;&#x60; (characters outside &#x60;[A-Za-z0-9._-]&#x60; replaced by &#x60;_&#x60;, truncated to 80 chars; duplicate names get a &#x60;-2&#x60;, &#x60;-3&#x60;, … suffix), plus a &#x60;manifest.json&#x60; listing what was &#x60;included&#x60; and &#x60;skipped&#x60;.  **Permission:** &#x60;passport:read&#x60; (read-only — no subscription/402 gate, and NOT subject to the programmatic API-write entitlement).  **Partial success:** an id that is unknown, not owned by your tenant, or outside an operator-scoped key&#39;s bound operator is **skipped and reported** in &#x60;manifest.json&#x60; (&#x60;{ id, reason }&#x60;) — it never fails the whole batch. Only the caller&#39;s own passports resolve, so this cannot enumerate another tenant&#39;s catalog.  **Limits:** at most **200** ids per call (mirrors the bulk-import cap); more returns **400** pointing at the async export. &#x60;hri: true&#x60; requires &#x60;format: \&quot;svg\&quot;&#x60; (same constraint as the single QR). &#x60;size&#x60; is clamped to 128–2048.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param bulkExportPassportLabelsRequest  (required)
   * @return ApiResponse&lt;File&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<File> bulkExportPassportLabelsWithHttpInfo(BulkExportPassportLabelsRequest bulkExportPassportLabelsRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = bulkExportPassportLabelsRequestBuilder(bulkExportPassportLabelsRequest);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("bulkExportPassportLabels", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<File>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<File>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {})
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

  private HttpRequest.Builder bulkExportPassportLabelsRequestBuilder(BulkExportPassportLabelsRequest bulkExportPassportLabelsRequest) throws ApiException {
    // verify the required parameter 'bulkExportPassportLabelsRequest' is set
    if (bulkExportPassportLabelsRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'bulkExportPassportLabelsRequest' when calling bulkExportPassportLabels");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/labels";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/zip, application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(bulkExportPassportLabelsRequest);
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
   * Export a print-grade QR code for an individual battery unit
   * Renders the battery unit&#39;s GS1 Digital Link URI as a print-grade QR code — the AI-21 path segment carries the unit&#39;s **real physical serial number** (e.g. &#x60;https://opendpp-node.eu/01/09501101530003/21/BAT-2026-000123&#x60;). This is the carrier each individual battery must wear (per-unit passports, per the EU Battery Regulation).  **Permission:** &#x60;battery:read&#x60; (read-only — subscription status is **not** checked on &#x60;:read&#x60; permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session.  **Identifier resolution:** &#x60;{id}&#x60; is the BatteryUnit **UUID only** — unlike the passport QR route there is **no** serial-number fallback. Lookup is tenant-scoped. Credentials scoped to an Economic Operator receive **403** (&#x60;Your access is restricted to Economic Operator: &lt;operatorId&gt;&#x60;) when the unit&#39;s parent passport belongs to a different operator.  **QR rendering:** identical pipeline to the passport QR export — 4-module quiet zone, &#x60;ecl&#x60; default &#x60;Q&#x60;, &#x60;size&#x60; clamped to 128–2048 px (clamped, not rejected; fractions truncated). The response carries &#x60;Content-Disposition: attachment; filename&#x3D;\&quot;qr-&lt;serialNumber&gt;.png\&quot;&#x60; (or &#x60;.svg&#x60;); the filename base is the unit&#39;s &#x60;serialNumber&#x60; with characters outside &#x60;[A-Za-z0-9._-]&#x60; replaced by &#x60;_&#x60;, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: &#x60;format must be png or svg&#x60;, &#x60;size must be a number&#x60;, &#x60;ecl must be M, Q or H&#x60;. An unknown unit returns **404** with message &#x60;Battery unit &lt;id&gt; not found under your Tenant workspace&#x60;.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id BatteryUnit UUID (primary key). Serial numbers are NOT accepted here. (required)
   * @param format Output image format. Case-insensitive; any other value returns 400 (&#x60;format must be png or svg&#x60;). (optional, default to png)
   * @param size Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (&#x60;size must be a number&#x60;). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response. (optional, default to 1024)
   * @param ecl QR error-correction level: &#x60;M&#x60; (~15% recovery), &#x60;Q&#x60; (~25%, GS1 product-label guidance, default) or &#x60;H&#x60; (~30%). Case-insensitive; any other value returns 400 (&#x60;ecl must be M, Q or H&#x60;). (optional, default to Q)
   * @param hri When &#x60;1&#x60;/&#x60;true&#x60;, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. &#x60;(01) 09501101530003 (21) BAT-2026-000123&#x60;) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires &#x60;format&#x3D;svg&#x60;; combining it with &#x60;format&#x3D;png&#x60; returns 400 (&#x60;hri (Human-Readable Interpretation) labels require format&#x3D;svg&#x60;). (optional, default to false)
   * @throws ApiException if fails to make API call
   */
  public void getBatteryUnitQrCode(String id, String format, Integer size, String ecl, Boolean hri) throws ApiException {
    getBatteryUnitQrCodeWithHttpInfo(id, format, size, ecl, hri);
  }

  /**
   * Export a print-grade QR code for an individual battery unit
   * Renders the battery unit&#39;s GS1 Digital Link URI as a print-grade QR code — the AI-21 path segment carries the unit&#39;s **real physical serial number** (e.g. &#x60;https://opendpp-node.eu/01/09501101530003/21/BAT-2026-000123&#x60;). This is the carrier each individual battery must wear (per-unit passports, per the EU Battery Regulation).  **Permission:** &#x60;battery:read&#x60; (read-only — subscription status is **not** checked on &#x60;:read&#x60; permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session.  **Identifier resolution:** &#x60;{id}&#x60; is the BatteryUnit **UUID only** — unlike the passport QR route there is **no** serial-number fallback. Lookup is tenant-scoped. Credentials scoped to an Economic Operator receive **403** (&#x60;Your access is restricted to Economic Operator: &lt;operatorId&gt;&#x60;) when the unit&#39;s parent passport belongs to a different operator.  **QR rendering:** identical pipeline to the passport QR export — 4-module quiet zone, &#x60;ecl&#x60; default &#x60;Q&#x60;, &#x60;size&#x60; clamped to 128–2048 px (clamped, not rejected; fractions truncated). The response carries &#x60;Content-Disposition: attachment; filename&#x3D;\&quot;qr-&lt;serialNumber&gt;.png\&quot;&#x60; (or &#x60;.svg&#x60;); the filename base is the unit&#39;s &#x60;serialNumber&#x60; with characters outside &#x60;[A-Za-z0-9._-]&#x60; replaced by &#x60;_&#x60;, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: &#x60;format must be png or svg&#x60;, &#x60;size must be a number&#x60;, &#x60;ecl must be M, Q or H&#x60;. An unknown unit returns **404** with message &#x60;Battery unit &lt;id&gt; not found under your Tenant workspace&#x60;.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id BatteryUnit UUID (primary key). Serial numbers are NOT accepted here. (required)
   * @param format Output image format. Case-insensitive; any other value returns 400 (&#x60;format must be png or svg&#x60;). (optional, default to png)
   * @param size Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (&#x60;size must be a number&#x60;). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response. (optional, default to 1024)
   * @param ecl QR error-correction level: &#x60;M&#x60; (~15% recovery), &#x60;Q&#x60; (~25%, GS1 product-label guidance, default) or &#x60;H&#x60; (~30%). Case-insensitive; any other value returns 400 (&#x60;ecl must be M, Q or H&#x60;). (optional, default to Q)
   * @param hri When &#x60;1&#x60;/&#x60;true&#x60;, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. &#x60;(01) 09501101530003 (21) BAT-2026-000123&#x60;) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires &#x60;format&#x3D;svg&#x60;; combining it with &#x60;format&#x3D;png&#x60; returns 400 (&#x60;hri (Human-Readable Interpretation) labels require format&#x3D;svg&#x60;). (optional, default to false)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> getBatteryUnitQrCodeWithHttpInfo(String id, String format, Integer size, String ecl, Boolean hri) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getBatteryUnitQrCodeRequestBuilder(id, format, size, ecl, hri);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getBatteryUnitQrCode", localVarResponse);
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

  private HttpRequest.Builder getBatteryUnitQrCodeRequestBuilder(String id, String format, Integer size, String ecl, Boolean hri) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getBatteryUnitQrCode");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/units/{id}/qr"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "format";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
    localVarQueryParameterBaseName = "size";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("size", size));
    localVarQueryParameterBaseName = "ecl";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("ecl", ecl));
    localVarQueryParameterBaseName = "hri";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("hri", hri));

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

    localVarRequestBuilder.header("Accept", "image/png, image/svg+xml, application/json");

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
   * Export a print-grade GS1 Digital Link QR code for a passport
   * Renders the passport&#39;s GS1 Digital Link URI (its &#x60;digitalLinkUri&#x60;, e.g. &#x60;https://opendpp-node.eu/01/09501101530003&#x60;) as a print-grade QR code and returns it as a binary file download. The printed carrier resolves through the public GS1 gateway.  **Permission:** &#x60;passport:read&#x60; (read-only — subscription status is **not** checked on &#x60;:read&#x60; permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session — plain same-origin &#x60;&lt;a href&gt;&#x60; downloads are supported.  **Identifier resolution:** &#x60;{id}&#x60; is matched first against the passport UUID, then against the caller-supplied &#x60;productId&#x60; (GTIN-14/GRAI/SKU), always scoped to your tenant. Credentials scoped to an Economic Operator receive **403** (&#x60;Your access is restricted to Economic Operator: &lt;operatorId&gt;&#x60;) when the passport belongs to a different operator.  **QR rendering:** 4-module quiet zone (GS1 guidance); error-correction level per &#x60;ecl&#x60; (default &#x60;Q&#x60;, the GS1 recommendation for product labels); &#x60;size&#x60; is **clamped** to 128–2048 px — out-of-range values are clamped to the nearest bound, not rejected, and fractional values are truncated. The response carries &#x60;Content-Disposition: attachment; filename&#x3D;\&quot;qr-&lt;productId&gt;.png\&quot;&#x60; (or &#x60;.svg&#x60;); the filename base is the passport&#39;s &#x60;productId&#x60; with characters outside &#x60;[A-Za-z0-9._-]&#x60; replaced by &#x60;_&#x60;, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: &#x60;format must be png or svg&#x60;, &#x60;size must be a number&#x60;, &#x60;ecl must be M, Q or H&#x60;. An unknown passport returns **404** with message &#x60;Passport &lt;id&gt; not found under your Tenant workspace&#x60;.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Passport UUID, or the caller-supplied &#x60;productId&#x60; (GTIN-14/GRAI/SKU) as a fallback. Resolution is tenant-scoped. (required)
   * @param format Output image format. Case-insensitive; any other value returns 400 (&#x60;format must be png or svg&#x60;). (optional, default to png)
   * @param size Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (&#x60;size must be a number&#x60;). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response. (optional, default to 1024)
   * @param ecl QR error-correction level: &#x60;M&#x60; (~15% recovery), &#x60;Q&#x60; (~25%, GS1 product-label guidance, default) or &#x60;H&#x60; (~30%). Case-insensitive; any other value returns 400 (&#x60;ecl must be M, Q or H&#x60;). &#x60;L&#x60; is intentionally not offered. (optional, default to Q)
   * @param hri When &#x60;1&#x60;/&#x60;true&#x60;, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. &#x60;(01) 09501101530003 (21) BAT-2026-000123&#x60;) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires &#x60;format&#x3D;svg&#x60;; combining it with &#x60;format&#x3D;png&#x60; returns 400 (&#x60;hri (Human-Readable Interpretation) labels require format&#x3D;svg&#x60;). (optional, default to false)
   * @throws ApiException if fails to make API call
   */
  public void getPassportQrCode(String id, String format, Integer size, String ecl, Boolean hri) throws ApiException {
    getPassportQrCodeWithHttpInfo(id, format, size, ecl, hri);
  }

  /**
   * Export a print-grade GS1 Digital Link QR code for a passport
   * Renders the passport&#39;s GS1 Digital Link URI (its &#x60;digitalLinkUri&#x60;, e.g. &#x60;https://opendpp-node.eu/01/09501101530003&#x60;) as a print-grade QR code and returns it as a binary file download. The printed carrier resolves through the public GS1 gateway.  **Permission:** &#x60;passport:read&#x60; (read-only — subscription status is **not** checked on &#x60;:read&#x60; permissions, so this endpoint never returns 402). Works with a Bearer API key, a Bearer JWT, or an authenticated browser session — plain same-origin &#x60;&lt;a href&gt;&#x60; downloads are supported.  **Identifier resolution:** &#x60;{id}&#x60; is matched first against the passport UUID, then against the caller-supplied &#x60;productId&#x60; (GTIN-14/GRAI/SKU), always scoped to your tenant. Credentials scoped to an Economic Operator receive **403** (&#x60;Your access is restricted to Economic Operator: &lt;operatorId&gt;&#x60;) when the passport belongs to a different operator.  **QR rendering:** 4-module quiet zone (GS1 guidance); error-correction level per &#x60;ecl&#x60; (default &#x60;Q&#x60;, the GS1 recommendation for product labels); &#x60;size&#x60; is **clamped** to 128–2048 px — out-of-range values are clamped to the nearest bound, not rejected, and fractional values are truncated. The response carries &#x60;Content-Disposition: attachment; filename&#x3D;\&quot;qr-&lt;productId&gt;.png\&quot;&#x60; (or &#x60;.svg&#x60;); the filename base is the passport&#39;s &#x60;productId&#x60; with characters outside &#x60;[A-Za-z0-9._-]&#x60; replaced by &#x60;_&#x60;, truncated to 80 characters.  **Errors:** an invalid query option returns **400** with one of these exact messages: &#x60;format must be png or svg&#x60;, &#x60;size must be a number&#x60;, &#x60;ecl must be M, Q or H&#x60;. An unknown passport returns **404** with message &#x60;Passport &lt;id&gt; not found under your Tenant workspace&#x60;.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @param id Passport UUID, or the caller-supplied &#x60;productId&#x60; (GTIN-14/GRAI/SKU) as a fallback. Resolution is tenant-scoped. (required)
   * @param format Output image format. Case-insensitive; any other value returns 400 (&#x60;format must be png or svg&#x60;). (optional, default to png)
   * @param size Rendered width in pixels (PNG) / SVG width attribute. Clamped to 128–2048 — out-of-range values are silently clamped, fractions truncated. A non-numeric value returns 400 (&#x60;size must be a number&#x60;). A PNG is never rendered below one pixel per module, so an unusually dense symbol comes back slightly wider than requested — see the 200 response. (optional, default to 1024)
   * @param ecl QR error-correction level: &#x60;M&#x60; (~15% recovery), &#x60;Q&#x60; (~25%, GS1 product-label guidance, default) or &#x60;H&#x60; (~30%). Case-insensitive; any other value returns 400 (&#x60;ecl must be M, Q or H&#x60;). &#x60;L&#x60; is intentionally not offered. (optional, default to Q)
   * @param hri When &#x60;1&#x60;/&#x60;true&#x60;, renders the GS1 Human-Readable Interpretation (the bracketed AI string, e.g. &#x60;(01) 09501101530003 (21) BAT-2026-000123&#x60;) as vector text beneath the QR symbol — the print-grade GS1 label form (machine-readable QR + the human-readable AI string). Requires &#x60;format&#x3D;svg&#x60;; combining it with &#x60;format&#x3D;png&#x60; returns 400 (&#x60;hri (Human-Readable Interpretation) labels require format&#x3D;svg&#x60;). (optional, default to false)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> getPassportQrCodeWithHttpInfo(String id, String format, Integer size, String ecl, Boolean hri) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getPassportQrCodeRequestBuilder(id, format, size, ecl, hri);
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getPassportQrCode", localVarResponse);
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

  private HttpRequest.Builder getPassportQrCodeRequestBuilder(String id, String format, Integer size, String ecl, Boolean hri) throws ApiException {
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getPassportQrCode");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/passports/{id}/qr"
        .replace("{id}", ApiClient.urlEncode(id.toString()));

    List<Pair> localVarQueryParams = new ArrayList<>();
    StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
    String localVarQueryParameterBaseName;
    localVarQueryParameterBaseName = "format";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("format", format));
    localVarQueryParameterBaseName = "size";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("size", size));
    localVarQueryParameterBaseName = "ecl";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("ecl", ecl));
    localVarQueryParameterBaseName = "hri";
    localVarQueryParams.addAll(ApiClient.parameterToPairs("hri", hri));

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

    localVarRequestBuilder.header("Accept", "image/png, image/svg+xml, application/json");

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
