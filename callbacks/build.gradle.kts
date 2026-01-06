import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.vanniktech.mavenPublish)
    id("dev.mokkery") version "3.1.1"
}

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

    val xcfName = "callbacksKit"
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcfName
        }
    }


    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                /* Coroutines */
                implementation(libs.kotlinx.coroutines.core)
                // Add KESPL dependency
                implementation(projects.library)
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

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(
        groupId = "io.github.developrofthings",
        artifactId = "kespl-callbacks",
        version = libs.versions.kesplVersion.get()
    )

    pom {
        name = "KESPL"
        description = "A Kotlin Extended Serial Protocol Library"
        inceptionYear = "2025"
        url = "https://github.com/DeveloprOfThings/KESPL"
        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/license/MIT"
                distribution = "https://opensource.org/license/MIT"
            }
        }
        developers {
            developer {
                id = "DeveloprOfThings"
                name = "Developr Of Things"
                url = "https://github.com/DeveloprOfThings"
            }
        }
        scm {
            url = "https://github.com/DeveloprOfThings/KESPL"
            connection = "scm:git:https://github.com/DeveloprOfThings/KESPL.git"
            developerConnection = "scm:git:https://github.com/DeveloprOfThings/KESPL.git"
        }
    }
}