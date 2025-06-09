# Changelog - Realm Kotlin SDK (Community Fork)

All notable changes to this community fork will be documented in this file.

## [2.1.0] - 2024-12-09

### 🎉 Initial Community Fork Release

This is the first release of the community-maintained fork of Realm Kotlin SDK, focused on providing up-to-date support for modern Kotlin and Compose Multiplatform versions.

### ✨ New Features
- **Kotlin 2.1.21** - Full support for the latest stable Kotlin version
- **Compose Multiplatform 1.8.1** - Updated example projects and compatibility
- **Gradle 8.5** - Modernized build system with latest Gradle
- **GitHub Packages** - Streamlined distribution via GitHub Packages

### 🔧 Technical Improvements
- Updated all Gradle wrapper files across the project to version 8.5
- Replaced deprecated `buildDir` references with `layout.buildDirectory`
- Modernized build scripts for Gradle 8.x compatibility
- Updated dependency versions for better stability and performance
- Improved build warnings and deprecated API usage

### 📦 Package Changes
- **Group ID**: Changed from `io.realm.kotlin` to `com.santipbarber.realm-kotlin`
- **Version Scheme**: Now aligned with Kotlin versions (2.1.x for Kotlin 2.1.x)
- **Publishing**: Migrated to GitHub Packages for easier distribution

### 🛠️ Migration Guide

If you're migrating from the original Realm Kotlin SDK:

1. **Update your build.gradle.kts**:
   ```kotlin
   // Old
   id("io.realm.kotlin") version "3.0.0"
   implementation("io.realm.kotlin:library-base:3.0.0")
   
   // New  
   id("com.santipbarber.realm-kotlin") version "2.1.0"
   implementation("com.santipbarber.realm-kotlin:library-base:2.1.0")
   ```

2. **Add GitHub Packages repository**:
   ```kotlin
   maven {
       url = uri("https://maven.pkg.github.com/santipbarber/realm-kotlin")
       credentials {
           username = System.getenv("GITHUB_ACTOR")
           password = System.getenv("GITHUB_TOKEN")
       }
   }
   ```

3. **No code changes required** - The API remains 100% compatible

### 📋 Supported Platforms
- Android (API 16+)
- JVM (Java 8+)  
- iOS (iOS 12+)
- macOS (macOS 10.14+)

### 🔗 Links
- **Repository**: https://github.com/santipbarber/realm-kotlin
- **Issues**: https://github.com/santipbarber/realm-kotlin/issues
- **Original Project**: https://github.com/realm/realm-kotlin

### 🙏 Acknowledgments

This fork is based on the excellent work of the MongoDB/Realm team. All core functionality and APIs remain their intellectual property. This fork exists solely to provide continued support for modern Kotlin toolchain versions.

---

**Previous versions**: For changelog of versions prior to this fork, please refer to the [original project's changelog](https://github.com/realm/realm-kotlin/blob/master/CHANGELOG.md).