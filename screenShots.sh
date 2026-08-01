#!/bin/bash
## written with help from Gemini 3.1 Pro

# Define paths
RES_DIR="app/src/main/res"
# Using the package path from our previous classloader fix
OUTPUT_DIR="app/src/screenshotTest/kotlin/"
OUTPUT_FILE="$OUTPUT_DIR/GenerateScreenShots.kt"

echo "🔍 Scanning for locales..."

# Start with the default language (Base)
LOCALES=("default")

# Find all values-* directories that contain a strings.xml
for dir in "$RES_DIR"/values-*/; do
    if [ -f "${dir}strings.xml" ]; then
        # Extract folder name (e.g., 'values-es' or 'values-pt-rBR')
        folder_name=$(basename "$dir")

        # Strip 'values-' prefix to get the locale tag
        raw_locale=${folder_name#values-}

        # Convert Android's region format (e.g., pt-rBR) to standard locale format (pt-BR) required by Compose Preview
        clean_locale=${raw_locale/-r/-}

        LOCALES+=("$clean_locale")
        echo "   Found translated strings: $clean_locale"
    fi
done

echo "📝 Generating $OUTPUT_FILE..."

# Create directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Write the top of the Kotlin file (Imports and Package)
cat <<EOF > "$OUTPUT_FILE"
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import dev.develsinthedetails.eatpoopyoucat.ui.draw.DrawingWithSentencePreview
import dev.develsinthedetails.eatpoopyoucat.ui.screens.HomeScreenPreview
import dev.develsinthedetails.eatpoopyoucat.ui.sentence.SentenceScreenPreview
import dev.develsinthedetails.eatpoopyoucat.ui.sentence.SentenceScreenWithDrawingPreview
import dev.develsinthedetails.eatpoopyoucat.ui.theme.AppTheme

EOF

# Define the screens we want to capture (Name:ComposableFunction)
SCREENS=(
    "HomeScreen:HomeScreenPreview()"
    "SentenceScreen:SentenceScreenPreview()"
    "DrawingWithSentence:DrawingWithSentencePreview()"
    "SentenceScreenWithDrawing:SentenceScreenWithDrawingPreview()"
)

index=1

for screen_info in "${SCREENS[@]}"; do
    screen_name="${screen_info%%:*}"
    screen_func="${screen_info##*:}"

    echo "@PreviewTest" >> "$OUTPUT_FILE"

    for loc in "${LOCALES[@]}"; do
        loc_param=""
        # IMPORTANT: Default to your base metadata folder name
        loc_label="en-US"

        if [ "$loc" != "default" ]; then
            # The compose preview still needs the standard Android locale code
            loc_param=", locale = \"$loc\""

            # Map Android locales to Fastlane (Google Play) compliant locale names
            case "$loc" in
                "cs") loc_label="cs-CZ" ;;
                "da") loc_label="da-DK" ;;
                "de") loc_label="de-DE" ;;
                "el") loc_label="el-GR" ;;
                "es") loc_label="es-ES" ;;
                "fi") loc_label="fi-FI" ;;
                "fr") loc_label="fr-FR" ;;
                "it") loc_label="it-IT" ;;
                "ja") loc_label="ja-JP" ;;
                "ko") loc_label="ko-KR" ;;
                "nl") loc_label="nl-NL" ;;
                "no") loc_label="no-NO" ;;
                "pl") loc_label="pl-PL" ;;
                "ru") loc_label="ru-RU" ;;
                "sv") loc_label="sv-SE" ;;
                "tr") loc_label="tr-TR" ;;
                # If a locale already contains a region (e.g., pt-BR) or doesn't need mapping, keep it as is
                *) loc_label="$loc" ;;
            esac
        fi

        # 1. Phone Screenshots
        echo "@Preview(name = \"${index}_${loc_label}_Light_phoneScreenshots\"$loc_param)" >> "$OUTPUT_FILE"
        echo "@Preview(name = \"${index}_${loc_label}_Dark_phoneScreenshots\"$loc_param, uiMode = Configuration.UI_MODE_NIGHT_YES)" >> "$OUTPUT_FILE"

        # 2. 7-inch Tablet Screenshots
        echo "@Preview(name = \"${index}_${loc_label}_Light_sevenInchScreenshots\"$loc_param, device = \"id:Nexus 7\")" >> "$OUTPUT_FILE"
        echo "@Preview(name = \"${index}_${loc_label}_Dark_sevenInchScreenshots\"$loc_param, uiMode = Configuration.UI_MODE_NIGHT_YES, device = \"id:Nexus 7\")" >> "$OUTPUT_FILE"

        # 3. 10-inch Tablet Screenshots
        echo "@Preview(name = \"${index}_${loc_label}_Light_tenInchScreenshots\"$loc_param, device = \"id:pixel_tablet\")" >> "$OUTPUT_FILE"
        echo "@Preview(name = \"${index}_${loc_label}_Dark_tenInchScreenshots\"$loc_param, uiMode = Configuration.UI_MODE_NIGHT_YES, device = \"id:pixel_tablet\")" >> "$OUTPUT_FILE"
    done

    # Write the actual Composable function
    cat <<EOF >> "$OUTPUT_FILE"
