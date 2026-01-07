import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val xcfName = "ComposeAppKit"
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            // Exposes KESPL library to Objective-C/Swift code
            export("io.github.developrofthings:kespl:0.9.0")
            baseName = xcfName
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.components.resources)
            implementation(libs.constraintlayout.compose.multiplatform)
            implementation(libs.kotlin.stdlib)
            implementation(libs.jetbrains.ui.backhandler)
            // Runtime permissions
            implementation(libs.permissions.bluetooth)
            implementation(libs.permissions.location)
            implementation(libs.permissions.compose)

            // JSON
            implementation(libs.kotlinx.serialization.json)
            // Add KESPL dependency
            implementation(libs.kespl.callbacks)

            // Koin core
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.adaptive)
            implementation(libs.adaptive.layout)
            implementation(libs.adaptive.navigation)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

    // KSP Common sourceSet
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

// Trigger Common Metadata Generation from Native tasks
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }
    .configureEach {
        dependsOn("kspCommonMainKotlinMetadata")
    }

android {
    namespace = "io.github.developrofthings.helloV1"
    compileSdk = libs.versions.compileSDK.get().toInt()

    defaultConfig {
        namespace = "io.github.developrofthings.helloV1"
        minSdk = libs.versions.minSDK.get().toInt()
        targetSdk = libs.versions.targetSDK.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
