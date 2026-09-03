// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.android.lint) apply false
}