# Codebase Structure

**Analysis Date:** 2026-03-27

## Directory Layout

```
qabatz-gradle-plugins/
├── build.gradle.kts                  # Build config: plugin registration, publishing, dependencies
├── settings.gradle.kts               # Root project name (single-project build)
├── gradle.properties                 # group=eu.qabatz, version=0.2.0
├── gradlew                           # Gradle wrapper script (Linux/macOS)
├── gradlew.bat                       # Gradle wrapper script (Windows)
├── README.md                         # Project overview (minimal)
├── .gitignore                        # Excludes build/, .gradle/, .idea/, *.iml, out/
├── catalog/
│   └── libs.versions.toml            # PUBLISHED version catalog (consumed by downstream projects)
├── gradle/
│   ├── libs.versions.toml            # INTERNAL version catalog (for building this project itself)
│   └── wrapper/
│       ├── gradle-wrapper.jar         # Wrapper JAR
│       └── gradle-wrapper.properties  # Wrapper config
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── eu/qabatz/gradle/
│   │   │       ├── JooqExtension.kt  # Gradle extension class for jOOQ plugin config
│   │   │       ├── Versions.kt       # Centralized version constants
│   │   │       └── plugins/
│   │   │           ├── LibraryPlugin.kt        # Base plugin: Kotlin/JVM 21, detekt, ktfmt
│   │   │           ├── TestingPlugin.kt        # Test deps: JUnit 5, MockK, Testcontainers, etc.
│   │   │           ├── ObservabilityPlugin.kt  # Logging + metrics: kotlin-logging, Logback, Micrometer
│   │   │           ├── KafkaConsumerPlugin.kt  # Kafka consumer: kafka-clients
│   │   │           ├── KafkaProducerPlugin.kt  # Kafka producer: kafka-clients
│   │   │           ├── PipelineServicePlugin.kt# Kafka Streams: kafka-streams
│   │   │           ├── JooqPlugin.kt           # Database: jOOQ + Flyway + HikariCP + codegen
│   │   │           └── SecretsPlugin.kt        # Secrets: qabatz-kotlin-commons-secrets
│   │   └── resources/
│   │       └── detekt/
│   │           └── detekt.yml        # Shared detekt config distributed to consumers via classpath
│   └── functionalTest/
│       └── kotlin/
│           └── eu/qabatz/gradle/     # Gradle TestKit functional tests (directory exists, no files yet)
└── .planning/
    └── codebase/                     # GSD planning documents (this file, etc.)
```

## Directory Purposes

**`catalog/`:**
- Purpose: Holds the PUBLISHED version catalog that downstream Qabatz projects import
- Contains: `libs.versions.toml` with all platform dependency versions and coordinates
- Key files: `catalog/libs.versions.toml`
- Consumers reference via: `from("eu.qabatz:qabatz-gradle-plugins-catalog:<version>")`
- This is NOT used to build the plugins project itself

**`gradle/`:**
- Purpose: Gradle wrapper and the INTERNAL version catalog for building this project
- Contains: `libs.versions.toml` (build-time deps like kotlin-gradle-plugin, detekt-gradle-plugin, ktfmt, junit5, kotest), wrapper JAR + properties
- Key files: `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`
- The internal catalog versions may differ from the published catalog

**`src/main/kotlin/eu/qabatz/gradle/`:**
- Purpose: Shared code used by multiple plugins (extensions, version constants)
- Contains: `Versions.kt` (version constants object), `JooqExtension.kt` (Gradle extension class)
- Naming: Top-level shared code lives in `eu.qabatz.gradle` package

**`src/main/kotlin/eu/qabatz/gradle/plugins/`:**
- Purpose: All 8 convention plugin implementations
- Contains: One Kotlin file per plugin, each implementing `Plugin<Project>`
- Naming: `{Concern}Plugin.kt` (e.g., `LibraryPlugin.kt`, `JooqPlugin.kt`)

**`src/main/resources/detekt/`:**
- Purpose: Bundled detekt configuration distributed to all consumer projects
- Contains: `detekt.yml` with Qabatz-standard detekt rules
- Loaded at runtime via classpath: `Thread.currentThread().contextClassLoader.getResource("detekt/detekt.yml")`

**`src/functionalTest/`:**
- Purpose: Gradle TestKit functional test source set
- Contains: Empty directory structure (`kotlin/eu/qabatz/gradle/`), no test files yet
- Configured in `build.gradle.kts` as a custom source set with its own classpath

## Key File Locations

**Entry Points:**
- `build.gradle.kts`: Build configuration, plugin registration (lines 61-98), publishing config, source sets
- `settings.gradle.kts`: Single line -- sets `rootProject.name = "qabatz-gradle-plugins"`

**Configuration:**
- `gradle.properties`: Group (`eu.qabatz`) and version (`0.2.0`)
- `gradle/libs.versions.toml`: Internal build dependency versions (Kotlin 2.3.10, detekt 2.0.0-alpha.2, ktfmt 0.25.0, etc.)
- `catalog/libs.versions.toml`: Published platform catalog (~40 versioned dependencies, ~140 library aliases, 6 plugin aliases)
- `src/main/resources/detekt/detekt.yml`: Shared detekt rules (complexity, naming, style, coroutines)

