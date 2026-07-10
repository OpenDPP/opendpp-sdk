import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    id("org.openapi.generator") version "7.12.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

// ─── Version lock ────────────────────────────────────────────────────────────
// The SDK's MAJOR.MINOR is the API contract version, DERIVED from the vendored openapi.json's
// info.version (OPENAPI_VERSION) so the published artifact can never drift from the contract it
// targets — the same guarantee the TypeScript SDK's check-version-lock script enforces, but here it
// holds by construction. The PATCH digit is the SDK's own lane: to ship a client-only fix against
// the same contract, set sdkPatch to a value above the contract's patch (e.g. contract 1.11.0 +
// sdkPatch = 1 → artifact 1.11.1); leave it null to release exactly the contract version. A new
// contract release (re-vendoring openapi.json) resets sdkPatch to null.
val sdkPatch: Int? = null
val specFile = layout.projectDirectory.file("openapi.json").asFile
val contractVersion: String = run {
    require(specFile.exists()) { "openapi.json not found at ${specFile.absolutePath} — copy the contract in first." }
    val info = (groovy.json.JsonSlurper().parse(specFile) as Map<*, *>)["info"] as? Map<*, *>
    requireNotNull(info?.get("version") as? String) { "could not read info.version from openapi.json" }
}

group = "eu.opendpp-node"
version = sdkPatch?.let { patch ->
    val parts = contractVersion.split(".")
    require(parts.size == 3 && parts.all { it.toIntOrNull() != null }) { "contract version $contractVersion is not plain MAJOR.MINOR.PATCH" }
    require(patch > parts[2].toInt()) { "sdkPatch ($patch) must exceed the contract's patch digit (${parts[2]}) — SDK-only fixes never trail the vendored spec" }
    "${parts[0]}.${parts[1]}.$patch"
} ?: contractVersion

repositories { mavenCentral() }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}

// Maven Central requires a javadoc jar. The generated sources carry spec-derived markdown/HTML in
// their doc comments, which strict doclint rejects — disable lint (the docs still render).
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

// Byte-reproducible archives + JPMS-friendly identity; ship LICENSE/NOTICE inside the jar.
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "eu.opendppnode.sdk",
            "Implementation-Title" to "opendpp-sdk",
            "Implementation-Version" to version,
        )
    }
    metaInf { from("LICENSE", "NOTICE") }
}

// Hand-authored code (the ergonomic OpenDpp entry point) lives in a SEPARATE source dir so the
// generated tree under src/main/java stays 100% generator-owned — it can be wiped and regenerated for
// the drift guard without ever touching anything we wrote by hand.
sourceSets {
    named("main") { java.srcDir("src/handwritten/java") }
}

