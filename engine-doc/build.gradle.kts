plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt(libs.spring.boot.configuration.processor)

    implementation(project(":engine-common"))

    implementation(libs.hutool.core)
    // TODO: 未知作用
//    implementation(libs.jackson.module.kotlin)
    implementation(libs.springdoc.openapi.starter)
    implementation(libs.therapi.runtime.javadoc)

    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.web)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.mockk) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    testRuntimeOnly(libs.bundles.junit.runtime)
}
