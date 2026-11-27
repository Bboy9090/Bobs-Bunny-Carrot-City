#!/usr/bin/env python3
"""Zero Illusion Engineering Standard (ZIES v1.0) — Executable Release Gate."""
from __future__ import annotations

import hashlib
import plistlib
import re
import subprocess
import sys
import tempfile
from pathlib import Path

REPO_DIR = Path(__file__).resolve().parent.parent

REQUIRED_DOCS = (
    "INTEGRITY_POLICY.md",
    "ZERO_ILLUSION_STANDARD.md",
    "REALITY_AUDIT.md",
    "AGENTS.md",
    "CLAUDE.md",
    "GEMINI.md",
    ".agents/rules/zies.md",
    ".cursor/rules/zies.mdc",
    ".github/copilot-instructions.md",
)

EXPECTED_ABC_SHA256 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def check_required_docs(failures: list[str]) -> None:
    for rel in REQUIRED_DOCS:
        p = REPO_DIR / rel
        if not p.is_file() or p.stat().st_size == 0:
            failures.append(f"Missing required ZIES deliverable: {rel}")


def check_known_answer_and_negative_io(failures: list[str]) -> None:
    missing = REPO_DIR / "__definitely_missing_zies_sentinel__.bin"
    if missing.exists():
        failures.append("Sentinel file unexpectedly exists")
    else:
        try:
            sha256_file(missing)
            failures.append("Negative control failed: reading missing file did not raise")
        except FileNotFoundError:
            pass

    with tempfile.NamedTemporaryFile(delete=False) as tf:
        tf.write(b"abc")
        tpath = Path(tf.name)
    try:
        got = sha256_file(tpath)
        if got != EXPECTED_ABC_SHA256:
            failures.append(f"FIPS 180-4 SHA-256('abc') mismatch: got {got}")
    finally:
        tpath.unlink(missing_ok=True)


def scan_patterns(text: str, rel_label: str, rules: list[tuple[str, str]]) -> list[str]:
    hits: list[str] = []
    for pat, msg in rules:
        if re.search(pat, text):
            hits.append(f"ZIES violation in {rel_label}: {msg}")
    return hits

FORBIDDEN_RULES = [
    (r"DESKTOP-RCHRVGF|192\.168\.56\.20", "Hardcoded mock lab host/IP in production code"),
    (r"True-Peak|dBTP", "Unverified audio label in source"),
]


def evaluate(self_test_negative: bool = False) -> list[str]:
    failures: list[str] = []
    if self_test_negative:
        hits = scan_patterns("True-Peak 192.168.56.20", "<negative-control>", FORBIDDEN_RULES)
        if len(hits) < 2:
            failures.append("Self-test negative failed to detect poisoned patterns")
        return failures

    check_required_docs(failures)
    check_known_answer_and_negative_io(failures)

    for p in list((REPO_DIR / "apple-port").glob("*.swift")):
        txt = p.read_text(encoding="utf-8", errors="ignore")
        failures.extend(scan_patterns(txt, f"apple-port/{p.name}", FORBIDDEN_RULES))

    priv = REPO_DIR / "apple-port" / "PrivacyInfo.xcprivacy"
    with priv.open("rb") as pf:
        if plistlib.load(pf).get("NSPrivacyTracking") is not False:
            failures.append("PrivacyInfo.xcprivacy NSPrivacyTracking must be False")

    info_mac = REPO_DIR / "apple-port" / "Info-macOS.plist"
    with info_mac.open("rb") as pf:
        pl = plistlib.load(pf)
        if pl.get("CFBundleIconFile") != "AppIcon" or pl.get("ITSAppUsesNonExemptEncryption") is not False:
            failures.append("Info-macOS.plist missing AppIcon or ITSAppUsesNonExemptEncryption=False")

    if not (REPO_DIR / "apple-port" / "AppIcon.icns").is_file():
        failures.append("Missing apple-port/AppIcon.icns")

    android_manifest = (REPO_DIR / "app" / "src" / "main" / "AndroidManifest.xml").read_text(encoding="utf-8")
    if 'usesCleartextTraffic="false"' not in android_manifest:
        failures.append("AndroidManifest.xml missing android:usesCleartextTraffic=\'false\'")

    with tempfile.TemporaryDirectory() as tmpd:
        bin_p = Path(tmpd) / "verify_bunny"
        r1 = subprocess.run(
            ["swiftc", "-parse-as-library", str(REPO_DIR / "apple-port" / "CarrotStyleComboEngine.swift"), str(REPO_DIR / "apple-port" / "verify_apple_port.swift"), "-o", str(bin_p)],
            capture_output=True, text=True
        )
        if r1.returncode != 0:
            failures.append(f"verify_apple_port compile failed: {r1.stderr}")
        else:
            r2 = subprocess.run([str(bin_p)], capture_output=True, text=True)
            if r2.returncode != 0 or "PASS:" not in r2.stdout:
                failures.append(f"verify_apple_port execution failed: {r2.stdout} {r2.stderr}")
    return failures


def main() -> int:
    self_neg = "--self-test-negative" in sys.argv
    failures = evaluate(self_test_negative=self_neg)
    if failures:
        print(f"[ZIES GATE FAILED] {REPO_DIR.name} ({len(failures)} issue(s)):")
        for f in failures:
            print("  -", f)
        return 1
    mode = " (NEGATIVE CONTROL DETECTOR VERIFIED)" if self_neg else ""
    print(f"[ZIES GATE PASSED] {REPO_DIR.name}{mode}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
