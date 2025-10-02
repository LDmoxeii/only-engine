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

    // SpringDoc OpenAPI
    implementation(libs.springdoc.openapi.starter)

    // Therapi Javadoc
    implementation(libs.therapi.runtime.javadoc)

    // Jackson Kotlin
    implementation(libs.jackson.module.kotlin)

    // Hutool Core (用于 IoUtil)
    implementation(libs.hutool.core)

    // Apache Commons Lang3
    implementation(libs.commons.lang3)

    // Spring Boot 核心依赖
    implementation(libs.spring.boot.autoconfigure)

    // 编译时依赖
    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.web)

    // 测试依赖
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testRuntimeOnly(libs.bundles.junit.runtime)
}
