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

    // Redisson Redis Client
    implementation(libs.redisson.spring.boot.starter)

    // Lock4j 分布式锁
    implementation(libs.lock4j.redisson.spring.boot.starter)

    // Caffeine 本地缓存
    implementation(libs.caffeine)

    // Jackson JSR310 时间类型支持
    implementation(libs.jackson.datatype.jsr310)

    // Hutool Core
    implementation(libs.hutool.core)
    // Hutool Extra (Spring 工具类)
    implementation(libs.hutool.extra)
    // Hutool Crypto (MD5 加密)
    implementation(libs.hutool.crypto)

    // Spring Boot 核心依赖
    implementation(libs.spring.boot.autoconfigure)

    // AOP 支持（幂等性）
    implementation(libs.spring.boot.starter.aop)

    // 编译时依赖
    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.web)
    compileOnly(libs.jakarta.servlet.api)

    // 测试依赖
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testRuntimeOnly(libs.bundles.junit.runtime)
}
