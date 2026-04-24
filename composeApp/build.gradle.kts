import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val composeMultiplatformVersion = "1.10.3"
val ktorVersion = "3.3.1"
val kotlinxDatetimeVersion = "0.7.1"

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SmartBudgetMvp"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.compose.runtime:runtime:$composeMultiplatformVersion")
                implementation("org.jetbrains.compose.foundation:foundation:$composeMultiplatformVersion")
                implementation("org.jetbrains.compose.material:material:$composeMultiplatformVersion")
                implementation("org.jetbrains.compose.ui:ui:$composeMultiplatformVersion")
                implementation("org.jetbrains.compose.components:components-resources:$composeMultiplatformVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.1")
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("androidx.appcompat:appcompat:1.7.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
                implementation("androidx.navigation:navigation-compose:2.7.7")
                implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
                implementation("androidx.compose.material3:material3:1.2.1")
                implementation("androidx.compose.material:material-icons-extended:1.6.8")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

                implementation("com.squareup.retrofit2:retrofit:2.11.0")
                implementation("com.squareup.retrofit2:converter-gson:2.11.0")
                implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

                implementation("androidx.room:room-runtime:2.8.4")
                implementation("androidx.room:room-ktx:2.8.4")

                implementation("androidx.work:work-runtime-ktx:2.9.0")
                implementation("com.yandex.android:authsdk:3.1.3")
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
    }
}

android {
    namespace = "com.hrach.financeapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hrach.financeapp.mvp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

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
        buildConfigField(
            "String",
            "FINANCE_API_TOKEN",
            "\"${providers.gradleProperty("FINANCE_API_TOKEN").orElse("").get()}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    add("kspAndroid", "androidx.room:room-compiler:2.8.4")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

compose.desktop {
    application {
        mainClass = "com.hrach.financeapp.mvp.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "SmartBudgetMvp"
            packageVersion = "1.0.0"
        }
    }
}
