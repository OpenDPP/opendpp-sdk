/*
 * Copyright (c) Opendpp UAB.
 * SPDX-License-Identifier: Apache-2.0
 */

package eu.opendppnode.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.opendppnode.sdk.api.PublicResolutionApi;
import eu.opendppnode.sdk.api.ServiceApi;
import eu.opendppnode.sdk.invoker.ApiClient;
import eu.opendppnode.sdk.invoker.ApiException;
import eu.opendppnode.sdk.model.HealthStatus;
import eu.opendppnode.sdk.model.MerkleTreeAttestationProof;
import eu.opendppnode.sdk.model.PublicPassportJsonLd;
import eu.opendppnode.sdk.model.ServiceVersion;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Live integration test against the public hosted node — the real proof that the generated client
 * works end-to-end: TLS, base-URL wiring, and Jackson deserialization of REAL payloads into the
 * generated models (enums, the anyOf proof wrapper, OffsetDateTime, the relaxed @context/metadata
 * Objects).
 *
 * Opt-in (network + public rate limits): {@code OPENDPP_LIVE_TEST=1 ./gradlew test}. Uses only
 * public endpoints (no API key) and a curated, stable demo passport listed in the public sitemap.
 * Stays well under the 30 req/min public-resolution limit.
 */
@EnabledIfEnvironmentVariable(named = "OPENDPP_LIVE_TEST", matches = "1")
class OpenDppLiveIT {

    private static final String DEMO_PASSPORT_ID = "demo-batteries-lfp-cell-200";

    private final ApiClient client = OpenDpp.client();

    @Test
    void healthAndVersionRoundTrip() throws ApiException {
        ServiceApi service = new ServiceApi(client);

        HealthStatus health = service.getHealth();
        assertNotNull(health.getStatus(), "health.status");

        ServiceVersion version = service.getApiVersion();
        assertNotNull(version.getApiVersion(), "version.apiVersion");
        assertTrue(version.getApiVersion().startsWith("1."),
                "live contract major should match the SDK's /api/v1 major, got " + version.getApiVersion());
    }

    @Test
    void resolvesDemoPassportIntoTypedModel() throws ApiException {
        PublicPassportJsonLd passport = new PublicResolutionApi(client).resolvePublicPassport(DEMO_PASSPORT_ID, null);

        // Identity + typed fields survived deserialization.
        assertEquals(DEMO_PASSPORT_ID, passport.getId(), "id");
        assertNotNull(passport.getProductId(), "productId");
        assertNotNull(passport.getAtContext(), "@context (relaxed to Object) should still carry the value");
        assertNotNull(passport.getCreatedAt(), "createdAt should parse as OffsetDateTime");

        // The free-form metadata object deserializes as a non-empty JSON map.
        Object metadata = passport.getMetadata();
        Map<?, ?> metadataMap = assertInstanceOf(Map.class, metadata, "metadata");
        assertFalse(metadataMap.isEmpty(), "demo passport metadata should be non-empty");

        // Enum tolerance: the live status must parse into a KNOWN constant, not the unknown sentinel.
        assertNotNull(passport.getStatus(), "status enum");
        assertNotEquals("UNKNOWN_DEFAULT_OPEN_API", passport.getStatus().name(),
                "live status should be a known enum constant");

        // Demo passports are sealed — the typed proof (anyOf with a null branch) must populate.
        MerkleTreeAttestationProof proof = passport.getProof();
        assertNotNull(proof, "sealed demo passport should carry a proof");
        assertNotNull(proof.getMerkleRoot(), "proof.merkleRoot");
    }

    @Test
    void resolvesTheSamePassportThroughTheGs1Path() throws ApiException {
        PublicResolutionApi resolution = new PublicResolutionApi(client);
        PublicPassportJsonLd byId = resolution.resolvePublicPassport(DEMO_PASSPORT_ID, null);

        String gtin = byId.getProductId();
        assertTrue(gtin != null && gtin.matches("\\d{14}"),
                "demo battery passport should be GS1-keyed (14-digit GTIN), got " + gtin);

        PublicPassportJsonLd byGtin = resolution.resolveGs1Gtin(gtin, null);
        assertEquals(byId.getId(), byGtin.getId(),
                "GS1 Digital Link resolution should land on the same passport");
    }

    @Test
    void missingPassportSurfacesTypedApiException() {
        ApiException e = assertThrows(ApiException.class,
                () -> new PublicResolutionApi(client).resolvePublicPassport("definitely-not-a-passport-xyz", null));
        assertEquals(404, e.getCode(), "expected a 404 for a missing passport");
        assertNotNull(e.getResponseBody(), "error body should be captured for diagnostics");
    }
}