// The `native` library targets java.net.http.HttpClient (JDK built-in) with Jackson for JSON; the
// only runtime deps are Jackson + the JsonNullable helper, both surfaced on generated public API.
val jackson = "2.18.2"
dependencies {
    api("com.fasterxml.jackson.core:jackson-core:$jackson")
    api("com.fasterxml.jackson.core:jackson-annotations:$jackson")
    api("com.fasterxml.jackson.core:jackson-databind:$jackson")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson")
    api("org.openapitools:jackson-databind-nullable:0.2.6")
    compileOnly("jakarta.annotation:jakarta.annotation-api:3.0.0")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ─── Generation input normalization ─────────────────────────────────────────
// The published contract is OpenAPI 3.1 with a top-level `webhooks:` block (inbound delivery callbacks:
// passport.ingested/sealed/recalled/status_updated/updated). A CLIENT SDK never *calls* those, and the
// Java generator maps them to the SAME WebhooksApi class as the real `Webhooks`-tagged management
// endpoints (create/list/update/delete subscription, rotate-secret, deliveries, test) — so the inbound
// block silently OVERWRITES the management operations. We strip the inbound block from the GENERATION
// INPUT only; the committed openapi.json (which drives the version lock) stays the pristine published
// contract, and the WebhookEnvelope payload model — a named component schema — is still generated so
// consumers can deserialize deliveries. (Groovy JSON ships with Gradle; no extra dependency.)
val generatorSpec = layout.buildDirectory.file("openapi-generator/openapi.json")
val prepareSpec = tasks.register("prepareSpec") {
    inputs.file(specFile)
    outputs.file(generatorSpec)
    doLast {
        @Suppress("UNCHECKED_CAST")
        val doc = groovy.json.JsonSlurper().parse(specFile) as MutableMap<String, Any?>
        doc.remove("webhooks")
        // JSON-LD `@context` is polymorphic (string | array | object). openapi-generator's 3.1 anyOf
        // deserializer emits illegal `List<Object>.class` / `Map<..>.class` for those composed models
        // (EpcisDocumentContext, PassportListItemContextInner, PublicBatteryUnitJsonLdContextInner),
        // which break javac. A typed client never hand-builds @context, so we relax every @context
        // property to a free-form schema (→ Object) in the generation input only.
        fun relax(node: Any?, inComposition: Boolean) {
            when (node) {
                is MutableMap<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val m = node as MutableMap<String, Any?>
                    val props = m["properties"]
                    if (props is MutableMap<*, *> && props.containsKey("@context")) {
                        @Suppress("UNCHECKED_CAST")
                        val p = props as MutableMap<String, Any?>
                        val relaxed = linkedMapOf<String, Any?>()
                        ((p["@context"] as? Map<*, *>)?.get("description"))?.let { relaxed["description"] = it }
                        p["@context"] = relaxed
                    }
                    // A schema with typed `properties` PLUS `additionalProperties` (the JSON-LD documents
                    // flatten metadata onto the root, hence additionalProperties:true) makes the generator
                    // emit `class X extends HashMap` — and Jackson deserializes any Map subtype AS A MAP,
                    // never calling the typed setters: every typed getter silently returns null. Strip
                    // additionalProperties from such schemas so they generate as plain beans; nothing is
                    // lost — the flattened root keys are documented duplicates of the `metadata` object,
                    // which the typed surface still exposes in full.
                    if (props is Map<*, *> && props.isNotEmpty() && m.containsKey("additionalProperties")) {
                        m.remove("additionalProperties")
                    }
                    for ((k, v) in m.toList()) {
                        // Direct anyOf/oneOf/allOf branches are compositions: a `{type:"null"}` branch
                        // there is the standard 3.1 nullable idiom, which the generator handles natively
                        // (typed field + JsonNullable) — those must NOT be collapsed.
                        val comp = v is List<*> && (k == "anyOf" || k == "oneOf" || k == "allOf")
                        if (comp) (v as List<*>).forEach { relax(it, true) } else relax(v, false)
                    }
                    // 3.1 quirks the Java generator mishandles, collapsed to free-form (→ Object, keeping
                    // the description) on property-less schemas:
                    //  • a container multi-type (free-form event `payload`, type:["object","array","null"])
                    //    → the generator emits a Map with an EMPTY value type (`Map<String, >`);
                    //  • a bare `type:"null"` OUTSIDE a composition (PassportListItem.manufacturingFacility,
                    //    always null in the list view) → an un-generated `ModelNull` class reference.
                    val t = m["type"]
                    val containerMulti = t is List<*> && (t.contains("object") || t.contains("array"))
                    val bareNull = t == "null" && !inComposition
                    if ((containerMulti || bareNull) && !m.containsKey("properties")) {
                        val desc = m["description"]
                        m.clear()
                        if (desc != null) m["description"] = desc
                    }
                }
                is List<*> -> node.forEach { relax(it, inComposition) }
            }
        }
        relax(doc, false)
        // The public resolvers are content-negotiated (RFC 7231): their responses offer JSON-LD, AAS,
        // VC-JWT, SD-JWT, and HTML alongside application/json. The generator turns that into an
        // equal-q multi-representation Accept header, so the server may legitimately answer with a
        // representation the typed return model does NOT match — which deserializes as an all-null
        // object (unknown properties are ignored). A typed JSON client can only consume the JSON
        // document, so wherever a response offers application/json among alternates keep ONLY
        // application/json (binary-only responses — QR images, ZIP export — are left untouched).
        // The alternate representations stay reachable by plain URL fetch.
        // NOTE the two generator behaviors this must defeat:
        //  • the Accept header is the UNION of content types across ALL of an operation's responses
        //    (errors and redirects included), so every inline response must be pruned, not just the 2xx;
        //  • the negotiated resolvers' 200 offers NO plain application/json — their JSON document
        //    representation is application/ld+json (the errors are what carry application/json).
        val jsonTypes = listOf("application/json", "application/ld+json")
        val httpMethods = setOf("get", "put", "post", "delete", "options", "head", "patch", "trace")
        val paths = doc["paths"] as? Map<*, *> ?: emptyMap<Any?, Any?>()
        for (pathItem in paths.values) {
            if (pathItem !is Map<*, *>) continue
            for ((method, op) in pathItem) {
                if (method !in httpMethods || op !is Map<*, *>) continue
                val responses = op["responses"] as? Map<*, *> ?: continue
                // Only operations whose SUCCESS representation has a JSON variant — binary-only ops
                // (QR images, ZIP export) and AAS/VC-typed exports keep their declarations untouched.
                val jsonSuccess = responses.any { (code, resp) ->
                    code.toString().startsWith("2") &&
                        (((resp as? Map<*, *>)?.get("content") as? Map<*, *>)?.keys?.any { it in jsonTypes } == true)
                }
                if (!jsonSuccess) continue
                for (resp in responses.values) {
                    if (resp !is MutableMap<*, *>) continue
                    @Suppress("UNCHECKED_CAST")
                    val r = resp as MutableMap<String, Any?>
                    val content = r["content"]
                    if (content !is MutableMap<*, *>) continue
                    @Suppress("UNCHECKED_CAST")
                    val c = content as MutableMap<String, Any?>
                    val pick = jsonTypes.firstOrNull { c.containsKey(it) }
                    // A response with no JSON variant at all (e.g. an HTML-only redirect body) is
                    // dropped from the declaration so it can't pollute the Accept union.
                    if (pick != null) c.keys.retainAll(setOf(pick)) else r.remove("content")
                }
            }
        }
        val out = generatorSpec.get().asFile
        out.parentFile.mkdirs()
        out.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(doc)))
    }
}

