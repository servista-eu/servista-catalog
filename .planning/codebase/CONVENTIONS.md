# Coding Conventions

**Analysis Date:** 2026-03-27

## Naming Patterns

**Plugin IDs:**
- Format: `qabatz.<feature-name>` with lowercase kebab-case for multi-word names
- Examples: `qabatz.library`, `qabatz.kafka-consumer`, `qabatz.pipeline-service`, `qabatz.jooq`
- Registration key in `build.gradle.kts` matches the feature portion: `create("library")`, `create("kafka-consumer")`

**Plugin Classes:**
- Format: `PascalCase` + `Plugin` suffix
- Location: `src/main/kotlin/eu/qabatz/gradle/plugins/`
- Examples: `LibraryPlugin`, `KafkaConsumerPlugin`, `PipelineServicePlugin`, `JooqPlugin`
- Each class implements `Plugin<Project>`

**Extension Classes:**
- Format: `PascalCase` + `Extension` suffix
- Location: `src/main/kotlin/eu/qabatz/gradle/` (one level above plugins)
- Example: `JooqExtension` at `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt`

**Utility Objects:**
- Format: `PascalCase` object name
- Example: `Versions` object at `src/main/kotlin/eu/qabatz/gradle/Versions.kt`

**Version Constants:**
- Format: `SCREAMING_SNAKE_CASE` const values inside `Versions` object
- Examples: `KOTLINX_COROUTINES`, `POSTGRESQL_JDBC`, `KOTLIN_LOGGING`

**Packages:**
- Base package: `eu.qabatz.gradle`
- Plugins subpackage: `eu.qabatz.gradle.plugins`
- Extensions and utilities live directly in `eu.qabatz.gradle`

## Code Style

**Formatting:**
- Tool: ktfmt (via `com.ncorti.ktfmt.gradle` plugin v0.25.0)
- Style: `kotlinLangStyle()` (Kotlin official style)
- Applied automatically by `LibraryPlugin` to all consuming projects
- Max line length: 120 characters (enforced by detekt, see below)

**Linting:**
- Tool: detekt v2.0.0-alpha.2 (Detekt 2.x format)
- Config file: `src/main/resources/detekt/detekt.yml`
- Applied to both this project itself AND bundled as a classpath resource for consuming projects
- `buildUponDefaultConfig = true` (extends detekt defaults)
- `parallel = true`

**Detekt Rules (from `src/main/resources/detekt/detekt.yml`):**

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

**Order (observed in plugin files):**
1. Project-internal imports (`eu.qabatz.gradle.*`)
2. Gradle API imports (`org.gradle.api.*`)
3. Third-party imports (`dev.detekt.*`, `com.ncorti.*`, `org.jetbrains.*`)

**Style:**
- Explicit imports (no wildcard `*` imports observed)
- Each import on its own line

## Plugin Structure Pattern

**All plugins follow a consistent structure:**

1. Package declaration
2. Imports
3. KDoc class comment describing what the plugin provides
4. Class implementing `Plugin<Project>`
5. `override fun apply(project: Project)` as the sole public method

**Base plugin application pattern -- every plugin applies `LibraryPlugin` first:**
```kotlin
class SomePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(LibraryPlugin::class.java)
        // Plugin-specific setup...
    }
}
```

This creates a hierarchy where `LibraryPlugin` is the root convention applied transitively by all other plugins. `LibraryPlugin` itself applies external plugins by string ID:
```kotlin
project.plugins.apply("org.jetbrains.kotlin.jvm")
project.plugins.apply("dev.detekt")
project.plugins.apply("com.ncorti.ktfmt.gradle")
```

**Plugin complexity tiers:**

- **Simple plugins** (5 of 8): Just apply `LibraryPlugin` + add dependencies. Examples: `ObservabilityPlugin`, `KafkaConsumerPlugin`, `KafkaProducerPlugin`, `PipelineServicePlugin`, `TestingPlugin`
- **Medium plugins** (2 of 8): Apply base + add dependencies + configure repository access or test fixtures. Example: `SecretsPlugin`
- **Complex plugins** (1 of 8): Full lifecycle with extension, custom tasks, source set configuration, XML generation. Example: `JooqPlugin`

## Dependency Management Pattern

**Version constants are centralized in `src/main/kotlin/eu/qabatz/gradle/Versions.kt`:**
```kotlin
object Versions {
    const val KTOR = "3.4.0"
    const val KOIN = "4.1.1"
    // ...
}
```

**Dependencies are added imperatively using string coordinates:**
```kotlin
project.dependencies.add("implementation", "org.apache.kafka:kafka-clients:${Versions.KAFKA}")
```

