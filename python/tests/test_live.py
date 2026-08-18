# opendpp-sdk tests — LIVE payload truth, opt-in via OPENDPP_LIVE_TEST=1. This is the check the
# hazard inventory exists for: real responses from the hosted node must parse into the generated
# models — covering the shapes that historically break generated clients (closed-world response
# models, response `const`s, the JSON-LD `@context`/`proof` unions, required-but-nullable fields,
# and the undiscriminated gs1 batch result union). Anonymous rate limits apply (the public resolver
# is 30/min/IP; the gs1 endpoints 2/min/IP) — set OPENDPP_API_KEY to lift them and to cover the
# authenticated per-unit read.
import os

import pytest

pytestmark = pytest.mark.skipif(os.environ.get("OPENDPP_LIVE_TEST") != "1", reason="live test — set OPENDPP_LIVE_TEST=1 (and optionally OPENDPP_API_KEY)")

DEMO_PASSPORT_ID = "demo-batteries-lfp-cell-200"


def _client():
    from opendpp_sdk.ergonomics import create_opendpp_client

    return create_opendpp_client(api_key=os.environ.get("OPENDPP_API_KEY"))


def test_version_and_health_parse() -> None:
    from opendpp_sdk.api.service_api import ServiceApi

    api = ServiceApi(_client())
    version = api.get_api_version()
    assert version.api_version.count(".") == 2
    health = api.get_health()
    assert health.status == "OK"
    assert health.api_version == version.api_version


def test_public_passport_resolves_and_parses() -> None:
    # The JSON-LD document: polymorphic @context, `proof` anyOf, required-but-nullable fields, and
    # metadata keys flattened onto the root (captured via additional_properties, never an error).
    from opendpp_sdk.api.public_resolution_api import PublicResolutionApi

    doc = PublicResolutionApi(_client()).resolve_public_passport(id=DEMO_PASSPORT_ID)
    assert doc.id == DEMO_PASSPORT_ID
    assert doc.product_id
    assert doc.digital_link_uri.startswith("https://")
    assert doc.metadata is not None


def test_ergonomics_negotiates_alternate_representations() -> None:
    from opendpp_sdk.ergonomics import resolve_public_passport_as

    client = _client()
    vc_jwt = resolve_public_passport_as(client, DEMO_PASSPORT_ID, "application/vc+jwt")
    assert isinstance(vc_jwt, str) and vc_jwt.count(".") == 2  # compact JWS
    aas = resolve_public_passport_as(client, DEMO_PASSPORT_ID, "application/aas+json")
    assert aas.asset_administration_shells


def test_gs1_batch_decode_union_resolves_per_item() -> None:
    # The undiscriminated ok/error union: one good and one bad item in a single request (the
    # anonymous per-IP budget on this endpoint is 2 req/min — this test spends one).
    from opendpp_sdk.api.public_resolution_api import PublicResolutionApi

    api = PublicResolutionApi(_client())
    response = api.decode_gs1_batch({"items": [{"elementString": "(01)09501101532007"}, {"digitalLink": "not-a-valid-link"}]})
    assert response.count == 2
    ok, bad = response.results
    assert ok.actual_instance.ok is True
    assert ok.actual_instance.ai["01"] == "09501101532007"
    assert bad.actual_instance.ok is False
    assert bad.actual_instance.error
