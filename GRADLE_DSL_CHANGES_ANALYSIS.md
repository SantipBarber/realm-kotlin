# Gradle DSL Changes Analysis for Kotlin 2.1.x Upgrade

This document outlines necessary updates to Gradle build scripts (`build.gradle.kts`) to align with DSL changes in Kotlin 2.1.x.

## Summary of Findings:

The primary areas requiring changes are:
1.  **Migration of `kotlinOptions.jvmTarget`**: Several modules use the older `kotlinOptions.jvmTarget` DSL. This needs to be updated to the new `compilerOptions.jvmTarget.set(JvmTarget.fromTarget("VERSION"))` or `kotlin { jvmToolchain(VERSION_INT) }` DSL.
2.  **Migration of `kotlinOptions.freeCompilerArgs`**: Some native target configurations in `packages/cinterop/build.gradle.kts` use the older `kotlinOptions.freeCompilerArgs += ...`. This needs to be updated to `compilerOptions.freeCompilerArgs.add(...)` or `compilerOptions.freeCompilerArgs.addAll(...)`.
3.  **Migration of Gradle `application` plugin**: One module (`examples/kmm-sample/compose-desktop/build.gradle.kts`) uses the standard Gradle `application` plugin and needs to be migrated to the new `kotlin { jvm { binaries.executable { ... } } }` DSL.

Other checked items (deprecated `resourcesDirProvider`, deprecated task registration functions, old platform plugin IDs) were not found to be an issue. Explicit dependencies on `kotlin-compiler-embeddable` were noted in test and compiler plugin modules, which seems intentional and not directly an issue for migration, but worth being aware of.

## Detailed Findings & Actions:

