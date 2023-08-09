plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
}

class RoomSchemaArgProvider(
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val schemaDir: File
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> {
        // Note: If you're using KSP, you should change the line below to return
        // listOf("room.schemaLocation=${schemaDir.path}")
        return listOf("-Aroom.schemaLocation=${schemaDir.path}")
    }
}

android {
    namespace = "com.nononsenseapps.feeder"
    defaultConfig {
        applicationId = "com.nononsenseapps.feeder"
        versionCode = 283
        versionName = "2.5.0"
        compileSdk = 33
        minSdk = 23
        targetSdk = 33

        vectorDrawables.useSupportLibrary = true

        resourceConfigurations.addAll(getListOfSupportedLocales())

        // For espresso tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Export Room schemas
        javaCompileOptions {
            annotationProcessorOptions {
                compilerArgumentProviders(
                    RoomSchemaArgProvider(File(projectDir, "schemas")),
                )
                argument("room.incremental", "true")
            }
        }
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

// TODO
configurations.all {
    resolutionStrategy {
//    failOnVersionConflict()
//
//    force "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
//    force "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
//    force "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion"
//    force "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
//    force "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
//    force "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
    }
}

dependencies {
    kapt(libs.room)
    // For java time
    coreLibraryDesugaring(libs.desugar)

    // BOMS
    implementation(platform(libs.okhttp.bom))
    implementation(libs.coil.bom)
    implementation(platform(libs.compose.bom))

    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    implementation(libs.work.runtime.ktx)

    implementation(libs.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)

    // ViewModel
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.paging.runtime.ktx)

    // Compose
    implementation(libs.activity.compose)
    implementation(libs.ui)
    implementation(libs.foundation)
    implementation(libs.foundation.layout)
    implementation(libs.compose.material3)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.runtime)
    implementation(libs.ui.tooling)
    implementation(libs.navigation.compose)
    implementation(libs.paging.compose)
    implementation(libs.window)
    implementation(libs.android.material)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.navigation.animation)
    implementation(libs.accompanist.adaptive)
    implementation(libs.compose.material3.windowsizeclass)

    // HTML parsing
    implementation(libs.jsoup)
    implementation(libs.tagsoup)
    // RSS
    implementation(libs.rome)
    implementation(libs.rome.modules)

    // Includes conscrypt
    implementation(libs.bundles.okhttp.android)
    // Image loading
    implementation(libs.coil.base)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.compose)

    implementation(libs.bundles.kotlin.stdlib)
    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    // For doing coroutines on UI thread
    implementation(libs.kotlin.coroutines.android)
    // Dependency injection
    implementation(libs.kodein.androidx)
    // Custom tabs
    implementation(libs.androidx.browser)
    // Full text
    implementation(libs.readability4j)
    // For feeder-sync
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.adapters)
    implementation(libs.qrgen)
    // tests
    testImplementation(libs.bundles.kotlin.stdlib)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockk)
    testImplementation(libs.mockwebserver)

    androidTestImplementation(platform(libs.compose.bom))

    androidTestImplementation(libs.bundles.kotlin.stdlib)
    androidTestImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.kotlin.coroutines.test)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.mockwebserver)

    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit.ktx)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
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
            val localesConfig = """
                <?xml version="1.0" encoding="utf-8"?>
                <locale-config xmlns:android="http://schemas.android.com/apk/res/android">
${langs.joinToString("\n") { "                  <locale android:name=\"$it\"/>" }}
                </locale-config>
            """.trimIndent()

            localesConfigFile.bufferedWriter().use { writer ->
                writer.write(localesConfig)
            }
        }
    }
}
