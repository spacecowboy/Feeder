plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint.gradle)
}

val commitCount by project.extra {
    providers
        .exec {
            commandLine("git", "rev-list", "--count", "HEAD")
        }.standardOutput.asText
        .get()
        .trim()
        .toInt()
}

val latestTag by project.extra {
    providers
        .exec {
            commandLine("git", "describe")
        }.standardOutput.asText
        .get()
        .trim()
}

android {
    namespace = "com.nononsenseapps.feeder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nononsenseapps.feeder"
        // The version fields are set with actual values to support F-Droid
        // In Play variant, they are overriden and taken from git.
        versionCode = 3657
        versionName = "2.11.1"
        minSdk = 23
        targetSdk = 35

        vectorDrawables.useSupportLibrary = true

        androidResources.localeFilters.addAll(getListOfSupportedLocales())

        // For espresso tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    ksp {
        arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
    }

    sourceSets {
        // To test Room we need to include the schema dir in resources
        named("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
    }

    signingConfigs {
        create("shareddebug") {
            storeFile = rootProject.file("shareddebug.keystore")
            storePassword = "android"
            keyAlias = "AndroidDebugKey"
            keyPassword = "android"
        }
        if (project.hasProperty("STORE_FILE")) {
            create("release") {
                @Suppress("LocalVariableName", "ktlint:standard:property-naming")
                val STORE_FILE: String by project.properties

                @Suppress("LocalVariableName", "ktlint:standard:property-naming")
                val STORE_PASSWORD: String by project.properties

                @Suppress("LocalVariableName", "ktlint:standard:property-naming")
                val KEY_ALIAS: String by project.properties

                @Suppress("LocalVariableName", "ktlint:standard:property-naming")
                val KEY_PASSWORD: String by project.properties
                storeFile = file(STORE_FILE)
                storePassword = STORE_PASSWORD
                keyAlias = KEY_ALIAS
                keyPassword = KEY_PASSWORD
            }
        }
    }

    buildTypes {
        val debug by getting {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            isPseudoLocalesEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("shareddebug")
        }
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (project.hasProperty("STORE_FILE")) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        // See androidComponents below for related configurations
        flavorDimensions += "store"
        productFlavors {
            create("fdroid") {
                dimension = "store"
                // Keeping default version values for F-Droid
            }
            create("play") {
                dimension = "store"
                versionName = "2.11.1"
                versionCode = commitCount
                applicationIdSuffix = ".play"
            }
        }
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 2"
                    // Use only API levels 27 and higher.
                    apiLevel = 30
                    // To include Google services, use "google".
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    packaging {
        resources {
            excludes.addAll(
                listOf(
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE.txt",
                    "META-INF/license.txt",
                    "META-INF/LICENSE-notice.md",
                    "META-INF/NOTICE",
                    "META-INF/NOTICE.txt",
                    "META-INF/notice.txt",
                    "META-INF/ASL2.0",
                    "META-INF/AL2.0",
                    "META-INF/LGPL2.1",
                ),
            )
        }
    }

    lint {
        abortOnError = true
        disable.addAll(listOf("MissingTranslation", "AppCompatCustomView", "InvalidPackage"))
        error.addAll(listOf("InlinedApi", "StringEscaping"))
        explainIssues = true
        ignoreWarnings = true
        textOutput = file("stdout")
        textReport = true
    }
}

androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.buildType == "debug") {
            // Only allow debug build of fdroid flavor
            variantBuilder.enable = variantBuilder.productFlavors.containsAll(listOf("store" to "fdroid"))
        }
    }
}

composeCompiler {
    includeSourceInformation = true
    // reportsDestination = layout.buildDirectory.dir("compose_metrics")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        allWarningsAsErrors = false
    }
}

configurations.all {
    resolutionStrategy {
//    failOnVersionConflict()
    }
}

// gradle ktlint are not updating ktlint as they should
// so need to specify to make it compatible with compose rules
ktlint {
    version.set("1.5.0")
}

dependencies {
    ktlintRuleset(libs.ktlint.compose)
    ksp(libs.room)
    // For java time
    coreLibraryDesugaring(libs.desugar)

    // BOMS
    implementation(platform(libs.okhttp.bom))
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.openai.client.bom))
    implementation(platform(libs.retrofit.bom))

    // Dependencies
    implementation(libs.bundles.android)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.jvm)
    implementation(libs.bundles.okhttp.android)
    implementation(libs.bundles.kotlin)
    implementation(libs.openai.client)
    implementation(libs.ktor.client.okhttp)

    // Nostr
    implementation(libs.rust.nostr)

    // Markdown
    implementation(libs.jetbrains.markdown)

    // Only for debug
    debugImplementation("com.squareup.leakcanary:leakcanary-android:3.0-alpha-8")

    // Tests
    testImplementation(libs.bundles.kotlin)
    testImplementation(libs.bundles.test)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.bundles.kotlin)
    androidTestImplementation(libs.bundles.android.test)

    debugImplementation(libs.compose.ui.test.manifest)
}

fun getListOfSupportedLocales(): List<String> {
    val resFolder = file(projectDir.resolve("src/main/res"))

    return resFolder
        .list { _, s ->
            s.startsWith("values")
        }?.filter { folder ->
            val stringsSize = resFolder.resolve("$folder/strings.xml").length()
            // values/strings.xml is over 13k in size so this filters out too partial translations
            stringsSize > 10_000L
        }?.map { folder ->
            if (folder == "values") {
                "en"
            } else {
                folder.substringAfter("values-")
            }
        }?.sorted()
        ?: listOf("en")
}

tasks {
    register("generateLocalesConfig") {
        val resFolder = file(projectDir.resolve("src/main/res"))
        inputs.files(
            resFolder
                .listFiles { file ->
                    file.name.startsWith("values")
                }?.map { file ->
                    file.resolve("strings.xml")
                } ?: error("Could not resolve values folders!"),
        )

        val localesConfigFile = file(projectDir.resolve("src/main/res/xml/locales_config.xml"))
        outputs.file(projectDir.resolve("src/main/res/xml/locales_config.xml"))

        doLast {
            val langs = getListOfSupportedLocales()
            val localesConfig =
                """
                <?xml version="1.0" encoding="utf-8"?>
                <locale-config xmlns:android="http://schemas.android.com/apk/res/android">
                ${langs.joinToString(" ") { "<locale android:name=\"$it\"/>" }}
                </locale-config>
                """.trimIndent()

            localesConfigFile.bufferedWriter().use { writer ->
                writer.write(localesConfig)
            }
        }
    }
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File,
) : CommandLineArgumentProvider {
    override fun asArguments(): Iterable<String> {
        // Note: If you're using KSP, change the line below to return
        return listOf("room.schemaLocation=${schemaDir.path}")
    }
}
