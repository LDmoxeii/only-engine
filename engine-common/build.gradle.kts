plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt(libs.spring.boot.configuration.processor)

    api(libs.transmittable.thread.local)
    api(libs.slf4j.api)

    implementation(libs.fastjson)
    implementation(libs.hutool.extra)
    implementation(libs.hutool.jwt)
    implementation(libs.hutool.http)
    implementation(libs.jakarta.servlet.api)

    compileOnly(libs.bundles.jackson)
    compileOnly(libs.spring.webmvc)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testRuntimeOnly(libs.bundles.junit.runtime)
}
