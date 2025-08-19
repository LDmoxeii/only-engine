plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(project(":engine-common"))

    api(platform(libs.spring.boot.dependencies))

    implementation(libs.hutool.jwt)
    implementation(libs.jackson)

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
