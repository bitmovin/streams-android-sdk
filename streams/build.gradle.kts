import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

buildscript {
    dependencies {
        classpath(libs.gradlePlugins.dokka.base)
    }
}

plugins {
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.dokka)

    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

android {
    namespace = "com.bitmovin.streams"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("build/reports/$version/docs"))

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customStyleSheets =
            listOf(
                rootDir.resolve("docs/dokka/logo-styles.css"),
                rootDir.resolve("docs/dokka/styles.css"),
            )
        customAssets =
            listOf(
                rootDir.resolve("docs/dokka/docs_logo.svg"),
                // Used as favicon
                rootDir.resolve("docs/dokka/logo-icon.svg"),
            )
    }
}

kotlin {
    explicitApi()

    explicitApi = ExplicitApiMode.Strict
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    api(libs.google.gson)
    api(libs.bitmovin.player)
    api(libs.csscolor4j)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
