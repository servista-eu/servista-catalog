<!-- GSD:project-start source:PROJECT.md -->
## Project

**qabatz-catalog**

A shared Gradle version catalog providing centralized dependency version alignment for all Qabatz Kotlin projects. Published as `eu.qabatz:qabatz-catalog` to the Forgejo Maven registry, consumed by projects like qabatz-kotlin-commons, qabatz-kotlin-ktor, and future Qabatz services. Each consuming project owns its own build-logic convention plugins locally; this project only manages the version catalog.

**Core Value:** Single source of truth for dependency versions across the entire Qabatz ecosystem — preventing version drift and dependency conflicts between projects.

### Constraints

- **Backward compatibility**: Consuming projects reference `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0` — coordinate change requires updating all consumers
- **Forgejo credentials**: Repository uses embedded token in git remote URL (user `heaphopdancer`)
- **GitHub mirror**: Push mirror token must be reused from current Forgejo repo configuration
<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->
## Technology Stack

## Languages
- Kotlin 2.3.10 - All plugin source code and build scripts (Kotlin DSL)
- XML - jOOQ codegen configuration template (generated at build time in `JooqPlugin.kt`)
- TOML - Version catalog definitions (`catalog/libs.versions.toml`, `gradle/libs.versions.toml`)
- YAML - Detekt configuration (`src/main/resources/detekt/detekt.yml`)
## Runtime
- JVM 21 (enforced by `LibraryPlugin` via `jvmToolchain(21)`)
- Gradle 9.3.1 (via wrapper: `gradle/wrapper/gradle-wrapper.properties`)
- Gradle with Kotlin DSL
- Lockfile: Not present (no dependency locking configured)
## Build System
- `kotlin-dsl` - Enables Kotlin DSL for Gradle plugin development
- `java-gradle-plugin` - Infrastructure for building Gradle plugins
- `maven-publish` - Publishing to Maven repositories
- `version-catalog` - Publishing a version catalog artifact
- `dev.detekt` 2.0.0-alpha.2 - Static analysis for the plugin project itself
- `build.gradle.kts` - Main build script
- `settings.gradle.kts` - Single-line root project name declaration
- `gradle.properties` - Group (`eu.qabatz`) and version (`0.2.0`)
- `gradle/libs.versions.toml` - Internal version catalog for building the plugins project
- `catalog/libs.versions.toml` - Published version catalog for consumer projects
## Convention Plugins (8 total)
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
## Key Dependencies
- `org.jetbrains.kotlin:kotlin-gradle-plugin` 2.3.10 - Kotlin compiler plugin on classpath
- `org.jetbrains.kotlin:kotlin-serialization` 2.3.10 - Serialization compiler plugin
- `dev.detekt:detekt-gradle-plugin` 2.0.0-alpha.2 - Detekt plugin on classpath
- `com.ncorti.ktfmt.gradle:plugin` 0.25.0 - ktfmt formatter plugin on classpath
- `org.jetbrains.kotlinx:kotlinx-serialization-json` 1.10.0 - Serialization runtime
- `gradleTestKit()` - Gradle TestKit for functional testing
- `org.junit.jupiter:junit-jupiter` 5.14.2 - JUnit 5
- `io.kotest:kotest-assertions-core` 6.1.4 - Kotest assertion matchers
- Defined in `src/main/kotlin/eu/qabatz/gradle/Versions.kt`
- Must stay in sync with `catalog/libs.versions.toml`
## Version Management
- `gradle/libs.versions.toml` - Used only for building the plugins project itself (referenced as `libs.*` in `build.gradle.kts`)
## Configuration
- `FORGEJO_USER` - Username for Forgejo Maven registry (default: "token")
- `FORGEJO_TOKEN` - Token for Forgejo Maven registry
- `publishUrl` - Override Maven publish URL (default: `https://git.hestia-ng.eu/api/packages/qabatz/maven`)
- `forgejoUser` / `forgejoToken` - Alternative to environment variables
- `publishToken` - Alternative token property
- Plugin artifacts: `eu.qabatz:qabatz-gradle-plugins:0.2.0`
- Version catalog artifact: `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0`
## Platform Requirements
- JDK 21+
- Gradle 9.3.1 (use wrapper: `./gradlew`)
- Consumed as Gradle plugins and version catalog by downstream Kotlin/JVM projects targeting JVM 21
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

