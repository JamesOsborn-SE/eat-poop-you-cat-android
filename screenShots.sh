
Nexus_7_2012_API_30="metadata/android/en-US/images/sevenInchScreenshots"
Pixel_6_API_33="metadata/android/en-US/images/phoneScreenshots"
Nexus_10_API_30="metadata/android/en-US/images/tenInchScreenshots"

devices=$(adb devices -l|grep emu|cut -f1 -d ' ')
for device in $devices
do
    adb -s $device root
    
    currentDevice=$(adb -s $device shell getprop|grep -e "ro.kernel.qemu.avd_name" -e "ro.boot.qemu.avd_name"|cut -d' ' -f2|tr -d "[]")
    echo $currentDevice
    if [ -v $(echo $currentDevice) ]
    then 
        for i in $(seq 1 4)
        do 
            adb -s $device pull /data/data/dev.develsinthedetails.eatpoopyoucat/files/${i}.png $(eval "echo \$$currentDevice")
        done
    fi

done
