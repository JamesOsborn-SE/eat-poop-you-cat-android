#!/bin/bash

# shellcheck disable=SC2034
applicationId=$(grep "applicationId" app/build.gradle.kts |cut -d'"' -f2)
echo "$applicationId"

# Key is the avd_name from getprop and the value is the output directory for the screen shots
Nexus_7_2012_API_33="metadata/android/en-US/images/sevenInchScreenshots"
Pixel_6_API_33="metadata/android/en-US/images/phoneScreenshots"
Nexus_10_API_33="metadata/android/en-US/images/tenInchScreenshots"

devices=("Nexus_10_API_33" "Nexus_7_2012_API_33" "Pixel_6_API_33")
for currentDevice in "${devices[@]}"; do
  killall qemu-system-x86_64
  echo "$currentDevice"
  "${ANDROID_HOME}/emulator/emulator" -avd "$currentDevice" &
  sleep 10
  adb shell "cmd uimode night yes"
  ./gradlew connectedDebugAndroidTest &

  adb root
  end=$((SECONDS+120))
  while [ $SECONDS -lt $end ]; do
      adbsync --force pull "/data/data/$applicationId/files/" "tmp/dark-$(eval "echo \$$currentDevice")"
      sleep 1
      left=$(expr $SECONDS - $end)
      echo "timeLeft = $left"
  done

  adb shell "cmd uimode night no"
  ./gradlew connectedDebugAndroidTest &
  end=$((SECONDS+120))
  while [ $SECONDS -lt $end ]; do
      adbsync --force pull "/data/data/$applicationId/files/" "tmp/light-$(eval "echo \$$currentDevice")"
      sleep 1
  done
done

