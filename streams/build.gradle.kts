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
    alias(libs.plugins.ktlint)
    `maven-publish`
    alias(libs.plugins.jfrogs.artifactory)
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
            val releaseArtifact = layout.buildDirectory.file("outputs/aar/streams-release.aar")
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

artifactory {
    setContextUrl("https://bitmovin.jfrog.io/bitmovin")

    publish {
        repository {
            // The Artifactory repository key to publish to
            repoKey = if (version.toString().endsWith("SNAPSHOT")) "libs-snapshot-local" else "libs-release-local"
            username = System.getenv("ARTIFACTORY_USER") // The publisher username
            password = System.getenv("ARTIFACTORY_PASSWORD") // The publisher password
        }

        defaults {
            // Tell the Artifactory Plugin which artifacts should be published to Artifactory.
            publications("maven")
            setPublishArtifacts(true)
            setPublishPom(true)
        }
    }
}

// Make the publish task depend on the assemble task
tasks.withType<AbstractPublishToMaven>().forEach {
    it.dependsOn("assembleRelease")
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

// Configure the dokka plugin
tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("build/reports/$version/docs"))

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customStyleSheets =
            listOf(
                rootDir.resolve("docs_assets/dokka/logo-styles.css"),
                rootDir.resolve("docs_assets/dokka/styles.css"),
            )
        customAssets =
            listOf(
                rootDir.resolve("docs_assets/dokka/docs_logo.svg"),
                // Used as favicon
                rootDir.resolve("docs_assets/dokka/logo-icon.svg"),
            )
    }
}

/**
 * Task that generate the dokka Html docs and copy the generated files to the docs folder
 */
tasks.register("generateDocs") {
    dependsOn("dokkaHtml")
    doLast {
        val docsDir = rootDir.resolve("docs")
        docsDir.deleteRecursively()
        docsDir.mkdirs()
        rootDir.resolve("build/reports/$version/docs").copyRecursively(docsDir, true)
    }
}

/**
 * Checks if latest docs is the same as currently generating docs
 */
tasks.register("verifyLatestDocsIsUpToDate") {
    dependsOn("dokkaHtml")
    doLast {
        val latestDocs = rootDir.resolve("docs")
        val currentDocs = rootDir.resolve("build/reports/$version/docs")
        if (!latestDocs.exists() || !currentDocs.exists()) {
            throw Exception("Docs not generated")
        }
        // Compare each files of the dirs and check if they are the same
        if (!latestDocs.compareRecursivelyWith(currentDocs)) {
            throw Exception("Latest docs is not up-to-date. Please run the generateDocs task")
        }
    }
}

fun File.compareRecursivelyWith(other: File): Boolean {
    if (this.isDirectory) {
        if (!other.isDirectory) return false
        val thisFiles = this.listFiles() ?: return false
        val otherFiles = other.listFiles() ?: return false
        if (thisFiles.size != otherFiles.size) return false
        return thisFiles.all { it.compareRecursivelyWith(other.resolve(it.name)) }
    } else {
        return this.readText() == other.readText()
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
