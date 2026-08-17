# opendpp-sdk — Official Python SDK for the OpenDPP Digital Product Passport API.
#
# The ONLY hand-written module in this package: everything else under opendpp_sdk/ is mechanically
# generated from the public OpenAPI contract (openapi.json) and version-locked to it. This module is
# **ergonomics only** — it embeds no tier/masking logic and no restricted-key knowledge; every
# privileged operation is simply a typed call to the hosted node behind your Developer-Plan key. It
# leaks nothing the public spec doesn't already. (Parity with the TypeScript lane's src/index.ts.)
#
# Copyright (c) Opendpp UAB.
# SPDX-License-Identifier: Apache-2.0
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is
# distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
# implied. See the License for the specific language governing permissions and limitations.
#
# "OpenDPP" is a trademark of Opendpp UAB; the Apache-2.0 license grants no rights to the marks.
#
# ---------------------------------------------------------------------------
# Content-negotiated public resolvers
#
# The 200 of `GET /passport/{id}`, `/01/{gtin14}`, `/8003/{grai}` and `/unit/{id}` is negotiated via
# the `Accept` header (JSON-LD default / AAS / VC-JWT / VC-LD / SD-JWT-VC / HTML), but the generated
# operations are typed against the default JSON-LD document only — their generation input prunes the
# alternate representations so the default `Accept` can never make the server answer a shape the
# typed model doesn't match. The `*_as` helpers below are the supported way to request an alternate
# representation: they set the `Accept` header AND the matching body parsing, and type the result per
# media type. Passing a hand-rolled `Accept` via `_headers` to a generated resolver operation would
# silently mismatch its declared response type.
# ---------------------------------------------------------------------------
from __future__ import annotations

import json
from typing import Any, Dict, Literal, Optional, Union, overload

from opendpp_sdk.api.public_resolution_api import PublicResolutionApi
from opendpp_sdk.api_client import ApiClient
from opendpp_sdk.configuration import Configuration
from opendpp_sdk.exceptions import ApiException
from opendpp_sdk.models.passport_aas_environment import PassportAasEnvironment
from opendpp_sdk.models.public_battery_unit_json_ld import PublicBatteryUnitJsonLd
from opendpp_sdk.models.public_passport_json_ld import PublicPassportJsonLd

__all__ = [
    "BatteryUnitRepresentation",
    "PassportRepresentation",
    "create_opendpp_client",
    "resolve_gs1_grai_as",
    "resolve_gs1_gtin_as",
    "resolve_public_battery_unit_as",
    "resolve_public_passport_as",
]

#: Representations negotiable on ``GET /passport/{id}``, ``GET /01/{gtin14}`` and
#: ``GET /8003/{grai}``: the JSON-LD passport document, the role-filtered AAS environment, a UNTP
#: credential as a compact JWS string (``vc+jwt``), the same credential with an embedded Data
#: Integrity proof (``vc+ld+json``), an SD-JWT-VC string (``dc+sd-jwt``), or the server-rendered
#: HTML page.
PassportRepresentation = Literal[
    "application/ld+json",
    "application/aas+json",
    "application/vc+jwt",
    "application/vc+ld+json",
    "application/dc+sd-jwt",
    "text/html",
]

#: Representations negotiable on ``GET /unit/{id}`` — same as passports, minus AAS (not offered there).
BatteryUnitRepresentation = Literal[
    "application/ld+json",
    "application/vc+jwt",
    "application/vc+ld+json",
    "application/dc+sd-jwt",
    "text/html",
]


def create_opendpp_client(base_url: Optional[str] = None, api_key: Optional[str] = None) -> ApiClient:
    """Create a configured OpenDPP API client.

    Pass the returned client to any generated API class — e.g.
    ``PassportsApi(create_opendpp_client(api_key=...))``. Ergonomics only: this just wires the base
    URL (defaulting to the public hosted node, ``https://opendpp-node.eu``) and bearer auth. Every
    privileged operation remains a call to the hosted node behind your Developer-Plan key; public
    operations work without one.

    Example::

        from opendpp_sdk.api.service_api import ServiceApi
        from opendpp_sdk.ergonomics import create_opendpp_client

        client = create_opendpp_client(api_key=os.environ["OPENDPP_API_KEY"])
        health = ServiceApi(client).get_health()
    """
    configuration = Configuration(host=base_url, access_token=api_key)
    return ApiClient(configuration)


def _fetch_as(resolver_call: Any, accept: str, kwargs: Dict[str, Any]) -> Union[str, Dict[str, Any], Any]:
    """Run a resolver's ``*_without_preload_content`` variant with an explicit ``Accept`` and parse
    the body per media type: ``+json`` representations parse as JSON, ``vc+jwt``/``dc+sd-jwt``/HTML
    are text on the wire (mirrors the TypeScript lane's ``parseAsFor``)."""
    response = resolver_call(_headers={"Accept": accept}, **{k: v for k, v in kwargs.items() if v is not None})
    body = response.read().decode("utf-8")
    if not 200 <= response.status <= 299:
        raise ApiException.from_response(http_resp=response, body=body, data=None)
    if accept == "application/ld+json" or accept == "application/aas+json":
        return body  # deserialized into the typed model by the caller
    if accept.endswith("+json"):
        return json.loads(body)
    return body


