#!/bin/bash

# shellcheck disable=SC2034
applicationId=$(grep "applicationId" app/build.gradle.kts |cut -d'"' -f2)

# Key is the avd_name from getprop and the value is the output directory for the screen shots
Nexus_7_2012_API_30="metadata/android/en-US/images/sevenInchScreenshots"
Pixel_6_API_33="metadata/android/en-US/images/phoneScreenshots"
Nexus_10_API_30="metadata/android/en-US/images/tenInchScreenshots"
number_of_shots=4

devices=$(adb devices -l | grep emu | cut -f1 -d ' ')
for device in $devices; do
  adb -s "$device" root

  currentDevice=$(adb -s "$device" shell getprop | grep -e "ro.kernel.qemu.avd_name" -e "ro.boot.qemu.avd_name" | cut -d' ' -f2 | tr -d "[]")
  echo "$currentDevice"
  if [ -v "$currentDevice" ]; then
    echo "$device"
    for i in $(seq 1 $number_of_shots); do
      adb -s "$device" pull "/data/data/$applicationId/files/${i}.png" "$(eval "echo \$$currentDevice")"
    done
  fi

done
