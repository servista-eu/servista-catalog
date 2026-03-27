# Phase 1: Strip Plugin Sources - Context

**Gathered:** 2026-03-27
**Status:** Ready for planning

<domain>
## Phase Boundary

Remove all convention plugin code, extensions, bundled configuration, AND simplify the build configuration — leaving only what is needed to publish the version catalog. This phase merges the original Phase 1 (Strip Plugin Sources) and Phase 2 (Simplify Build) because they are tightly coupled: deleting plugin source files without also removing their build.gradle.kts registrations breaks compilation.

**Requirements covered:** CLN-01, CLN-02, CLN-03, CLN-04, CLN-05

</domain>

<decisions>
## Implementation Decisions

### Phase Merge
- **D-01:** Merge original Phase 1 and Phase 2 into a single phase. Reason: Phase 1's success criteria requires "project still compiles after all deletions," but the `gradlePlugin` block in build.gradle.kts references the deleted plugin classes. Removing source files without their build registrations creates a broken intermediate state. CLN-01 through CLN-05 are all handled together.

### Deletion Scope
- **D-02:** Delete the entire `src/` directory tree (including `src/main/kotlin/`, `src/main/resources/`, and empty `src/functionalTest/`). After stripping all plugin code, no source files remain — this project becomes a pure catalog publisher.
- **D-03:** Remove plugin-related build configuration from `build.gradle.kts`: the `gradlePlugin` block, `kotlin-dsl` plugin, `java-gradle-plugin` plugin, functional test source set configuration, and plugin-only dependencies. Retain only version catalog publishing logic.

### JooqPlugin Migration
- **D-04:** No migration doc action needed. The JooqPlugin migration guide already exists at `qabatz-kotlin-ktor/TODO-BUILD-LOGIC.md` — comprehensive doc covering the 177-line plugin logic, XML config template, known issues, and migration approach. Safe to delete source.

### Claude's Discretion
- Order of deletions within the phase (source files first, then build config, or interleaved)
- Whether to verify compilation incrementally or only at the end

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Build Configuration
- `build.gradle.kts` — Contains `gradlePlugin` block, plugin registrations, all build dependencies to strip
- `settings.gradle.kts` — Root project name declaration (unchanged in this phase)
- `gradle.properties` — Group and version (unchanged in this phase)

### Source Files to Remove
- `src/main/kotlin/eu/qabatz/gradle/plugins/` — 8 plugin classes (LibraryPlugin, TestingPlugin, ObservabilityPlugin, KafkaConsumerPlugin, KafkaProducerPlugin, PipelineServicePlugin, JooqPlugin, SecretsPlugin)
- `src/main/kotlin/eu/qabatz/gradle/Versions.kt` — Centralized version constants
- `src/main/kotlin/eu/qabatz/gradle/JooqExtension.kt` — Gradle extension for jOOQ plugin
- `src/main/resources/detekt/detekt.yml` — Bundled detekt configuration
- `src/functionalTest/` — Empty functional test source set directory

### Requirements
- `.planning/REQUIREMENTS.md` §Cleanup — CLN-01 through CLN-05 requirement definitions

### Migration Reference (do not modify)
- `../qabatz-kotlin-ktor/TODO-BUILD-LOGIC.md` — JooqPlugin migration guide (already exists, no action needed)

</canonical_refs>

<code_context>
## Existing Code Insights

### Files to Delete (11 total)
- 8 plugin classes in `src/main/kotlin/eu/qabatz/gradle/plugins/`
- `Versions.kt` and `JooqExtension.kt` in `src/main/kotlin/eu/qabatz/gradle/`
- `detekt.yml` in `src/main/resources/detekt/`
- Empty `src/functionalTest/` directory

### Build Config to Strip
- `gradlePlugin { plugins { ... } }` block registering all 8 plugins
- `kotlin-dsl` plugin application
- `java-gradle-plugin` plugin application
- Functional test source set configuration
- Plugin-only dependencies (kotlin-gradle-plugin, detekt, ktfmt, kotlinx-serialization, etc.)
- `gradleTestKit()` and test dependencies tied to plugin testing

### What Stays
- `catalog/libs.versions.toml` — The published version catalog (sole artifact)
- `gradle/libs.versions.toml` — Internal build catalog (may need pruning but that's Phase 3 territory)
- `version-catalog` plugin and `catalog { }` block in build.gradle.kts
- `maven-publish` plugin and publishing configuration
- `gradle.properties` (group + version)
- `settings.gradle.kts` (project name)

</code_context>

<specifics>
## Specific Ideas

No specific requirements — open to standard approaches

</specifics>

<deferred>
## Deferred Ideas

### Roadmap Update Required
- The merge of Phase 1 and Phase 2 requires updating ROADMAP.md: renumber subsequent phases (old Phase 3 becomes Phase 2, etc.) and update requirement traceability. This should happen before planning.

</deferred>

---

*Phase: 01-strip-plugin-sources*
*Context gathered: 2026-03-27*
