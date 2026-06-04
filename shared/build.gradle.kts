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

val generateMasterdocBuildConfig = tasks.register("generateMasterdocBuildConfig") {
    val generatedKotlinDir =
        layout.buildDirectory.dir("generated/masterdocBuildConfig/kotlin/pro/masterdoc/data/config")
    val generatedFile = generatedKotlinDir.map { it.file("MasterdocBuildConfig.kt") }
    if (localPropertiesFile.exists()) {
        inputs.file(localPropertiesFile)
    }
    outputs.file(generatedFile)
    doLast {
        val props = Properties()
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { props.load(it) }
        }
        val baseUrl = props.getProperty("masterdoc.api.baseUrl")?.trim().orEmpty()
        val dir = generatedKotlinDir.get().asFile
        dir.mkdirs()
        fun String.escapeForKotlin(): String = replace("\\", "\\\\").replace("\"", "\\\"")
        dir.resolve("MasterdocBuildConfig.kt").writeText(
            """
            package pro.masterdoc.data.config

            /** Generated from root [local.properties] (masterdoc.api.baseUrl). Do not edit. */
            internal object MasterdocBuildConfig {
                val API_BASE_URL: String = "${baseUrl.escapeForKotlin()}"
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
            kotlin.srcDir(layout.buildDirectory.dir("generated/masterdocBuildConfig/kotlin"))
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
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${libs.versions.coroutines.get()}")
        }
    }
}

tasks.configureEach {
    if (name.startsWith("compile") && "Kotlin" in name) {
        dependsOn(generateMasterdocBuildConfig)
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
