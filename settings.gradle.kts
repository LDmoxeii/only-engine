// The settings file is the entry point of every Gradle build.
// Its primary purpose is to define the subprojects.
// It is also used for some aspects of project-wide configuration, like managing plugins, dependencies, etc.
// https://docs.gradle.org/current/userguide/settings_file_basics.html

dependencyResolutionManagement {
    // Use Maven Central as the default repository (where Gradle will download dependencies) in all subprojects.
    @Suppress("UnstableApiUsage")
    repositories {
        if (providers.gradleProperty("onlyEngine.useMavenLocalCap4k").map(String::toBoolean).getOrElse(false)) {
            mavenLocal()
        }
        mavenCentral()
        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

providers.gradleProperty("onlyEngine.cap4kCompositePath")
    .orNull
    ?.takeIf { it.isNotBlank() }
    ?.let { cap4kPath ->
        val cap4kDir = file(cap4kPath)
        require(cap4kDir.isDirectory) {
            "onlyEngine.cap4kCompositePath must point to a cap4k checkout: $cap4kPath"
        }
        includeBuild(cap4kDir) {
            dependencySubstitution {
                substitute(module("com.only4:cap4k-plugin-pipeline-api"))
                    .using(project(":cap4k-plugin-pipeline-api"))
            }
        }
    }

// Include the `app` and `utils` subprojects in the build.
// If there are changes in only one of the projects, Gradle will rebuild only the one that has changed.
// Learn more about structuring projects with Gradle - https://docs.gradle.org/8.7/userguide/multi_project_builds.html
include(":engine-common")
include(":engine-spi")
include(":engine-doc")
include(":engine-job")
include(":engine-json")
include(":engine-redis")
include(":engine-security")
include(":engine-satoken")
include(":engine-web")
include(":engine-captcha")
include(":engine-translation")
include(":engine-oss")
include(":engine-sms")
include(":engine-audit")
include(":engine-cap4k-addon")

rootProject.name = "only-engine"
