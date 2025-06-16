import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

/*
 * Copyright 2024 Realm Inc.
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

// AtomicFu configuration for modern implementation
project.extensions.configure(kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension::class) {
    transformJvm = false
}

// Kotlin multiplatform configuration
@Suppress("UNUSED_VARIABLE")
kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosSimulatorArm64()
    iosArm64()
    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(kotlin("reflect"))
                
                // Core Kotlin dependencies
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
                implementation("org.jetbrains.kotlinx:atomicfu:${Versions.atomicfu}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serialization}")
                
                // Keep library-base as reference for compatibility
                // Temporarily disabled while library-base has compilation issues
                // implementation(project(":library-base"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${Versions.coroutines}")
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
            }
        }
        
        // Remove explicit dependsOn calls to use Kotlin's default hierarchy template
        // val nativeMain by creating {
        //     dependsOn(commonMain)
        // }
        
        // val iosArm64Main by getting {
        //     dependsOn(nativeMain)
        // }
        // val iosSimulatorArm64Main by getting {
        //     dependsOn(nativeMain)
        // }
        // val iosX64Main by getting {
        //     dependsOn(nativeMain)
        // }
    }

    // Explicit API mode for clean public API
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
}

// Configure module name for internal methods
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.moduleName.set("io.realm.kotlin.library.modern")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-opt-in=kotlin.ExperimentalMultiplatform")
    }
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
        freeCompilerArgs.add("-opt-in=kotlin.native.runtime.NativeRuntimeApi")
        freeCompilerArgs.add("-opt-in=kotlin.experimental.ExperimentalNativeApi")
    }
}

// Android configuration
android {
    namespace = "io.realm.kotlin.modern"
    compileSdk = Versions.Android.compileSdkVersion
    // buildToolsVersion removed as AGP 8.6.1 uses default version automatically

    defaultConfig {
        minSdk = Versions.Android.minSdk
        // targetSdk deprecated in library modules, removed
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
    
    buildFeatures {
        buildConfig = false
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

// Documentation configuration
tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    moduleName.set("Realm Kotlin Modern SDK")
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
            perPackageOption {
                matchingRegex.set(""".*\.modern\..*""")
                suppress.set(true)
            }
            jdkVersion.set(8)
        }
        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting {
            includes.from(
                "overview.md"
            )
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

// Publishing configuration
publishing {
    publications.withType<MavenPublication> {
        artifact(javadocJar.get())
        
        pom {
            name.set("Realm Kotlin - Modern Interop Layer Edition")
            description.set("Revolutionary Modern Interop Layer for Realm Kotlin with complete SWIG elimination, " +
                "reactive programming with Flow<RealmChange<T>>, advanced query system with 20+ operators, " +
                "dynamic proxy system, and performance optimizations. Private fork by SantipBarber.")
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