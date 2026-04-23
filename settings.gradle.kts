import org.gradle.api.JavaVersion
import org.gradle.api.GradleException

val allowUnsupportedBuildJdk = providers.gradleProperty("allowUnsupportedBuildJdk")
    .map { it.toBoolean() }
    .getOrElse(false)

if (!allowUnsupportedBuildJdk) {
    val currentJvm = JavaVersion.current()
    if (currentJvm > JavaVersion.VERSION_25) {
        throw GradleException(
            """
            Unsupported build JDK: ${System.getProperty("java.version")}.
            This project currently uses Kotlin 1.9.24 + KSP 1.9.24-1.0.20, which can crash on JDK 26+ during KSP tasks.

            Please run Gradle with JDK 17, 21, 24, or 25.
            Android Studio: File > Settings > Build Tools > Gradle > Gradle JDK.
            CLI example: JAVA_HOME=/path/to/jdk-21 ./gradlew assembleRelease

            If you intentionally want to bypass this check, use -PallowUnsupportedBuildJdk=true.
            """.trimIndent()
        )
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SmartBudget"
include(":app")
include(":composeApp")