// ─── Code generation (committed output; drift-checked in CI) ─────────────────
// Generates the fully-typed client into src/main/java. The output is COMMITTED; CI regenerates and
// fails on any diff (the drift guard), so the checked-in client always matches the contract.
// `hideGenerationTimestamp` keeps the output byte-stable for that guard.
openApiGenerate {
    generatorName.set("java")
    library.set("native")
    inputSpec.set(generatorSpec.map { it.asFile.absolutePath })
    outputDir.set(layout.projectDirectory.asFile.absolutePath)
    invokerPackage.set("eu.opendppnode.sdk.invoker")
    apiPackage.set("eu.opendppnode.sdk.api")
    modelPackage.set("eu.opendppnode.sdk.model")
    generateApiTests.set(false)
    generateApiDocumentation.set(false)
    generateModelTests.set(false)
    generateModelDocumentation.set(false)
    cleanupOutput.set(false)
    configOptions.set(
        mapOf(
            "hideGenerationTimestamp" to "true",
            "useJakartaEe" to "true",
            "serializationLibrary" to "jackson",
            "sourceFolder" to "src/main/java",
            // The API contract treats a NEW output enum value as a backward-compatible MINOR change
            // (docs/contributing/Versioning.md in the service repo). Generated Java enums deserialize
            // through a @JsonCreator that THROWS on unknown values, so without this a routine MINOR
            // server release (e.g. a new advisory `code`) would crash deployed clients mid-response.
            // With it, unknown values map to the UNKNOWN_DEFAULT_OPEN_API sentinel instead.
            "enumUnknownDefaultCase" to "true",
        ),
    )
}

// prepareSpec produces the normalized spec the generator reads; wire the ordering explicitly
// (a plain layout Provider doesn't carry the producer task dependency).
tasks.named("openApiGenerate") { dependsOn(prepareSpec) }

tasks.test { useJUnitPlatform() }

// Emitted for CI logging + release tooling (parity with the TS SDK's version surface).
tasks.register("printVersion") { doLast { println(version) } }

// ─── Publishing (Maven Central via the Central Portal; GPG-signed) ───────────
// Keyless OIDC isn't available on Maven Central, so publish is credentialed: the Central Portal token
// (mavenCentralUsername/Password) + an in-memory GPG key (signingInMemoryKey/Password) are injected in
// CI as ORG_GRADLE_PROJECT_* env vars — never committed. Coordinates: eu.opendpp-node:opendpp-sdk.
mavenPublishing {
    // automaticRelease: validated deployments release without the Central Portal UI click — the same
    // one-gate flow as the TypeScript SDK (tag → approve the `release` environment → live). The
    // manual-release mode was used once, for the first 1.11.0 publish, to eyeball Portal validation.
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
    coordinates(group.toString(), "opendpp-sdk", version.toString())
    pom {
        name.set("OpenDPP Java SDK")
        description.set(
            "Official Java SDK for the OpenDPP Digital Product Passport API — a fully-typed client " +
                "generated from the public OpenAPI contract and version-locked to it.",
        )
        url.set("https://github.com/OpenDPP/opendpp-sdk")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("opendpp")
                name.set("Opendpp UAB")
                email.set("info@opendpp-node.eu")
                url.set("https://opendpp-node.eu")
            }
        }
        scm {
            url.set("https://github.com/OpenDPP/opendpp-sdk")
            connection.set("scm:git:git://github.com/OpenDPP/opendpp-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/OpenDPP/opendpp-sdk.git")
        }
    }
}
