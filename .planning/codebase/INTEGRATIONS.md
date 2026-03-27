# External Integrations

**Analysis Date:** 2026-03-27

## Overview

This project is a set of Gradle convention plugins. It does not connect to external services at runtime. Instead, it configures downstream consumer projects to use specific libraries, tools, and services. The "integrations" here are the external ecosystems these plugins wire up for consumers.

## APIs & External Services (Configured for Consumers)

**Kafka (Apache Kafka 4.1.1):**
- Configured by: `qabatz.kafka-consumer`, `qabatz.kafka-producer`, `qabatz.pipeline-service`
- Consumer dependencies: `kafka-clients` (consumer/producer), `kafka-streams` (pipeline)
- Version catalog also provides: `kafka-streams-test`, Avro 1.12.1, Apicurio Registry serdes 3.0.0.M4
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/KafkaConsumerPlugin.kt`, `src/main/kotlin/eu/qabatz/gradle/plugins/KafkaProducerPlugin.kt`, `src/main/kotlin/eu/qabatz/gradle/plugins/PipelineServicePlugin.kt`

**PostgreSQL (via jOOQ + Flyway + HikariCP):**
- Configured by: `qabatz.jooq`
- Dependencies added: jOOQ 3.20.11 (core, kotlin, kotlin-coroutines), HikariCP 7.0.2, Flyway 12.0.3 (core + postgresql), PostgreSQL JDBC 42.7.10
- DDL-based code generation: parses Flyway SQL migrations without a live database
- Extension: `jooq {}` block with `packageName`, `migrationDir`, `excludes` properties
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt`, `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt`

**Secret Stores (OpenBao/Vault):**
- Configured by: `qabatz.secrets`
- Dependencies added: `qabatz-kotlin-commons-secrets` 0.2.0 (implementation + test fixtures)
- File: `src/main/kotlin/eu/qabatz/gradle/plugins/SecretsPlugin.kt`

**Observability Stack:**
- Configured by: `qabatz.observability`
- Dependencies added: kotlin-logging 7.0.3, Logback 1.5.32, Micrometer Prometheus 1.16.3
- File: `src/main/kotlin/eu/qabatz/gradle/plugins/ObservabilityPlugin.kt`

## Version Catalog - Full Ecosystem Map

The published version catalog (`catalog/libs.versions.toml`) defines the complete Qabatz platform dependency surface. Consumer projects import it via:

```kotlin
from("eu.qabatz:qabatz-catalog:<version>")
```

### Web Framework - Ktor 3.4.0

**Server modules:**
- `ktor-server-core`, `ktor-server-netty` - HTTP server
- `ktor-server-content-negotiation`, `ktor-serialization-kotlinx-json` - JSON serialization
- `ktor-server-status-pages` - Error handling
- `ktor-server-auth`, `ktor-server-auth-jwt` - Authentication
- `ktor-server-openapi`, `ktor-server-swagger` - API documentation
- `ktor-server-call-logging` - Request logging
- `ktor-server-metrics-micrometer` - Metrics integration
- `ktor-server-test-host` - Testing

**Client modules:**
- `ktor-client-core`, `ktor-client-cio`, `ktor-client-content-negotiation`

### Dependency Injection - Koin 4.1.1

- `koin-ktor` - Ktor integration
- `koin-test` - Testing support

### Database - jOOQ 3.20.11 + PostgreSQL

- `jooq-core`, `jooq-kotlin`, `jooq-kotlin-coroutines` - Query building
- `jooq-postgres-extensions` - PostgreSQL-specific features
- `jooq-codegen`, `jooq-meta-extensions` - Code generation tools
- `flyway-core`, `flyway-postgresql` (12.0.3) - Schema migration
- `hikari` (7.0.2) - Connection pooling
- `postgresql-jdbc` (42.7.10) - JDBC driver

### Messaging - Kafka 4.1.1

- `kafka-clients`, `kafka-streams`, `kafka-streams-test`
- `avro` (1.12.1) - Schema serialization
- `apicurio-serdes-avro` (3.0.0.M4) - Apicurio Schema Registry integration

### Infrastructure Clients

- `openfga-sdk` (0.9.6) - OpenFGA authorization
- `immudb4j` (1.0.1) - immudb immutable database client
- `jedis` (5.2.0) - Redis/Valkey client (sync)
- `lettuce` (6.5.3.RELEASE) - Redis/Valkey client (async/reactive)
- `nimbus-jose-jwt` (10.8) - JWT processing
- `konform-jvm` (0.11.0) - Validation library
- `tink` (1.13.0) - Google Tink cryptography

### Observability

- `otel-bom`, `otel-api`, `otel-sdk`, `otel-exporter-otlp`, `otel-sdk-autoconfigure`, `otel-extension-kotlin` (1.47.0) - OpenTelemetry
- `otel-agent` (2.20.0) - OpenTelemetry Java agent
- `otel-sdk-testing` (1.47.0) - OTel test utilities
- `micrometer-core`, `micrometer-prometheus` (1.16.3) - Metrics
- `kotlin-logging` (7.0.3), `logback` (1.5.32), `logstash-logback-encoder` (8.1), `janino` (3.1.12), `slf4j-api` (2.0.17) - Logging

