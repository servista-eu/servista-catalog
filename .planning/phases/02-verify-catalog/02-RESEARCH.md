# Phase 2: Verify Catalog - Research

**Researched:** 2026-03-27
**Domain:** Gradle Version Catalog (TOML format), catalog verification, documentation correction
**Confidence:** HIGH

## Summary

The published version catalog (`catalog/libs.versions.toml`) is structurally sound. Gradle's `generateCatalogAsToml` task parses it cleanly and produces valid output. The catalog contains **35 versions, 75 library aliases, and 6 plugin aliases** -- significantly fewer than the ~140 library count stated in REQUIREMENTS.md and PROJECT.md, confirming the CONTEXT.md decision (D-01, D-02) to correct these counts.

No orphaned version refs, no missing version refs, no duplicate entries. All 35 version keys are referenced by at least one library or plugin. The `outgoingVariants` task confirms the build produces exactly one variant: `versionCatalogElements` with category `platform` and usage `version-catalog` -- confirming the catalog is the sole artifact.

**Primary recommendation:** Audit the naming conventions (a few aliases use shortened forms vs. exact artifact names -- this is intentional and follows Gradle best practices), verify the section grouping is complete and consistent, then update REQUIREMENTS.md and PROJECT.md with the actual counts.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** The ~140 library alias count in requirements was an overestimate. The actual catalog has ~39 versions, ~75 library aliases, and 6 plugin aliases. Accept the current catalog as the correct baseline.
- **D-02:** Update PROJECT.md and REQUIREMENTS.md to reflect the actual counts (~39 versions, ~75 libraries, 6 plugins) as part of this phase.
- **D-03:** TOML audit + Gradle task. Audit `catalog/libs.versions.toml` structure (valid version refs, no orphaned entries, consistent naming), run `generateCatalogAsToml` to confirm Gradle parses it cleanly, and count entries to establish the actual baseline.
- **D-04:** Fix all issues found during verification within this phase. This includes orphaned version refs, naming inconsistencies, missing entries, and any structural problems. Do not defer fixes.
- **D-05:** Fix naming inconsistencies found during audit (e.g., aliases that don't match common usage patterns). Ensure sections are consistently ordered. Keep the existing section-comment-based grouping -- do not enforce strict alphabetical ordering within sections.

### Claude's Discretion
- Whether to add bundle definitions to the catalog. Add bundles only if the audit reveals obvious groupings that consumers consistently use together. Otherwise, keep the catalog as a flat list.
- Order of verification steps (TOML audit vs Gradle task first)
- Specific naming fixes -- decide which alias names need correction based on audit findings

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CAT-01 | Version catalog (`libs.versions.toml`) is the sole published artifact | Verified via `outgoingVariants` -- only `versionCatalogElements` variant exists. `build.gradle.kts` contains only `maven-publish` + `version-catalog` plugins. |
| CAT-02 | Catalog includes all current dependency versions (~40 versions, ~140 libraries, 6 plugins) | Actual counts: 35 versions, 75 libraries, 6 plugins. CONTEXT D-01/D-02 requires correcting documentation to match reality. |
| CAT-03 | Catalog includes qabatz-kotlin-commons library entries | Verified: 9 entries under `# Qabatz Commons` section (core + 6 adapters + kafka-avro + secrets). |
| CAT-04 | Catalog includes qabatz-kotlin-ktor library entries | Verified: 1 entry (`qabatz-ktor`) under `# Qabatz Ktor` section. |
</phase_requirements>

## Catalog Audit Findings

### Structural Integrity (all PASS)

| Check | Result | Details |
|-------|--------|---------|
| TOML syntax | PASS | Gradle parses cleanly, `generateCatalogAsToml` succeeds |
| Orphaned version refs | PASS | All 35 version keys are referenced |
| Missing version refs | PASS | All `version.ref` values resolve to defined versions |
| Duplicate entries | PASS | No duplicates in versions, libraries, or plugins |
| Sole artifact | PASS | Only `versionCatalogElements` variant produced |
| Section grouping | PASS | 12 comment-delimited sections in `[libraries]`, logically organized |

### Exact Counts (verified baseline)

| Section | Count | Notes |
|---------|-------|-------|
| Versions | 35 | Not 39 as estimated in CONTEXT -- precise count is 35 |
| Libraries | 75 | Matches CONTEXT estimate |
| Plugins | 6 | Matches CONTEXT and REQUIREMENTS |
| Bundles | 0 | None defined (see discretion analysis below) |

### Library Section Grouping

The catalog has 12 well-organized sections:

| Section Comment | Library Count | Content |
|-----------------|---------------|---------|
| Ktor Server | 12 | All ktor-server-* modules + serialization |
| Ktor Client | 3 | ktor-client-core, cio, content-negotiation |
| Dependency Injection | 2 | koin-ktor, koin-test |
| Database | 10 | jOOQ (6), Flyway (2), HikariCP, PostgreSQL |
| Kafka | 5 | kafka-clients, streams, streams-test, avro, apicurio |
| Infrastructure Clients | 7 | openfga, immudb4j, jedis, lettuce, nimbus, konform, tink |
| Qabatz Commons | 9 | All qabatz-kotlin-commons modules |
| Qabatz Ktor | 1 | qabatz-kotlin-ktor |
| Observability | 10 | OpenTelemetry (8), Micrometer (2) |
| Logging | 5 | kotlin-logging, logback, logstash-encoder, janino, slf4j |
| Kotlin Extensions | 5 | kotlinx-serialization, coroutines (3), datetime |
| Testing | 6 | junit5, mockk, testcontainers (3), kotest |

### Naming Convention Analysis

Aliases follow a **consistent abbreviated pattern** where the group prefix (e.g., `io.opentelemetry` -> `otel`) is shortened for ergonomics. This follows the Gradle blog best practice of using the project group as the first segment without repetition.

**Aliases that diverge from artifact name (all intentional):**

| Alias | Artifact | Rationale |
|-------|----------|-----------|
| `jooq-core` | `jooq` | Adds `-core` suffix for clarity alongside `jooq-kotlin`, `jooq-codegen` |
| `flyway-postgresql` | `flyway-database-postgresql` | Shortened -- `database` is redundant in context |
| `hikari` | `HikariCP` | Lowercase kebab-case per Gradle convention |
| `postgresql-jdbc` | `postgresql` | Adds `-jdbc` to distinguish from PostgreSQL server |
| `kafka-streams-test` | `kafka-streams-test-utils` | Shortened -- `utils` suffix dropped |
| `apicurio-serdes-avro` | `apicurio-registry-serdes-avro-serde` | Heavily shortened -- `registry` and `-serde` suffix dropped |
| `otel-*` | `opentelemetry-*` | Consistent `otel` prefix for all OpenTelemetry entries |
| `otel-agent` | `opentelemetry-javaagent` | Different group (`io.opentelemetry.javaagent`) |
| `kotlin-logging` | `kotlin-logging-jvm` | Drops platform suffix -- JVM is implicit |
| `logback` | `logback-classic` | Shortened -- `classic` is the standard variant |
| `micrometer-prometheus` | `micrometer-registry-prometheus` | Shortened -- `registry` is redundant |
| `junit5` | `junit-jupiter` | Uses version label, not artifact name |
| `testcontainers-core` | `testcontainers` | Adds `-core` suffix for clarity alongside tc-postgresql, tc-kafka |
| `kotest-assertions` | `kotest-assertions-core` | Shortened -- `-core` suffix dropped |
| `qabatz-commons-*` | `qabatz-kotlin-commons-*` | Drops `kotlin` -- implied in this ecosystem |
| `qabatz-ktor` | `qabatz-kotlin-ktor` | Drops `kotlin` -- same pattern as commons |

**Assessment:** All divergences follow reasonable patterns. No naming fixes needed -- the shortened aliases are more ergonomic for consumers and follow the Gradle blog's best practices (omit implicit terms, use project group as first segment).

### Versions Section Ordering

The `[versions]` section uses a **thematic/importance ordering** (not alphabetical):
1. Core frameworks: kotlin, ktor, koin
2. Database: jooq, flyway, hikari, postgresql-jdbc
3. Messaging: kafka, avro, apicurio-serdes
4. Infrastructure: openfga, immudb4j, jedis, lettuce, nimbus-jose-jwt
5. Observability: otel, otel-agent, micrometer
6. Testing: mockk, testcontainers, junit5, kotest
7. Kotlin extensions: kotlinx-serialization, kotlinx-coroutines, kotlinx-datetime
8. Logging: kotlin-logging, logback, logstash-logback-encoder, janino, slf4j
9. Tooling: detekt, konform, tink
10. Internal: qabatz-kotlin-commons, qabatz-kotlin-ktor

This ordering does not exactly match the library section grouping order. The versions section mixes testing frameworks mid-list with kotlin extensions and logging after them, while the library sections have a different flow. Per D-05, this is acceptable (no strict alphabetical required), but the versions could be reordered to match the library section flow for consistency.

### Bundle Analysis (Discretion Area)

Examining common consumer usage patterns, potential bundles could be:

| Potential Bundle | Libraries | Use Case |
|------------------|-----------|----------|
| `ktor-server` | ktor-server-core, ktor-server-netty, ktor-server-content-negotiation, ktor-serialization-kotlinx-json | Every Ktor server needs these |
| `jooq` | jooq-core, jooq-kotlin, jooq-kotlin-coroutines | Basic jOOQ Kotlin setup |
| `jooq-codegen` | jooq-codegen, jooq-meta-extensions | Code generation dependencies |
| `database` | hikari, postgresql-jdbc, flyway-core, flyway-postgresql | Full database stack |
| `otel` | otel-api, otel-sdk, otel-exporter-otlp, otel-sdk-autoconfigure | Standard OTel setup |
| `testing` | junit5, mockk, kotest-assertions | Core test dependencies |

**Recommendation: Do NOT add bundles in this phase.** Bundles are syntactic sugar for consumers but add maintenance burden. Since this is a verification phase, adding bundles would be a feature addition beyond scope. If bundles are desired, they should be a separate future effort after verifying actual consumer usage patterns.

## Architecture Patterns

### Verification Workflow

The verification should follow this sequence:

1. **TOML structural audit** -- Check syntax, refs, duplicates, naming (already done in research, planner can codify as verification steps)
2. **Gradle task verification** -- `./gradlew generateCatalogAsToml` confirms Gradle parses the catalog
3. **Artifact verification** -- `./gradlew outgoingVariants` confirms sole artifact
4. **Count establishment** -- Document precise counts as the new baseline
5. **Documentation update** -- Correct REQUIREMENTS.md and PROJECT.md with actual counts

### Documentation Files to Update

| File | What to Change | Section |
|------|----------------|---------|
| `.planning/REQUIREMENTS.md` | CAT-02: Change "~40 versions, ~140 libraries" to "35 versions, 75 libraries" | Line 14 |
| `.planning/PROJECT.md` | Change "~40 versions, ~140 library aliases" to "35 versions, 75 library aliases" | Requirements > Validated section, line 15 |

### Recommended Project Structure (no changes needed)

```
catalog/
  libs.versions.toml     # Published version catalog (75 libraries, 6 plugins)
gradle/
  libs.versions.toml     # Internal catalog (empty -- no build dependencies)
build.gradle.kts         # maven-publish + version-catalog only
settings.gradle.kts      # Root project name
gradle.properties        # Group + version
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| TOML validation | Custom TOML parser/validator | `generateCatalogAsToml` Gradle task | Gradle already validates syntax, ref resolution, and alias naming |
| Artifact verification | Manual POM/metadata inspection | `outgoingVariants` Gradle task | Authoritative -- shows exactly what Gradle produces |
| Entry counting | Manual line counting | `awk` extraction from TOML sections | Precise, reproducible, scriptable |

## Common Pitfalls

### Pitfall 1: Overcounting Versions
**What goes wrong:** CONTEXT.md estimated ~39 versions but actual count is 35. Easy to miscount when comments and blank lines are present.
**Why it happens:** Manual counting of a 147-line file with section headers and comments.
**How to avoid:** Use `awk` to extract only entry lines from each section.
**Warning signs:** Counts don't match between sources.

### Pitfall 2: Confusing Internal vs. Published Catalog
**What goes wrong:** Editing `gradle/libs.versions.toml` instead of `catalog/libs.versions.toml`.
**Why it happens:** Two files with the same name in different directories.
**How to avoid:** `gradle/libs.versions.toml` is the internal catalog (empty). `catalog/libs.versions.toml` is the published one. Always specify full path.
**Warning signs:** Changes to internal catalog have no effect on published artifact.

### Pitfall 3: Naming Convention False Positives
**What goes wrong:** Flagging intentional alias shortenings (e.g., `otel-*` vs `opentelemetry-*`) as "naming inconsistencies."
**Why it happens:** Comparing alias names 1:1 against artifact names.
**How to avoid:** Evaluate against Gradle's best practices -- shortened aliases are recommended when the full artifact name would be redundant or overly verbose.
**Warning signs:** Proposing renames that would make consumer code more verbose without adding clarity.

### Pitfall 4: Documentation Count Mismatch
**What goes wrong:** Updating REQUIREMENTS.md but forgetting PROJECT.md (or vice versa).
**Why it happens:** Counts appear in multiple documentation files.
**How to avoid:** Update both files in the same plan/task.
**Warning signs:** `grep -r "~140"` still finds matches after update.

## Code Examples

### Counting Catalog Entries (verified pattern)

```bash
# Count versions
awk '/^\[versions\]/{p=1;next} /^\[/{p=0} p && /^[a-zA-Z]/' catalog/libs.versions.toml | wc -l
# Result: 35

# Count libraries
awk '/^\[libraries\]/{p=1;next} /^\[/{p=0} p && /^[a-zA-Z]/' catalog/libs.versions.toml | wc -l
# Result: 75

# Count plugins
awk '/^\[plugins\]/{p=1;next} /^\[/{p=0} p && /^[a-zA-Z]/' catalog/libs.versions.toml | wc -l
# Result: 6
```

### Checking for Orphaned Version Refs

```bash
# Find version keys not referenced by any library or plugin
awk '/^\[versions\]/{p=1;next} /^\[/{p=0} p && /^[a-zA-Z]/' catalog/libs.versions.toml \
  | sed 's/ *=.*//' \
  | while read ver; do
      if ! grep -q "version.ref = \"$ver\"" catalog/libs.versions.toml; then
        echo "ORPHANED: $ver"
      fi
    done
```

### Verifying Sole Artifact

```bash
# Confirm only version catalog variant is produced
./gradlew outgoingVariants 2>&1 | grep -A5 "Variant "
# Should show only: versionCatalogElements
```

### Gradle Task for Catalog Validation

```bash
# Parses TOML, validates all refs, generates normalized output
./gradlew generateCatalogAsToml
# Output: build/version-catalog/libs.versions.toml
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `Versions.kt` compile-time constants | Published version catalog TOML | Phase 1 (just completed) | Consumers use `libs.xxx` accessors instead of hardcoded strings |
| 8 convention plugins + catalog | Catalog-only project | Phase 1 (just completed) | Simplified build, single artifact |
| `org.testcontainers:postgresql` | `org.testcontainers:testcontainers-postgresql` | Testcontainers 2.0 | Artifact names changed in TC 2.0 -- catalog already uses correct names |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | None -- no test infrastructure exists |
| Config file | None |
| Quick run command | `./gradlew generateCatalogAsToml` |
| Full suite command | `./gradlew generateCatalogAsToml outgoingVariants` |

### Phase Requirements to Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| CAT-01 | Sole published artifact | smoke | `./gradlew outgoingVariants 2>&1 \| grep -c "Variant "` (expect: 1) | N/A (CLI) |
| CAT-02 | Correct entry counts | smoke | `awk` count commands (see Code Examples) | N/A (CLI) |
| CAT-03 | Commons entries present | smoke | `grep -c "qabatz-commons" catalog/libs.versions.toml` (expect: 9) | N/A (CLI) |
| CAT-04 | Ktor entries present | smoke | `grep -c "qabatz-ktor" catalog/libs.versions.toml` (expect: 1) | N/A (CLI) |

### Sampling Rate
- **Per task commit:** `./gradlew generateCatalogAsToml`
- **Per wave merge:** Full Gradle task + count verification
- **Phase gate:** All 4 smoke checks pass

### Wave 0 Gaps
None -- this phase uses Gradle tasks and shell commands for verification, not a test framework. No test infrastructure needed.

## Open Questions

1. **Versions section ordering: align with library sections?**
   - What we know: Versions are in thematic/importance order that does not match the library section order (e.g., testing versions appear before kotlin-extensions versions, but testing libraries appear after)
   - What's unclear: Whether reordering versions to match library sections improves maintainability enough to justify the diff
   - Recommendation: Leave as-is unless the planner identifies a strong reason to reorder. D-05 says "ensure sections are consistently ordered" but also "keep existing section-comment-based grouping." The versions section has no comments/sub-sections, so "section ordering" applies to the `[libraries]` section primarily.

2. **Precise version count discrepancy with CONTEXT.md**
   - What we know: CONTEXT.md says "~39 versions" but actual count is 35
   - What's unclear: Nothing -- this is just a counting correction
   - Recommendation: Use the precise count (35) in documentation updates, not the estimate

## Sources

### Primary (HIGH confidence)
- Direct audit of `catalog/libs.versions.toml` -- structural analysis, entry counting, ref validation
- `./gradlew generateCatalogAsToml` -- Gradle 9.3.1 parses and regenerates catalog (BUILD SUCCESSFUL)
- `./gradlew outgoingVariants` -- confirms single `versionCatalogElements` variant
- `build.gradle.kts` -- confirms only `maven-publish` + `version-catalog` plugins applied

### Secondary (MEDIUM confidence)
- [Gradle Blog: Best Practices for Naming Version Catalog Entries](https://blog.gradle.org/best-practices-naming-version-catalog-entries) -- alias naming conventions
- [Gradle Docs: Version Catalogs](https://docs.gradle.org/current/userguide/version_catalogs.html) -- TOML format specification, bundles, publishing
- [Maven Central: testcontainers-postgresql 2.0.3](https://mvnrepository.com/artifact/org.testcontainers/testcontainers-postgresql/2.0.3) -- verified artifact name change in TC 2.0

## Project Constraints (from CLAUDE.md)

- **GSD Workflow Enforcement:** All edits must go through GSD commands (`/gsd:quick`, `/gsd:execute-phase`, etc.)
- **Backward compatibility:** Consuming projects reference `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0` -- no coordinate changes in this phase
- **Three-file version sync:** `Versions.kt`, `catalog/libs.versions.toml`, and `gradle/libs.versions.toml` must stay synchronized -- however `Versions.kt` was removed in Phase 1 and `gradle/libs.versions.toml` is empty, so only `catalog/libs.versions.toml` matters now
- **Gradle wrapper:** Always use `./gradlew`, not system Gradle
- **Kotlin DSL:** Build scripts use Kotlin DSL (`.kts`)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- no stack changes needed, existing setup is correct
- Architecture: HIGH -- catalog structure verified via Gradle tasks
- Pitfalls: HIGH -- identified through direct audit, not speculation
- Naming conventions: HIGH -- verified against Gradle official blog post

**Research date:** 2026-03-27
**Valid until:** 2026-04-27 (stable -- version catalog format is not changing rapidly)