**Core Logic:**
- `src/main/kotlin/eu/qabatz/gradle/plugins/LibraryPlugin.kt`: Base plugin -- every other plugin depends on this
- `src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt`: Most complex plugin -- DDL-based jOOQ codegen with extension, tasks, source set wiring
- `src/main/kotlin/eu/qabatz/gradle/Versions.kt`: Version constants used by all plugins at apply-time
- `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt`: Consumer-facing configuration DSL for jOOQ codegen

**Testing:**
- `src/functionalTest/kotlin/eu/qabatz/gradle/`: Functional test location (empty, ready for TestKit tests)
- No unit test directory exists yet (`src/test/kotlin/` is not present)

## Naming Conventions

**Files:**
- Plugin implementations: `{Concern}Plugin.kt` -- PascalCase concern name + `Plugin` suffix
- Extensions: `{PluginName}Extension.kt` -- PascalCase plugin name + `Extension` suffix
- Shared constants: `Versions.kt` -- descriptive PascalCase name

**Packages:**
- Shared code: `eu.qabatz.gradle` (e.g., `Versions`, `JooqExtension`)
- Plugin classes: `eu.qabatz.gradle.plugins` (all 8 plugins)

**Plugin IDs:**
- Format: `qabatz.{kebab-case-concern}` (e.g., `qabatz.library`, `qabatz.kafka-consumer`, `qabatz.pipeline-service`)

**Directories:**
- Standard Gradle/Maven layout: `src/main/kotlin/`, `src/main/resources/`
- Custom test source set: `src/functionalTest/kotlin/`

## Where to Add New Code

**New Convention Plugin:**
1. Create `src/main/kotlin/eu/qabatz/gradle/plugins/{Concern}Plugin.kt`
2. Implement `Plugin<Project>`, call `project.plugins.apply(LibraryPlugin::class.java)` first
3. Add dependency coordinates using `Versions.XXX` constants
4. Register in `build.gradle.kts` under `gradlePlugin { plugins { create("...") { ... } } }`
5. If new version constants are needed, add to `src/main/kotlin/eu/qabatz/gradle/Versions.kt` AND `catalog/libs.versions.toml`

**New Plugin Extension (custom DSL):**
1. Create `src/main/kotlin/eu/qabatz/gradle/{Name}Extension.kt`
2. Use `abstract class` with Gradle `Property<T>` / `DirectoryProperty` / `ListProperty<T>` fields
3. Register in the plugin's `apply()` via `project.extensions.create("name", ExtClass::class.java)`
4. Set convention defaults via `.convention()`

**New Version Constant:**
1. Add `const val` to `src/main/kotlin/eu/qabatz/gradle/Versions.kt`
2. Add matching entry in `catalog/libs.versions.toml` under `[versions]` and `[libraries]`
3. Both MUST have identical version strings

**New Bundled Resource (e.g., config file):**
1. Place in `src/main/resources/` under a descriptive subdirectory
2. Load in plugin via `Thread.currentThread().contextClassLoader.getResource("path/to/resource")`

**New Published Catalog Entry (no plugin change):**
1. Add version under `[versions]` in `catalog/libs.versions.toml`
2. Add library alias under `[libraries]` in `catalog/libs.versions.toml`
3. Optionally add plugin alias under `[plugins]` in `catalog/libs.versions.toml`

**Functional Tests (Gradle TestKit):**
1. Add test files to `src/functionalTest/kotlin/eu/qabatz/gradle/`
2. Use JUnit 5 + Gradle TestKit (`GradleRunner`)
3. Tests run via `./gradlew functionalTest` (included in `check` task)

**Unit Tests:**
1. Create `src/test/kotlin/eu/qabatz/gradle/` directory if it does not exist
2. Add test files following `{ClassName}Test.kt` naming
3. Use JUnit 5 + Kotest assertions (already in test dependencies)

## Special Directories

**`catalog/`:**
- Purpose: Published version catalog source file
- Generated: No (hand-maintained)
- Committed: Yes
- Published as: `eu.qabatz:qabatz-gradle-plugins-catalog` Maven artifact

**`src/functionalTest/`:**
- Purpose: Gradle TestKit functional tests (separate source set)
- Generated: No
- Committed: Yes (directory structure committed, no test files yet)
- Has own classpath extending `testImplementation` and `testRuntimeOnly`

**`build/generated-src/jooq/main/` (in consumer projects):**
- Purpose: jOOQ generated source code (created by `generateJooq` task in consumer projects)
- Generated: Yes (by JooqPlugin)
- Committed: No (in consumer projects)
- Not present in this project -- this is where JooqPlugin outputs code in consumer builds

## Two Version Catalogs

This project maintains TWO separate version catalogs -- understanding the distinction is critical:

| Catalog | Location | Purpose | Used by |
|---------|----------|---------|---------|
| Internal | `gradle/libs.versions.toml` | Build dependencies for compiling/testing the plugins project itself | This project's `build.gradle.kts` |
| Published | `catalog/libs.versions.toml` | Platform dependency catalog for all downstream Qabatz services | Consumer projects via `from("eu.qabatz:qabatz-gradle-plugins-catalog:x.y.z")` |

The internal catalog contains Gradle plugin artifacts (kotlin-gradle-plugin, detekt-gradle-plugin, ktfmt-gradle-plugin) and test libraries. The published catalog contains the full Qabatz platform dependency set (Ktor, Kafka, jOOQ, OTel, Testcontainers, Qabatz commons, etc.).

Some versions appear in BOTH catalogs (e.g., `kotlin`, `kotlinx-serialization`, `junit5`, `kotest`, `detekt`) but they serve different contexts.

---

*Structure analysis: 2026-03-27*
