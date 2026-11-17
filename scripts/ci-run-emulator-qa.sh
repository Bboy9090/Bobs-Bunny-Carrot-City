#!/usr/bin/env bash
set -euo pipefail

# Run inside Android emulator runner while adb is attached. Capture diagnostic
# evidence even when test assertions fail; never override the test exit status.
set +e
./gradlew connectedDebugAndroidTest --console=plain
test_status=$?
set -e

output_dir="reports/android-emulator"
mkdir -p "$output_dir"
adb devices -l | tee "$output_dir/adb-devices.txt" || true
adb shell getprop ro.build.version.sdk | tr -d '\r' > "$output_dir/android-api-level.txt" || true
adb shell getprop ro.opengles.version | tr -d '\r' > "$output_dir/device-gles-version.txt" || true
adb shell getprop ro.hardware | tr -d '\r' > "$output_dir/emulator-hardware.txt" || true
adb logcat -d -s BobsBunnyDiorama:I > "$output_dir/diorama-gl-logcat.txt" || true

# Development package is debuggable; run-as reads only its dedicated QA files.
if adb exec-out run-as com.example.myapplication cat files/bobs_w1_1_render_qa.txt > "$output_dir/w1-1-render-diagnostics.txt"; then
  cat "$output_dir/w1-1-render-diagnostics.txt"
else
  echo "Render test did not produce the diagnostics receipt" > "$output_dir/w1-1-render-diagnostics.txt"
fi

if adb exec-out run-as com.example.myapplication cat files/bobs_w1_1_visual_qa.png > "$output_dir/w1-1-device-capture.png"; then
  if test -s "$output_dir/w1-1-device-capture.png"; then
    file "$output_dir/w1-1-device-capture.png" | tee "$output_dir/image-info.txt"
    sha256sum "$output_dir/w1-1-device-capture.png" | tee "$output_dir/SHA256SUMS.txt"
  fi
else
  rm -f "$output_dir/w1-1-device-capture.png"
fi

if [ "$test_status" -ne 0 ]; then
  echo "Android instrumentation failed (exit $test_status); diagnostics and screenshot retained in artifacts."
  exit "$test_status"
fi

test -s "$output_dir/w1-1-device-capture.png"
echo "Verified W1-1 first-frame screenshot on API 35 emulator."
