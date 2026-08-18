# opendpp-sdk tests — offline smoke: the package imports, the version is single-sourced, and the
# models tolerate additive server changes (the failure class the shared spec normalizer exists to
# prevent: `additionalProperties: false` would have rendered pydantic `extra="forbid"` and rejected
# real payloads the moment the server added a field).
import re
from pathlib import Path

import opendpp_sdk
from opendpp_sdk.models.health_status import HealthStatus
from opendpp_sdk.models.service_version import ServiceVersion


def test_package_version_matches_pyproject() -> None:
    pyproject = (Path(__file__).resolve().parent.parent / "pyproject.toml").read_text(encoding="utf-8")
    declared = re.search(r'^version = "([^"]+)"$', pyproject, re.MULTILINE)
    assert declared is not None
    assert opendpp_sdk.__version__ == declared.group(1)


def test_models_parse_known_payloads() -> None:
    health = HealthStatus.from_dict(
        {"status": "OK", "service": "OpenDPP B2B Enterprise Engine", "timestamp": "2026-08-17T00:00:00.000Z", "apiVersion": "1.13.0", "commit": "abc123", "builtAt": "2026-08-17T00:00:00Z"}
    )
    assert health is not None and health.status == "OK"
    version = ServiceVersion.from_dict({"apiVersion": "1.13.0", "commit": "abc123", "builtAt": "2026-08-17T00:00:00Z"})
    assert version is not None and version.api_version == "1.13.0"


def test_models_tolerate_unknown_response_fields() -> None:
    # A future MINOR server release may add a response field; deployed clients must not crash on it.
    # The generated models capture unknowns in `additional_properties` (lossless) instead of raising.
    payload = {"apiVersion": "1.13.0", "commit": "abc123", "builtAt": "2026-08-17T00:00:00Z", "addedInSomeFutureMinor": {"nested": True}}
    version = ServiceVersion.from_dict(payload)
    assert version is not None
    assert version.additional_properties["addedInSomeFutureMinor"] == {"nested": True}
    # and the round-trip keeps the unknown field rather than silently dropping it
    assert version.to_dict()["addedInSomeFutureMinor"] == {"nested": True}


def test_ergonomics_module_is_exposed() -> None:
    from opendpp_sdk.ergonomics import (  # noqa: F401
        create_opendpp_client,
        resolve_gs1_grai_as,
        resolve_gs1_gtin_as,
        resolve_public_battery_unit_as,
        resolve_public_passport_as,
    )

    client = create_opendpp_client()
    assert client.configuration.host == "https://opendpp-node.eu"
    keyed = create_opendpp_client(api_key="op_dpp_token_test")
    assert keyed.configuration.access_token == "op_dpp_token_test"
