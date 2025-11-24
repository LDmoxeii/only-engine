plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt(libs.spring.boot.configuration.processor)

    implementation(project(":engine-common"))
    implementation(project(":engine-spi"))

    api(libs.redisson.spring.boot.starter)
    api(libs.caffeine)

    implementation(libs.hutool.core)
    implementation(libs.hutool.extra)
    implementation(libs.hutool.crypto)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.module.kotlin)  // 支持 Kotlin data class 序列化

    compileOnly(libs.spring.web)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.starter.aop)
    compileOnly(libs.jakarta.servlet.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testRuntimeOnly(libs.bundles.junit.runtime)

    testImplementation(libs.spring.boot.starter.test) {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation(libs.spring.boot.starter)
}
