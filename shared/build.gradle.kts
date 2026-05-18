import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.androidLibrary)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

fun readLocalProperty(key: String): String = localProperties.getProperty(key)?.trim().orEmpty()

val onyxBaseUrlFromLocal: String = readLocalProperty("onyx.baseUrl")
val onyxPatFromLocal: String = readLocalProperty("onyx.pat")

val generateOnyxBuildConfig = tasks.register("generateOnyxBuildConfig") {
    val generatedKotlinDir =
        layout.buildDirectory.dir("generated/onyxBuildConfig/kotlin/pro/masterdoc/data/config")
    outputs.dir(generatedKotlinDir)
    doLast {
        val dir = generatedKotlinDir.get().asFile
        dir.mkdirs()
        fun String.escapeForKotlin(): String = replace("\\", "\\\\").replace("\"", "\\\"")
        dir.resolve("OnyxBuildConfig.kt").writeText(
            """
            package pro.masterdoc.data.config

            /** Generated from root [local.properties] (onyx.baseUrl, onyx.pat). Do not edit. */
            internal object OnyxBuildConfig {
                const val BASE_URL: String = "${onyxBaseUrlFromLocal.escapeForKotlin()}"
                const val PAT: String = "${onyxPatFromLocal.escapeForKotlin()}"
            }
            """.trimIndent() + "\n",
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/onyxBuildConfig/kotlin"))
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.decompose)
            implementation(libs.essenty.lifecycle)
            implementation(libs.essenty.stateKeeper)
            implementation(libs.essenty.instanceKeeper)

            api(libs.mvikotlin)
            api(libs.mvikotlin.main)
            api(libs.mvikotlin.extensions.coroutines)

            implementation(libs.koin.core)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.java)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

tasks.configureEach {
    if (name.startsWith("compile") && "Kotlin" in name) {
        dependsOn(generateOnyxBuildConfig)
    }
}

android {
    namespace = "pro.masterdoc.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
