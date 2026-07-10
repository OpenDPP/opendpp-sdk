# OpenDPP Java SDK

Official **Java SDK** for the [OpenDPP](https://opendpp-node.eu) Digital Product Passport API — a
fully-typed client for every endpoint, **generated from the public OpenAPI contract** and
**version-locked** to it (this artifact's version **is** the API contract version). Java 17+, built on
the JDK's `java.net.http.HttpClient` with Jackson.

> Part of the OpenDPP **open client** surface (Apache-2.0). The SDK is **ergonomics only** — it embeds
> no tier/masking logic and no restricted-key knowledge; every privileged operation is a typed call to
> the hosted node behind your Developer-Plan key. A rival could regenerate an equivalent from the
> public spec in an afternoon — the value is the hosted node it calls, not the client.

## Install

Coordinates: `eu.opendpp-node:opendpp-sdk`.

Gradle (Kotlin DSL):

```kotlin
implementation("eu.opendpp-node:opendpp-sdk:1.11.0")
```

Maven:

```xml
<dependency>
  <groupId>eu.opendpp-node</groupId>
  <artifactId>opendpp-sdk</artifactId>
  <version>1.11.0</version>
</dependency>
```

## Quick start

```java
import eu.opendppnode.sdk.OpenDpp;
import eu.opendppnode.sdk.api.PassportsApi;
import eu.opendppnode.sdk.api.ServiceApi;
import eu.opendppnode.sdk.invoker.ApiClient;

ApiClient client = OpenDpp.client(System.getenv("OPENDPP_API_KEY"));

// Public, no key needed:
var health = new ServiceApi(client).getHealth();

// Behind your Developer-Plan key:
var created = new PassportsApi(client).createPassport(request);
```

`OpenDpp.client(...)` defaults the base URL to the public node (`https://opendpp-node.eu`) and sends
the key as `Authorization: Bearer …` on operations that require it; public operations work without one.
Each API tag is its own `*Api` class (`PassportsApi`, `WebhooksApi`, `PublicResolutionApi`,
`BatteryUnitsApi`, …), and every request/response shape is a generated model under
`eu.opendppnode.sdk.model`.

## Kotlin

The same artifact is the Kotlin SDK — no separate package. Getters surface as properties and the
builder-style models read naturally:

```kotlin
import eu.opendppnode.sdk.OpenDpp
import eu.opendppnode.sdk.api.PublicResolutionApi
import eu.opendppnode.sdk.api.ServiceApi

val client = OpenDpp.client(System.getenv("OPENDPP_API_KEY"))

val health = ServiceApi(client).health                                  // public, no key needed
val passport = PublicResolutionApi(client).resolvePublicPassport(id, null)
println(passport.productId)
```

## Versioning

`opendpp-sdk` is **version-locked to `OPENAPI_VERSION`** — e.g. `1.11.0` targets API contract `1.11.0`.
The generated client under `src/main/java` is committed and CI-checked against `openapi.json` (a drift
guard: regenerating must produce no diff). Use the SDK major that matches the `/api/v1` major you call.

## How it's built

Mechanically generated from [`openapi.json`](https://opendpp-node.eu/openapi.json) with
[OpenAPI Generator](https://openapi-generator.tech) (`java` / `native` library) via a pinned Gradle
plugin — `./gradlew openApiGenerate`. The published contract is OpenAPI 3.1; a small, reproducible
pre-generation step in `build.gradle.kts` normalizes the generation input (the committed spec stays
pristine):

- drops the inbound `webhooks:` callbacks — a client never *calls* those, and they collide with the
  webhook-management operations (the `WebhookEnvelope` payload model is still generated);
- relaxes the 3.1 constructs the Java generator can't yet model (`@context`, free-form `payload`);
- strips `additionalProperties` from typed schemas — otherwise models generate as `Map` subtypes,
  which Jackson deserializes as maps and every typed getter silently returns `null` (the flattened
  JSON-LD root keys are documented duplicates of `metadata`, so no data is lost);
- pins the content-negotiated resolvers to their JSON representation, so the server can never answer a
  typed call with AAS / VC-JWT / HTML (those representations, like the QR/label binaries, are fetched
  by plain URL — the SDK models the JSON surface).

Nothing here re-implements a server-side capability. Only the ergonomic `OpenDpp` factory
(`src/handwritten/java`) is hand-written. `OPENDPP_LIVE_TEST=1 ./gradlew test` additionally runs a live
integration suite against the public hosted node (public endpoints only, no key).

## Publishing (maintainers)

Released to Maven Central (via the Central Portal) when a `sdk-java-<semver>` tag is pushed. Maven
Central has no OIDC trusted publishing, so the workflow GPG-signs the artifacts and authenticates with a
Central Portal token, supplied as `release`-environment secrets: `MAVEN_CENTRAL_USERNAME`,
`MAVEN_CENTRAL_PASSWORD`, `GPG_SIGNING_KEY` (ASCII-armored private key), and `GPG_SIGNING_PASSWORD`.

## License

[Apache-2.0](./LICENSE) © Opendpp UAB. See [`NOTICE`](./NOTICE). "OpenDPP" is a trademark of Opendpp
UAB; this license grants no rights to the marks.
