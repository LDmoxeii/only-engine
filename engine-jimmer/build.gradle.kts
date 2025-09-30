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

    // Jimmer 核心依赖
    implementation(libs.jimmer.core.kotlin)

    // Spring Boot 核心依赖
    implementation(libs.spring.boot.autoconfigure)

    // 编译时依赖
    compileOnly(libs.bundles.jackson)
    compileOnly(libs.spring.web)

    // 测试依赖
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.spring.web) // 测试需要
    testImplementation(libs.bundles.jackson) // 测试需要
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testRuntimeOnly(libs.bundles.junit.runtime)
}