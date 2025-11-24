plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt(libs.spring.boot.configuration.processor)

    implementation(project(":engine-common"))
    implementation(project(":engine-json"))

    implementation(libs.hutool.jwt)
    implementation(libs.hutool.core)
    implementation(libs.hutool.extra)
    implementation(libs.hutool.http)
    implementation(libs.commons.lang3)

    compileOnly(libs.spring.web)
    compileOnly(libs.spring.webmvc)
    compileOnly(libs.spring.boot.starter.tomcat)
    compileOnly(libs.jakarta.validation.api)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.sa.token.core)
    compileOnly(libs.lock4j.core)
    compileOnly(libs.sms4j.comm)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testRuntimeOnly(libs.bundles.junit.runtime)
}