@Composable
fun TakePreview${screen_name}ScreenShots() {
    AppTheme {
        $screen_func
    }
}

EOF

index=$((index+1))
done

./gradlew :app:updateDebugScreenshotTest
echo "✅ Success! Screenshots generated."

SRC_DIR="app/src/screenshotTestDebug/reference/GenerateScreenShotsKt"

echo "🖼️ Processing and merging screenshots..."

for darkImage in $(find "$SRC_DIR" -iname "*_Dark_*.png" -type f); do

    # Extract the filename (e.g. TakePreviewDrawingWithSentenceScreenShots_3_de-DE_Dark_phoneScreenshots_56d77e56_0.png)
    filename=$(basename "$darkImage")

    # Use Regex to find our specific pattern anywhere in the filename, ignoring prefixes/suffixes
    # The regex naturally captures our new Fastlane-compliant loc_labels (like de-DE) due to [a-zA-Z0-9-]+
    if [[ "$filename" =~ ([0-9]+)_([a-zA-Z0-9-]+)_Dark_(phoneScreenshots|sevenInchScreenshots|tenInchScreenshots) ]]; then
        screenNum="${BASH_REMATCH[1]}" # e.g., 3
        locale="${BASH_REMATCH[2]}"    # e.g., de-DE
        deviceDir="${BASH_REMATCH[3]}" # e.g., phoneScreenshots

        # --- LOGIC: FIND MATCHING LIGHT IMAGE ---
        # 1. Grab everything in the filename *before* "_Dark_"
        prefix="${filename%%_Dark_*}"

        # 2. Build a glob search string looking for "_Light_" and any hash at the end
        lightImageGlob="${SRC_DIR}/${prefix}_Light_${deviceDir}_*.png"

        # 3. Expand the glob to get the actual file path
        lightImages=($lightImageGlob)
        lightImage="${lightImages[0]}"

        # 4. Check if the Light image actually exists before continuing
        if [[ ! -f "$lightImage" ]]; then
            echo "⚠️ Could not find light image matching $lightImageGlob. Skipping."
            continue
        fi
        # --------------------------------------------

    else
        echo "⚠️ Could not parse expected structure from: $filename. Skipping."
        continue
    fi

    # Maps directly to the Fastlane folder structure because $locale is now Fastlane-compliant
    dest="./metadata/android/$locale/images/$deviceDir/${screenNum}.png"

    # Ensure destination directory exists
    mkdir -p "${dest%/*}"
    echo "made dir ${dest%/*}"

    magick "$darkImage" "$lightImage" <(magick "$darkImage" -clone 0 -alpha off -fill black -colorize 100 -fill white -draw "polygon 0,0 0,%h %w,%h" - ) -composite "$dest"
    echo "✅ Created $dest"
done

echo "🎉 All screenshots successfully merged and moved!"