## Naming Patterns
- Format: `qabatz.<feature-name>` with lowercase kebab-case for multi-word names
- Examples: `qabatz.library`, `qabatz.kafka-consumer`, `qabatz.pipeline-service`, `qabatz.jooq`
- Registration key in `build.gradle.kts` matches the feature portion: `create("library")`, `create("kafka-consumer")`
- Format: `PascalCase` + `Plugin` suffix
- Location: `src/main/kotlin/eu/qabatz/gradle/plugins/`
- Examples: `LibraryPlugin`, `KafkaConsumerPlugin`, `PipelineServicePlugin`, `JooqPlugin`
- Each class implements `Plugin<Project>`
- Format: `PascalCase` + `Extension` suffix
- Location: `src/main/kotlin/eu/qabatz/gradle/` (one level above plugins)
- Example: `JooqExtension` at `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt`
- Format: `PascalCase` object name
- Example: `Versions` object at `src/main/kotlin/eu/qabatz/gradle/Versions.kt`
- Format: `SCREAMING_SNAKE_CASE` const values inside `Versions` object
- Examples: `KOTLINX_COROUTINES`, `POSTGRESQL_JDBC`, `KOTLIN_LOGGING`
- Base package: `eu.qabatz.gradle`
- Plugins subpackage: `eu.qabatz.gradle.plugins`
- Extensions and utilities live directly in `eu.qabatz.gradle`
## Code Style
- Tool: ktfmt (via `com.ncorti.ktfmt.gradle` plugin v0.25.0)
- Style: `kotlinLangStyle()` (Kotlin official style)
- Applied automatically by `LibraryPlugin` to all consuming projects
- Max line length: 120 characters (enforced by detekt, see below)
- Tool: detekt v2.0.0-alpha.2 (Detekt 2.x format)
- Config file: `src/main/resources/detekt/detekt.yml`
- Applied to both this project itself AND bundled as a classpath resource for consuming projects
- `buildUponDefaultConfig = true` (extends detekt defaults)
- `parallel = true`
| Category | Key Rules |
|----------|-----------|
| complexity | `CyclomaticComplexMethod`: max 15, `LongMethod`: max 60 lines, `LargeClass`: max 600 lines, `TooManyFunctions`: max 15 per file/class |
| style | `MagicNumber`: ignores -1, 0, 1, 2 + hashCode/property/annotation/enum, `MaxLineLength`: 120 chars |
| naming | Active (uses detekt defaults) |
| coroutines | Active (uses detekt defaults) |
| empty-blocks | Active |
| exceptions | Active |
| performance | Active |
| potential-bugs | Active |
| comments | Active |
## Import Organization
- Explicit imports (no wildcard `*` imports observed)
- Each import on its own line
## Plugin Structure Pattern
- **Simple plugins** (5 of 8): Just apply `LibraryPlugin` + add dependencies. Examples: `ObservabilityPlugin`, `KafkaConsumerPlugin`, `KafkaProducerPlugin`, `PipelineServicePlugin`, `TestingPlugin`
- **Medium plugins** (2 of 8): Apply base + add dependencies + configure repository access or test fixtures. Example: `SecretsPlugin`
- **Complex plugins** (1 of 8): Full lifecycle with extension, custom tasks, source set configuration, XML generation. Example: `JooqPlugin`
## Dependency Management Pattern
- `gradle/libs.versions.toml` -- internal catalog for building this plugin project itself
- `catalog/libs.versions.toml` -- published catalog for consuming projects
- `Versions.kt` -- compile-time constants used by plugin code at runtime
- All three must stay synchronized when bumping versions
## Extension / DSL Pattern
## Task Registration Pattern
## Private Method Decomposition
## Companion Object Usage
## KDoc / Documentation
## Trailing Commas
## Error Handling
## Repository Configuration
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

