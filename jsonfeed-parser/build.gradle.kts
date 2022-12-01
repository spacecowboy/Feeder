import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.jvmTarget = "1.8"

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()
    }
}

dependencies {
    implementation(libs.bundles.kotlin.stdlib)
    api(platform(libs.okhttp.bom))
    api(libs.bundles.okhttp)
    api(libs.moshi)
    api(libs.moshi.kotlin)

    // tests
    testImplementation(libs.bundles.kotlin.stdlib)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit)
}
