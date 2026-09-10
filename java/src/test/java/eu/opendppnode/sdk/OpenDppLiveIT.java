/*
 * Copyright (c) Opendpp UAB.
 * SPDX-License-Identifier: Apache-2.0
 */

package eu.opendppnode.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
 *
 * <p>SPEC-TOLERANT BY DESIGN, and it has to be: this file is compiled against the client generated
 * from whichever spec is in play, and there are two. This repository's own CI generates from the
 * VENDORED {@code openapi.json}, while opendpp-node's "SDK regen" gate generates from its LIVE
 * contract — so at any moment the two can be a contract apart. A hand-written test that names a
 * generated signature or a generated getter therefore breaks the OTHER lane, which is exactly what
 * happened when contract 1.16.0 added the {@code representation} query parameter to the two public
 * resolvers and moved the passport body out of a {@code metadata} object onto the document root: this
 * file stopped compiling in node's gate and blocked that release, while remaining green here.
 *
 * <p>So resolver calls go through {@link #resolve} — which finds the method by NAME and supplies
 * {@code null} for every parameter after the key, i.e. no grant and the default representation — and
 * an assertion about a field only one spec declares is made through {@link #optionalMap}. Both keep
 * this file compiling against a client generated from either contract. Anything asserted
 * unconditionally below must be true of BOTH.
 */
@EnabledIfEnvironmentVariable(named = "OPENDPP_LIVE_TEST", matches = "1")
class OpenDppLiveIT {

    private static final String DEMO_PASSPORT_ID = "demo-batteries-lfp-cell-200";

    private final ApiClient client = OpenDpp.client();

    /**
     * Call a generated resolver by name, whatever its arity. The key is the first parameter in every
     * contract; everything after it is optional on the wire (a grant token, and since 1.16.0 the
     * {@code representation} flag), so passing {@code null} asks for the anonymous tier and the
     * default compressed document — the same request the two-argument form used to make.
     */
    private PublicPassportJsonLd resolve(PublicResolutionApi api, String name, String key) throws ApiException {
        Method m = Arrays.stream(PublicResolutionApi.class.getMethods())
                .filter(x -> x.getName().equals(name) && x.getReturnType() == PublicPassportJsonLd.class)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "the generated client has no " + name + " returning PublicPassportJsonLd — has the operation been renamed?"));
        Object[] args = new Object[m.getParameterCount()];
        args[0] = key;
        try {
            return (PublicPassportJsonLd) m.invoke(api, args);
        } catch (InvocationTargetException e) {
            // Surface the real failure: the 404 case below asserts on a typed ApiException.
            if (e.getCause() instanceof ApiException cause) {
                throw cause;
            }
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw new AssertionError(name + " failed", e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot invoke " + name, e);
        }
    }

    /**
     * Read a {@code Map}-valued getter that only SOME contracts declare, without naming it at compile
     * time. Returns {@code null} when this client's model has no such property.
     */
    private Map<?, ?> optionalMap(Object model, String getter) {
        try {
            Object value = model.getClass().getMethod(getter).invoke(model);
            return value instanceof Map<?, ?> map ? map : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

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
        PublicPassportJsonLd passport = resolve(new PublicResolutionApi(client), "resolvePublicPassport", DEMO_PASSPORT_ID);

        // Identity + typed fields survived deserialization.
        assertEquals(DEMO_PASSPORT_ID, passport.getId(), "id");
        assertNotNull(passport.getProductId(), "productId");
        assertNotNull(passport.getAtContext(), "@context (relaxed to Object) should still carry the value");
        assertNotNull(passport.getCreatedAt(), "createdAt should parse as OffsetDateTime");

        // Untyped JSON survives deserialization. This is what the old `getMetadata()` assertion was
        // really about, and it is asserted here through a field BOTH contracts declare: `@context` is
        // relaxed to Object in the spec, so a structured value arriving intact proves Jackson handled
        // an untyped shape rather than flattening it to a string.
        assertTrue(passport.getAtContext() instanceof Map || passport.getAtContext() instanceof java.util.List,
                "@context should deserialize as a structured untyped value, got " + passport.getAtContext().getClass());

        // And where the client's model still declares `metadata` — contracts up to 1.15.0, before the
        // body moved onto the document root — the map must be non-empty. Absent on 1.16.0+, and then
        // the body's own elements are root members with no generated getter, so there is nothing to
        // read here: the untyped-deserialization claim above is the part that holds either way.
        Map<?, ?> legacyMetadata = optionalMap(passport, "getMetadata");
        if (legacyMetadata != null) {
            assertFalse(legacyMetadata.isEmpty(), "demo passport metadata should be non-empty");
        }

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
        PublicPassportJsonLd byId = resolve(resolution, "resolvePublicPassport", DEMO_PASSPORT_ID);

        String gtin = byId.getProductId();
        assertTrue(gtin != null && gtin.matches("\\d{14}"),
                "demo battery passport should be GS1-keyed (14-digit GTIN), got " + gtin);

        PublicPassportJsonLd byGtin = resolve(resolution, "resolveGs1Gtin", gtin);
        assertEquals(byId.getId(), byGtin.getId(),
                "GS1 Digital Link resolution should land on the same passport");
    }

    @Test
    void missingPassportSurfacesTypedApiException() {
        ApiException e = assertThrows(ApiException.class,
                () -> resolve(new PublicResolutionApi(client), "resolvePublicPassport", "definitely-not-a-passport-xyz"));
        assertEquals(404, e.getCode(), "expected a 404 for a missing passport");
        assertNotNull(e.getResponseBody(), "error body should be captured for diagnostics");
    }
}
