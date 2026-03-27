# Architecture

**Analysis Date:** 2026-03-27

## Pattern Overview

**Overall:** Gradle Convention Plugin Pattern (precompiled script plugins using `kotlin-dsl`)

**Key Characteristics:**
- Single-project Gradle build producing 8 convention plugins and a published version catalog
- Hub-and-spoke plugin hierarchy: `LibraryPlugin` is the base; all other plugins apply it first
- Dependency injection via hardcoded version constants in `Versions.kt`, kept in sync with the published catalog `catalog/libs.versions.toml`
- Plugins are "opinionated defaults" -- they apply Gradle plugins AND add specific dependency coordinates to the consumer project
- Only one plugin (`JooqPlugin`) exposes a custom Gradle extension for consumer configuration; the rest are zero-config

## Plugin Hierarchy

```
LibraryPlugin  (qabatz.library)
    |
    +-- TestingPlugin          (qabatz.testing)
    +-- ObservabilityPlugin    (qabatz.observability)
    +-- KafkaConsumerPlugin    (qabatz.kafka-consumer)
    +-- KafkaProducerPlugin    (qabatz.kafka-producer)
    +-- PipelineServicePlugin  (qabatz.pipeline-service)
    +-- JooqPlugin             (qabatz.jooq)
    +-- SecretsPlugin          (qabatz.secrets)
```

Every plugin except `LibraryPlugin` calls `project.plugins.apply(LibraryPlugin::class.java)` as its first action. This guarantees that any consumer applying *any* Qabatz plugin gets the full base setup (Kotlin/JVM 21, detekt, ktfmt, coroutines, datetime, structure validation).

Because Gradle plugin application is idempotent, consumers can safely apply multiple Qabatz plugins (e.g., `qabatz.jooq` + `qabatz.testing` + `qabatz.observability`) without double-applying the base.

## Layers

**Base Layer -- LibraryPlugin:**
- Purpose: Establish the Kotlin/JVM compilation baseline and code quality tooling for every Qabatz project
- Location: `src/main/kotlin/eu/qabatz/gradle/plugins/LibraryPlugin.kt`
- Applies: `org.jetbrains.kotlin.jvm`, `dev.detekt`, `com.ncorti.ktfmt.gradle`
- Configures: JVM 21 toolchain, detekt with bundled config from classpath, ktfmt with `kotlinLangStyle()`
- Registers: `validateProjectStructure` task (verifies `src/main/kotlin`, `src/main/resources`, `src/test/kotlin` exist)
- Adds dependencies: `kotlinx-coroutines-core`, `kotlinx-datetime`
- Used by: All other plugins (mandatory transitive base)

**Feature Plugins -- Additive dependency plugins:**
- Purpose: Add curated dependency sets for specific platform concerns
- Location: `src/main/kotlin/eu/qabatz/gradle/plugins/`
- Pattern: Apply `LibraryPlugin`, then add `implementation` and/or `testImplementation` dependencies
- Contains:
  - `TestingPlugin.kt` -- JUnit 5, MockK, Kotest assertions, Testcontainers (core + PostgreSQL + Kafka), Ktor test host, Koin test
  - `ObservabilityPlugin.kt` -- kotlin-logging, Logback, Micrometer Prometheus
  - `KafkaConsumerPlugin.kt` -- kafka-clients
  - `KafkaProducerPlugin.kt` -- kafka-clients
  - `PipelineServicePlugin.kt` -- kafka-streams
  - `SecretsPlugin.kt` -- qabatz-kotlin-commons-secrets (with test fixtures capability)

**Rich Plugin -- JooqPlugin:**
- Purpose: Full database access setup with DDL-based jOOQ code generation
- Location: `src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt`
- Extension: `JooqExtension` at `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt`
- Configures: custom `jooqCodegen` configuration, `generateJooq` JavaExec task, source set wiring
- Adds dependencies: jOOQ (core + kotlin + kotlin-coroutines), HikariCP, Flyway (core + PostgreSQL), PostgreSQL JDBC
- Code generation: Uses `org.jooq.meta.extensions.ddl.DDLDatabase` to parse Flyway SQL migrations without a live database