## Pattern Overview
- Single-project Gradle build producing 8 convention plugins and a published version catalog
- Hub-and-spoke plugin hierarchy: `LibraryPlugin` is the base; all other plugins apply it first
- Dependency injection via hardcoded version constants in `Versions.kt`, kept in sync with the published catalog `catalog/libs.versions.toml`
- Plugins are "opinionated defaults" -- they apply Gradle plugins AND add specific dependency coordinates to the consumer project
- Only one plugin (`JooqPlugin`) exposes a custom Gradle extension for consumer configuration; the rest are zero-config
## Plugin Hierarchy
```
```
## Layers
- Purpose: Establish the Kotlin/JVM compilation baseline and code quality tooling for every Qabatz project
- Location: `src/main/kotlin/eu/qabatz/gradle/plugins/LibraryPlugin.kt`
- Applies: `org.jetbrains.kotlin.jvm`, `dev.detekt`, `com.ncorti.ktfmt.gradle`
- Configures: JVM 21 toolchain, detekt with bundled config from classpath, ktfmt with `kotlinLangStyle()`
- Registers: `validateProjectStructure` task (verifies `src/main/kotlin`, `src/main/resources`, `src/test/kotlin` exist)
- Adds dependencies: `kotlinx-coroutines-core`, `kotlinx-datetime`
- Used by: All other plugins (mandatory transitive base)
- Purpose: Add curated dependency sets for specific platform concerns
- Location: `src/main/kotlin/eu/qabatz/gradle/plugins/`
- Pattern: Apply `LibraryPlugin`, then add `implementation` and/or `testImplementation` dependencies
- Contains:
- Purpose: Full database access setup with DDL-based jOOQ code generation
- Location: `src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt`
- Extension: `JooqExtension` at `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt`
- Configures: custom `jooqCodegen` configuration, `generateJooq` JavaExec task, source set wiring
- Adds dependencies: jOOQ (core + kotlin + kotlin-coroutines), HikariCP, Flyway (core + PostgreSQL), PostgreSQL JDBC
- Code generation: Uses `org.jooq.meta.extensions.ddl.DDLDatabase` to parse Flyway SQL migrations without a live database
- `src/main/kotlin/eu/qabatz/gradle/Versions.kt` -- Centralized version constants; must stay in sync with `catalog/libs.versions.toml`
- `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt` -- Gradle extension class for the jOOQ plugin
- `src/main/resources/detekt/detekt.yml` -- Shared detekt configuration distributed to all consumers via classpath resource loading
## Data Flow
## Key Abstractions
- Purpose: Standard Gradle contract for all convention plugins
- Examples: All 8 plugin classes in `src/main/kotlin/eu/qabatz/gradle/plugins/`
- Pattern: Each class implements `Plugin<Project>` with a single `apply(project: Project)` method
- Purpose: Consumer-facing configuration DSL for jOOQ code generation
- Location: `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt`
- Properties:
- Pattern: Gradle lazy properties (`Property<T>`, `DirectoryProperty`) with conventions
- Purpose: Single source of truth for dependency versions used by plugin code at apply-time
- Location: `src/main/kotlin/eu/qabatz/gradle/Versions.kt`
- Pattern: Kotlin `object` with `const val` constants -- hardcoded, not derived from catalog at runtime
## Entry Points
- Location: `build.gradle.kts` lines 61-98
- Triggers: Gradle plugin resolution when consumers use `id("qabatz.xxx")`
- Maps plugin IDs to implementation classes:
- Location: `build.gradle.kts` lines 40-44
- Artifact: `eu.qabatz:qabatz-gradle-plugins-catalog`
- Source: `catalog/libs.versions.toml`
- Consumers import via: `from("eu.qabatz:qabatz-gradle-plugins-catalog:x.y.z")`
## Error Handling
- `LibraryPlugin` registers `validateProjectStructure` task that throws `GradleException` if `src/main/kotlin`, `src/main/resources`, or `src/test/kotlin` directories are missing
- `JooqPlugin` uses `onlyIf { ext.packageName.isPresent }` to gracefully skip code generation when not configured, rather than failing
- Detekt config loading uses a null check (`if (detektConfig != null)`) to silently skip if the classpath resource is missing
## Design Decisions and Trade-offs
- Decision: Versions are duplicated as compile-time constants in `Versions.kt` AND in `catalog/libs.versions.toml`
- Trade-off: Simpler plugin code (no catalog parsing at apply-time), but requires manual sync when bumping versions
- Risk: Version drift between `Versions.kt` and `catalog/libs.versions.toml` if only one is updated
- Decision: `project.plugins.apply(LibraryPlugin::class.java)` rather than `project.plugins.apply("qabatz.library")`
- Trade-off: Direct class reference is faster and avoids plugin resolution overhead; but creates compile-time coupling within the plugin project (which is acceptable since they ship in the same artifact)
- Decision: Separate plugins with the same dependency (`kafka-clients`)
- Trade-off: Semantic clarity for consumers (express intent), at the cost of code duplication
- Future: These may diverge as consumer-specific or producer-specific dependencies are added
- Decision: `SecretsPlugin` hardcodes the Qabatz Forgejo Maven URL and reads credentials from environment variables
- Trade-off: Convenience for consumers (they don't need to configure the repo), but couples the plugin to a specific registry URL
- Decision: Uses `DDLDatabase` to parse SQL files rather than connecting to a running PostgreSQL
- Trade-off: Faster builds, no database needed in CI; but may miss runtime-only PostgreSQL features or extensions not supported by DDLDatabase parser
## Cross-Cutting Concerns
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
