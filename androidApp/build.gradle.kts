plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.screenshot)
}

android {
    namespace = "dev.develsinthedetails.eatpoopyoucat"
    compileSdk = 37
    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    defaultConfig {
        applicationId = "dev.develsinthedetails.eatpoopyoucat"
        minSdk = 26
        targetSdk = 37
        versionCode = 25
        versionName = "1.9.9"

        testInstrumentationRunner =
            "dev.develsinthedetails.eatpoopyoucat.core.utilities.MainTestRunner"
        proguardFiles("proguard-rules.pro")
        manifestPlaceholders += mapOf(
            "deeplinkScheme" to project.findProperty("deeplink.scheme").toString(),
            "deeplinkHost" to project.findProperty("deeplink.host").toString(),
            "deeplinkPlay" to project.findProperty("deeplink.play").toString(),
            "deeplinkDraw" to project.findProperty("deeplink.draw").toString(),
            "deeplinkSentence" to project.findProperty("deeplink.sentence").toString(),
            "deeplinkPreviousGames" to project.findProperty("deeplink.previousGames").toString(),
            "deeplinkPreviousGameDetails" to project.findProperty("deeplink.previousGameDetails")
                .toString()
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.resValues.put(
            variant.makeResValueKey("string", "applicationId"),
            com.android.build.api.variant.ResValue(variant.applicationId.get())
        )
    }
}



dependencies {
    implementation(project(":shared"))
    implementation(libs.datastore.preferences.core)

    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.core.ktx)
    implementation(libs.core.splashscreen)

    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.constraintlayout.compose)

    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.ui.test.manifest)

    testImplementation(libs.koin.test)
    testImplementation(libs.junit.ktx)

    androidTestImplementation(libs.core.testing)

    androidTestImplementation(libs.koin.test.junit4)
    androidTestImplementation(libs.koin.android.test)

    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.ext.junit)

    androidTestImplementation(libs.ui.test)
    androidTestImplementation(libs.ui.test.junit4)

    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)


}

