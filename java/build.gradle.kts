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

// The generated sources are UTF-8 and carry the contract's own punctuation — em dashes, ellipses and
// curly quotes lifted straight from the OpenAPI descriptions. javac and javadoc default to the JVM's
// PLATFORM encoding, which on a CI runner is US-ASCII: every such character then becomes `?`, javac
// emits an `unmappable character (0xE2)` line per occurrence, and — because those are warnings, not
// failures — the build stays green while the sources and javadoc jars published to Maven Central ship
// the corruption. Pin UTF-8 explicitly so the toolchain reads what the generator actually wrote.
// (opendpp-node#1156.)
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// Maven Central requires a javadoc jar. The generated sources carry spec-derived markdown/HTML in
// their doc comments, which strict doclint rejects — disable lint (the docs still render).
// `encoding` is how javadoc READS the sources; `charSet`/`docEncoding` are how it WRITES the HTML —
// all three are needed, or the jar is corrupted at one end or the other.
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        charSet = "UTF-8"
        docEncoding = "UTF-8"
        addStringOption("Xdoclint:none", "-quiet")
    }
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
// The generation input is the pristine vendored contract rewritten by the SHARED normalizer
// `scripts/normalize-spec.mjs` (repo root) — one tested rewrite for every openapi-generator lane
// (Java here, Python in python/), replacing the private Groovy transform that used to live inline in
// this file. The script documents each transform and the generator failure it prevents (webhooks
// collision, @context polymorphism, additionalProperties → extends HashMap / extra="forbid",
// 3.1 multi-type containers, Accept-union content negotiation). It is AUTHORED in opendpp-node
// (scripts/lib/spec-codegen-normalize.mjs, unit-tested there) and synced into this repo by the same
// mechanism that carries CHANGELOG.md — edit it upstream, never here. The committed openapi.json
// (which drives the version lock) stays the pristine published contract. Requires `node` on PATH
// (already true everywhere this build runs: CI sets up Node for the version-lock step).
val normalizerScript = layout.projectDirectory.file("../scripts/normalize-spec.mjs").asFile
val generatorSpec = layout.buildDirectory.file("openapi-generator/openapi.json")
val prepareSpec = tasks.register<Exec>("prepareSpec") {
    inputs.file(specFile)
    inputs.file(normalizerScript)
    outputs.file(generatorSpec)
    commandLine("node", normalizerScript.absolutePath, specFile.absolutePath, generatorSpec.get().asFile.absolutePath)
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
