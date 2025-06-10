import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

/*
 * Copyright 2020 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("realm-publisher")
    `maven-publish`
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization") version Versions.kotlin
    id("org.jetbrains.kotlinx.atomicfu") version Versions.atomicfu
}
repositories {
    google()
    mavenCentral()
}

// AtomicFu cannot transform JVM code. Maybe an issue with using IR backend. Throws
// ClassCastException: org.objectweb.asm.tree.InsnList cannot be cast to java.lang.Iterable
project.extensions.configure(kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension::class) {
    transformJvm = false
}

// Common Kotlin configuration
@Suppress("UNUSED_VARIABLE")
kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosSimulatorArm64()
    iosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"))
                // If runtimeapi is merged with cinterop then we will be exposing both to the users
                // Runtime holds annotations, etc. that has to be exposed to users
                // Cinterop does not hold anything required by users
                // Temporarily removed cinterop dependency for simplified publishing
                // api(project(":packages:cinterop"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
                implementation("org.jetbrains.kotlinx:atomicfu:${Versions.atomicfu}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serialization}")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
            }
        }
        val nativeIos by creating {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting {
            dependsOn(nativeIos)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(nativeIos)
        }
        val iosX64Main by getting {
            dependsOn(nativeIos)
        }
    }

    // Require that all methods in the API have visibility modifiers and return types.
    // Anything inside `io.realm.kotlin.internal.*` is considered internal regardless of their
    // visibility modifier and will be stripped from Dokka, but will unfortunately still
    // leak into auto-complete in the IDE.
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
}

// Using a custom name module for internal methods to avoid default name mangling in Kotlin compiler which uses the module
// name and build type variant as a suffix, this default behaviour can cause mismatch at runtime https://github.com/realm/realm-kotlin/issues/621
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.moduleName.set("io.realm.kotlin.library")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
    }
}

// Android configuration
android {
    namespace = "io.realm.kotlin"
    compileSdk = Versions.Android.compileSdkVersion
    buildToolsVersion = Versions.Android.buildToolsVersion

    defaultConfig {
        minSdk = Versions.Android.minSdk
        targetSdk = Versions.Android.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        sourceSets {
            getByName("main") {
                manifest.srcFile("src/androidMain/AndroidManifest.xml")
                jniLibs.srcDir("src/androidMain/jniLibs")
            }
        }
        ndk {
            abiFilters += setOf("x86_64", "arm64-v8a")
        }
    }

    buildTypes {
        getByName("debug") {
            consumerProguardFiles("proguard-rules-consumer-common.pro")
        }
        getByName("release") {
            consumerProguardFiles("proguard-rules-consumer-common.pro")
        }
    }
    compileOptions {
        sourceCompatibility = Versions.sourceCompatibilityVersion
        targetCompatibility = Versions.targetCompatibilityVersion
    }
    // Skip BuildConfig generation as it overlaps with io.realm.kotlin.BuildConfig from realm-java
    buildFeatures {
        buildConfig = false
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

// realmPublish {
//     pom {
//         name = "Library"
//         description = "Library code for Realm Kotlin. This artifact is not " +
//             "supposed to be consumed directly, but through " +
//             "'io.realm.kotlin:gradle-plugin:${Realm.version}' instead."
//     }
// }

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    moduleName.set("Realm Kotlin SDK")
    moduleVersion.set(Realm.version)
    dokkaSourceSets {
        configureEach {
            moduleVersion.set(Realm.version)
            reportUndocumented.set(true)
            skipEmptyPackages.set(true)
            perPackageOption {
                matchingRegex.set(""".*\.internal.*""")
                suppress.set(true)
            }
            jdkVersion.set(8)
        }
        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting {
            includes.from(
                "overview.md",
                // TODO We could actually include package descriptions in top level overview file
                //  with:
                //    # package io.realm.kotlin
                //  Maybe worth a consideration
                "src/commonMain/kotlin/io/realm/kotlin/info.md",
                "src/commonMain/kotlin/io/realm/kotlin/log/info.md"
            )
            sourceRoot("../runtime-api/src/commonMain/kotlin")
        }
    }
}

tasks.register("dokkaJar", Jar::class) {
    val dokkaTask = "dokkaHtmlPartial"
    dependsOn(dokkaTask)
    archiveClassifier.set("dokka")
    from(tasks.named(dokkaTask).get().outputs)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

// Make sure that docs are published for the Metadata publication as well. This is required
// by Maven Central
publishing {
    // See https://dev.to/kotlin/how-to-build-and-publish-a-kotlin-multiplatform-library-going-public-4a8k
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())
        
        // Configure POM
        pom {
            name.set("Realm Kotlin Library Base")
            description.set("Library code for Realm Kotlin. This artifact is not " +
                "supposed to be consumed directly, but through " +
                "'com.santipbarber.realm-kotlin:gradle-plugin:${Realm.version}' instead.")
            url.set("https://github.com/santipbarber/realm-kotlin")
            
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            
            developers {
                developer {
                    name.set("Santi P. Barber")
                    email.set("santipbr@gmail.com")
                }
            }
            
            scm {
                connection.set("scm:git:git://github.com/santipbarber/realm-kotlin.git")
                developerConnection.set("scm:git:ssh://github.com/santipbarber/realm-kotlin.git")
                url.set("https://github.com/santipbarber/realm-kotlin")
            }
        }
    }

    val common = publications.getByName("kotlinMultiplatform") as MavenPublication
    // Configuration through examples/kmm-sample does not work if we do not resolve the tasks
    // completely, hence the .get() below.
    common.artifact(tasks.named("dokkaJar").get())
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/santipbarber/realm-kotlin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
