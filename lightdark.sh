#!/bin/bash
files=$(find tmp/dark-metadata -iname "*.png" -type f)
tempRect="tmp/rect_mask.png"
for darkImage in $files
do
    dimensions=$(identify -format "%w %h" "$darkImage")
    lightImage=${darkImage/dark-metadata/light-metadata}
    dest=${lightImage/tmp\/light-/}
    mkdir -p "${dest%/*}"
    read -r width height <<< "$dimensions"
    convert -size "${width}x${height}" xc:black -fill white -draw "polygon 0,0 0,${height} ${width},${height}" "PNG8:$tempRect"
    magick "$darkImage" "$lightImage" "$tempRect" -composite "$dest"
done
