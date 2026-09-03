# opendpp-sdk tests — content negotiation, offline. The public resolvers' 200 is Accept-negotiated;
# the generation input prunes the alternates so the DEFAULT Accept can never fetch a representation
# the typed model doesn't match, and the ergonomics `*_as` helpers are the supported way to choose
# one explicitly. Both halves are pinned here by mocking at the module boundary (ApiClient.call_api).
import json
from pathlib import Path
from typing import Any, Dict, Optional
from unittest import mock

import pytest

from opendpp_sdk.api.public_resolution_api import PublicResolutionApi
from opendpp_sdk.exceptions import ApiException
from opendpp_sdk.ergonomics import (
    create_opendpp_client,
    resolve_public_battery_unit_as,
    resolve_public_passport_as,
)
from opendpp_sdk.models.public_passport_json_ld import PublicPassportJsonLd


def _passport_example() -> Dict[str, Any]:
    """The passport document the CONTRACT publishes for this operation, read from the vendored spec.

    This was a frozen literal, and a frozen literal falls behind the contract silently: when the node
    added the EN 18223 header attributes as required properties of `PublicPassportJsonLd`, this fixture
    still omitted them, and the generated pydantic model rejected it — the negotiation tests failed for
    a reason that had nothing to do with negotiation, and the literal had also kept a Digital Link shape
    the resolver stopped emitting. `openapi.json` here is the same file the client is generated from, so
    reading the example out of it makes fixture and model regenerate together and disagree never.
    """
    spec_path = Path(__file__).resolve().parents[1] / "openapi.json"
    spec = json.loads(spec_path.read_text(encoding="utf-8"))
    try:
        return spec["paths"]["/passport/{id}"]["get"]["responses"]["200"]["content"]["application/ld+json"]["example"]
    except KeyError as exc:  # a moved path/representation must name itself, not KeyError deep in a test
        raise AssertionError(
            f"{spec_path} has no 200 application/ld+json example for GET /passport/{{id}} (missing key {exc}); "
            "the contract moved it — point this fixture at the operation's new example."
        ) from exc


PASSPORT_DOC: Dict[str, Any] = _passport_example()


class FakeRestResponse:
    """Duck-types both shapes call_api's callers touch: the RESTResponse wrapper (`.read()`,
    `.status`) and, via `.response`, the raw urllib3 response the `*_without_preload_content`
    variants hand back."""

    def __init__(self, body: str, status: int = 200) -> None:
        self._body = body.encode("utf-8")
        self.status = status
        self.reason = "OK" if status == 200 else "Error"
        self.data = self._body
        self.response = self

    def read(self) -> bytes:
        return self._body

    def getheaders(self) -> Dict[str, str]:
        return {}

    def getheader(self, name: str, default: Optional[str] = None) -> Optional[str]:
        return default


def call_api_capture(captured: Dict[str, Any], body: str, status: int = 200):
    def _call_api(method: str, url: str, header_params: Optional[Dict[str, str]] = None, body_param: Any = None, post_params: Any = None, _request_timeout: Any = None) -> FakeRestResponse:
        captured["method"] = method
        captured["url"] = url
        captured["headers"] = dict(header_params or {})
        return FakeRestResponse(body, status)

    return _call_api


def test_default_accept_is_the_pinned_json_ld() -> None:
    # The generated operation's default Accept comes from the normalized generation input — the
    # JSON-LD document representation, never the HTML/VC alternates whose union would let the
    # server legitimately answer a shape the typed model can't hold (select_header_accept picks the
    # first JSON media type of the pruned declaration).
    client = create_opendpp_client()
    captured: Dict[str, Any] = {}
    with mock.patch.object(client, "call_api", side_effect=call_api_capture(captured, json.dumps(PASSPORT_DOC))):
        PublicResolutionApi(client).resolve_public_passport_without_preload_content(id=PASSPORT_DOC["id"])
    assert captured["headers"]["Accept"] == "application/ld+json"


def test_resolve_as_pins_the_requested_accept_and_types_the_result() -> None:
    client = create_opendpp_client()
    captured: Dict[str, Any] = {}
    with mock.patch.object(client, "call_api", side_effect=call_api_capture(captured, json.dumps(PASSPORT_DOC))):
        doc = resolve_public_passport_as(client, PASSPORT_DOC["id"], "application/ld+json")
    assert captured["headers"]["Accept"] == "application/ld+json"
    assert isinstance(doc, PublicPassportJsonLd)
    assert doc.product_id == "09501101530003"


def test_resolve_as_text_representations_stay_text() -> None:
    client = create_opendpp_client()
    captured: Dict[str, Any] = {}
    jws = "eyJhbGciOiJFUzI1NiJ9.payload.signature"
    with mock.patch.object(client, "call_api", side_effect=call_api_capture(captured, jws)):
        token = resolve_public_passport_as(client, PASSPORT_DOC["id"], "application/vc+jwt")
    assert captured["headers"]["Accept"] == "application/vc+jwt"
    assert token == jws


def test_resolve_as_vc_ld_json_parses_as_plain_json() -> None:
    client = create_opendpp_client()
    captured: Dict[str, Any] = {}
    credential = {"@context": ["https://www.w3.org/ns/credentials/v2"], "type": ["VerifiableCredential"]}
    with mock.patch.object(client, "call_api", side_effect=call_api_capture(captured, json.dumps(credential))):
        doc = resolve_public_passport_as(client, PASSPORT_DOC["id"], "application/vc+ld+json")
    assert captured["headers"]["Accept"] == "application/vc+ld+json"
    assert doc == credential


def test_resolve_as_raises_api_exception_on_error_status() -> None:
    client = create_opendpp_client()
    captured: Dict[str, Any] = {}
    error_body = json.dumps({"error": "Not Found", "message": "No Digital Product Passport found matching identifier: nope"})
    with mock.patch.object(client, "call_api", side_effect=call_api_capture(captured, error_body, status=404)):
        with pytest.raises(ApiException):
            resolve_public_passport_as(client, "nope", "application/ld+json")


def test_battery_unit_helper_pins_accept() -> None:
    client = create_opendpp_client()
    captured: Dict[str, Any] = {}
    with mock.patch.object(client, "call_api", side_effect=call_api_capture(captured, "<!doctype html><html></html>")):
        page = resolve_public_battery_unit_as(client, "0f0e0d0c-0b0a-4908-8706-050403020100", "text/html")
    assert captured["headers"]["Accept"] == "text/html"
    assert isinstance(page, str) and page.startswith("<!doctype html>")
