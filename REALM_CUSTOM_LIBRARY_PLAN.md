# Plan Estratégico: Publicación de library-base Personalizada para Realm Kotlin

## 📋 Objetivo
Publicar una versión personalizada y mantenible de `library-base` de Realm Kotlin compilada con Kotlin 2.1.21+ para independizarse de las librerías oficiales y mantener compatibilidad con versiones modernas de Kotlin.

## 🎯 Estado Actual
- ✅ **Plugin personalizado**: `com.santipbarber.realm-kotlin` v2.1.0 publicado exitosamente
- ❌ **Library-base**: Falla por dependencias complejas con `cinterop` y Realm Core nativo
- 🔄 **Solución temporal**: Plugin personalizado + librerías oficiales v3.0.0

## 🔍 Análisis del Problema

### Dependencias Críticas Identificadas
```
library-base
├── cinterop (API dependency)
│   ├── Realm Core (C++ nativo)
│   ├── JNI bindings
│   └── Native compilation (iOS, Android, macOS)
├── jni-swig-stub
└── External dependencies (coroutines, serialization, etc.)
```

### Problemas Encontrados
1. **Compilación nativa compleja**: Realm Core requiere CMake, NDK, ccache
2. **Dependencias circulares**: `library-base` → `cinterop` → `jni-swig-stub`
3. **Referencias de proyecto incorrectas**: Paths `:cinterop` vs `:packages:cinterop`
4. **Configuración Android**: AndroidX, NDK, build tools
5. **Targets multiplataforma**: iOS, Android, macOS con diferentes toolchains

## 📈 Plan de Implementación

### Fase 1: Preparación del Entorno (1-2 días)

#### 1.1 Configuración de Dependencias del Sistema
```bash
# Instalar herramientas necesarias
brew install cmake ninja ccache
brew install --cask android-studio  # Para NDK actualizado

# Configurar variables de entorno
export ANDROID_HOME="/Users/$USER/Library/Android/sdk"
export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/26.1.10909125"  # Versión más reciente
export PATH="/opt/homebrew/bin:$PATH"  # Para ccache
```

#### 1.2 Verificación de Configuración Android
- ✅ Android SDK 34+
- ✅ NDK 26.1+ (compatible con AGP 8.6.1)
- ✅ CMake 3.22+
- ✅ Build Tools 34.0.0+

#### 1.3 Configuración del Proyecto Base
```bash
cd /path/to/clean/realm-kotlin-fork
git checkout main
git clean -fd
./gradlew clean
```

### Fase 2: Corrección de Configuración (2-3 días)

#### 2.1 Corrección de Referencias de Proyecto
**Archivo**: `packages/*/build.gradle.kts`
```kotlin
// Cambiar todas las referencias incorrectas:
// ❌ project("::cinterop") 
// ✅ project(":packages:cinterop")

// ❌ project("::library-base")
// ✅ project(":packages:library-base")
```

#### 2.2 Actualización de Configuración AtomicFU
**Archivos afectados**: `cinterop`, `library-sync`, `test-*`
```kotlin
// ❌ Sintaxis antigua
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${Versions.atomicfu}")
    }
}
apply(plugin = "kotlinx-atomicfu")

// ✅ Sintaxis nueva
plugins {
    id("org.jetbrains.kotlinx.atomicfu") version Versions.atomicfu
}
```

#### 2.3 Corrección de Compiler Options
**Archivo**: `packages/cinterop/build.gradle.kts`
```kotlin
// ❌ Sintaxis incorrecta en Kotlin 2.1.21
compilerOptions {
    freeCompilerArgs.addAll(...)
}

// ✅ Sintaxis correcta
kotlinOptions {
    freeCompilerArgs += ...
}
```

#### 2.4 Corrección de Dependency Substitution
**Archivos**: `test-base`, `test-sync`
```kotlin
// ✅ Configuración corregida
configurations.all {
    resolutionStrategy.dependencySubstitution {
        rootProject.allprojects
            .filter { it != project && it != rootProject && it.path.startsWith(":packages:") }
            .forEach { subproject: Project ->
                substitute(module("io.realm.kotlin:${subproject.name}:${Realm.version}")).using(
                    project(subproject.path)
                )
            }
    }
}
```

### Fase 3: Compilación Modular (3-4 días)

#### 3.1 Orden de Compilación Estratégico
```bash
# 1. Módulos base sin dependencias nativas
./gradlew :packages:jni-swig-stub:build

# 2. Cinterop (el más complejo)
./gradlew :packages:cinterop:build -x test

# 3. Library-base
./gradlew :packages:library-base:build -x test

# 4. Library-sync
./gradlew :packages:library-sync:build -x test
```

#### 3.2 Configuración de Targets Selectivos
**Archivo**: `packages/gradle.properties`
```properties
# Publicar solo targets necesarios inicialmente
realm.kotlin.targets=android,iosArm64,iosSimulatorArm64,metadata

# Desactivar targets problemáticos temporalmente
# realm.kotlin.targets=jvm,macosX64,macosArm64
```

#### 3.3 Manejo de Errores Comunes

##### Error: ccache not found
```bash
export PATH="/opt/homebrew/bin:$PATH"
# O instalar: brew install ccache
```

##### Error: NDK version mismatch
```bash
# Actualizar NDK en Android Studio
# Configurar ANDROID_NDK_HOME correctamente
```

##### Error: AndroidX dependencies
```properties
# En gradle.properties
android.useAndroidX=true
```

### Fase 4: Publicación Gradual (2-3 días)

