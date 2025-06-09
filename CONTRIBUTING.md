# Contributing to Realm Kotlin SDK (Community Fork)

Thank you for your interest in contributing to this community fork of the Realm Kotlin SDK!

## About This Fork

This is a community-maintained fork focused on keeping the Realm Kotlin SDK up-to-date with the latest Kotlin and Compose Multiplatform versions. We welcome contributions that help achieve this goal.

## How to Contribute

### 🐛 Bug Reports
- Open an issue on [GitHub Issues](https://github.com/santipbarber/realm-kotlin/issues)
- Include detailed steps to reproduce
- Specify your environment (Kotlin version, platform, etc.)

### ✨ Feature Requests
- Open an issue with the "enhancement" label
- Describe your use case and proposed solution
- Focus on modernization and compatibility improvements

### 🔧 Pull Requests
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Test your changes thoroughly
5. Commit your changes (`git commit -m 'Add some amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## Development Setup

### Prerequisites

- **Java 11+**
- **Kotlin 2.1.21**
- **Android SDK** (for Android targets)
- **Xcode** (for iOS/macOS targets, macOS only)
- **CMake 3.18.1+**
- **Swig 4.2.0+** (install via `brew install swig` on macOS)
- **Ccache** (install via `brew install ccache` on macOS)

### Environment Variables

```bash
export ANDROID_HOME=/path/to/android/sdk
export JAVA_HOME=/path/to/java/11
export NDK_HOME=$ANDROID_HOME/ndk/version
```

### Getting the Source Code

```bash
git clone --recursive https://github.com/santipbarber/realm-kotlin.git
cd realm-kotlin
```

### Building

```bash
# Build all packages
./gradlew build

# Build specific module
./gradlew :packages:library-base:build

# Run tests
./gradlew test
```

### Publishing Locally

```bash
# Publish to local repository for testing
./gradlew publishToMavenLocal
```

## Development Guidelines

### 🎯 Focus Areas
- **Kotlin Version Updates**: Keep compatible with latest Kotlin releases
- **Compose Multiplatform**: Ensure compatibility with latest CMP versions
- **Gradle Modernization**: Update build scripts and remove deprecations
- **Dependency Updates**: Keep dependencies current and secure

### 📝 Code Style
- Follow existing code conventions
- Use ktlint for formatting (`./gradlew ktlintFormat`)
- Run detekt for static analysis (`./gradlew detekt`)

### 🧪 Testing
- Add tests for new features
- Ensure existing tests pass
- Test on multiple platforms when possible

## Release Process

Releases follow semantic versioning aligned with Kotlin versions:
- `2.1.x` - Compatible with Kotlin 2.1.x
- `2.2.x` - Compatible with Kotlin 2.2.x (future)

## Getting Help

- **Documentation**: Refer to the [original Realm docs](https://www.mongodb.com/docs/atlas/device-sdks/sdk/kotlin/)
- **Issues**: Search [existing issues](https://github.com/santipbarber/realm-kotlin/issues)
- **Discussions**: Use GitHub Discussions for questions

## License

By contributing to this project, you agree that your contributions will be licensed under the Apache License 2.0.

## Acknowledgments

This fork is based on the original [Realm Kotlin SDK](https://github.com/realm/realm-kotlin). We're grateful to the MongoDB/Realm team for their excellent work on the core functionality.

---

**Questions?** Feel free to reach out at santipbr@gmail.com or open an issue!