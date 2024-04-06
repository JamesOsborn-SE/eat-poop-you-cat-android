#!/bin/bash

rm -rf tmp/

# Key is the avd_name from getprop and the value is the output directory for the screen shots
# shellcheck disable=SC2034
Nexus_7_2012_API_33="metadata/android/en-US/images/sevenInchScreenshots"
# shellcheck disable=SC2034
Pixel_6_API_33="metadata/android/en-US/images/phoneScreenshots"
# shellcheck disable=SC2034
Nexus_10_API_33="metadata/android/en-US/images/tenInchScreenshots"

Pictures_Path="/mnt/sdcard/Pictures/"

devices=("Nexus_10_API_33" "Nexus_7_2012_API_33" "Pixel_6_API_33")
for currentDevice in "${devices[@]}"; do
  dark_path="tmp/dark-$(eval "echo \$$currentDevice")"
  light_path="tmp/light-$(eval "echo \$$currentDevice")"
  mkdir -p "$dark_path"
  mkdir -p "$light_path"

  killall qemu-system-x86_64
  sleep 3
  echo "$currentDevice"
  "${ANDROID_HOME}/emulator/emulator" -avd "$currentDevice" >> tmp/.emu.log 2>&1 &
  echo "wait for 60"
  sleep 60
  adb root
  echo "delete pictures"
  adb shell "rm $Pictures_Path*"
  echo "reboot"
  adb shell "reboot"
  echo "wait for 45"
  sleep 45
  adb root
  adb shell "cmd uimode night yes"
  ./gradlew connectedDebugAndroidTest >> /dev/null 2>&1
  echo "sleep for 10"
  sleep 10
  echo "pull dark pictures"
  adbsync --exclude .thumbnails --force pull "$Pictures_Path" "$dark_path"

  echo "sleep for 10"
  sleep 10

  echo "delete pictures"
  adb shell "rm $Pictures_Path*"
  echo "reboot"
  adb shell "reboot"
  echo "wait for 45"
  sleep 45
  adb root
  adb shell "cmd uimode night no"
  ./gradlew connectedDebugAndroidTest >>/dev/null 2>&1
  echo "sleep for 10"
  sleep 10
  echo "pull light pictures"
  adbsync --exclude .thumbnails --force pull "$Pictures_Path" "$light_path"
done
