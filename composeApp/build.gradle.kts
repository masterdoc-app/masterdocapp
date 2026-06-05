import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidApplication)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)

            implementation(projects.shared)
            implementation(libs.decompose)
            implementation(libs.decompose.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.mvikotlin.extensions.coroutines)
            implementation(libs.markdown.renderer)
            implementation(libs.markdown.renderer.m3)
            implementation(libs.imagepickerkmp)
        }

        val voiceMain by creating {
            dependsOn(commonMain.get())
        }

        voiceMain.dependencies {
            implementation(libs.kodio.core)
            implementation(libs.kodio.compose)
            implementation(libs.kotlinx.io.core)
        }

        androidMain {
            dependsOn(voiceMain)
        }
        val desktopMain by getting {
            dependsOn(voiceMain)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.decompose.android)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.java)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    if (name == "desktopTest") {
        systemProperty("compose.test.root.dir", projectDir.absolutePath)
    }
}

android {
    namespace = "pro.masterdoc.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "pro.masterdoc.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        checkDependencies = false
        abortOnError = false
    }
}

// ImagePickerKMP + Compose: lintAnalyzeDebug can crash (NonNullableMutableLiveDataDetector).
tasks.matching { it.name.startsWith("lint") }.configureEach {
    enabled = false
}

compose.desktop {
    application {
        mainClass = "pro.masterdoc.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Masterdoc"
            packageVersion = "1.0.0"
        }
    }
}

configurations.all {
    resolutionStrategy {
        val cmp = libs.versions.composeMultiplatform.get()
        // ImagePickerKMP 1.0.34 pulls newer Compose/Skiko and breaks desktop uiTest.
        eachDependency {
            if (requested.group.startsWith("org.jetbrains.compose")) {
                useVersion(cmp)
                because("Align Compose with CMP $cmp")
            }
            if (requested.group == "org.jetbrains.skiko") {
                useVersion("0.8.18")
                because("Align Skiko with CMP $cmp")
            }
        }
        // ImagePickerKMP 1.0.34 pulls activity-compose 1.11 (needs compileSdk 36); stay on project stack.
        force("androidx.activity:activity-compose:${libs.versions.activityCompose.get()}")
        force("androidx.activity:activity-ktx:${libs.versions.activityCompose.get()}")
        force("androidx.activity:activity:${libs.versions.activityCompose.get()}")
        // Kodio pulls androidx.core 1.17 (needs compileSdk 36 + AGP 8.9); stay on compileSdk 35.
        force("androidx.core:core-ktx:${libs.versions.androidxCore.get()}")
        force("androidx.core:core:${libs.versions.androidxCore.get()}")
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
