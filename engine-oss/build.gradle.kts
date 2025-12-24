plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt(libs.spring.boot.configuration.processor)

    implementation(project(":engine-common"))
    implementation(project(":engine-json"))
    implementation(project(":engine-redis"))

    // AWS SDK v2 - S3 + auth
    implementation("software.amazon.awssdk:s3:2.25.69")
    implementation("software.amazon.awssdk:netty-nio-client:2.25.69")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.25.69")

    // Hutool for small utils if needed
    implementation(libs.hutool.core)

    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.web)
    compileOnly(libs.spring.webmvc)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testRuntimeOnly(libs.bundles.junit.runtime)
}
