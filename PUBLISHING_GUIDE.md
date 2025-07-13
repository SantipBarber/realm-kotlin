# 📦 Guía de Publicación - Modern Interop Layer

Esta guía explica cómo publicar la librería y plugin con soporte para Kotlin 2.2.0.

## 🎯 Estrategias de Publicación

### 1. GitHub Packages (Recomendado - Ya configurado)

**✅ Ventajas:**
- Integración perfecta con GitHub
- Control total de acceso
- Versionado automático
- Ya configurado en el proyecto

**📦 Información de publicación:**
- **Repository**: `https://maven.pkg.github.com/santipbarber/realm-kotlin`
- **Group ID**: `com.santipbarber.realm-kotlin`
- **Version**: `3.1.0-kotlin-2.2.0`

### 2. Maven Central (Opcional - Para distribución pública)

**✅ Ventajas:**
- Alcance global máximo
- No requiere credenciales especiales para usuarios
- Integración con todos los IDE

## 🚀 Publicación en GitHub Packages

### Paso 1: Configurar credenciales

Crea un Personal Access Token en GitHub con permisos `write:packages`:

1. Ve a GitHub → Settings → Developer settings → Personal access tokens
2. Genera un nuevo token con scopes: `write:packages`, `read:packages`
3. Configura las variables de entorno:

```bash
export GITHUB_ACTOR=santipbarber
export GITHUB_TOKEN=tu_github_token_aqui
```

O crea un `gradle.properties` local (NO subir al repo):
```properties
GITHUB_ACTOR=santipbarber
GITHUB_TOKEN=tu_github_token_aqui
```

### Paso 2: Publicar librería y plugin

```bash
# Publicar solo la librería moderna
./gradlew :packages:library-modern:publish

# Publicar solo el plugin Gradle
./gradlew :packages:gradle-plugin:publish

# Publicar todo lo esencial para un proyecto real
./gradlew :packages:library-modern:publish :packages:gradle-plugin:publish

# Verificar publicación
./gradlew :packages:library-modern:publishToMavenLocal
./gradlew :packages:gradle-plugin:publishToMavenLocal
```

### Paso 3: Verificar publicación

Los artefactos publicados serán:
- `com.santipbarber.realm-kotlin:library-modern:3.1.0-kotlin-2.2.0`
- `com.santipbarber.realm-kotlin:gradle-plugin:3.1.0-kotlin-2.2.0`

## 📱 Usar en Proyecto Real

### 1. Configuración del proyecto cliente

#### `settings.gradle.kts`
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/santipbarber/realm-kotlin")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: findProperty("gpr.token") as String?
            }
        }
    }
}
```

#### `build.gradle.kts` (proyecto cliente)
```kotlin
plugins {
    kotlin("multiplatform") version "2.2.0"
    id("com.android.library") // o application
    id("com.santipbarber.realm-kotlin") version "3.1.0-kotlin-2.2.0"
}

kotlin {
    androidTarget {
        compilations.all {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.santipbarber.realm-kotlin:library-modern:3.1.0-kotlin-2.2.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
    }
}

// Configuración Realm Modern Interop Layer
realm {
    schema {
        enableModernInteropLayer = true
        addSchemaClass("com.tuapp.model.Task")
        addSchemaClass("com.tuapp.model.User")
    }
}
```

### 2. Credenciales en proyecto cliente

El usuario final necesita configurar:

#### `gradle.properties` (local - no subir)
```properties
gpr.user=su_usuario_github
gpr.token=su_github_token_con_permisos_read_packages
```

O variables de entorno:
```bash
export GITHUB_ACTOR=su_usuario_github  
export GITHUB_TOKEN=su_github_token_con_permisos_read_packages
```

## 🔧 Comandos de Publicación

### Para desarrollo y testing
```bash
# Compilar y verificar que todo funciona
./gradlew compileKotlin

# Publicar a repositorio local para pruebas
./gradlew publishToMavenLocal

# Verificar artefactos locales
ls ~/.m2/repository/com/santipbarber/realm-kotlin/
```

### Para producción
```bash
# Limpiar antes de publicar
./gradlew clean

# Compilar y publicar librería moderna
./gradlew :packages:library-modern:publish

# Compilar y publicar plugin Gradle
./gradlew :packages:gradle-plugin:publish

# Verificar en GitHub que los paquetes aparecen
# https://github.com/santipbarber/realm-kotlin/packages
```

## 📋 Checklist antes de publicar

- [ ] ✅ Versión actualizada en `Config.kt`
- [ ] ✅ Documentación actualizada (README.md, MIGRATION_GUIDE.md)
- [ ] ✅ Compilación exitosa en Android e iOS
- [ ] ✅ Tests de integración pasan
- [ ] ✅ Credenciales GitHub configuradas
- [ ] ✅ Cambios commiteados en Git
- [ ] ✅ Tag de versión creado

## 🏗️ Pipeline de Publicación Automatizada

Para automatizar las publicaciones, podrías crear un GitHub Action:

#### `.github/workflows/publish.yml`
```yaml
name: Publish Modern Interop Layer

on:
  push:
    tags:
      - 'v*'
  workflow_dispatch:

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 11
      uses: actions/setup-java@v4
      with:
        java-version: '11'
        distribution: 'adopt'
    
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    
    - name: Build and Publish
      env:
        GITHUB_ACTOR: ${{ github.actor }}
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      run: |
        ./gradlew :packages:library-modern:publish
        ./gradlew :packages:gradle-plugin:publish
```

## 🔍 Verificación post-publicación

### 1. Verificar en GitHub Packages
- Ve a: https://github.com/santipbarber/realm-kotlin/packages
- Confirma que aparecen los paquetes publicados
- Verifica la versión `3.1.0-kotlin-2.2.0`

### 2. Probar en proyecto nuevo
```bash
# Crear proyecto KMP de prueba
mkdir test-realm-modern
cd test-realm-modern

# Configurar con la librería publicada
# (usar ejemplo de configuración de arriba)

# Verificar que compila
./gradlew compileKotlin
```

### 3. Validar que funciona en Android e iOS
```bash
# Android
./gradlew :shared:compileDebugKotlinAndroid

# iOS  
./gradlew :shared:compileKotlinIosArm64
```

## 📊 Métricas de adopción

Para rastrear el uso de tu librería:

1. **GitHub Package downloads**: Ve a GitHub → Insights → Traffic
2. **Stars y forks**: Indicadores de interés
3. **Issues y PRs**: Feedback de la comunidad

## 💡 Mejores prácticas

### Versionado semántico
- `3.1.0-kotlin-2.2.0` - Versión mayor con soporte Kotlin específico
- `3.1.1-kotlin-2.2.0` - Patch/bugfix
- `3.2.0-kotlin-2.2.0` - Minor con nuevas características

### Release notes
Mantén un `CHANGELOG.md` con:
- Nuevas características
- Cambios breaking
- Mejoras de rendimiento  
- Bugs corregidos

### Documentación
- README actualizado
- Migration guide mantenido
- Ejemplos funcionales
- API reference

---

## 📞 Siguiente paso recomendado

**Para usar inmediatamente en un proyecto real:**

1. **Ejecutar publicación**:
   ```bash
   ./gradlew :packages:library-modern:publish :packages:gradle-plugin:publish
   ```

2. **Crear proyecto KMP nuevo** usando las instrucciones del README

3. **Verificar que funciona** en Android e iOS

4. **Iterar y mejorar** basado en feedback real

**⚡ Ready to publish with Kotlin 2.2.0 support!**