# Codebase Concerns

**Analysis Date:** 2026-03-27

## Tech Debt

**Duplicated version constants between Versions.kt and catalog/libs.versions.toml:**
- Issue: Every dependency version is declared twice -- once as a Kotlin `const` in `src/main/kotlin/eu/qabatz/gradle/Versions.kt` and once in `catalog/libs.versions.toml`. The KDoc on `Versions.kt` acknowledges this ("update BOTH this file and the catalog") but there is no automated check to verify they stay in sync.
- Files: `src/main/kotlin/eu/qabatz/gradle/Versions.kt`, `catalog/libs.versions.toml`
- Impact: A version bump in one file but not the other causes silent inconsistency. Plugins would resolve one version at build time while the published catalog tells consumers to use a different version. This is especially dangerous for transitive dependency conflicts (e.g., jOOQ codegen at 3.20.11 vs consumer runtime at a different version).
- Fix approach: Either (a) generate `Versions.kt` from the TOML file at build time using a Gradle task, or (b) have plugins read the version catalog at apply-time via `project.extensions.getByType(VersionCatalogsExtension::class.java)` instead of hardcoded constants, or (c) add a Gradle verification task that parses both files and fails on mismatch.

**KafkaConsumerPlugin and KafkaProducerPlugin are identical:**
- Issue: `KafkaConsumerPlugin` and `KafkaProducerPlugin` have identical implementations -- both add the single dependency `org.apache.kafka:kafka-clients`. There is no behavioral difference between them.
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/KafkaConsumerPlugin.kt`, `src/main/kotlin/eu/qabatz/gradle/plugins/KafkaProducerPlugin.kt`
- Impact: Maintaining two identical plugins adds confusion. Consumers cannot tell which to choose because the effect is the same. If consumer-specific dependencies (e.g., Avro SerDes, schema registry client) or producer-specific dependencies are intended, they are missing.
- Fix approach: Either differentiate the plugins by adding consumer-specific dependencies (e.g., `apicurio-serdes-avro`, `avro`) to KafkaConsumerPlugin and producer-specific ones to KafkaProducerPlugin, or merge them into a single `qabatz.kafka` plugin until real differentiation is needed.

**Hardcoded Forgejo repository URL in SecretsPlugin:**
- Issue: `SecretsPlugin` hardcodes the Forgejo Maven registry URL (`https://git.hestia-ng.eu/api/packages/qabatz/maven`) directly in plugin source code. The `build.gradle.kts` publishing block handles this more flexibly via `findProperty("publishUrl")`, but the SecretsPlugin does not offer this override.
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/SecretsPlugin.kt` (line 17)
- Impact: Consumers in environments with a different registry (e.g., mirrored, internal proxy, or self-hosted Forgejo at a different URL) cannot override this without modifying the plugin. It also couples all consuming projects to a specific infrastructure URL.
- Fix approach: Read the URL from a Gradle property (`findProperty("qabatzRepoUrl")`) with the current URL as a fallback default, matching the pattern already used in `build.gradle.kts`.

**LibraryPlugin forces unconditional dependencies on all consumers:**
- Issue: `LibraryPlugin.apply()` unconditionally adds `kotlinx-coroutines-core` and `kotlinx-datetime` as `implementation` dependencies to every project that applies any convention plugin (since all other plugins apply `LibraryPlugin`).
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/LibraryPlugin.kt` (lines 60-67)
- Impact: Projects that do not use coroutines or datetime still pull these into their compile and runtime classpaths. This increases build times and artifact sizes. More critically, it makes an opinionated decision that every Qabatz module needs these specific libraries.
- Fix approach: Move these dependency additions to dedicated plugins (e.g., a coroutines plugin) or make them opt-in. At minimum, consider whether `api` vs `implementation` is the correct scope.

## Known Bugs

**No known bugs detected.**
- The codebase is clean of TODO/FIXME/HACK markers and the detekt report shows 0 findings.

## Security Considerations

