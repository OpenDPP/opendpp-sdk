#!/usr/bin/env python3
# Version lock: opendpp-sdk's MAJOR.MINOR MUST equal the vendored openapi.json's info.version
# (OPENAPI_VERSION) — the PATCH digit is the SDK's own lane, so a client-only fix can ship against
# the same contract (e.g. 1.11.1 targets contract 1.11.0). The SDK patch must never sit BELOW the
# contract's (that would claim a contract state older than the vendored spec). Run in CI and before
# publish. Exits non-zero on violation. Port of typescript/scripts/check-version-lock.mjs, plus one
# python-specific check: the generated code embeds the version at generation time (__version__ /
# User-Agent), so it must match pyproject.toml or the wheel misreports itself.
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def parse(label: str, version: str) -> tuple[int, int, int]:
    m = re.fullmatch(r"(\d+)\.(\d+)\.(\d+)", version)
    if not m:
        print(f"✗ version lock FAILED: {label} version {version!r} is not plain MAJOR.MINOR.PATCH", file=sys.stderr)
        sys.exit(1)
    return tuple(int(g) for g in m.groups())  # type: ignore[return-value]


pyproject = (ROOT / "pyproject.toml").read_text(encoding="utf-8")
pkg_match = re.search(r'^version = "([^"]+)"$', pyproject, re.MULTILINE)
if not pkg_match:
    print("✗ version lock FAILED: no version field in pyproject.toml", file=sys.stderr)
    sys.exit(1)
pkg_version = pkg_match.group(1)

spec_version = json.loads((ROOT / "openapi.json").read_text(encoding="utf-8"))["info"]["version"]

pkg_major, pkg_minor, pkg_patch = parse("pyproject.toml", pkg_version)
spec_major, spec_minor, spec_patch = parse("openapi.json info.version", spec_version)

if (pkg_major, pkg_minor) != (spec_major, spec_minor):
    print(f"✗ version lock FAILED: pyproject.toml {pkg_version} != openapi.json info.version {spec_version} (major.minor)", file=sys.stderr)
    print("  The SDK's major.minor is locked to the API contract — bump pyproject.toml to match the vendored openapi.json (and regenerate).", file=sys.stderr)
    sys.exit(1)
if pkg_patch < spec_patch:
    print(f"✗ version lock FAILED: pyproject.toml {pkg_version} is BEHIND openapi.json info.version {spec_version}", file=sys.stderr)
    print("  The SDK patch digit may exceed the contract's (SDK-only fixes) but never trail it.", file=sys.stderr)
    sys.exit(1)

generated = (ROOT / "opendpp_sdk" / "__init__.py").read_text(encoding="utf-8")
gen_match = re.search(r'^__version__ = "([^"]+)"$', generated, re.MULTILINE)
if not gen_match or gen_match.group(1) != pkg_version:
    found = gen_match.group(1) if gen_match else "<absent>"
    print(f"✗ version lock FAILED: generated __version__ {found} != pyproject.toml {pkg_version}", file=sys.stderr)
    print("  The generated code stamps the version at generation time — run scripts/regenerate.sh after a version change.", file=sys.stderr)
    sys.exit(1)

print(f"✓ version lock: opendpp-sdk {pkg_version} targets contract {spec_version} (major.minor locked; patch is the SDK fix lane)")
