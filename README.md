# servista-catalog

Shared Gradle version catalog providing centralized dependency version alignment for all servista
Kotlin projects. Published as `eu.servista:servista-catalog` to the Forgejo Maven registry.

## Usage

Import the catalog in your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://git.hestia-ng.eu/api/packages/servista/maven")
            credentials {
                username = System.getenv("FORGEJO_USER") ?: "token"
                password = System.getenv("FORGEJO_TOKEN") ?: ""
            }
        }
        mavenCentral()
    }
    versionCatalogs {
        create("libs") { from("eu.servista:servista-catalog:0.2.0") }
    }
}
```

Then reference libraries and plugins in `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.servista.commons.core)
    implementation(libs.servista.service.runtime)
    implementation(libs.ktor.server.core)
    implementation(libs.koin.core)
}
```

## What the catalog pins

### Servista internal libraries

| Version key | Modules | Version |
|-------------|---------|---------|
| `servista-kotlin-commons` | All `commons-*` and `commons-wiring-*` modules | 0.1.0 |
| `servista-service-runtime` | `servista-service-runtime`, `servista-service-runtime-events` | 0.2.0 |
| `servista-avro-schemas` | `servista-avro-schemas` | 0.1.0 |

### Third-party dependencies

| Category | Key libraries |
|----------|--------------|
| Kotlin | Kotlin 2.3.10, kotlinx-serialization 1.10.0, kotlinx-coroutines 1.10.2, kotlinx-datetime 0.7.1 |
| HTTP | Ktor 3.4.3 (server + client), Netty 4.2.5.Final |
| DI | Koin 4.2.0 |
| Database | jOOQ 3.20.11, Flyway 12.0.3, HikariCP 7.0.2, PostgreSQL JDBC 42.7.11, Oracle JDBC 23.7.0.25.01 |
| Messaging | Kafka 4.2.0, Avro 1.12.1, Apicurio Serdes 3.0.0.M4 |
| Cache | Lettuce 7.5.0.RELEASE |
| Observability | OpenTelemetry 1.54.0, OTel Java Agent 2.20.0, Micrometer 1.16.3, Pyroscope 1.0.4 |
| Logging | kotlin-logging 7.0.3, Logback 1.5.32, Logstash Encoder 8.1, SLF4J 2.0.17, Janino 3.1.12 |
| Security | Nimbus JOSE+JWT 10.8, Tink 1.13.0, BouncyCastle 1.80, Vault Java Driver 6.2.1 |
| Authorization | OpenFGA SDK 0.9.7 |
| Cloud | AWS SDK 2.44.1 |
| Validation | Konform 0.11.0 |
| Testing | JUnit 5.14.2, Kotest 6.1.4, Testcontainers 2.0.3, MockK 1.14.7 |
| Analysis | Detekt 2.0.0-alpha.2, Dokka 2.2.0, ktfmt 0.25.0, BCV 0.18.1 |
| Build | Shadow 9.0.0-beta12 |

### Plugins

| Alias | Plugin ID |
|-------|-----------|
| `kotlin-jvm` | `org.jetbrains.kotlin.jvm` |
| `kotlin-serialization` | `org.jetbrains.kotlin.plugin.serialization` |
| `ktor` | `io.ktor.plugin` |
| `jooq-codegen` | `org.jooq.jooq-codegen-gradle` |
| `flyway` | `org.flywaydb.flyway` |
| `detekt` | `dev.detekt` |
| `dokka` | `org.jetbrains.dokka` |
| `ktfmt` | `com.ncorti.ktfmt.gradle` |
| `bcv` | `org.jetbrains.kotlinx.binary-compatibility-validator` |
| `shadow` | `com.gradleup.shadow` |

The `[libraries]` section also includes `gradle-*` prefixed entries (e.g. `gradle-kotlin`, `gradle-detekt`) exposing plugin artifacts for use in `build-logic` convention plugins via `implementation(libs.gradle.kotlin)`.

See [`catalog/libs.versions.toml`](catalog/libs.versions.toml) for the complete list.

## Publishing

```bash
FORGEJO_USER=<user> FORGEJO_TOKEN=<token> ./gradlew publish
```

Forgejo does not support overwriting published Maven artifacts. Bump the version in `gradle.properties` before each publish.

### Credential resolution

| Parameter | Resolution order |
|-----------|-----------------|
| Username | `forgejoUser` Gradle property > `FORGEJO_USER` env var > `"token"` |
| Password | `publishToken` Gradle property > `forgejoToken` Gradle property > `FORGEJO_TOKEN` env var |
| Registry URL | `publishUrl` Gradle property > `https://git.hestia-ng.eu/api/packages/servista/maven` |

## Project structure

```
servista-catalog/
  catalog/libs.versions.toml   -- the published artifact (consumed by downstream projects)
  gradle/libs.versions.toml    -- project-internal build catalog (empty)
  build.gradle.kts             -- version-catalog + maven-publish plugins
  settings.gradle.kts          -- rootProject.name = "servista-catalog"
  gradle.properties            -- group (eu.servista) + version
```

This project has no source code. The `test` and `detekt` tasks are registered as no-ops for CI pipeline compatibility.

## License

Proprietary. Internal use only.
