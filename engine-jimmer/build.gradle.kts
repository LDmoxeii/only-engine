plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt("org.springframework.boot:spring-boot-configuration-processor:3.1.12")

    // Engine 模块依赖 - 只依赖 common，不依赖 web
    implementation(project(":engine-common"))

    // Jimmer 依赖
    implementation(libs.jimmer.core)

    // Spring Boot 依赖
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    // Jackson 依赖 - 需要用于 ObjectMapper 和 Builder
    compileOnly(libs.bundles.jackson)
    compileOnly("org.springframework:spring-web") // 需要 Jackson2ObjectMapperBuilder

    // Common dependencies
    implementation(libs.slf4j)

    // Test dependencies
    testImplementation(platform(libs.junit.bom))
    testImplementation("org.springframework:spring-web") // 测试需要
    testImplementation(libs.bundles.jackson) // 测试需要
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}