#### 4.1 Publicación Individual por Módulo
```bash
# Exportar credenciales
export GITHUB_ACTOR=santipbarber
export GITHUB_TOKEN=your_github_token_here

# 1. Publicar jni-swig-stub primero
./gradlew :packages:jni-swig-stub:publishAllPublicationsToGitHubPackagesRepository

# 2. Publicar cinterop
./gradlew :packages:cinterop:publishAllPublicationsToGitHubPackagesRepository

# 3. Publicar library-base
./gradlew :packages:library-base:publishAllPublicationsToGitHubPackagesRepository

# 4. Publicar library-sync (opcional)
./gradlew :packages:library-sync:publishAllPublicationsToGitHubPackagesRepository
```

#### 4.2 Verificación de Publicación
```kotlin
// Crear proyecto de prueba con:
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

dependencies {
    implementation("com.santipbarber.realm-kotlin:library-base:2.1.0")
}
```

### Fase 5: Integración y Testing (1-2 días)

#### 5.1 Configuración Final para Proyectos Cliente
```toml
[versions]
realm-custom = "2.1.0"

[libraries]
realm-base = { group = "com.santipbarber.realm-kotlin", name = "library-base", version.ref = "realm-custom" }

[plugins]
realmKotlin = { id = "com.santipbarber.realm-kotlin", version.ref = "realm-custom" }
```

#### 5.2 Testing de Integración
- ✅ Crear proyecto KMP de prueba
- ✅ Verificar compilación iOS/Android
- ✅ Verificar funcionalidad básica de Realm
- ✅ Testing en CI/CD

## 🛠 Estrategias de Resolución de Problemas

### Problema 1: Compilación Nativa Falla
**Síntomas**: Errores de CMake, NDK, o ccache
**Solución**:
1. Verificar instalación de herramientas
2. Actualizar NDK a versión compatible
3. Limpiar cache nativo: `rm -rf .cxx build`
4. Compilar con verbose: `--info --stacktrace`

### Problema 2: Dependencias Circulares
**Síntomas**: "Project not found" o "Circular dependency"
**Solución**:
1. Revisar todas las referencias de proyecto
2. Verificar settings.gradle.kts includes
3. Compilar módulos en orden correcto

### Problema 3: Targets Multiplataforma
**Síntomas**: Errores específicos de iOS/macOS
**Solución**:
1. Compilar solo Android inicialmente
2. Agregar targets iOS gradualmente
3. Usar `realm.kotlin.targets` para controlar

### Problema 4: Versiones de Dependencias
**Síntomas**: Incompatibilidades de versiones
**Solución**:
1. Usar BOM de Kotlin consistente
2. Verificar versiones en buildSrc/Config.kt
3. Actualizar gradle wrapper si necesario

## 📅 Timeline Estimado

| Fase | Duración | Dependencias | Entregables |
|------|----------|--------------|-------------|
| **Fase 1: Preparación** | 1-2 días | Herramientas del sistema | Entorno configurado |
| **Fase 2: Corrección** | 2-3 días | Fase 1 | Build configuration limpia |
| **Fase 3: Compilación** | 3-4 días | Fase 2 | Módulos compilando |
| **Fase 4: Publicación** | 2-3 días | Fase 3 | Librerías publicadas |
| **Fase 5: Testing** | 1-2 días | Fase 4 | Integración verificada |
| **Total** | **9-14 días** | - | **Solución completa** |

## 🚨 Riesgos y Mitigaciones

### Riesgo Alto: Compilación Nativa Compleja
- **Mitigación**: Comenzar con targets simples (Android)
- **Plan B**: Usar librerías oficiales temporalmente

### Riesgo Medio: Incompatibilidades de Versiones
- **Mitigación**: Testing exhaustivo en múltiples proyectos
- **Plan B**: Rollback a configuración anterior

### Riesgo Bajo: Mantenimiento a Largo Plazo
- **Mitigación**: Documentación detallada del proceso
- **Plan B**: Automatización con scripts

## 📚 Recursos Adicionales

### Documentación Clave
- [Realm Kotlin Contributing Guide](../CONTRIBUTING.md)
- [Kotlin Multiplatform Guide](https://kotlinlang.org/docs/multiplatform.html)
- [Android NDK Guide](https://developer.android.com/ndk/guides)
- [GitHub Packages Maven](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)

### Scripts de Automatización
```bash
# Script de compilación completa
#!/bin/bash
set -e

echo "🚀 Iniciando compilación de Realm Kotlin personalizado..."

# Verificar entorno
./scripts/verify-environment.sh

# Compilar módulos en orden
./gradlew :packages:jni-swig-stub:build
./gradlew :packages:cinterop:build -x test
./gradlew :packages:library-base:build -x test

# Publicar
./gradlew :packages:library-base:publishAllPublicationsToGitHubPackagesRepository

echo "✅ Compilación completada exitosamente!"
```

## 🎯 Objetivos de Éxito

### Corto Plazo (2 semanas)
- ✅ `library-base` compilando sin errores
- ✅ Publicación exitosa en GitHub Packages
- ✅ Proyecto de prueba funcionando

### Mediano Plazo (1-2 meses)
- ✅ Integración en proyectos de producción
- ✅ CI/CD automatizado para releases
- ✅ Documentación completa del proceso

### Largo Plazo (3-6 meses)
- ✅ Mantenimiento independiente
- ✅ Updates automáticos de Kotlin
- ✅ Alternativa robusta a librerías oficiales

---

## 📞 Próximos Pasos

1. **Revisar este plan** y ajustar según recursos disponibles
2. **Programar sesión dedicada** de 1-2 días para implementación
3. **Preparar entorno** según Fase 1
4. **Ejecutar plan** fase por fase con validación en cada paso

**¡Con este plan estructurado podremos conseguir la independencia completa de las librerías oficiales de Realm!** 🚀