plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    compileOnly(libs.cap4k.plugin.pipeline.api)

    testImplementation(libs.cap4k.plugin.pipeline.api)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit.core)
    testRuntimeOnly(libs.bundles.junit.runtime)
}
