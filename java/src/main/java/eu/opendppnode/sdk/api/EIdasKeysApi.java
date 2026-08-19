/*
 * OpenDPP Integration API
 * OpenDPP is a B2B platform for EU Digital Product Passports (DPPs), aligned with the ESPR data requirements and the EU Battery Regulation. This specification documents the **public integration surface**: everything an external system needs to create, validate, seal, publish, resolve and verify passports.  ## Authentication Authenticate with a tenant **API key** sent as a Bearer token: `Authorization: Bearer op_dpp_token_…`. Keys are created in the Client Console (Developers → API keys), are shown **once** at creation, carry a role plus optional narrowed permissions and optional expiry, and can be revoked at any time. API-key clients are exempt from CSRF requirements. Public endpoints (tagged **Public Resolution**, plus the public validators and the audit verifier) need no credentials.  ## Tenancy Tenant identity is **token-bound** — it is derived from your API key, never from the request host. The same paths work on the apex host and on tenant workspace hosts (`https://<workspace>.opendpp-node.eu`); when a workspace host is used, it must match the key's tenant (requests across workspaces are rejected with `403`).  ## Versioning & compatibility This contract carries a SemVer version, readable at runtime from `GET /api/v1/version`. **Pin the MAJOR.** It equals the `/api/v1` URL major, so a breaking change ships as a new path major (`/api/v2`) that you adopt deliberately — not as an edit to the contract you already integrated against.  Within a major line:  - **MINOR** is additive — a new endpoint, a new optional parameter, a new field on a response. A client that ignores what it does not recognise keeps working. Do not treat unknown response fields as errors. - **PATCH** is documentation only: wording, examples, descriptions. Nothing observable in the contract changes.  The tier is not asserted by hand. Every change is diffed structurally against the previous contract in CI, and a version bump lower than the diff requires fails the build — so the number you pin to is derived from the contract itself.  **One exception, disclosed rather than hidden.** While this contract is pre-GA, a breaking change may exceptionally ship on the existing major line under a recorded waiver instead of forcing a new path major. It is not a standing option: it requires a maintainer to enable it for a single merge, and every use is recorded with its justification. It has been used during the pre-GA period. Once this line reaches GA the waiver is retired, and the MAJOR promise above becomes unconditional. If you need a contract that cannot move under you before then, pin the exact version you generated your client from and upgrade deliberately.  ## Errors Authenticated endpoints return `{ success: false, error, message }` (some omit `success`). Across the developer-facing write/ingest surface (passport / operator / unit / resolver / facility / events / webhooks) the body also carries a **machine-stable `code`** you can branch on instead of parsing `message` — see the `code` enum on the shared **Error** schema for the full set. ESPR metadata validation failures return the richer shape documented as **ValidationFailed** with per-field `errors[]`/`warnings[]` (localizable via `?lang=` or `Accept-Language`; 28 languages). Bulk endpoints report row-level problems as `errors: string[]`. Malformed JSON and query-string violations are rejected before the handler runs and return a `{ statusCode, code, error, message }` body.  Every response — success or error — carries an **`X-Request-Id`** header; generic (server-error / framework) bodies also include it as `requestId`. Quote it to support to correlate with server logs. Send your own well-formed `X-Request-Id` and it is adopted for end-to-end tracing.  ## Advisories: `warnings[]` & `notices[]` Success responses may carry two non-blocking advisory channels of **coded** items (`AdvisoryItem`: `{ code, path?, message, friendlyMessage }`). **`warnings[]`** are heads-ups the request still succeeded on (`NON_GS1_PRODUCT_ID`, `PII_SHAPE_DETECTED`, `UNIT_NO_SCANNABLE_LINK`, `DRAFT_DEMOTED`, `EORI_NOT_FOUND`); **`notices[]`** are informational — helpful things the API did (`OPERATOR_AUTO_ATTRIBUTED`, `GTIN_AUTO_COPIED`). Branch on the STABLE `code`; treat `message` (developer English) and `friendlyMessage` (end-user, localized via `?lang=`/`Accept-Language` across 28 languages) as display text that may be reworded. Interfaces may also map a `code` to their own localized string.  ## Rate limits Two limits apply, and the one that bites first depends on how you call us.  **Per API key (authenticated calls).** Each key gets a per-minute budget set by the plan: **Growth 120**, **Scale 600**, **Enterprise unlimited**. A second ceiling of **3x that rate** applies across all of a workspace's keys together, so issuing more keys divides throughput fairly between your own systems rather than multiplying it. Plans below Growth do not include API access. Exceeding either budget returns `429` with a `Retry-After` header giving the seconds to wait.  **Per IP (all traffic).** A ceiling of **100 requests/min per IP** applies to anonymous traffic. Authenticated calls sit on a higher ceiling, so that several integrations behind one egress address are not held to the anonymous budget. `x-ratelimit-*` response headers report the applicable ceiling. Every plan that can reach the API sits at or above the anonymous figure, so an authenticated caller never meets a stricter limit than the number above.  Public passport resolution is additionally limited to **30 requests/min per IP** (no headers). The public validator is limited to **10 requests/min per IP**.  Stay under these limits with client-side queueing; on `429`, back off and retry after the indicated window. A `429` never indicates a credential problem — an invalid or revoked key returns `401`, so do not rotate a key in response to rate limiting.  ## Sealing & verification Passport seals are **advanced electronic seals** — ECDSA P-256 over a Merkle root of the passport content, with an optional RFC 3161 timestamp. (Advanced, not qualified: a qualified seal would require a QTSP.) Anyone can verify a seal — no account required. `POST /api/v1/audit/verify` recomputes every Merkle leaf from the submitted values, so it requires the unredacted document (caller-supplied redacted-leaf hashes are deliberately not trusted). Redacted documents remain verifiable **offline**: masked fields keep their true leaf hashes in `proof.redactedLeaves`, letting any verifier rebuild the sealed root without the privileged values.  ## Public access tiers Public resolution endpoints serve **tiered** views of the same URL: the public tier for anonymous callers; a restricted tier for holders of legitimate-interest (`dpp_li_…`) or authority (`dpp_auth_…`) capability tokens (presented as a Bearer token or `?grant=` query parameter); and the owner tier for the issuing tenant's own credentials.  ## Webhooks Subscribe to passport lifecycle events (`passport.ingested`, `passport.sealed`, `passport.recalled`, or `*`). Deliveries are HMAC-SHA256-signed; see the **webhooks** section of this document for the exact signature scheme, retry schedule, and payloads.  This document is also served machine-readably at [`/openapi.json`](https://opendpp-node.eu/openapi.json) and [`/openapi.yaml`](https://opendpp-node.eu/openapi.yaml).  ## Open interoperability kit The interoperability boundary — the official AAS + UNTP/W3C-VC schemas, live-reproducible samples, an offline conformance validator, and the field mappings — is **open source** at [github.com/OpenDPP/opendpp-interop](https://github.com/OpenDPP/opendpp-interop) (Apache-2.0). It lets any integrator validate and verify OpenDPP's standards-conformant output without access to the product source.
 *
 * The version of the OpenAPI document: 1.14.0
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

import eu.opendppnode.sdk.model.Error;
import eu.opendppnode.sdk.model.GetSealCaCertificate429Response;
import eu.opendppnode.sdk.model.InlineObject;
import eu.opendppnode.sdk.model.OperatorMinimalErrorResponse;
import eu.opendppnode.sdk.model.PassportQuotaError;
import eu.opendppnode.sdk.model.RotateTenantKeysResponse;

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
public class EIdasKeysApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
  private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

  public EIdasKeysApi() {
    this(Configuration.getDefaultApiClient());
  }

  public EIdasKeysApi(ApiClient apiClient) {
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
   * Download the platform seal-CA certificate (PEM)
   * Downloads the platform seal-CA certificate as PEM. Third parties pin this CA to validate the &#x60;x5c&#x60; certificate chains embedded in sealed-passport &#x60;proof&#x60; blocks — the chain&#39;s leaf certificate binds a tenant&#39;s signing key to its legal identity (the advanced-seal creator identification; the seal is an *advanced*, not qualified, electronic seal). The certificate is provisioned server-side on first use.  No authentication, no permission (public endpoint). Successful responses carry &#x60;Cache-Control: public, max-age&#x3D;3600&#x60;. Like every documented path except &#x60;/health&#x60;, a request on an unknown tenant workspace host receives a platform-level JSON 404 before this handler runs.  **Rate limit:** 30 requests/min/IP — note that this route&#39;s 429 body carries ONLY &#x60;{\&quot;error\&quot;: \&quot;Too Many Requests\&quot;}&#x60; (no &#x60;message&#x60;, no &#x60;success&#x60;), unlike the other public resolvers. The global platform limit (100 req/min/IP) applies on top: a global-limit 429 carries the platform&#39;s default &#x60;{statusCode, error, message}&#x60; body instead, and the global limiter&#39;s &#x60;x-ratelimit-*&#x60; headers appear on every response from this route. Returns &#x60;503&#x60; if the seal CA cannot be provisioned or loaded.
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String getSealCaCertificate() throws ApiException {
    ApiResponse<String> localVarResponse = getSealCaCertificateWithHttpInfo();
    return localVarResponse.getData();
  }

  /**
   * Download the platform seal-CA certificate (PEM)
   * Downloads the platform seal-CA certificate as PEM. Third parties pin this CA to validate the &#x60;x5c&#x60; certificate chains embedded in sealed-passport &#x60;proof&#x60; blocks — the chain&#39;s leaf certificate binds a tenant&#39;s signing key to its legal identity (the advanced-seal creator identification; the seal is an *advanced*, not qualified, electronic seal). The certificate is provisioned server-side on first use.  No authentication, no permission (public endpoint). Successful responses carry &#x60;Cache-Control: public, max-age&#x3D;3600&#x60;. Like every documented path except &#x60;/health&#x60;, a request on an unknown tenant workspace host receives a platform-level JSON 404 before this handler runs.  **Rate limit:** 30 requests/min/IP — note that this route&#39;s 429 body carries ONLY &#x60;{\&quot;error\&quot;: \&quot;Too Many Requests\&quot;}&#x60; (no &#x60;message&#x60;, no &#x60;success&#x60;), unlike the other public resolvers. The global platform limit (100 req/min/IP) applies on top: a global-limit 429 carries the platform&#39;s default &#x60;{statusCode, error, message}&#x60; body instead, and the global limiter&#39;s &#x60;x-ratelimit-*&#x60; headers appear on every response from this route. Returns &#x60;503&#x60; if the seal CA cannot be provisioned or loaded.
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> getSealCaCertificateWithHttpInfo() throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = getSealCaCertificateRequestBuilder();
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("getSealCaCertificate", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<String>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<String>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<String>() {})
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

  private HttpRequest.Builder getSealCaCertificateRequestBuilder() throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/.well-known/opendpp-seal-ca.pem";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/x-pem-file, application/json");

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
   * Rotate the tenant&#39;s ECDSA signing key pair
   * Generates a brand-new ECDSA **prime256v1 (P-256)** key pair for your workspace&#39;s advanced-seal signing and rotates it into the encrypted database vault, replacing the previous key. No request body is required; a valid JSON body, if sent, is ignored.  What happens: - The new private key (PKCS#8) is encrypted with AES-256-GCM (per-entry HKDF-derived key; the tenant id is bound as GCM additional authenticated data) and upserted into the vault — the **previous private key is overwritten and unrecoverable**. - The tenant&#39;s published &#x60;eidasPublicKey&#x60; is updated to the new public key (SPKI PEM), which is also returned in the response. - A best-effort X.509 identity certificate is minted from the platform seal CA, binding the new key to the tenant&#39;s legal name (creator identification); a certificate-minting failure does **not** fail the rotation (the certificate fields simply stay null until backfilled).  **Operational impact:** rotation does not invalidate existing seals. Each sealed passport embeds the signing public key and certificate chain at sealing time, so previously sealed passports keep verifying with their embedded key material. Passports sealed after rotation use the new key.  **Permission:** &#x60;key:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;; Bearer clients are exempt.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @return RotateTenantKeysResponse
   * @throws ApiException if fails to make API call
   */
  public RotateTenantKeysResponse rotateTenantKeys() throws ApiException {
    ApiResponse<RotateTenantKeysResponse> localVarResponse = rotateTenantKeysWithHttpInfo();
    return localVarResponse.getData();
  }

  /**
   * Rotate the tenant&#39;s ECDSA signing key pair
   * Generates a brand-new ECDSA **prime256v1 (P-256)** key pair for your workspace&#39;s advanced-seal signing and rotates it into the encrypted database vault, replacing the previous key. No request body is required; a valid JSON body, if sent, is ignored.  What happens: - The new private key (PKCS#8) is encrypted with AES-256-GCM (per-entry HKDF-derived key; the tenant id is bound as GCM additional authenticated data) and upserted into the vault — the **previous private key is overwritten and unrecoverable**. - The tenant&#39;s published &#x60;eidasPublicKey&#x60; is updated to the new public key (SPKI PEM), which is also returned in the response. - A best-effort X.509 identity certificate is minted from the platform seal CA, binding the new key to the tenant&#39;s legal name (creator identification); a certificate-minting failure does **not** fail the rotation (the certificate fields simply stay null until backfilled).  **Operational impact:** rotation does not invalidate existing seals. Each sealed passport embeds the signing public key and certificate chain at sealing time, so previously sealed passports keep verifying with their embedded key material. Passports sealed after rotation use the new key.  **Permission:** &#x60;key:write&#x60;. Cookie-session clients must send &#x60;X-CSRF-Token&#x60;; Bearer clients are exempt.  **Rate limit:** your plan&#39;s per-key budget applies — **Growth** 120/min, **Scale** 600/min, **Enterprise** unlimited — with a ceiling of 3x that rate across all of the workspace&#39;s keys. The per-IP ceiling is not the binding limit for authenticated calls. Standard &#x60;x-ratelimit-*&#x60; headers; **429** carries &#x60;Retry-After&#x60;.
   * @return ApiResponse&lt;RotateTenantKeysResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RotateTenantKeysResponse> rotateTenantKeysWithHttpInfo() throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = rotateTenantKeysRequestBuilder();
    try {
      HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(
          localVarRequestBuilder.build(),
          HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      try {
        if (localVarResponse.statusCode()/ 100 != 2) {
          throw getApiException("rotateTenantKeys", localVarResponse);
        }
        if (localVarResponse.body() == null) {
          return new ApiResponse<RotateTenantKeysResponse>(
              localVarResponse.statusCode(),
              localVarResponse.headers().map(),
              null
          );
        }

        String responseBody = new String(localVarResponse.body().readAllBytes());
        localVarResponse.body().close();

        return new ApiResponse<RotateTenantKeysResponse>(
            localVarResponse.statusCode(),
            localVarResponse.headers().map(),
            responseBody.isBlank()? null: memberVarObjectMapper.readValue(responseBody, new TypeReference<RotateTenantKeysResponse>() {})
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

  private HttpRequest.Builder rotateTenantKeysRequestBuilder() throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/api/v1/tenants/rotate-keys";

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

}
