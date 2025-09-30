plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    // Spring Boot Platform - 版本管理
    api(platform(libs.spring.boot.dependencies))
    kapt(libs.spring.boot.configuration.processor)

    // 模块依赖
    implementation(project(":engine-common"))

    // JSON 核心依赖
    implementation(libs.bundles.jackson)

    // Spring Boot 核心依赖
    implementation(libs.spring.boot.autoconfigure)

    // Spring Web 依赖 - 仅编译时需要
    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.web)

    // 测试依赖
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.spring.web) // 测试需要
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testRuntimeOnly(libs.bundles.junit.runtime)
}