**For multiple dependencies, use `project.dependencies.apply {}` block:**
```kotlin
project.dependencies.apply {
    add("implementation", "org.jooq:jooq:${Versions.JOOQ}")
    add("implementation", "org.jooq:jooq-kotlin:${Versions.JOOQ}")
}
```

**Important dual-catalog architecture:**
- `gradle/libs.versions.toml` -- internal catalog for building this plugin project itself
- `catalog/libs.versions.toml` -- published catalog for consuming projects
- `Versions.kt` -- compile-time constants used by plugin code at runtime
- All three must stay synchronized when bumping versions

## Extension / DSL Pattern

**When a plugin needs user configuration, create an abstract extension class:**
```kotlin
// In eu.qabatz.gradle package (not plugins subpackage)
abstract class JooqExtension {
    abstract val packageName: Property<String>
    abstract val migrationDir: DirectoryProperty
    abstract val excludes: Property<String>
}
```

**Register extensions in the plugin's `apply()` method with conventions:**
```kotlin
val ext = project.extensions.create("jooq", JooqExtension::class.java)
ext.migrationDir.convention(
    project.layout.projectDirectory.dir("src/main/resources/db/migration")
)
ext.excludes.convention("flyway_schema_history")
```

**Extension properties use Gradle's lazy `Property<T>` / `DirectoryProperty` types**, never plain Kotlin fields.

## Task Registration Pattern

**Use `project.tasks.register()` with `Action<T>` for configuration:**
```kotlin
project.tasks.register("generateJooq", JavaExec::class.java)
generateJooq.configure(Action<JavaExec> {
    group = "jooq"
    description = "Generates jOOQ classes from Flyway SQL migrations"
    // ...
})
```

**Use `onlyIf` for conditional execution:**
```kotlin
onlyIf { ext.packageName.isPresent }
```

**Wire task dependencies explicitly:**
```kotlin
project.tasks.named("compileKotlin").configure(Action<Task> {
    dependsOn("validateProjectStructure")
})
```

## Private Method Decomposition

**For complex plugins, decompose `apply()` into private methods with clear responsibilities:**
```kotlin
override fun apply(project: Project) {
    project.plugins.apply(LibraryPlugin::class.java)
    val ext = configureExtension(project)
    val jooqTools = configureDependencies(project)
    val generateJooq = configureCodeGenTask(project, ext, jooqTools)
    configureSourceSets(project, generateJooq)
}
```

This pattern is demonstrated in `JooqPlugin` at `src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt`. Simple plugins do not need this -- they inline everything in `apply()`.

## Companion Object Usage

**Use `private companion object` for class-level constants:**
```kotlin
class LibraryPlugin : Plugin<Project> {
    private companion object {
        const val JVM_VERSION = 21
    }
}
```

**Use `companion object` with `internal` visibility for testable helper functions:**
```kotlin
companion object {
    internal fun buildJooqConfig(
        scripts: String,
        packageName: String,
        directory: String,
        excludes: String,
    ): String = """..."""
}
```

## KDoc / Documentation

**Every plugin class has a KDoc comment describing what it provides:**
```kotlin
/**
 * Convention plugin for database access with jOOQ code generation.
 * Provides: jOOQ + HikariCP + Flyway + PostgreSQL JDBC driver + DDL-based code generation.
 */
```

**Extension properties have KDoc describing purpose and defaults:**
```kotlin
/** Target package name for generated jOOQ classes. Must be set to enable code generation. */
abstract val packageName: Property<String>
```

**The `Versions` object has a block KDoc explaining the dual-catalog sync requirement.**

## Trailing Commas

**Trailing commas are used consistently in function calls and parameter lists:**
```kotlin
project.dependencies.add(
    "implementation",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.KOTLINX_COROUTINES}",
)
```

## Error Handling

**Use `GradleException` for build-time validation failures:**
```kotlin
throw GradleException(
    "Missing required directories: ${missing.joinToString(", ")}. " +
        "Qabatz projects must follow the standard directory layout."
)
```

## Repository Configuration

**When a plugin needs access to the private Forgejo registry (e.g., for qabatz-kotlin-commons), configure it in the plugin:**
```kotlin
project.repositories.maven {
    name = "QabatzForgejo"
    setUrl("https://git.hestia-ng.eu/api/packages/qabatz/maven")
    credentials {
        username = System.getenv("FORGEJO_USER") ?: "token"
        password = System.getenv("FORGEJO_TOKEN") ?: ""
    }
}
```

Credential resolution pattern: environment variable with fallback to `"token"` (for Forgejo token-based auth where username is literal `"token"`).

---

*Convention analysis: 2026-03-27*
