#!/usr/bin/env python3
"""Deterministic Android identity preflight; no network or secret disclosure."""
import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def inspect(root: Path, profile: str) -> list[str]:
    problems = []
    gradle = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
    catalog = (root / "gradle/libs.versions.toml").read_text(encoding="utf-8")
    identity = re.search(r'\\bapplicationId\\s*=\\s*"([^"]+)"', gradle)
    namespace = re.search(r'\\bnamespace\\s*=\\s*"([^"]+)"', gradle)
    if not identity or not namespace:
        return ["Missing explicit applicationId or namespace"]
    app_id, package_ns = identity.group(1), namespace.group(1)
    if not re.fullmatch(r'[A-Za-z_][\\w]*(?:\\.[A-Za-z_][\\w]*)+', app_id):
        problems.append("Invalid Android applicationId syntax")
    if not re.fullmatch(r'[A-Za-z_][\\w]*(?:\\.[A-Za-z_][\\w]*)+', package_ns):
        problems.append("Invalid Android namespace syntax")
    for label in ("minSdk", "targetSdk"):
        if not re.search(rf'\\b{label}\\s*=\\s*\\d+', gradle):
            problems.append(f"Missing numeric {label}")
    min_sdk = re.search(r'\\bminSdk\\s*=\\s*(\\d+)', gradle)
    target_sdk = re.search(r'\\btargetSdk\\s*=\\s*(\\d+)', gradle)
    if min_sdk and target_sdk and int(min_sdk.group(1)) > int(target_sdk.group(1)):
        problems.append("minSdk exceeds targetSdk")
    if re.search(r'(?m)^\\s*(?:firebaseAi|[\\w-]*firebase[\\w-]*)\\s*=\\s*"[^"]*\\+', catalog, re.I):
        problems.append("Unpinned Firebase dependency version")

    # Development builds may use a temporary applicationId but must never
    # carry an enabled Google Services plugin without a matching registration.
    using_google = "libs.plugins.google.gms.google.services" in gradle
    firebase_path = root / "app/google-services.json"
    if using_google:
        if not firebase_path.is_file():
            problems.append("Google Services plugin active but no Firebase config")
        else:
            try:
                registration = json.loads(firebase_path.read_text(encoding="utf-8"))
                registered = {
                    c["client_info"]["android_client_info"]["package_name"]
                    for c in registration.get("client", [])
                }
                if app_id not in registered:
                    problems.append("Firebase Android registration differs from applicationId")
            except (ValueError, KeyError, TypeError):
                problems.append("Malformed Firebase Android config")

    if profile == "release":
        if app_id.startswith(("com.example.", "org.example.")):
            problems.append("Store build uses a placeholder applicationId")
        # An unsigned release APK/AAB must never be called Play-ready.
        release_block = gradle.partition("release {")[2].partition("\n        }")[0]
        if not release_block or "signingConfig" not in release_block:
            problems.append("No explicit release signing configuration in Gradle")
        if using_google and firebase_path.is_file():
            try:
                project_id = registration["project_info"]["project_id"]
                if project_id.startswith("com-example-"):
                    problems.append("Firebase project still has sample/demo identity")
            except (KeyError, TypeError, UnboundLocalError):
                problems.append("Cannot validate Firebase project ID")

    return problems


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--profile", choices=["debug", "release"], required=True)
    args = parser.parse_args()
    failures = inspect(ROOT, args.profile)
    if failures:
        print(f"{args.profile.upper()} PRE-FLIGHT: BLOCKED")
        for message in failures:
            print(f" - {message}")
        sys.exit(1)
    print(f"{args.profile.upper()} PRE-FLIGHT: configuration checks passed")
    if args.profile == "release":
        print("NOTICE: art approval, physical-device QA and store account review are separate mandatory gates")