@overload
def resolve_public_passport_as(client: ApiClient, id: str, accept: Literal["application/ld+json"], *, grant: Optional[str] = None) -> PublicPassportJsonLd: ...
@overload
def resolve_public_passport_as(client: ApiClient, id: str, accept: Literal["application/aas+json"], *, grant: Optional[str] = None) -> PassportAasEnvironment: ...
@overload
def resolve_public_passport_as(client: ApiClient, id: str, accept: Literal["application/vc+ld+json"], *, grant: Optional[str] = None) -> Dict[str, Any]: ...
@overload
def resolve_public_passport_as(client: ApiClient, id: str, accept: Literal["application/vc+jwt", "application/dc+sd-jwt", "text/html"], *, grant: Optional[str] = None) -> str: ...
def resolve_public_passport_as(client: ApiClient, id: str, accept: PassportRepresentation, *, grant: Optional[str] = None) -> Union[PublicPassportJsonLd, PassportAasEnvironment, Dict[str, Any], str]:
    """``GET /passport/{id}`` in an explicitly chosen representation, correctly parsed and typed."""
    api = PublicResolutionApi(client)
    body = _fetch_as(api.resolve_public_passport_without_preload_content, accept, {"id": id, "grant": grant})
    if accept == "application/ld+json":
        return PublicPassportJsonLd.from_json(body)
    if accept == "application/aas+json":
        return PassportAasEnvironment.from_json(body)
    return body


@overload
def resolve_gs1_gtin_as(client: ApiClient, gtin14: str, accept: Literal["application/ld+json"], *, grant: Optional[str] = None) -> PublicPassportJsonLd: ...
@overload
def resolve_gs1_gtin_as(client: ApiClient, gtin14: str, accept: Literal["application/aas+json"], *, grant: Optional[str] = None) -> PassportAasEnvironment: ...
@overload
def resolve_gs1_gtin_as(client: ApiClient, gtin14: str, accept: Literal["application/vc+ld+json"], *, grant: Optional[str] = None) -> Dict[str, Any]: ...
@overload
def resolve_gs1_gtin_as(client: ApiClient, gtin14: str, accept: Literal["application/vc+jwt", "application/dc+sd-jwt", "text/html"], *, grant: Optional[str] = None) -> str: ...
def resolve_gs1_gtin_as(client: ApiClient, gtin14: str, accept: PassportRepresentation, *, grant: Optional[str] = None) -> Union[PublicPassportJsonLd, PassportAasEnvironment, Dict[str, Any], str]:
    """``GET /01/{gtin14}`` in an explicitly chosen representation, correctly parsed and typed."""
    api = PublicResolutionApi(client)
    body = _fetch_as(api.resolve_gs1_gtin_without_preload_content, accept, {"gtin14": gtin14, "grant": grant})
    if accept == "application/ld+json":
        return PublicPassportJsonLd.from_json(body)
    if accept == "application/aas+json":
        return PassportAasEnvironment.from_json(body)
    return body


@overload
def resolve_gs1_grai_as(client: ApiClient, grai: str, accept: Literal["application/ld+json"], *, grant: Optional[str] = None) -> PublicPassportJsonLd: ...
@overload
def resolve_gs1_grai_as(client: ApiClient, grai: str, accept: Literal["application/aas+json"], *, grant: Optional[str] = None) -> PassportAasEnvironment: ...
@overload
def resolve_gs1_grai_as(client: ApiClient, grai: str, accept: Literal["application/vc+ld+json"], *, grant: Optional[str] = None) -> Dict[str, Any]: ...
@overload
def resolve_gs1_grai_as(client: ApiClient, grai: str, accept: Literal["application/vc+jwt", "application/dc+sd-jwt", "text/html"], *, grant: Optional[str] = None) -> str: ...
def resolve_gs1_grai_as(client: ApiClient, grai: str, accept: PassportRepresentation, *, grant: Optional[str] = None) -> Union[PublicPassportJsonLd, PassportAasEnvironment, Dict[str, Any], str]:
    """``GET /8003/{grai}`` in an explicitly chosen representation, correctly parsed and typed."""
    api = PublicResolutionApi(client)
    body = _fetch_as(api.resolve_gs1_grai_without_preload_content, accept, {"grai": grai, "grant": grant})
    if accept == "application/ld+json":
        return PublicPassportJsonLd.from_json(body)
    if accept == "application/aas+json":
        return PassportAasEnvironment.from_json(body)
    return body


@overload
def resolve_public_battery_unit_as(client: ApiClient, id: str, accept: Literal["application/ld+json"], *, grant: Optional[str] = None) -> PublicBatteryUnitJsonLd: ...
@overload
def resolve_public_battery_unit_as(client: ApiClient, id: str, accept: Literal["application/vc+ld+json"], *, grant: Optional[str] = None) -> Dict[str, Any]: ...
@overload
def resolve_public_battery_unit_as(client: ApiClient, id: str, accept: Literal["application/vc+jwt", "application/dc+sd-jwt", "text/html"], *, grant: Optional[str] = None) -> str: ...
def resolve_public_battery_unit_as(client: ApiClient, id: str, accept: BatteryUnitRepresentation, *, grant: Optional[str] = None) -> Union[PublicBatteryUnitJsonLd, Dict[str, Any], str]:
    """``GET /unit/{id}`` in an explicitly chosen representation, correctly parsed and typed."""
    api = PublicResolutionApi(client)
    body = _fetch_as(api.resolve_public_battery_unit_without_preload_content, accept, {"id": id, "grant": grant})
    if accept == "application/ld+json":
        return PublicBatteryUnitJsonLd.from_json(body)
    return body
