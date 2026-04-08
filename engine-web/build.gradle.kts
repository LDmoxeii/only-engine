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

    testImplementation(libs.spring.boot.starter.test) {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation(libs.spring.web)
    testImplementation(libs.spring.webmvc)
    testImplementation(libs.spring.boot.starter.tomcat)
    testImplementation(libs.jakarta.validation.api)
    testImplementation(libs.sa.token.core)
    testImplementation(libs.lock4j.core)
    testRuntimeOnly(libs.bundles.junit.runtime)
}
