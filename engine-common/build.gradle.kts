plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    // Spring Boot Platform - 版本管理
    api(platform(libs.spring.boot.dependencies))
    kapt(libs.spring.boot.configuration.processor)

    // 核心传递依赖 - 所有模块都会用到
    api(libs.transmittable.thread.local)
    api(libs.slf4j.api)

    // 可选依赖 - 用户可以选择使用
    compileOnly(libs.bundles.jackson)
    compileOnly(libs.fastjson)
    compileOnly(libs.spring.boot.starter)

    // 测试依赖
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testRuntimeOnly(libs.bundles.junit.runtime)
}
