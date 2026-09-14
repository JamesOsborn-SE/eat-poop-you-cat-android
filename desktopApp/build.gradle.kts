plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()
    jvmToolchain(17)
    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":shared"))
                implementation(libs.kotlinx.coroutines.swing)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        javaHome = "/usr/lib/jvm/java-17-openjdk"
        mainClass = "dev.develsinthedetails.eatpoopyoucat.MainKt"

        nativeDistributions {
            modules("jdk.unsupported")
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage
            )
            packageName = "EatPoopYouCat"
            packageVersion = "1.0.0"
            linux {
                shortcut = true
                iconFile.set(project.file("packaging/icons/linux_launch_icon.png"))
                modules("jdk.security.auth")
            }
        }
    }
}