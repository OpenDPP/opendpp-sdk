#!/usr/bin/env bash
# Regenerate the Python client from the vendored contract — the lane's ONE regeneration entrypoint
# (CI's drift guard, opendpp-node's sdk-regen-verify.ts and humans all run this; parity with the TS
# lane's `npm run generate` and the Java lane's `./gradlew openApiGenerate`).
#
# Steps:
#   1. normalize: the pristine openapi.json is rewritten by the SHARED normalizer
#      (../scripts/normalize-spec.mjs — authored + unit-tested in opendpp-node, synced here like
#      CHANGELOG.md) into the generation input. The vendored spec itself is never normalized: it
#      drives the version lock and the drift check.
#   2. wipe: openapi-generator never deletes, so a contract-REMOVED type would leave a stale module.
#      The two hand-authored files inside the package (ergonomics.py, py.typed) are excluded — keep
#      that list in sync with .openapi-generator-ignore and pyproject's wheel includes.
#   3. generate: openapi-generator 7.12.0 (pinned in openapitools.json; the npx wrapper version is
#      pinned here), stamping the lane version from pyproject.toml into the generated code.
#
# Needs: node on PATH (the normalizer + npx) and a JRE (the generator; CI sets up Temurin 17).
# `disallowAdditionalPropertiesIfNotPresent=false` is LOAD-BEARING: the generator's default treats an
# ABSENT additionalProperties as false, which would render pydantic models extra="forbid" — exactly
# the deserialization brittleness the shared normalizer exists to prevent.
set -euo pipefail
cd "$(dirname "$0")/.."

VERSION="$(sed -n 's/^version = "\(.*\)"$/\1/p' pyproject.toml | head -1)"
if [ -z "$VERSION" ]; then
  echo "could not read the lane version from pyproject.toml" >&2
  exit 1
fi

node ../scripts/normalize-spec.mjs openapi.json build/openapi-generator/openapi.json

find opendpp_sdk -mindepth 1 -not -name ergonomics.py -not -name py.typed -delete

# --name-mappings is LOAD-BEARING too: the JSON-LD documents carry both `@id` and `id` (and the
# proof both `@type` and `type`). The generator strips the `@`, emitting two class attributes with
# ONE name — the second silently shadows the first and the model rejects real payloads. Mapping the
# three JSON-LD keywords to `at_*` keeps every attribute distinct (the wire alias stays `@id` etc.).
npx --yes @openapitools/openapi-generator-cli@2.40.1 generate \
  -i build/openapi-generator/openapi.json \
  -g python \
  -o . \
  --name-mappings "@id=at_id,@type=at_type,@context=at_context" \
  --additional-properties=packageName=opendpp_sdk,projectName=opendpp-sdk,packageVersion="$VERSION",hideGenerationTimestamp=true,disallowAdditionalPropertiesIfNotPresent=false,generateSourceCodeOnly=true

echo "✓ regenerated opendpp_sdk at $VERSION from openapi.json ($(node -p "require('./openapi.json').info.version"))"
