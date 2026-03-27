# Testing Patterns

**Analysis Date:** 2026-03-27

## Test Framework

**Runner:**
- JUnit 5 (Jupiter) v5.14.2
- Config: `build.gradle.kts` (inline, no separate test config file)

**Assertion Library:**
- Kotest assertions v6.1.4

**Run Commands:**
```bash
./gradlew test                # Run unit tests
./gradlew functionalTest      # Run Gradle TestKit functional tests
./gradlew check               # Run all tests + detekt (check depends on functionalTest)
```

## Test Infrastructure Setup

**The build file (`build.gradle.kts`) defines two test source sets:**

1. **Unit tests** (`src/test/kotlin/`) -- standard Gradle test source set
2. **Functional tests** (`src/functionalTest/kotlin/eu/qabatz/gradle/`) -- custom source set for Gradle TestKit

**Functional test source set configuration (from `build.gradle.kts` lines 48-58):**
```kotlin
val functionalTest by sourceSets.creating {
    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
}

val functionalTestImplementation by configurations.getting {
    extendsFrom(configurations["testImplementation"])
}
val functionalTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations["testRuntimeOnly"])
}
```

**Functional test task registration (from `build.gradle.kts` lines 100-108):**
```kotlin
tasks.register<Test>("functionalTest") {
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("functionalTest")
}
```

**Gradle TestKit is wired via `gradlePlugin.testSourceSets` (line 97):**
```kotlin
gradlePlugin {
    testSourceSets(sourceSets["functionalTest"])
}
```

## Test Dependencies

**Available in both test and functionalTest source sets:**
- `gradleTestKit()` -- Gradle TestKit for running Gradle builds programmatically
- `org.junit.jupiter:junit-jupiter:5.14.2` -- JUnit 5 test framework
- `org.junit.platform:junit-platform-launcher` -- JUnit platform launcher (runtime only)
- `io.kotest:kotest-assertions-core:6.1.4` -- Kotest assertion DSL

## Current Test File Status

**Unit tests:** `src/test/kotlin/` -- Directory does NOT exist. No unit tests.

**Functional tests:** `src/functionalTest/kotlin/eu/qabatz/gradle/` -- Directory exists but is EMPTY. No functional tests.

**Coverage:** No coverage tooling configured. No coverage requirements enforced.

## Test File Organization

**Intended Location (based on configured source sets):**
- Unit tests: `src/test/kotlin/eu/qabatz/gradle/`
- Functional tests: `src/functionalTest/kotlin/eu/qabatz/gradle/`

**Intended Naming:**
- Unit tests: `*Test.kt` (JUnit 5 convention)
- Functional tests: `*FunctionalTest.kt` or `*Test.kt`

## How to Write Unit Tests

**What can be unit tested:**

1. **`Versions` object** (`src/main/kotlin/eu/qabatz/gradle/Versions.kt`) -- Verify version constants match `catalog/libs.versions.toml` values.

2. **`JooqPlugin.buildJooqConfig()`** (`src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt` line 130) -- This companion object function is `internal` visibility, making it directly testable from the same module. It generates XML configuration as a string.

**Unit test pattern for `buildJooqConfig`:**
```kotlin
package eu.qabatz.gradle.plugins

import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class JooqPluginTest {
    @Test
    fun `buildJooqConfig produces valid XML with given parameters`() {
        val xml = JooqPlugin.buildJooqConfig(
            scripts = "/path/to/migrations/*.sql",
            packageName = "eu.qabatz.myservice.generated",
            directory = "/build/generated-src/jooq/main",
            excludes = "flyway_schema_history",
        )

        xml shouldContain "<packageName>eu.qabatz.myservice.generated</packageName>"
        xml shouldContain "<value>/path/to/migrations/*.sql</value>"
        xml shouldContain "<excludes>flyway_schema_history</excludes>"
    }
}
```

## How to Write Functional Tests

**Functional tests use Gradle TestKit to apply plugins in a temporary Gradle project and verify behavior.**

**Pattern for testing convention plugins:**
```kotlin
package eu.qabatz.gradle

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class LibraryPluginFunctionalTest {
    @TempDir
    lateinit var projectDir: File

    @Test
    fun `library plugin applies Kotlin JVM and detekt`() {
        // Arrange: create minimal project
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("qabatz.library")
            }
        """.trimIndent())
        projectDir.resolve("src/main/kotlin").mkdirs()
        projectDir.resolve("src/main/resources").mkdirs()
        projectDir.resolve("src/test/kotlin").mkdirs()

        // Act
        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .build()

        // Assert
        result.output shouldContain "validateProjectStructure"
    }

    @Test
    fun `library plugin fails when required directories are missing`() {
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("qabatz.library")
            }
        """.trimIndent())
        // Deliberately do NOT create src dirs

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("validateProjectStructure")
            .buildAndFail()

        result.output shouldContain "Missing required directories"
    }
}
```

**Key TestKit patterns:**
- `@TempDir` from JUnit 5 for disposable project directories
- `GradleRunner.create().withPluginClasspath()` -- automatically includes the plugin under test (wired by `gradlePlugin.testSourceSets`)
- `build()` for expected success, `buildAndFail()` for expected failure
- Check `result.task(":taskName")?.outcome` for `TaskOutcome.SUCCESS`, `TaskOutcome.UP_TO_DATE`, etc.
- Check `result.output` for log messages and error text

## What to Test Per Plugin

