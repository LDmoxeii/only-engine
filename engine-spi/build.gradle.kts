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

    // SnailJob 依赖
    implementation(libs.snail.job.client.starter)
    implementation(libs.snail.job.client.job.core)

    // Spring Boot 核心依赖
    implementation(libs.spring.boot.autoconfigure)

    // 编译时依赖
    compileOnly(libs.spring.boot.starter)

    // 测试依赖
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testRuntimeOnly(libs.bundles.junit.runtime)
}
