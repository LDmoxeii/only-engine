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
    implementation(project(":engine-spi"))
    implementation(project(":engine-security"))
    implementation(project(":engine-redis"))

    // Sa-Token 核心依赖
    implementation(libs.sa.token.spring.boot3.starter)
    implementation(libs.sa.token.jwt)

    // Spring Web 依赖 - 仅编译时需要
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
