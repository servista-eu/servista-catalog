# servista-catalog

Shared Gradle version catalog providing centralized dependency version alignment for all servista
Kotlin projects. Published as `eu.servista:servista-catalog` to the Forgejo Maven registry.

**Current version:** 0.4.0

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
        create("libs") { from("eu.servista:servista-catalog:0.4.0") }
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
    // servista commons (all referenced as libs.servista.commons.*)
    implementation(libs.servista.commons.core)
    implementation(libs.servista.commons.db.postgres)
    implementation(libs.servista.commons.observability.otel)

    // servista service runtime (bootstrap + auth + observability + health)
    implementation(libs.servista.service.runtime)

    // Third-party libs pinned by the catalog
    implementation(libs.ktor.server.core)
    implementation(libs.koin.core)
    // ... etc.
}
```

## What the catalog pins

- **servista internal:** commons (0.3.1), service-runtime (0.4.0)
- **Core Kotlin:** Kotlin 2.3.10, Ktor 3.4.0, Koin 4.2.0
- **Database:** jOOQ, Flyway, HikariCP, PostgreSQL + MySQL drivers
- **Messaging:** Kafka, Avro, Apicurio Serdes
- **Cache / ID:** Lettuce 7.5, Memcached
- **Observability:** OpenTelemetry 1.47, Micrometer, logstash-logback-encoder
- **Cloud SDKs:** AWS SDK v2, Azure (Blob, Key Vault, Application Gateway)
- **Security:** Nimbus JOSE+JWT, Vault Java Driver, Google Tink
- **Testing:** JUnit 5, Kotest, Testcontainers, MockK, Mockito

See `catalog/libs.versions.toml` for the full list of versions, libraries, and plugins.

## Publishing

```bash
./gradlew publish
```

Credentials come from `forgejoUser`/`forgejoToken` Gradle properties or `FORGEJO_USER`/
`FORGEJO_TOKEN` environment variables.
