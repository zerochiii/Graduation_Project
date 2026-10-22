import com.android.build.api.variant.BuildConfigField
import groovy.json.JsonOutput
import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun normalizeApiBaseUrl(value: String): String {
    val normalized = value.trim().trimEnd('/') + "/"
    val uri = runCatching { URI(normalized) }.getOrNull()
    require(uri != null && uri.scheme?.lowercase() in setOf("http", "https")) {
        "API_BASE_URL must use HTTP or HTTPS"
    }
    require(!uri.host.isNullOrBlank()) {
        "API_BASE_URL must include a host"
    }
    require(uri.port == -1 || uri.port in 1..65535) {
        "API_BASE_URL port must be between 1 and 65535"
    }
    require(uri.rawUserInfo == null && uri.rawQuery == null && uri.rawFragment == null) {
        "API_BASE_URL must not contain credentials, a query, or a fragment"
    }
    return normalized
}

val apiBaseUrl = providers.provider {
    val localSettings = Properties().apply {
        val settingsFile = rootProject.file("local.properties")
        if (settingsFile.exists()) settingsFile.inputStream().use { load(it) }
    }
    val configuredValue = localSettings.getProperty("API_BASE_URL")
    require(!configuredValue.isNullOrBlank()) {
        "Missing API_BASE_URL in root local.properties (for example, API_BASE_URL=http://127.0.0.1:8000/)"
    }
    normalizeApiBaseUrl(configuredValue)
}

androidComponents {
    onVariants { variant ->
        variant.buildConfigFields.put(
            "API_BASE_URL",
            apiBaseUrl.map { BuildConfigField("String", JsonOutput.toJson(it), null) }
        )
    }
}

android {
    namespace = "com.example.graduationproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.graduationproject"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    useLibrary("org.apache.http.legacy")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
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
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.apache.commons.lang3) //apache
    implementation ("com.google.code.gson:gson:2.9.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation(project(":cameraModule"))
    implementation("io.coil-kt:coil-compose:2.6.0")
}
