import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)

    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

apply(plugin = "dagger.hilt.android.plugin")

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("key.properties")

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

val ciStoreFile = project.findProperty("STORE_FILE") as String?
val ciStorePassword = project.findProperty("STORE_PASSWORD") as String?
val ciKeyAlias = project.findProperty("KEY_ALIAS") as String?
val ciKeyPassword = project.findProperty("KEY_PASSWORD") as String?

val hasCiSigning = listOf(
    ciStoreFile,
    ciStorePassword,
    ciKeyAlias,
    ciKeyPassword
).all { it != null }

val hasLocalSigning = keystorePropertiesFile.exists()


android {
    namespace = "com.nexusbihar.fitnessapp"
    compileSdk = 36
//    compileSdk {
//        version = release(36)
//    }

    defaultConfig {
        applicationId = "com.nexusbihar.fitnessapp"
        minSdk = 26
        targetSdk = 36
        versionCode = (project.findProperty("VERSION_CODE") as String?)?.toInt() ?: 1
        versionName = project.findProperty("VERSION_NAME") as String? ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Load Google Maps API key from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = localProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: "dummy_key_for_testing"
    }

    signingConfigs {
        if (hasCiSigning) {
            create("release") {
                storeFile = rootProject.file(ciStoreFile!!)
                storePassword = ciStorePassword
                keyAlias = ciKeyAlias
                keyPassword = ciKeyPassword
            }
        } else if (hasLocalSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false

            if (hasCiSigning || hasLocalSigning) {
                signingConfig = signingConfigs.getByName("release")
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions{
//        kotlinCompilerExtensionVersion = "1.4.6"
        kotlinCompilerExtensionVersion = "2.3.0"
    }

    packaging {
        resources{
//            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes += listOf(
                "META-INF/*.md",
                "META-INF/*.txt"
            )
        }
    }

    lint{
        abortOnError = false
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.credentials)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials.play.services.auth)

    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Room DB
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.8.4")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.58")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    kapt("com.google.dagger:hilt-compiler:2.58")
    kapt("androidx.hilt:hilt-compiler:1.3.0")

    // Google Fit
    implementation("com.google.android.gms:play-services-fitness:21.3.0")
    implementation("com.google.android.gms:play-services-auth:21.5.0")

    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.maps.android:maps-compose:7.0.0")
    implementation("com.google.maps.android:maps-ktx:5.2.2")
    implementation("com.google.maps.android:maps-utils-ktx:5.2.2")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // WorkManager for notifications
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    implementation("androidx.hilt:hilt-work:1.3.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    // PDF Generation
    implementation("com.itextpdf:itext7-core:9.5.0")

}