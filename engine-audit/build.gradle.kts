plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt(libs.spring.boot.configuration.processor)

    implementation(project(":engine-common"))
    implementation(project(":engine-spi"))

    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.spring.boot.starter)
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testImplementation(libs.spring.boot.starter)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testRuntimeOnly(libs.bundles.junit.runtime)
}