**Shared Code:**
- `src/main/kotlin/eu/qabatz/gradle/Versions.kt` -- Centralized version constants; must stay in sync with `catalog/libs.versions.toml`
- `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt` -- Gradle extension class for the jOOQ plugin

**Bundled Resources:**
- `src/main/resources/detekt/detekt.yml` -- Shared detekt configuration distributed to all consumers via classpath resource loading

## Data Flow

**Plugin Application Flow (consumer perspective):**

1. Consumer applies `id("qabatz.jooq")` in their `plugins {}` block
2. Gradle resolves catalog from the `qabatz-catalog` artifact
3. `JooqPlugin.apply()` calls `project.plugins.apply(LibraryPlugin::class.java)`
4. `LibraryPlugin.apply()` applies Kotlin/JVM + detekt + ktfmt, sets JVM 21, registers `validateProjectStructure`
5. `JooqPlugin.apply()` creates the `jooq {}` extension, adds runtime dependencies, registers `generateJooq` task
6. At build time: `compileKotlin` depends on `validateProjectStructure` and `generateJooq`

**jOOQ Code Generation Flow:**

1. Consumer configures `jooq { packageName.set("eu.qabatz.myservice.generated") }`
2. `generateJooq` task checks `onlyIf { ext.packageName.isPresent }` -- skips if not configured
3. Task reads Flyway SQL files from `ext.migrationDir` (default: `src/main/resources/db/migration`)
4. Generates XML config at `build/jooq/jooq-config.xml` via `buildJooqConfig()`
5. Runs `org.jooq.codegen.GenerationTool` as a `JavaExec` with the `jooqCodegen` classpath
6. Output goes to `build/generated-src/jooq/main` which is added to the `main` source set

**Detekt Configuration Distribution:**

1. `detekt.yml` is bundled at `src/main/resources/detekt/detekt.yml`
2. `LibraryPlugin` loads it via `Thread.currentThread().contextClassLoader.getResource("detekt/detekt.yml")`
3. Applied to consumer project's detekt extension via `config.from(project.resources.text.fromUri(...))`

## Key Abstractions

**Plugin Interface (`Plugin<Project>`):**
- Purpose: Standard Gradle contract for all convention plugins
- Examples: All 8 plugin classes in `src/main/kotlin/eu/qabatz/gradle/plugins/`
- Pattern: Each class implements `Plugin<Project>` with a single `apply(project: Project)` method

**JooqExtension (custom Gradle extension):**
- Purpose: Consumer-facing configuration DSL for jOOQ code generation
- Location: `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt`
- Properties:
  - `packageName: Property<String>` -- Target package for generated classes (required to enable codegen)
  - `migrationDir: DirectoryProperty` -- Flyway SQL migration directory (default: `src/main/resources/db/migration`)
  - `excludes: Property<String>` -- Table name exclusion pattern (default: `flyway_schema_history`)
- Pattern: Gradle lazy properties (`Property<T>`, `DirectoryProperty`) with conventions

**Versions Object:**
- Purpose: Single source of truth for dependency versions used by plugin code at apply-time
- Location: `src/main/kotlin/eu/qabatz/gradle/Versions.kt`
- Pattern: Kotlin `object` with `const val` constants -- hardcoded, not derived from catalog at runtime

## Entry Points

