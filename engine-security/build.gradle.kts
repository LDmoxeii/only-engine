plugins {
    id("buildsrc.convention.kotlin-jvm")
    kotlin("kapt")
}

dependencies {
    api(platform(libs.spring.boot.dependencies))
    kapt("org.springframework.boot:spring-boot-configuration-processor:3.1.12")

    implementation(project(":engine-common"))

    implementation(libs.bundles.jackson)
    implementation(libs.validation)

    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.tomcat)
    compileOnly(libs.spring.web)
    compileOnly(libs.spring.web.mvc)

    // Common dependencies
    implementation(libs.slf4j)

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
