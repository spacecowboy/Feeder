plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.nononsenseapps.feeder"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.nononsenseapps.feeder"
        versionCode = 294
        versionName = "2.6.9"
        minSdk = 23
        targetSdk = 33

        vectorDrawables.useSupportLibrary = true

        resourceConfigurations.addAll(getListOfSupportedLocales())

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
                @Suppress("LocalVariableName")
                val STORE_FILE: String by project.properties

                @Suppress("LocalVariableName")
                val STORE_PASSWORD: String by project.properties

                @Suppress("LocalVariableName")
                val KEY_ALIAS: String by project.properties

                @Suppress("LocalVariableName")
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
        val debugMini by creating {
            initWith(debug)
            isMinifyEnabled = true
            isShrinkResources = true
            matchingFallbacks.add("debug")
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
//            else {
//                signingConfig = signingConfigs.getByName("shareddebug")
//            }
        }
        val play by creating {
            applicationIdSuffix = ".play"
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (project.hasProperty("STORE_FILE")) {
                signingConfig = signingConfigs.getByName("release")
            }
//            else {
//                signingConfig = signingConfigs.getByName("shareddebug")
//            }
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
        jvmTarget = "1.8"
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
        buildConfig = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
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

// https://chris.banes.dev/composable-metrics/
// gw installDebugMini -Pmyapp.enableComposeCompilerReports=true
// build/compose_metrics/[...]-composables.txt
// tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
//  kotlinOptions {
//    if (project.findProperty("myapp.enableComposeCompilerReports") == "true") {
//      freeCompilerArgs += [
//              "-P",
//              "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
//                      project.buildDir.absolutePath + "/compose_metrics"
//      ]
//      freeCompilerArgs += [
//              "-P",
//              "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
//                      project.buildDir.absolutePath + "/compose_metrics"
//      ]
//    }
//  }
// }

configurations.all {
    resolutionStrategy {
//    failOnVersionConflict()
    }
}

dependencies {
    ksp(libs.room)
    // For java time
    coreLibraryDesugaring(libs.desugar)

    // BOMS
    implementation(platform(libs.okhttp.bom))
    implementation(platform(libs.coil.bom))
    implementation(platform(libs.compose.bom))

    // Dependencies
    implementation(libs.bundles.android)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.jvm)
    implementation(libs.bundles.okhttp.android)
    implementation(libs.bundles.kotlin)

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

    return resFolder.list { _, s ->
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
            resFolder.listFiles { file ->
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
