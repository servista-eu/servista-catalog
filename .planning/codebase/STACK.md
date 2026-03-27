# Technology Stack

**Analysis Date:** 2026-03-27

## Languages

**Primary:**
- Kotlin 2.3.10 - All plugin source code and build scripts (Kotlin DSL)

**Secondary:**
- XML - jOOQ codegen configuration template (generated at build time in `JooqPlugin.kt`)
- TOML - Version catalog definitions (`catalog/libs.versions.toml`, `gradle/libs.versions.toml`)
- YAML - Detekt configuration (`src/main/resources/detekt/detekt.yml`)

## Runtime

**Environment:**
- JVM 21 (enforced by `LibraryPlugin` via `jvmToolchain(21)`)
- Gradle 9.3.1 (via wrapper: `gradle/wrapper/gradle-wrapper.properties`)

**Package Manager:**
- Gradle with Kotlin DSL
- Lockfile: Not present (no dependency locking configured)

## Build System

**Gradle Plugins Applied (to this project itself):**
- `kotlin-dsl` - Enables Kotlin DSL for Gradle plugin development
- `java-gradle-plugin` - Infrastructure for building Gradle plugins
- `maven-publish` - Publishing to Maven repositories
- `version-catalog` - Publishing a version catalog artifact
- `dev.detekt` 2.0.0-alpha.2 - Static analysis for the plugin project itself

**Build Configuration Files:**
- `build.gradle.kts` - Main build script
- `settings.gradle.kts` - Single-line root project name declaration
- `gradle.properties` - Group (`eu.qabatz`) and version (`0.2.0`)
- `gradle/libs.versions.toml` - Internal version catalog for building the plugins project
- `catalog/libs.versions.toml` - Published version catalog for consumer projects

## Convention Plugins (8 total)

These are the artifacts this project produces. Each is a Gradle convention plugin.

| Plugin ID | Implementation Class | Purpose |
|---|---|---|
| `qabatz.library` | `eu.qabatz.gradle.plugins.LibraryPlugin` | Base plugin: Kotlin/JVM 21, detekt, ktfmt, directory validation, coroutines, datetime |
| `qabatz.testing` | `eu.qabatz.gradle.plugins.TestingPlugin` | Test dependencies: JUnit 5, MockK, Kotest, Testcontainers, Ktor test host, Koin test |
| `qabatz.observability` | `eu.qabatz.gradle.plugins.ObservabilityPlugin` | Logging and metrics: kotlin-logging, Logback, Micrometer Prometheus |
| `qabatz.kafka-consumer` | `eu.qabatz.gradle.plugins.KafkaConsumerPlugin` | Kafka client dependency for consumers |
| `qabatz.kafka-producer` | `eu.qabatz.gradle.plugins.KafkaProducerPlugin` | Kafka client dependency for producers |
| `qabatz.pipeline-service` | `eu.qabatz.gradle.plugins.PipelineServicePlugin` | Kafka Streams dependency for stream processing |
| `qabatz.jooq` | `eu.qabatz.gradle.plugins.JooqPlugin` | Database: jOOQ + HikariCP + Flyway + PostgreSQL + DDL-based code generation |
| `qabatz.secrets` | `eu.qabatz.gradle.plugins.SecretsPlugin` | Secret store access via qabatz-kotlin-commons-secrets |

## Plugin Hierarchy

All plugins apply `LibraryPlugin` as their base. The dependency chain is:

```
qabatz.library (base for all)
├── qabatz.testing (extends library, adds test deps)
├── qabatz.observability (extends library, adds logging + metrics)
├── qabatz.kafka-consumer (extends library, adds kafka-clients)
├── qabatz.kafka-producer (extends library, adds kafka-clients)
├── qabatz.pipeline-service (extends library, adds kafka-streams)
├── qabatz.jooq (extends library, adds jOOQ + Flyway + PostgreSQL)
└── qabatz.secrets (extends library, adds commons-secrets)
```

## Key Dependencies

**Build-time dependencies (for building this project):**
- `org.jetbrains.kotlin:kotlin-gradle-plugin` 2.3.10 - Kotlin compiler plugin on classpath
- `org.jetbrains.kotlin:kotlin-serialization` 2.3.10 - Serialization compiler plugin
- `dev.detekt:detekt-gradle-plugin` 2.0.0-alpha.2 - Detekt plugin on classpath
- `com.ncorti.ktfmt.gradle:plugin` 0.25.0 - ktfmt formatter plugin on classpath
- `org.jetbrains.kotlinx:kotlinx-serialization-json` 1.10.0 - Serialization runtime

**Test dependencies (for testing this project):**
- `gradleTestKit()` - Gradle TestKit for functional testing
- `org.junit.jupiter:junit-jupiter` 5.14.2 - JUnit 5
- `io.kotest:kotest-assertions-core` 6.1.4 - Kotest assertion matchers

**Critical runtime constants (injected into consumer projects):**
- Defined in `src/main/kotlin/eu/qabatz/gradle/Versions.kt`
- Must stay in sync with `catalog/libs.versions.toml`

## Version Management

**Two version sources exist and MUST be kept in sync:**

1. `src/main/kotlin/eu/qabatz/gradle/Versions.kt` - Compile-time constants used by plugin code to add dependencies with explicit version strings
2. `catalog/libs.versions.toml` - Published version catalog that consumers can import

The `Versions.kt` object contains only the subset of versions that plugins reference directly. The catalog contains the full set of platform dependency versions.

**Internal build catalog:**
- `gradle/libs.versions.toml` - Used only for building the plugins project itself (referenced as `libs.*` in `build.gradle.kts`)

## Configuration

**Environment Variables (for publishing):**
- `FORGEJO_USER` - Username for Forgejo Maven registry (default: "token")
- `FORGEJO_TOKEN` - Token for Forgejo Maven registry

**Gradle Properties (for publishing):**
- `publishUrl` - Override Maven publish URL (default: `https://git.hestia-ng.eu/api/packages/qabatz/maven`)
- `forgejoUser` / `forgejoToken` - Alternative to environment variables
- `publishToken` - Alternative token property

**Build Output:**
- Catalog artifact: `eu.qabatz:qabatz-catalog:0.1.0`

## Platform Requirements

**Development:**
- JDK 21+
- Gradle 9.3.1 (use wrapper: `./gradlew`)

**Production:**
- Consumed as Gradle plugins and version catalog by downstream Kotlin/JVM projects targeting JVM 21

---

*Stack analysis: 2026-03-27*
