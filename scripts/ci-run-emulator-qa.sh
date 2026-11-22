#!/usr/bin/env bash
set -euo pipefail

# Run inside Android emulator runner while adb is attached. Capture diagnostic
# evidence even when test assertions fail; never override the test exit status.
set +e
output_dir="reports/android-emulator"
mkdir -p "$output_dir"
# Keep the same 16:9 ratio while qualifying two useful landscape sizes. The
# second run checks the quip's safe slot against actual HUD/control bounds at
# a narrower viewport; the player app remains density-independent.
adb shell wm density 240 || true

run_visual_viewport() {
  local size="$1"
  local label="$2"
  adb shell wm size "$size" || return $?
  adb shell rm -f /sdcard/Download/bobs_w1_1_visual_qa.png || true
  adb shell wm size > "$output_dir/display-size-${label}.txt" || true
  adb shell wm density > "$output_dir/display-density-${label}.txt" || true
  ./gradlew connectedDebugAndroidTest --console=plain \
    '-Pandroid.testInstrumentationRunnerArguments.class=com.example.myapplication.ExampleInstrumentedTest#w1One_visualCaptureWaitsForFirst3dDraw'
  local visual_status=$?
  if ! adb pull /sdcard/Download/bobs_w1_1_visual_qa.png "$output_dir/w1-1-${label}.png"; then
    echo "No retained screenshot for viewport $size"
    return 1
  fi
  return "$visual_status"
}

test_status=0
run_visual_viewport 1280x720 1280x720
test_status=$?
if [ "$test_status" -eq 0 ]; then
  run_visual_viewport 960x540 960x540
  test_status=$?
fi

# Restore the stable primary viewport before running the full instrumentation
# suite and exporting the compatibility screenshot.
adb shell wm size 1280x720 || true
adb shell rm -f /sdcard/Download/bobs_w1_1_visual_qa.png || true
if [ "$test_status" -eq 0 ]; then
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
  file "$output_dir/w1-1-device-capture.png" | tee "$output_dir/image-info.txt"
  sha256sum "$output_dir/w1-1-device-capture.png" | tee "$output_dir/SHA256SUMS.txt"
else
  rm -f "$output_dir/w1-1-device-capture.png"
fi
for image in "$output_dir/w1-1-1280x720.png" "$output_dir/w1-1-960x540.png"; do
  if test -s "$image"; then
    file "$image" | tee -a "$output_dir/image-info.txt"
    sha256sum "$image" | tee -a "$output_dir/SHA256SUMS.txt"
  fi
done

if [ "$test_status" -ne 0 ]; then
  echo "Android instrumentation failed (exit $test_status); diagnostics and viewport screenshots retained in artifacts."
  exit "$test_status"
fi

test -s "$output_dir/w1-1-device-capture.png"
test -s "$output_dir/w1-1-1280x720.png"
test -s "$output_dir/w1-1-960x540.png"
echo "Verified W1-1 first-frame screenshots on API 35 emulator at 1280x720 and 960x540."
