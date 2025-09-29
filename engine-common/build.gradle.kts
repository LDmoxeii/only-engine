
plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt("org.springframework.boot:spring-boot-configuration-processor:3.1.12")

    compileOnly(libs.bundles.jackson)
    compileOnly(libs.fastjson)
    api(libs.transmittable.thread.local)

    compileOnly(libs.spring.boot.starter)
    // Common dependencies
    compileOnly(libs.slf4j)

    // Test dependencies
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