**Empty password fallback in SecretsPlugin:**
- Risk: `SecretsPlugin` falls back to an empty string for the Forgejo password (`System.getenv("FORGEJO_TOKEN") ?: ""`). If the environment variable is not set, Gradle will attempt authentication with an empty password, which will silently fail or produce a confusing error at dependency resolution time.
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/SecretsPlugin.kt` (lines 19-20)
- Current mitigation: None. The empty string silently proceeds.
- Recommendations: Fail fast with a descriptive error if `FORGEJO_TOKEN` is not set, or log a clear warning. Consider also supporting the `forgejoToken` Gradle property (as `build.gradle.kts` does) for consistency.

**No insecure protocol protection in SecretsPlugin:**
- Risk: `build.gradle.kts` includes `isAllowInsecureProtocol = repoUrl.startsWith("http://")` which explicitly allows HTTP if the URL starts with `http://`. The `SecretsPlugin` hardcodes an HTTPS URL so this is not currently exploitable, but if the URL is ever made configurable without the same guard, credentials could be sent in cleartext.
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/SecretsPlugin.kt` (line 17), `build.gradle.kts` (line 128)
- Current mitigation: URL is hardcoded to HTTPS.
- Recommendations: If the URL becomes configurable, add the same `isAllowInsecureProtocol` guard.

**jOOQ XML config constructed via string interpolation:**
- Risk: The `buildJooqConfig()` function constructs XML by string interpolation of user-provided values (`packageName`, `scripts`, `excludes`, `directory`). If any of these contain XML special characters (`<`, `>`, `&`, `"`), the generated XML will be malformed or potentially inject unexpected elements.
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt` (lines 129-176)
- Current mitigation: Values come from Gradle properties, not untrusted user input. Exploitation risk is low but the pattern is fragile.
- Recommendations: Use proper XML escaping (e.g., `StringEscapeUtils.escapeXml11()` from Apache Commons Text, or Kotlin's XML libraries) for interpolated values.

## Performance Bottlenecks

**validateProjectStructure runs before every compileKotlin:**
- Problem: `LibraryPlugin` registers a `validateProjectStructure` task that checks for three directories and hooks it as a dependency of `compileKotlin`. This runs on every single compilation, even though directory structure changes are extremely rare.
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/LibraryPlugin.kt` (lines 28-45)
- Cause: The task has no inputs/outputs declared, so Gradle cannot skip it via up-to-date checks. It always executes.
- Improvement path: Declare task inputs (e.g., the directory paths as inputs) so Gradle's up-to-date mechanism can skip it. Alternatively, move this to a lifecycle hook (e.g., `afterEvaluate`) that runs once per configuration rather than per build.

## Fragile Areas

**Detekt config loading via Thread.currentThread().contextClassLoader:**
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/LibraryPlugin.kt` (lines 50-54)
- Why fragile: Using `Thread.currentThread().contextClassLoader.getResource()` to locate the bundled `detekt.yml` is sensitive to classloader context. In Gradle Worker API isolation, composite builds, or specific classloader configurations, this may silently return `null` (causing detekt to run without the shared config). The `if (detektConfig != null)` guard means failures are silent.
- Safe modification: Test any changes to this in a multi-project build and a composite build.
- Test coverage: No tests exist for this behavior.

**JooqPlugin XML template coupled to jOOQ codegen schema version:**
- Files: `src/main/kotlin/eu/qabatz/gradle/plugins/JooqPlugin.kt` (line 137)
- Why fragile: The XML namespace `http://www.jooq.org/xsd/jooq-codegen-3.20.0.xsd` is hardcoded to version 3.20.0 while the jOOQ dependency version is 3.20.11. Although jOOQ typically maintains backward compatibility within minor versions, a major version bump would require updating this namespace. It is easy to forget.
- Safe modification: When bumping `Versions.JOOQ`, also check whether the XSD namespace in `buildJooqConfig()` needs updating.
- Test coverage: No tests exist.

## Scaling Limits

**Single-module project structure:**
- Current capacity: The project has 10 source files and 8 plugins in a single Gradle module.
- Limit: As plugins grow in complexity or number, a flat single-module structure makes it harder to test plugins independently, manage their individual classpaths, and reason about interdependencies.
- Scaling path: Consider a multi-module layout where each plugin (or logical group of plugins) is its own subproject. This enables parallel compilation and finer-grained testing.

## Dependencies at Risk

