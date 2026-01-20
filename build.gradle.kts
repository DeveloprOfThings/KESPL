// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

// 1. Fetch properties at the very top
val projectGroup = project.findProperty("GROUP")?.toString() ?: "io.github.developrofthings"
val kesplLibVersion = libs.versions.kespl.get()

// Explicitly apply to your modules
project(":kespl") {
    configurePublishing(
        groupId = projectGroup,
        artifactId = this.name,
        versionName = kesplLibVersion,
    )
}

project(":kespl-callbacks") {
    configurePublishing(
        groupId = projectGroup,
        artifactId = this.name,
        versionName = kesplLibVersion,
    )
}

allprojects {
    version = kesplLibVersion
}

// A helper function to apply the publishing logic
fun Project.configurePublishing(
    groupId: String,
    artifactId: String,
    versionName: String,
) {
    apply(plugin = "com.vanniktech.maven.publish")

    extensions.configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {

        publishToMavenCentral()

        signAllPublications()

        coordinates(
            groupId = groupId,
            artifactId = artifactId,
            version = versionName
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
}

