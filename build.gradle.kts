// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply true
    alias(libs.plugins.legacy.kapt) apply false
}
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}