**Detekt 2.0.0-alpha.2:**
- Risk: The project depends on an alpha pre-release of Detekt 2.x (`dev.detekt:detekt-gradle-plugin:2.0.0-alpha.2`). Alpha versions may introduce breaking API changes between releases. The Detekt 2.x API has already changed the package structure (from `io.gitlab.arturbobrecki.detekt` to `dev.detekt`).
- Impact: Any future alpha update may break `LibraryPlugin.kt`'s direct reference to `dev.detekt.gradle.extensions.DetektExtension`. The detekt config YAML format may also change.
- Files: `gradle/libs.versions.toml` (line 6), `catalog/libs.versions.toml` (line 35), `src/main/kotlin/eu/qabatz/gradle/plugins/LibraryPlugin.kt` (line 47)
- Migration plan: Track Detekt 2.0.0 stable release. When available, update and verify all config format changes. Pin to stable as soon as possible.

**Apicurio SerDes 3.0.0.M4 (in published catalog):**
- Risk: The published catalog includes `apicurio-serdes = "3.0.0.M4"` -- a milestone pre-release. Milestone releases may have API instability.
- Impact: Consuming projects using Avro SerDes may face breaking changes on update.
- Files: `catalog/libs.versions.toml` (line 14)
- Migration plan: Monitor for Apicurio 3.0.0 GA release and update promptly.

## Missing Critical Features

**No tests at all:**
- Problem: There are zero test files. The `src/test/` directory does not exist. The `src/functionalTest/kotlin/eu/qabatz/gradle/` directory exists but is empty. Despite the build.gradle.kts defining a functional test source set and wiring it into the `check` lifecycle, no tests have been written.
- Blocks: There is no automated verification that any of the 8 plugins work correctly. Plugin behavior changes (dependency version bumps, configuration changes) cannot be validated before publishing. Regressions will only be caught by downstream consumers at their build time.
- Files: `src/functionalTest/kotlin/eu/qabatz/gradle/` (empty), `src/test/` (does not exist)
- Priority: High. Gradle TestKit functional tests are the standard approach for testing convention plugins and the infrastructure is already wired up in `build.gradle.kts`.

**No CI/CD pipeline:**
- Problem: There are no CI configuration files (no `.github/workflows/`, no `Jenkinsfile`, no `.forgejo/workflows/`, no `Woodpecker` config). Publishing appears to be manual via `./gradlew publish`.
- Blocks: No automated build verification on push/PR. No automated publishing on tag/release. Manual publishing is error-prone (can publish with uncommitted changes, wrong version, etc.).

**Minimal README:**
- Problem: The README is 2 lines: a title and a one-sentence description. It provides no usage instructions, no list of available plugins, no configuration examples, no version compatibility information.
- Files: `README.md`
- Blocks: New developers or consumers have no documentation to reference. They must read source code to understand what each plugin does and how to use it.

**Incomplete .gitignore:**
- Problem: The `.gitignore` only lists `build/`, `.gradle/`, `*.iml`, `.idea/`, `out/`. It is missing entries for `.kotlin/` (Kotlin compiler sessions directory, which exists in the working tree) and common patterns like `.env`, `local.properties`, `*.log`.
- Files: `.gitignore`
- Impact: The `.kotlin/sessions/` directory could be accidentally committed. Environment files with secrets could be accidentally committed if they are ever created.

## Test Coverage Gaps

**All plugins are untested:**
- What's not tested: Plugin application, dependency resolution, task registration, task execution, detekt/ktfmt configuration, jOOQ code generation XML output, validateProjectStructure behavior, SecretsPlugin repository configuration.
- Files: All 10 source files under `src/main/kotlin/eu/qabatz/gradle/`
- Risk: Any change to plugin behavior is unverified. The `JooqPlugin` is the most complex (177 lines, XML generation, task configuration) and has the highest regression risk. The `LibraryPlugin` applies to all downstream projects and any misconfiguration affects the entire platform.
- Priority: High. Start with functional tests for `LibraryPlugin` (base for all others) and `JooqPlugin` (most complex). Use Gradle TestKit as already configured in `build.gradle.kts`.

---

*Concerns audit: 2026-03-27*
