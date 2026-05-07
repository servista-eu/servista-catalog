## Project

**servista-catalog**

A pure Gradle version catalog providing centralized dependency version alignment for all servista Kotlin projects. Published as `eu.servista:servista-catalog:0.1.0` to the Forgejo Maven registry, consumed by servista-kotlin-commons (root + build-logic), servista-service-runtime, and all servista services.

**Core Value:** Single source of truth for dependency versions across the entire servista ecosystem — preventing version drift and dependency conflicts between projects.

### Constraints

- **Forgejo credentials**: Registry requires `FORGEJO_USER`/`FORGEJO_TOKEN` for publish and resolve
- **No source code**: Pure catalog — no Kotlin, no convention plugins, no compilation
- **Build-logic sharing**: The catalog is imported by both main builds and their `build-logic` included builds via `versionCatalogs { create("libs") { from(...) } }` in settings

## Technology Stack

## Languages
- TOML - Version catalog definition (`catalog/libs.versions.toml`)

## Runtime
- Gradle 9.3.1 (via wrapper)

## Build System
- `maven-publish` + `version-catalog` plugins
- `catalog/libs.versions.toml` — **The published artifact** consumed by all downstream projects
- `gradle.properties` — Group (`eu.servista`) and version (`0.1.0`)

## Configuration
- `FORGEJO_USER` / `FORGEJO_TOKEN` — Maven registry authentication
- `publishUrl` — Override Maven publish URL (default: `https://git.hestia-ng.eu/api/packages/servista/maven`)
- `forgejoUser` / `forgejoToken` / `publishToken` — Alternative Gradle properties

## Conventions

## Version Catalog Structure
- `[versions]` section: version strings keyed by dependency short-name (e.g., `ktor = "3.4.3"`)
- `[libraries]` section: module coordinates referencing version keys via `version.ref`
- `[plugins]` section: plugin IDs referencing version keys
- Naming: lowercase kebab-case for version keys (e.g., `postgresql-jdbc`, `aws-sdk`, `otel-agent`)
- Servista internal libraries use `servista-` prefix in keys
- Gradle plugin artifacts prefixed with `gradle-` (e.g., `gradle-kotlin`, `gradle-detekt`) for build-logic consumption

## Dependency Management
- `catalog/libs.versions.toml` is the **single source of truth** for all dependency versions
- When bumping versions, only this file needs updating
- Consuming projects pull versions via: `versionCatalogs { create("libs") { from("eu.servista:servista-catalog:0.1.0") } }`
- After modifying, must republish since Forgejo doesn't support overwriting Maven artifacts

## Architecture

## What the catalog pins
- Servista internal: commons (0.1.0), service-runtime (0.1.0), service-runtime-events (0.1.0)
- Kotlin 2.3.10, Ktor 3.4.3, Koin 4.2.0
- Database: jOOQ 3.20.11, Flyway 12.0.3, HikariCP 7.0.2, PostgreSQL JDBC 42.7.11, Oracle JDBC 23.7
- Messaging: Kafka 4.2.0, Avro 1.12.1
- Cache: Lettuce 7.5.0.RELEASE
- Observability: OpenTelemetry 1.54.0, Micrometer 1.16.3
- Security: Nimbus JOSE+JWT 10.8, Tink 1.13.0, BouncyCastle 1.80, OpenFGA 0.9.7
- Cloud: AWS SDK 2.44.1
- Testing: JUnit 5.14.2, Kotest 6.1.4, Testcontainers 2.0.3, MockK 1.14.7
- Analysis: Detekt 2.0.0-alpha.2, Dokka 2.2.0, ktfmt 0.25.0
- Gradle plugins: kotlin-jvm, kotlin-serialization, detekt, dokka, ktfmt, bcv, shadow
