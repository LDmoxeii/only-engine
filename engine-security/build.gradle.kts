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

    // 安全相关依赖 - 仅需要的部分
    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.web)
    compileOnly(libs.spring.webmvc)

    // 测试依赖
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testRuntimeOnly(libs.bundles.junit.runtime)
}
