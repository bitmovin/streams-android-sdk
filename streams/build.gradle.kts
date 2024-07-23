import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ktlint)
    `maven-publish`
}

// Setting up variables for the project
val streamsGroupId: String by project
val streamsArtifactId: String by project
val streamsVersion: String by project
group = streamsGroupId
version = streamsVersion

// Check if the version matches semantic versioning standard
val versionPattern = Regex("""^\d+\.\d+\.\d+(-SNAPSHOT)?${'$'}""")
if (!versionPattern.matches(version.toString())) {
    throw Exception("Invalid version")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            // Group and artifact configuration
            groupId = streamsGroupId
            artifactId = streamsArtifactId

            // Artifact configuration
            val releaseArtifact = layout.buildDirectory.file("outputs/aar/$streamsArtifactId-release.aar")
            artifact(artifact(releaseArtifact))

            // POM configuration
            pom {
                name.set("Bitmovin Streams SDK")
                description.set("Bitmovin Streams SDK")
                url.set("https://bitmovin.com")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                // Transitive dependencies
                withXml {
                    // dependencies { dependency {...}, ... }
                    // Changes made to the the XML are affected indirectly just because we manipulate in the withXml scope thanks to asNode().
                    val dependenciesNode = asNode().appendNode("dependencies")
                    configurations["api"].dependencies.forEach {
                        val dependencyNode = dependenciesNode.appendNode("dependency")
                        dependencyNode.appendNode("groupId", it.group)
                        dependencyNode.appendNode("artifactId", it.name)
                        dependencyNode.appendNode("version", it.version)
                    }
                }
            }
        }
    }
}

// Publishing will automatically generate the artifact
tasks.forEach {
    if (it.name.startsWith("publish")) {
        it.dependsOn("assembleRelease")
    }
}

android {
    namespace = streamsGroupId
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
