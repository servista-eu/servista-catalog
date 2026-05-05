# servista-catalog

Shared Gradle version catalog providing centralized dependency version alignment for all servista
Kotlin projects. Published as `eu.servista:servista-catalog` to the Forgejo Maven registry.

**Version:** 0.1.0

## Usage

Consume the catalog in your `settings.gradle.kts`:

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
        create("libs") { from("eu.servista:servista-catalog:0.1.0") }
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
    implementation(libs.servista.service.runtime.events)
    implementation(libs.ktor.server.core)
    implementation(libs.koin.core)
}
```

## What the catalog pins

### Servista internal libraries

| Library | Version |
|---------|---------|
| `servista-kotlin-commons` (all modules) | 0.1.0 |
| `servista-service-runtime` | 0.1.0 |
| `servista-service-runtime-events` | 0.1.0 |

### Third-party dependencies

| Category | Key libraries |
|----------|--------------|
| Kotlin | Kotlin 2.3.10, kotlinx-serialization 1.10.0, kotlinx-coroutines 1.10.2 |
| HTTP | Ktor 3.4.3 |
| DI | Koin 4.2.0 |
| Database | jOOQ 3.20.11, Flyway 12.0.3, HikariCP 7.0.2, PostgreSQL JDBC 42.7.11 |
| Messaging | Kafka 4.2.0, Avro 1.12.1, Apicurio Serdes 3.0.0.M4 |
| Cache | Lettuce 7.5.0.RELEASE |
| Observability | OpenTelemetry 1.54.0, Micrometer 1.16.3 |
| Security | Nimbus JOSE+JWT 10.8, Tink 1.13.0, Vault Java Driver 6.2.1 |
| Authorization | OpenFGA SDK 0.9.7 |
| Cloud | AWS SDK 2.44.1 |
| Logging | kotlin-logging 7.0.3, Logback 1.5.32, Logstash Encoder 8.1 |
| Testing | JUnit 5.14.2, Kotest 6.1.4, Testcontainers 2.0.3, MockK 1.14.7 |
| Analysis | Detekt 2.0.0-alpha.2 |

See [`catalog/libs.versions.toml`](catalog/libs.versions.toml) for the complete list.

## Publishing

```bash
FORGEJO_USER=sven FORGEJO_TOKEN=<token> ./gradlew publish
```

Credentials come from `forgejoUser`/`forgejoToken` Gradle properties or `FORGEJO_USER`/
`FORGEJO_TOKEN` environment variables.

## Development

This project has no source code — it is a pure version catalog publisher. The entire deliverable
is `catalog/libs.versions.toml` packaged as a Maven artifact.

```
servista-catalog/
  catalog/libs.versions.toml  -- THE published artifact
  build.gradle.kts            -- version-catalog + maven-publish config
  settings.gradle.kts         -- rootProject.name
  gradle.properties           -- group + version
```

## License

Proprietary. Internal use only.
