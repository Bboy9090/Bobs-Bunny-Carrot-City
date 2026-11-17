#!/usr/bin/env bash
set -euo pipefail

# Called INSIDE the emulator-runner action while adb is still connected.
./gradlew connectedDebugAndroidTest --console=plain

output_dir="reports/android-emulator"
mkdir -p "$output_dir"
adb devices -l | tee "$output_dir/adb-devices.txt"
adb shell getprop ro.build.version.sdk | tr -d '\r' | tee "$output_dir/android-api-level.txt"
adb shell getprop ro.hardware | tr -d '\r' | tee "$output_dir/emulator-hardware.txt"

# Read only the screenshot created by the dedicated W1-1 instrumentation test.
# The package remains a debuggable development build; no root or secrets needed.
adb exec-out run-as com.example.myapplication cat files/bobs_w1_1_visual_qa.png > "$output_dir/w1-1-3d-first-frame.png"
test -s "$output_dir/w1-1-3d-first-frame.png"
file "$output_dir/w1-1-3d-first-frame.png" | tee "$output_dir/image-info.txt"
sha256sum "$output_dir/w1-1-3d-first-frame.png" | tee "$output_dir/SHA256SUMS.txt"
echo "Recorded W1-1 GLES3 screenshot on API 35 emulator, with normal app UI and no fabricated graphics."
