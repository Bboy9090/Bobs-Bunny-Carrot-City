#!/usr/bin/env bash
set -euo pipefail

# Run one viewport per fresh Android emulator job. Reusing a SwiftShader AVD
# across multiple launches caused Quickstep/Launcher ANR dialogs at 960x540.
# Keep test evidence for both viewport sizes independent and do not dismiss
# system overlays or change the strict scene-color assertions.
set +e
output_dir="reports/android-emulator"
mkdir -p "$output_dir"
size="${BOBS_BUNNY_QA_VIEWPORT:-1280x720}"
case "$size" in
  1280x720|960x540) ;;
  *) echo "Unsupported QA viewport: $size"; exit 2 ;;
esac
label="$size"
adb shell wm size "$size" || true
adb shell wm density 240 || true
adb shell rm -f /sdcard/Download/bobs_w1_1_visual_qa.png || true
adb shell wm size > "$output_dir/display-size-$label.txt" || true
adb shell wm density > "$output_dir/display-density-$label.txt" || true

# Run visual qualification alone first. The matrix gives each viewport a fresh
# emulator so the second-size result is not contaminated by another launch.
./gradlew connectedDebugAndroidTest --console=plain \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.example.myapplication.ExampleInstrumentedTest#w1One_visualCaptureWaitsForFirst3dDraw'
visual_status=$?
if adb pull /sdcard/Download/bobs_w1_1_visual_qa.png "$output_dir/w1-1-$label.png"; then
  file "$output_dir/w1-1-$label.png" | tee "$output_dir/image-info.txt"
  sha256sum "$output_dir/w1-1-$label.png" | tee "$output_dir/SHA256SUMS.txt"
else
  rm -f "$output_dir/w1-1-$label.png"
fi

test_status=$visual_status
if [ "$visual_status" -eq 0 ]; then
  # Run the complete suite on this viewport; the 1280x720 job is the primary
  # compatibility run and the 960x540 job also checks compact-header geometry.
  adb shell rm -f /sdcard/Download/bobs_w1_1_visual_qa.png || true
  ./gradlew connectedDebugAndroidTest --console=plain
  test_status=$?
fi
set -e

adb devices -l | tee "$output_dir/adb-devices.txt" || true
adb shell getprop ro.build.version.sdk | tr -d '\r' > "$output_dir/android-api-level.txt" || true
adb shell getprop ro.opengles.version | tr -d '\r' > "$output_dir/device-gles-version.txt" || true
adb shell getprop ro.hardware | tr -d '\r' > "$output_dir/emulator-hardware.txt" || true
adb shell wm size > "$output_dir/display-size.txt" || true
adb shell wm density > "$output_dir/display-density.txt" || true
adb logcat -d -v threadtime -s BobsBunnyDiorama:I > "$output_dir/diorama-gl-logcat.txt" || true
adb shell dumpsys activity activities > "$output_dir/activity-state.txt" || true
grep 'W1-1 hosted QA:' "$output_dir/diorama-gl-logcat.txt" > "$output_dir/w1-1-render-diagnostics.txt" || true
if test ! -s "$output_dir/w1-1-render-diagnostics.txt"; then
  echo "Render test did not produce a logged diagnostic receipt" > "$output_dir/w1-1-render-diagnostics.txt"
fi

if adb pull /sdcard/Download/bobs_w1_1_visual_qa.png "$output_dir/w1-1-device-capture.png"; then
  file "$output_dir/w1-1-device-capture.png" | tee -a "$output_dir/image-info.txt"
  sha256sum "$output_dir/w1-1-device-capture.png" | tee -a "$output_dir/SHA256SUMS.txt"
else
  rm -f "$output_dir/w1-1-device-capture.png"
fi

if [ "$test_status" -ne 0 ]; then
  echo "Android instrumentation failed (exit $test_status); diagnostics and viewport screenshot retained in artifacts."
  exit "$test_status"
fi

test -s "$output_dir/w1-1-$label.png"
test -s "$output_dir/w1-1-device-capture.png"
echo "Verified W1-1 first-frame screenshot on API 35 emulator at $size."
