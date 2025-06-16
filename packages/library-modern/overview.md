# Realm Kotlin Modern Library

**Realm Kotlin Modern** is a modernized implementation of the Realm database for Kotlin Multiplatform projects. It serves as a drop-in replacement for `library-base` with improved performance, simplified dependencies, and modern Kotlin/Native FFI.

## Key Features

- **100% API Compatible**: Drop-in replacement for `library-base`
- **Modern FFI**: Uses Kotlin/Native FFI instead of SWIG+cinterop
- **Simplified Build**: No complex C++ compilation dependencies
- **Better Performance**: Direct FFI access reduces overhead
- **Improved Maintenance**: Cleaner architecture with fewer dependencies

## Architecture

The library maintains the same public API as `library-base` while implementing a modern internal architecture:

- **Public API Layer**: Identical interfaces to `library-base`
- **Modern Interop Layer**: Kotlin/Native FFI instead of SWIG
- **Platform Abstraction**: Clean expect/actual implementations
- **Direct Core Access**: Simplified bridge to realm-core

## Compatibility

Library Modern is designed to be a drop-in replacement. Simply change your dependency from:

```kotlin
implementation("com.santipbarber.realm-kotlin:library-base:2.1.0")
```

to:

```kotlin
implementation("com.santipbarber.realm-kotlin:library-modern:2.1.0")
```

All existing code continues to work without changes.

## Supported Platforms

- **Android** (API 16+)
- **JVM** (Java 11+)
- **iOS** (iOS 13+)
- **macOS** (macOS 10.15+)

## Performance Improvements

The modern implementation provides several performance benefits:

- Reduced JNI overhead through direct FFI
- Simplified object lifecycle management
- Better memory usage patterns
- Faster query execution

## Migration Guide

No code changes are required when migrating from `library-base` to `library-modern`. The API is identical.

The only change needed is updating your dependency declaration in your build files.