#!/usr/bin/env bash
set -euo pipefail

# Run inside Android emulator runner while adb is attached. Capture diagnostic
# evidence even when test assertions fail; never override the test exit status.
set +e
# Run the visual qualification alone first. Other tests may change the same
# Activity/GLSurfaceView lifecycle; their first-frame receipts must not be
# mistaken for evidence that this test actually rendered W1-1.
./gradlew connectedDebugAndroidTest --console=plain \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.example.myapplication.ExampleInstrumentedTest#w1One_visualCaptureWaitsForFirst3dDraw'
visual_status=$?
if [ "$visual_status" -eq 0 ]; then
  # Still qualify the complete normal Android instrumentation suite.
  ./gradlew connectedDebugAndroidTest --console=plain
  test_status=$?
else
  echo "Focused W1-1 test failed (exit $visual_status); preserving its diagnostic screenshot."
  test_status=$visual_status
fi
set -e

output_dir="reports/android-emulator"
mkdir -p "$output_dir"
adb devices -l | tee "$output_dir/adb-devices.txt" || true
adb shell getprop ro.build.version.sdk | tr -d '\r' > "$output_dir/android-api-level.txt" || true
adb shell getprop ro.opengles.version | tr -d '\r' > "$output_dir/device-gles-version.txt" || true
adb shell getprop ro.hardware | tr -d '\r' > "$output_dir/emulator-hardware.txt" || true
adb logcat -d -s BobsBunnyDiorama:I > "$output_dir/diorama-gl-logcat.txt" || true

# The test emits its first-frame receipt to logcat. Gradle uninstalls the
# debuggable app after instrumentation, so private run-as files are gone.
# The test also saves a real device screenshot to shared Downloads.
grep 'W1-1 hosted QA:' "$output_dir/diorama-gl-logcat.txt" > "$output_dir/w1-1-render-diagnostics.txt" || true
if test ! -s "$output_dir/w1-1-render-diagnostics.txt"; then
  echo "Render test did not produce a logged diagnostic receipt" > "$output_dir/w1-1-render-diagnostics.txt"
fi

if adb pull /sdcard/Download/bobs_w1_1_visual_qa.png "$output_dir/w1-1-device-capture.png"; then
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
