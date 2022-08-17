import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-library`
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.jvmTarget = "1.8"

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()

        val kotlinVersion: String by project
        val okio_version: String by project

        force("com.squareup.okio:okio:$okio_version")
        force("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
        force("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    }
}

dependencies {
    val kotlinVersion: String by project
    val okhttp_version: String by project
    val moshi_version: String by project
    val junitVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api(platform("com.squareup.okhttp3:okhttp-bom:$okhttp_version"))
    api("com.squareup.okhttp3:okhttp")
    api("com.squareup.moshi:moshi:$moshi_version")
    api("com.squareup.moshi:moshi-kotlin:$moshi_version")

    // tests
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("junit:junit:$junitVersion")
}
