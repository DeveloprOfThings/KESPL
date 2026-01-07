import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("dev.mokkery") version "3.1.1"
}

mokkery {
    ignoreInlineMembers.set(true) // ignores only inline members
    ignoreFinalMembers.set(true)  // ignores final members (inline included)
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "io.github.developrofthings.kespl"
        compileSdk = libs.versions.compileSDK.get().toInt()
        minSdk = libs.versions.minSDK.get().toInt()

        // Configure test that will run on JVM ie "unit test"
        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    val xcfName = "KESPLKit"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
            // Koin core
            implementation(libs.koin.core)
            // Koin Annotations
            api(libs.koin.annotations)
            /* Kotlin-io Core */
            implementation(libs.kotlinx.io.core)
            /* Coroutines */
            implementation(libs.kotlinx.coroutines.core)
            /* Atomic-fu */
            implementation(libs.atomicfu)
            /* Arrow */
            implementation(libs.arrow.core)
            implementation(libs.arrow.fx.coroutines)
            /* Datastore */
            implementation(libs.datastore.core.okio)
            /* Kotlinx-serialization-protobuf */
            implementation(libs.kotlinx.serialization.proto)
            // JSON
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies{
            implementation(libs.kotlin.test)
            implementation(libs.square.turbine)
            implementation(libs.kotlinx.coroutines.test)
        }

        named("androidHostTest").dependencies {
            implementation(libs.mockk)
        }

        androidMain.dependencies {
            implementation(libs.androidx.annotation.jvm)
            implementation(libs.androidx.core)
            // Add Android-specific dependencies here. Note that this source set depends on
            // commonMain by default and will correctly pull the Android artifacts of any KMP
            // dependencies declared in commonMain.
        }

        iosMain.dependencies {
            // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
            // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
            // part of KMP’s default source set hierarchy. Note that this source set depends
            // on common by default and will correctly pull the iOS artifacts of any
            // KMP dependencies declared in commonMain.
        }
    }

    // KSP Common sourceSet
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

// KSP Tasks
dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}

// Trigger Common Metadata Generation from Native tasks
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").configure {
    // Explicitly make sourcesJar depend on the KSP metadata task.
    // This solves the "implicit dependency" error.
    dependsOn("kspCommonMainKotlinMetadata")
}

ksp {
    arg("KOIN_DEFAULT_MODULE","false")
}

tasks.withType<Test> {
    useJUnitPlatform()
}