### 1. Root and `buildSrc`
*   **`build.gradle.kts` (root):** No changes needed.
*   **`buildSrc/build.gradle.kts`:** No direct changes needed. (Note: Potential indirect impact if custom plugins relied on `kotlin-compiler-embeddable` being transitively available via KGP's runtime classpath).
*   **`buildSrc/buildSrc/build.gradle.kts`:** No changes needed.

### 2. `benchmarks` Directory
*   **`benchmarks/androidApp/build.gradle.kts`:**
    *   **Action:** Migrate `kotlinOptions { jvmTarget = Versions.kotlinJvmTarget }` (line 18) to the new compiler options DSL (e.g., within `android { kotlinOptions { jvmTarget = ... } }` or preferably `tasks.withType<KotlinCompile>().configureEach { compilerOptions.jvmTarget.set(JvmTarget.fromTarget(Versions.kotlinJvmTarget)) }` or `kotlin { jvmToolchain(...) }`).
*   **`benchmarks/build.gradle.kts`:**
    *   **Action:** The `allprojects { tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = Versions.kotlinJvmTarget } }` block (line 35) uses the old DSL. This should be migrated. Preferably, `jvmTarget` should be set per-module using the new DSL. If a global approach is kept, it must use the new `compilerOptions` syntax.
*   **`benchmarks/jvmApp/build.gradle.kts`:**
    *   **Action:** Migrate `tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = Versions.kotlinJvmTarget }` (line 23) to the new compiler options DSL.
*   **`benchmarks/shared/build.gradle.kts`:**
    *   **Action:** `jvmTarget` is currently inherited from `benchmarks/build.gradle.kts`. If that parent configuration is removed or made per-module, ensure `jvmTarget` is explicitly set here using the new DSL (if not relying on defaults).

### 3. `examples` Directory
*   **`examples/kmm-sample/androidApp/build.gradle.kts`:**
    *   **Action (Recommendation):** Consider explicitly setting the Kotlin JVM target using the new DSL for clarity (e.g., `kotlin { jvmToolchain(...) }` or `tasks.withType<KotlinCompile>().configureEach { compilerOptions.jvmTarget.set(...) }`), even though the Android plugin often aligns it with Java compatibility.
*   **`examples/kmm-sample/build.gradle.kts`:** No changes needed.
*   **`examples/kmm-sample/compose-desktop/build.gradle.kts`:**
    *   **Action:** Migrate `tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = Versions.kotlinJvmTarget }` (line 21) to the new compiler options DSL.
    *   **Action:** Migrate the `application` plugin usage (line 6) to the new `kotlin { jvm { binaries.executable { mainClass.set("MainKt") } } }` DSL.
*   **`examples/kmm-sample/shared/build.gradle.kts`:** No specific changes needed based on the checklist. Ensure `jvmTarget` for JVM and Android targets is correctly configured (likely via defaults or future explicit settings if parent `allprojects` blocks are refactored).
*   **`examples/min-android-sample/app/build.gradle.kts`:**
    *   **Action (Recommendation):** Consider explicitly setting the Kotlin JVM target using the new DSL for clarity.
*   **`examples/min-android-sample/build.gradle.kts`:** No changes needed.
*   **`examples/min-android-sample/shared/build.gradle.kts`:**
    *   **Action (Minor):** `freeCompilerArgs` (line 30) already uses the new `compilerOptions` DSL. Consider explicitly setting `jvmTarget` using the new DSL if defaults are not desired for its JVM/Android targets.
*   **`examples/realm-java-compatibility/build.gradle.kts`:** No changes needed.

### 4. `integration-tests` Directory
*   The root build files for various Gradle version tests (`integration-tests/gradle/current/build.gradle.kts`, `integration-tests/gradle/gradle72-test/build.gradle.kts`, etc.) do not configure Kotlin compilation themselves but set up classpaths for their subprojects.
*   **Action:** Subprojects *within* these test configurations (e.g., `single-platform`, `multi-platform` modules, which were not explicitly listed to be read individually) will need to be checked for `kotlinOptions.jvmTarget` or other old DSL usages and migrated, similar to the `benchmarks` and `examples` modules. The provided list did not include these sub-modules.

### 5. `packages` Directory
*   **`packages/build.gradle.kts`:**
    *   The `allprojects { ... compilerOptions { jvmTarget.set(...) } }` block (line 22) is already using the new DSL for `jvmTarget`. This is good.
*   **`packages/cinterop/build.gradle.kts`:**
    *   **Action:** Multiple instances of `kotlinOptions.freeCompilerArgs += ...` for native targets need migration to `compilerOptions.freeCompilerArgs.add(...)` or `.addAll(...)`. Examples:
        *   Line 140 (iosX64)
        *   Line 153 (iosSimulatorArm64)
        *   Line 166 (iosArm64)
        *   Line 179 (macosX64)
        *   Line 191 (macosArm64)
    *   Other `freeCompilerArgs` usages (lines 259, 265) are already using the new `compilerOptions` DSL.
*   **`packages/gradle-plugin/build.gradle.kts`:** No changes needed. `jvmTarget` handled by parent `allprojects`.
*   **`packages/jni-swig-stub/build.gradle.kts`:** No changes needed (Java library).
*   **`packages/library-base/build.gradle.kts`:** No specific changes needed. Compiler options largely use new DSL; `jvmTarget` handled by parent `allprojects`.
*   **`packages/library-sync/build.gradle.kts`:** No specific changes needed. Compiler options largely use new DSL; `jvmTarget` handled by parent `allprojects`.
*   **`packages/plugin-compiler/build.gradle.kts`:** No specific changes needed. `freeCompilerArgs` uses new DSL; `jvmTarget` handled by parent `allprojects`. Explicit `kotlin-compiler-embeddable` dependency noted (intentional).
*   **`packages/plugin-compiler-shaded/build.gradle.kts`:** No changes needed (Java library for shading).
*   **`packages/test-base/build.gradle.kts`:** No specific changes needed. `freeCompilerArgs` uses new DSL; `jvmTarget` handled by parent `allprojects`. Explicit `kotlin-compiler-embeddable` dependency noted (intentional for test setup).
*   **`packages/test-sync/build.gradle.kts`:** No specific changes needed. `freeCompilerArgs` uses new DSL; `jvmTarget` handled by parent `allprojects`. Explicit `kotlin-compiler-embeddable` dependency noted (intentional for test setup).

This analysis provides a roadmap for updating the Gradle build scripts to be compatible with Kotlin 2.1.x DSL changes.
