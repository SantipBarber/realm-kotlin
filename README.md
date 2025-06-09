# Realm Kotlin SDK - Community Fork

[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache-blue.svg)](https://github.com/santipbarber/realm-kotlin/blob/main/LICENSE)
[![GitHub](https://img.shields.io/github/v/release/santipbarber/realm-kotlin)](https://github.com/santipbarber/realm-kotlin/releases)

## About This Fork

This is a community-maintained fork of the Realm Kotlin SDK, updated to support the latest versions of Kotlin and Compose Multiplatform. This fork was created to provide continued support and updates for Kotlin Multiplatform projects using modern toolchain versions.

### Key Updates

- ✅ **Kotlin 2.1.21** support
- ✅ **Compose Multiplatform 1.8.1** compatibility  
- ✅ **Gradle 8.5** compatibility
- ✅ Updated to latest dependency versions
- ✅ Modernized build scripts and deprecated API replacements

## Why Use This Fork

* **Up-to-date Dependencies**: Always compatible with the latest Kotlin and Compose Multiplatform versions
* **Community Driven**: Maintained by developers, for developers
* **Backward Compatible**: Drop-in replacement for the original Realm Kotlin SDK
* **Active Maintenance**: Regular updates and bug fixes

## Installation

### Gradle (Kotlin DSL)

Add this to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.santipbarber.realm-kotlin") version "2.1.0"
}

dependencies {
    implementation("com.santipbarber.realm-kotlin:library-base:2.1.0")
    // For sync features (optional)
    implementation("com.santipbarber.realm-kotlin:library-sync:2.1.0")
}
```

### Maven

```xml
<dependency>
    <groupId>com.santipbarber.realm-kotlin</groupId>
    <artifactId>library-base</artifactId>
    <version>2.1.0</version>
</dependency>
```

## GitHub Packages Setup

This library is published to GitHub Packages. Add this repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/santipbarber/realm-kotlin")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token") as String?
            }
        }
    }
}
```

## Quick Start

```kotlin
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Task : RealmObject {
    @PrimaryKey
    var id: String = ""
    var name: String = ""
    var completed: Boolean = false
}

// Configure and open realm
val config = RealmConfiguration.Builder(schema = setOf(Task::class))
    .build()
val realm = Realm.open(config)

// Write data
realm.write {
    copyToRealm(Task().apply {
        id = "task1"
        name = "My first task"
    })
}

// Query data
val tasks = realm.query<Task>().find()
```

## Supported Platforms

- Android (API 16+)
- JVM
- iOS (iOS 12+)  
- macOS (macOS 10.14+)

## Migration from Original Realm Kotlin

This fork is a drop-in replacement. Simply update your dependencies:

1. Replace `io.realm.kotlin` with `com.santipbarber.realm-kotlin` in your build files
2. Update to version `2.1.0`
3. Ensure you have access to GitHub Packages

## Documentation

For detailed documentation, please refer to the [original Realm Kotlin documentation](https://www.mongodb.com/docs/atlas/device-sdks/sdk/kotlin/) as the API remains the same.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## Versioning

This fork follows semantic versioning aligned with Kotlin versions:
- `2.1.x` - Compatible with Kotlin 2.1.x
- `2.0.x` - Compatible with Kotlin 2.0.x

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This fork is based on the original [Realm Kotlin SDK](https://github.com/realm/realm-kotlin) by MongoDB/Realm. All credit for the core functionality goes to the original authors and maintainers.

---

**Maintained by**: [Santi P. Barber](https://github.com/santipbarber)  
**Contact**: santipbr@gmail.com