**Plugin Registration (`build.gradle.kts` gradlePlugin block):**
- Location: `build.gradle.kts` lines 61-98
- Triggers: Gradle plugin resolution when consumers use `id("qabatz.xxx")`
- Maps plugin IDs to implementation classes:
  - `qabatz.library` -> `eu.qabatz.gradle.plugins.LibraryPlugin`
  - `qabatz.testing` -> `eu.qabatz.gradle.plugins.TestingPlugin`
  - `qabatz.observability` -> `eu.qabatz.gradle.plugins.ObservabilityPlugin`
  - `qabatz.kafka-consumer` -> `eu.qabatz.gradle.plugins.KafkaConsumerPlugin`
  - `qabatz.kafka-producer` -> `eu.qabatz.gradle.plugins.KafkaProducerPlugin`
  - `qabatz.pipeline-service` -> `eu.qabatz.gradle.plugins.PipelineServicePlugin`
  - `qabatz.jooq` -> `eu.qabatz.gradle.plugins.JooqPlugin`
  - `qabatz.secrets` -> `eu.qabatz.gradle.plugins.SecretsPlugin`

**Version Catalog Publication:**
- Location: `build.gradle.kts` lines 40-44
- Artifact: `eu.qabatz:qabatz-catalog`
- Source: `catalog/libs.versions.toml`
- Consumers import via: `from("eu.qabatz:qabatz-catalog:x.y.z")`

## Error Handling

**Strategy:** Fail-fast with `GradleException` for structural violations

**Patterns:**
- `LibraryPlugin` registers `validateProjectStructure` task that throws `GradleException` if `src/main/kotlin`, `src/main/resources`, or `src/test/kotlin` directories are missing
- `JooqPlugin` uses `onlyIf { ext.packageName.isPresent }` to gracefully skip code generation when not configured, rather than failing
- Detekt config loading uses a null check (`if (detektConfig != null)`) to silently skip if the classpath resource is missing

## Design Decisions and Trade-offs

**Hardcoded versions in Versions.kt vs. reading from catalog:**
- Decision: Versions are duplicated as compile-time constants in `Versions.kt` AND in `catalog/libs.versions.toml`
- Trade-off: Simpler plugin code (no catalog parsing at apply-time), but requires manual sync when bumping versions
- Risk: Version drift between `Versions.kt` and `catalog/libs.versions.toml` if only one is updated

**All plugins apply LibraryPlugin by class reference:**
- Decision: `project.plugins.apply(LibraryPlugin::class.java)` rather than `project.plugins.apply("qabatz.library")`
- Trade-off: Direct class reference is faster and avoids plugin resolution overhead; but creates compile-time coupling within the plugin project (which is acceptable since they ship in the same artifact)

**KafkaConsumerPlugin and KafkaProducerPlugin are functionally identical:**
- Decision: Separate plugins with the same dependency (`kafka-clients`)
- Trade-off: Semantic clarity for consumers (express intent), at the cost of code duplication
- Future: These may diverge as consumer-specific or producer-specific dependencies are added

**SecretsPlugin adds a Forgejo Maven repository:**
- Decision: `SecretsPlugin` hardcodes the Qabatz Forgejo Maven URL and reads credentials from environment variables
- Trade-off: Convenience for consumers (they don't need to configure the repo), but couples the plugin to a specific registry URL

**DDL-based jOOQ codegen (no live database):**
- Decision: Uses `DDLDatabase` to parse SQL files rather than connecting to a running PostgreSQL
- Trade-off: Faster builds, no database needed in CI; but may miss runtime-only PostgreSQL features or extensions not supported by DDLDatabase parser

## Cross-Cutting Concerns

**Code Quality:** Enforced via detekt (bundled `detekt.yml` at `src/main/resources/detekt/detekt.yml`) and ktfmt (`kotlinLangStyle()`) applied by `LibraryPlugin` to every consumer project

**Version Management:** Dual system -- `Versions.kt` for plugin apply-time dependency resolution, `catalog/libs.versions.toml` as a published version catalog for consumer `libs.xxx` references

**Repository Configuration:** Most plugins rely on consumers having `mavenCentral()` configured. `SecretsPlugin` is the exception, adding the Qabatz Forgejo Maven repository explicitly for internal artifacts.

**Build Validation:** `validateProjectStructure` task runs before `compileKotlin` in every consumer project, enforcing the standard directory layout

---

*Architecture analysis: 2026-03-27*
