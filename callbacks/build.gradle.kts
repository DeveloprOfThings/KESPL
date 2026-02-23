import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.skie)
}

skie {
    build {
        produceDistributableFramework()
    }
    features {
        enableSwiftUIObservingPreview = true
    }
}
val xcfName = "KESPLCallbacksKit"
kotlin {
    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "io.github.developrofthings.kespl.callbacks"
        compileSdk = 36
        minSdk = 24

        // Configure test that will run on JVM ie "unit test"
        withHostTestBuilder {}

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    val xcf = XCFramework(xcFrameworkName = xcfName)
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            export(dependency = projects.kespl)
            baseName = xcfName
            binaryOption("bundleId", "io.github.developrofthings.${xcfName}")
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
                api(projects.kespl)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

val cleanKESPLIOS = tasks.register<Delete>("cleanKESPLIOS") {
    group = "distribution"
    description = "Deletes existing $xcfName.xcframework/"
    delete(layout.projectDirectory.dir("../../KESPL-iOS/${xcfName}.xcframework/"))
}

val copyXCFramework = tasks.register<Copy>("copyXCFramework") {
    dependsOn(cleanKESPLIOS) // Ensure deletion happens first
    group = "distribution"
    description = "Copies the build/../release/$xcfName.xcframework/ to KESPL-IOS directory"

    // 1. Source: Your project's src directory
    from(layout.buildDirectory.dir("XCFrameworks/release/")) {
        include("${xcfName}.xcframework/**")
    }

    // 2. Destination: Use ".." to go up one level from the project root
    // This will create a folder named 'external-backup' next to your project folder
    into(layout.projectDirectory.dir("../../KESPL-iOS"))
}

// Replace 'test' with whatever task you want to trigger this flow
tasks.named("assemble${xcfName}XCFramework") {
    finalizedBy(copyXCFramework)
}
