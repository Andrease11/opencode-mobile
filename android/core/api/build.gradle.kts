// Generated OpenCode API client. Sources under src/commonMain/kotlin are produced by
// tools/api-gen (`bun run generate`) and must not be edited by hand.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "io.github.beppe2880.opencodemobile.core.api"
        compileSdk = 37
        minSdk = 26
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
        lint {
            // Generated code; lint findings are not actionable here.
            checkGeneratedSources = false
            abortOnError = false
        }
    }
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
        suppressWarnings.set(true)
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