### Kotlin Extensions

- `kotlinx-serialization-json` (1.10.0)
- `kotlinx-coroutines-core`, `kotlinx-coroutines-slf4j`, `kotlinx-coroutines-test` (1.10.2)
- `kotlinx-datetime` (0.7.1)

### Testing

- `junit5` (5.14.2)
- `mockk` (1.14.7)
- `testcontainers-core`, `testcontainers-postgresql`, `testcontainers-kafka` (2.0.3)
- `kotest-assertions` (6.1.4)

### Qabatz Internal Libraries

- `qabatz-kotlin-commons` (0.2.0) - Platform commons library with adapters:
  - `qabatz-commons-core` - Core abstractions
  - `qabatz-commons-adapter-kafka` - Kafka adapter
  - `qabatz-commons-adapter-valkey` - Valkey/Redis adapter
  - `qabatz-commons-adapter-jooq` - jOOQ adapter
  - `qabatz-commons-adapter-otel` - OpenTelemetry adapter
  - `qabatz-commons-adapter-micrometer` - Micrometer adapter
  - `qabatz-commons-adapter-konform` - Validation adapter
  - `qabatz-commons-kafka-avro` - Kafka Avro support
  - `qabatz-commons-secrets` - Secret store abstraction
- `qabatz-kotlin-ktor` (0.1.1) - Ktor integration library

### Catalog Plugins

- `kotlin-jvm` (2.3.10) - Kotlin JVM compiler
- `kotlin-serialization` (2.3.10) - Serialization compiler plugin
- `ktor` (3.4.0) - Ktor Gradle plugin
- `jooq-codegen` (3.20.11) - jOOQ code generation
- `flyway` (12.0.3) - Flyway migration
- `detekt` (2.0.0-alpha.2) - Static analysis

## Code Quality Tools (Applied to Consumers)

**Detekt 2.0.0-alpha.2:**
- Applied by: `qabatz.library` (base plugin)
- Shared config bundled as resource: `src/main/resources/detekt/detekt.yml`
- Key settings: max complexity 15, max method length 60 lines, max class 600 lines, max line length 120
- Config loaded from classpath at runtime so all consumer projects share identical rules

**ktfmt 0.25.0:**
- Applied by: `qabatz.library` (base plugin)
- Style: `kotlinLangStyle()` (official Kotlin coding conventions)
- Plugin: `com.ncorti.ktfmt.gradle`

## Data Storage (Configured for Consumers)

**Databases:**
- PostgreSQL (via jOOQ + Flyway + HikariCP)
- immudb (via immudb4j client in catalog)

**Caching/Key-Value:**
- Redis/Valkey (via Jedis and Lettuce clients in catalog)

**Schema Registry:**
- Apicurio Registry (via apicurio-serdes-avro in catalog)

## Authentication & Identity (Configured for Consumers)

**Auth Provider:**
- OpenFGA (via openfga-sdk in catalog) - Fine-grained authorization
- JWT processing (via nimbus-jose-jwt in catalog, Ktor auth-jwt server module)

## Publishing & Distribution

**Artifact Registry:**
- Forgejo Maven registry at `https://git.hestia-ng.eu/api/packages/qabatz/maven`
- Publishes both plugin artifacts and version catalog artifact
- Auth: `FORGEJO_USER`/`FORGEJO_TOKEN` env vars or Gradle properties

**Published Artifacts:**
- `eu.qabatz:qabatz-catalog:0.1.0` - Version catalog

**Consumer Usage:**
```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        maven("https://git.hestia-ng.eu/api/packages/qabatz/maven")
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from("eu.qabatz:qabatz-catalog:0.1.0")
        }
    }
}

// build.gradle.kts
plugins {
    id("qabatz.library")
    id("qabatz.testing")
    id("qabatz.jooq")
    // etc.
}
```

## CI/CD & Deployment

**Hosting:**
- Forgejo instance at `git.hestia-ng.eu`

**CI Pipeline:**
- Not detected in this repository (no `.forgejo/`, `.github/`, or `Jenkinsfile`)

## Environment Configuration

**Required for publishing:**
- `FORGEJO_USER` or `forgejoUser` Gradle property
- `FORGEJO_TOKEN` or `forgejoToken` / `publishToken` Gradle property

**Required for SecretsPlugin consumers:**
- `FORGEJO_USER` and `FORGEJO_TOKEN` - Needed at configuration time to resolve `qabatz-kotlin-commons-secrets` from the private Forgejo registry

**Secrets location:**
- Environment variables or `~/.gradle/gradle.properties`
- `.env` files: Not detected in this project

## Webhooks & Callbacks

**Incoming:**
- None (this is a build-time tool, not a runtime service)

**Outgoing:**
- None

---

*Integration audit: 2026-03-27*
