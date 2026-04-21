plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.hrach.financeapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hrach.financeapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 6
        versionName = "1.0.4"

        manifestPlaceholders["YANDEX_CLIENT_ID"] = providers
            .gradleProperty("YANDEX_CLIENT_ID")
            .orElse("")
            .get()

        buildConfigField(
            "String",
            "YANDEX_CLIENT_ID",
            "\"${providers.gradleProperty("YANDEX_CLIENT_ID").orElse("").get()}\""
        )
        buildConfigField(
            "boolean",
            "FEATURE_AI_HELP_BUTTON",
            providers.gradleProperty("FEATURE_AI_HELP_BUTTON").orElse("false").get()
        )
    }

    signingConfigs {
        create("release") {
            storeFile = file("financeapp-release-key.jks")
            storePassword = "finance123"
            keyAlias = "financeapp-key"
            keyPassword = "finance123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true  // Включить для оптимизации
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("com.yandex.android:authsdk:3.1.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