**`LibraryPlugin` (`src/main/kotlin/eu/qabatz/gradle/plugins/LibraryPlugin.kt`):**
- Applies Kotlin JVM plugin
- Applies detekt with bundled config
- Applies ktfmt with kotlinLangStyle
- Registers `validateProjectStructure` task
- `validateProjectStructure` fails when dirs are missing
- `compileKotlin` depends on `validateProjectStructure`
- Adds `kotlinx-coroutines-core` and `kotlinx-datetime` to implementation

**`TestingPlugin` (`src/main/kotlin/eu/qabatz/gradle/plugins/TestingPlugin.kt`):**
- Applies `LibraryPlugin` transitively
- Adds all test dependencies (JUnit 5, MockK, Kotest, Testcontainers, Ktor test, Koin test)
- Configures `useJUnitPlatform()` on all Test tasks

**`ObservabilityPlugin` (`src/main/kotlin/eu/qabatz/gradle/plugins/ObservabilityPlugin.kt`):**
- Applies `LibraryPlugin` transitively
- Adds kotlin-logging, Logback, Micrometer Prometheus

**`KafkaConsumerPlugin` (`src/main/kotlin/eu/qabatz/gradle/plugins/KafkaConsumerPlugin.kt`):**
- Adds `kafka-clients` dependency

**`KafkaProducerPlugin` (`src/main/kotlin/eu/qabatz/gradle/plugins/KafkaProducerPlugin.kt`):**
- Adds `kafka-clients` dependency

**`PipelineServicePlugin` (`src/main/kotlin/eu/qabatz/gradle/plugins/PipelineServicePlugin.kt`):**
- Adds `kafka-streams` dependency

**`JooqPlugin` (`src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt`):**
- Creates `jooq {}` extension
- Registers `generateJooq` task
- `generateJooq` skips when `packageName` is not set
- `generateJooq` runs when `packageName` is set and migrations exist
- Adds generated source directory to main source set
- `compileKotlin` and `compileJava` depend on `generateJooq`
- Adds jOOQ, HikariCP, Flyway, PostgreSQL dependencies

**`SecretsPlugin` (`src/main/kotlin/eu/qabatz/gradle/plugins/SecretsPlugin.kt`):**
- Adds QabatzForgejo Maven repository
- Adds `qabatz-kotlin-commons-secrets` implementation dependency
- Adds test fixtures capability for secrets

## Mocking

**Framework:** MockK v1.14.7 (available as test dependency)

**Mocking is generally not needed for Gradle plugin testing.** Gradle TestKit provides the real Gradle runtime. For unit tests of helper functions (like `buildJooqConfig`), the functions are pure and require no mocking.

**If mocking is needed for Gradle API objects in unit tests:**
```kotlin
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project

val project = mockk<Project>(relaxed = true)
every { project.extensions.getByType(any<Class<*>>()) } returns mockk(relaxed = true)
```

However, prefer functional tests with TestKit over mocked unit tests for plugin behavior.

## Coverage

**Requirements:** None enforced. No coverage plugin configured.

**Recommendation:** Add JaCoCo for coverage reporting:
```kotlin
plugins {
    jacoco
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, tasks.named("functionalTest"))
    executionData.setFrom(
        fileTree(buildDir) { include("jacoco/*.exec") }
    )
}
```

## Test Types

**Unit Tests:**
- Scope: Pure functions and utility objects
- Testable targets: `JooqPlugin.buildJooqConfig()`, `Versions` constants validation
- Location: `src/test/kotlin/eu/qabatz/gradle/`

**Functional Tests (Gradle TestKit):**
- Scope: Full plugin application and behavior in a real Gradle build
- Tests each plugin can be applied, configures the project correctly, and tasks execute
- Location: `src/functionalTest/kotlin/eu/qabatz/gradle/`

**Integration Tests:**
- Not applicable -- plugins do not make network calls or access external services at configuration time

**E2E Tests:**
- Not used (would require publishing plugins and consuming them in a real multi-module project)

## Test Coverage Gaps

**Critical: No tests exist at all.** The infrastructure is fully set up (source sets, configurations, task registration, dependencies) but no test files have been written.

**Priority 1 -- Functional tests for `LibraryPlugin`:**
- This is the base plugin applied by all others
- Tests should verify: Kotlin/JVM applied, detekt configured, ktfmt configured, directory validation works
- Files: `src/functionalTest/kotlin/eu/qabatz/gradle/LibraryPluginFunctionalTest.kt`

**Priority 2 -- Functional tests for `JooqPlugin`:**
- Most complex plugin with extension, custom task, XML generation, source set manipulation
- Tests should verify: extension defaults, task registration, skip-when-no-packageName, generated source set inclusion
- Files: `src/functionalTest/kotlin/eu/qabatz/gradle/JooqPluginFunctionalTest.kt`

**Priority 3 -- Unit test for `JooqPlugin.buildJooqConfig()`:**
- Pure function producing XML string, easy to test
- Files: `src/test/kotlin/eu/qabatz/gradle/plugins/JooqPluginTest.kt`

**Priority 4 -- Functional tests for dependency-adding plugins:**
- `TestingPlugin`, `ObservabilityPlugin`, `KafkaConsumerPlugin`, `KafkaProducerPlugin`, `PipelineServicePlugin`
- Verify each plugin adds the expected dependencies to the correct configuration
- Can use `GradleRunner` with `dependencies` task and check output

**Priority 5 -- Functional test for `SecretsPlugin`:**
- Requires Forgejo credentials or mocked repository
- Verify repository is added and dependencies are configured (may need `--dry-run` or dependency resolution check that tolerates missing repo)

---

*Testing analysis: 2026-03-27*
