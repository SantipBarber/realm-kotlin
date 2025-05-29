# Potential Breaking Changes and Deprecations for Kotlin 2.1.x Upgrade

This document summarizes potential breaking changes and deprecations identified from the Kotlin 2.1.0 compatibility guide and Kotlin 2.1.20 release notes that might be relevant to this project.

## Kotlin 2.1.0 Compatibility Guide

**Language Features:**

*   **Removed Language Versions 1.4 and 1.5:**
    *   **Impact:** Source incompatibility if the project uses these older language versions.
    *   **Action:** Ensure the project's Kotlin language version is 1.6 or higher (1.8+ recommended).
*   **Prohibit Exposing Types Through Type Parameters' Bounds:**
    *   **Impact:** Source incompatibility. Code exposing types with lower visibility through type parameter bounds will no longer compile.
    *   **Action:** Refactor code to ensure type parameter bounds follow standard visibility rules.
*   **Prohibit Inheriting Abstract `var` and `val` with Same Name:**
    *   **Impact:** Source incompatibility. Classes inheriting an abstract `var` from an interface and a `val` with the same name from a superclass will cause a compilation error.
    *   **Action:** Rename or refactor conflicting properties.
*   **Error on Accessing Uninitialized Enum Entries:**
    *   **Impact:** Source incompatibility. Code accessing enum entries before they are initialized will cause an error.
    *   **Action:** Review enum initializations to ensure entries are accessed only after full initialization.
*   **JSpecify Nullability Mismatch Severity Raised to Error:**
    *   **Impact:** Source incompatibility. Code that previously had warnings for JSpecify nullability mismatches will now have errors.
    *   **Action:** Fix nullability issues or use the `-Xnullability-annotations` compiler option to manage severity.
*   **SAM Constructors for JDK Interfaces - Nullable Values Prohibited:**
    *   **Impact:** Source incompatibility. Returning nullable values from lambdas in SAM constructors of JDK functional interfaces (where the type argument is non-nullable) is an error.
    *   **Action:** Ensure lambdas return values matching the nullability of the interface's type argument.
*   **Private Operator Functions in Public Inline Functions Forbidden:**
    *   **Impact:** Source incompatibility. Accessing private operator functions (e.g., `getValue`, `setValue`) within public inline functions is an error.
    *   **Action:** Make operator functions internal or public if they need to be accessed from public inline functions, or refactor the inline function.

**Standard Library:**

*   **Deprecated Locale-Sensitive Case Conversion Functions (now errors):**
    *   **Impact:** Source incompatibility. Usage of `Char.toUpperCase()`, `String.toLowerCase()`, etc., without a `Locale` will cause errors.
    *   **Action:** Replace with locale-agnostic alternatives (`String.lowercase()`, `Char.uppercase()`) or provide an explicit `Locale` (e.g., `String.lowercase(Locale.getDefault())`).
*   **`kotlin-stdlib-common.jar` Artifact Removed:**
    *   **Impact:** Binary incompatibility, though unlikely to directly affect most projects unless they manually interact with multiplatform metadata.
    *   **Action:** No action is likely needed for this project.
*   **`appendln()` Deprecated in Favor of `appendLine()` (now an error):**
    *   **Impact:** Source incompatibility. `StringBuilder.appendln()` will cause an error.
    *   **Action:** Replace with `StringBuilder.appendLine()`.
*   **Deprecated Freezing-Related APIs in Kotlin/Native (now errors):**
    *   **Impact:** Source incompatibility for Kotlin/Native code.
    *   **Action:** Migrate away from freezing APIs if any Kotlin/Native code exists.

**Gradle Plugin:**

*   **`KotlinCompilationOutput#resourcesDirProvider` Deprecated:**
    *   **Impact:** Build script warnings, potential future errors.
    *   **Action:** Use `KotlinSourceSet.resources` in Gradle build scripts.
*   **Deprecated `registerKotlinJvmCompileTask(taskName, moduleName)`:**
    *   **Impact:** Build script warnings, potential future errors.
    *   **Action:** Use the new overload `registerKotlinJvmCompileTask(taskName, compilerOptions, explicitApiMode)`.
*   **Deprecated `registerKaptGenerateStubsTask(taskName)`:**
    *   **Impact:** Build script warnings, potential future errors (if `kapt` is used).
    *   **Action:** Use the new overload `registerKaptGenerateStubsTask(compileTask, kaptExtension, explicitApiMode)`.
*   **`kotlin-compiler-embeddable` Removed from Build Runtime Dependencies:**
    *   **Impact:** Source incompatibility if the project relied on this transitive dependency from the Kotlin Gradle Plugin.
    *   **Action:** Explicitly add `kotlin-compiler-embeddable` if needed, or adjust dependencies.
*   **Removed Deprecated Platform Plugin IDs (e.g., `kotlin-platform-common`):**
    *   **Impact:** Source incompatibility if these old plugin IDs are used.
    *   **Action:** Use current plugin IDs (e.g., `org.jetbrains.kotlin.multiplatform`).

## Kotlin 2.1.20 Release Notes

**Breaking Changes and Deprecations:**

*   **`withJava()` Function Phased Out (Kotlin Multiplatform):**
    *   **Impact:** Relevant for Kotlin Multiplatform projects. Java source sets are now created by default.
    *   **Action:** Remove explicit `withJava()` calls. If using Java test fixtures, upgrade to Kotlin 2.1.21 or later. (Likely not directly relevant to this project unless it has unobserved multiplatform modules).
*   **`kotlin-android-extensions` Plugin Deprecated (Error):**
    *   **Impact:** Using this plugin now causes a configuration error.
    *   **Action:** Migrate to alternatives like View Binding or Jetpack Compose. (Not relevant for this non-Android project).
*   **`kotlin.incremental.classpath.snapshot.enabled` Property Removed:**
    *   **Impact:** Build configuration. This property is obsolete.
    *   **Action:** Remove this property from `gradle.properties` if present.

**General Focus Points for This Project:**

*   **K2 Compiler:** Kotlin 2.1.x continues the rollout and refinement of the K2 compiler.
    *   **K2 `kapt` by Default (from 2.1.20):** If `kapt` is used (even transitively or in other modules not yet touched), its behavior might change or issues could arise.
    *   **Behavioral Changes:** Be aware of subtle behavioral changes listed in the compatibility guide (e.g., smart cast propagation, visibility alignment) that might only surface during runtime or testing.
*   **Thorough Testing:** Essential to catch any regressions or unexpected behavior due to compiler updates or API changes.

This list is based on the provided documentation and aims to highlight areas of concern for the upgrade. A detailed audit of the codebase and build scripts is recommended.
