<!-- GSD:project-start source:PROJECT.md -->
## Project

**servista-catalog**

A pure Gradle version catalog providing centralized dependency version alignment for all servista Kotlin projects. Published as `eu.servista:servista-catalog:0.1.0` to the Forgejo Maven registry, consumed by servista-kotlin-commons, servista-service-runtime, and all servista services. Each consuming project owns its own build-logic convention plugins locally; this project only manages the version catalog.

**Core Value:** Single source of truth for dependency versions across the entire servista ecosystem — preventing version drift and dependency conflicts between projects.

### Constraints

- **Artifact history**: Consuming projects previously referenced `eu.servista:servista-gradle-plugins-catalog:0.2.0` — now published as `eu.servista:servista-catalog:0.1.0`
- **Forgejo credentials**: Registry requires `FORGEJO_USER`/`FORGEJO_TOKEN` for publish and resolve
- **No source code**: Pure catalog — no Kotlin, no convention plugins, no compilation
<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->
## Technology Stack

## Languages
- TOML - Version catalog definition (`catalog/libs.versions.toml`)

## Runtime
- Gradle 9.3.1 (via wrapper: `gradle/wrapper/gradle-wrapper.properties`)

## Build System
- `maven-publish` - Publishing to Maven repositories
- `version-catalog` - Publishing a version catalog artifact
- `build.gradle.kts` - Main build script (catalog publishing + Forgejo repository config)
- `settings.gradle.kts` - Single-line root project name declaration
- `gradle.properties` - Group (`eu.servista`) and version (`0.1.0`)
- `gradle/libs.versions.toml` - Internal catalog for building this project (empty)
- `catalog/libs.versions.toml` - **The published version catalog** consumed by all downstream projects

## Project Structure
```
servista-catalog/
  build.gradle.kts          # version-catalog + maven-publish config
  settings.gradle.kts       # rootProject.name = "servista-catalog"
  gradle.properties         # group=eu.servista, version=0.1.0
  catalog/libs.versions.toml  # THE artifact -- all dependency versions live here
  gradle/libs.versions.toml   # Empty internal catalog (no build deps)
```

## Configuration
- `FORGEJO_USER` - Username for Forgejo Maven registry (default: "token")
- `FORGEJO_TOKEN` - Token for Forgejo Maven registry
- `publishUrl` - Override Maven publish URL (default: `https://git.hestia-ng.eu/api/packages/servista/maven`)
- `forgejoUser` / `forgejoToken` - Alternative to environment variables
- `publishToken` - Alternative token property
- Catalog artifact: `eu.servista:servista-catalog:0.1.0`

## Platform Requirements
- Gradle 9.3.1 (use wrapper: `./gradlew`)
- No JDK required to build (pure catalog, no compilation)
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

## Version Catalog Structure
- `[versions]` section: version strings keyed by dependency short-name (e.g., `ktor = "3.4.3"`)
- `[libraries]` section: module coordinates referencing version keys via `version.ref`
- `[plugins]` section: plugin IDs referencing version keys
- Naming: lowercase kebab-case for version keys (e.g., `postgresql-jdbc`, `aws-sdk`, `otel-agent`)
- Servista internal libraries use `servista-` prefix in keys

## Dependency Management
- `catalog/libs.versions.toml` is the **single source of truth** for all dependency versions
- When bumping versions, only this file needs updating
- Consuming projects pull versions via: `versionCatalogs { create("libs") { from("eu.servista:servista-catalog:0.1.0") } }`
- After modifying, must delete+republish since Forgejo doesn't support overwriting Maven artifacts
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

## Pattern Overview
- Single-project Gradle build publishing a version catalog artifact
- No source code, no plugins, no compilation
- The entire deliverable is `catalog/libs.versions.toml` packaged as a Maven artifact

## Entry Points
- `./gradlew publish` — publishes the catalog to Forgejo
- Consumers import via: `from("eu.servista:servista-catalog:0.1.0")`

## What the catalog pins
- Servista internal: commons (0.1.0), service-runtime (0.1.0), service-runtime-events (0.1.0)
- Kotlin 2.3.10, Ktor 3.4.3, Koin 4.2.0
- Database: jOOQ 3.20.11, Flyway 12.0.3, HikariCP 7.0.2, PostgreSQL JDBC 42.7.11
- Messaging: Kafka 4.2.0, Avro 1.12.1
- Cache: Lettuce 7.5.0.RELEASE
- Observability: OpenTelemetry 1.54.0, Micrometer 1.16.3
- Security: Nimbus JOSE+JWT 10.8, Tink 1.13.0, OpenFGA 0.9.7
- Testing: JUnit 5.14.2, Kotest 6.1.4, Testcontainers 2.0.3, MockK 1.14.7
<!-- GSD:architecture-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd:quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd:debug` for investigation and bug fixing
- `/gsd:execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->

<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd:profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
