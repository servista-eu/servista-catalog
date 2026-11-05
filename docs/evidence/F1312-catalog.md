# F1312 #1312 — catalog car: `gradle-shadow` alias (servista-catalog 0.96.0 -> 0.97.0)

Measured 2026-11-05 (UTC 22:46 for the currency reads). Parent commit `154a1be` (origin/main,
`version=0.96.0`). Track EP29, row #1312 D1.

⛔ **No publish, no tag, no re-pin.** This car lands source on `main` only. The publish of 0.97.0 is
a separate, approved act after it lands. No consumer is re-pinned, and no `servista-build-logic`
edit is made. `catalog/currency-markers.toml`, `CLAUDE.md`, `README.md`, `DISTRIBUTION.md` and
`build.gradle.kts` are untouched.

## Change

`catalog/libs.versions.toml`, `[libraries]`, after `gradle-ktfmt`:

```diff
 gradle-ktfmt = { module = "com.ncorti.ktfmt.gradle:plugin", version.ref = "ktfmt" }
+gradle-shadow = { module = "com.gradleup.shadow:shadow-gradle-plugin", version.ref = "shadow" }
 gradle-jooq-codegen = { module = "org.jooq:jooq-codegen-gradle", version.ref = "jooq" }
```

`git diff origin/main -- catalog/libs.versions.toml`: one added line, zero removed. No `[versions]`
line changed. The existing `shadow = "9.6.1"` key (`[versions]`) is reused; no second key was added.

`gradle.properties`:

```diff
+# 0.96.0 -> 0.97.0 on 2026-11-05: row #1312 -- new alias gradle-shadow (com.gradleup.shadow:shadow-gradle-plugin, version.ref shadow, 9.6.1, already the latest stable). No ref moves; servista-kotlin-commons stays 0.45.0, servista-service-runtime 0.58.0, avro-schemas 0.10.0.
-version=0.96.0
+version=0.97.0
```

## Currency of `com.gradleup.shadow:shadow-gradle-plugin`

Maven Central, `GET https://repo1.maven.org/maven2/com/gradleup/shadow/shadow-gradle-plugin/maven-metadata.xml`:

- `<latest>9.6.1</latest>`
- `<release>9.6.1</release>`
- `GET .../9.6.1/shadow-gradle-plugin-9.6.1.pom` -> HTTP 200

Gradle plugin portal, `GET https://plugins.gradle.org/m2/com/gradleup/shadow/shadow-gradle-plugin/maven-metadata.xml`:

- `<latest>9.6.1</latest>`
- `<release>9.6.1</release>`
- `GET .../9.6.1/shadow-gradle-plugin-9.6.1.pom` -> HTTP 200 after redirect (`-L`; the portal answers 303 first)

Both `<release>` values are **9.6.1**. The pin stays at `shadow = "9.6.1"`, and no third-party ref moves,
because the pin is already current and a move would make already-published POMs stale and oblige a
library republish.

## `dependency-currency`

Command: `node <servista-planning>/tools/check-dependency-currency.mjs` (the gate line is
`node tools/gate.mjs --only dependency-currency`).

- **Base** (`154a1be`, catalog and markers read from `git show HEAD:`):
  `✗ 6 finding(s) — 6 currency, 0 stale batch marker(s), 0 ref drift, 0 inline-vs-catalog, 0 advisory-bearing.`
  Batch markers `ktor`, `ktor-plugin`, `oracle-jdbc` are EXPIRED (written against 0.95.0; 0.96.0 has shipped).
  The six Class I movers: `ktor 3.6.0→3.6.1`, `ktor-plugin 3.6.0→3.6.1`, `oracle-jdbc 23.26.3.0.0→23.26.4.0.0`,
  `netty 4.2.19.Final→4.2.20.Final`, `dokka 2.2.0→2.3.0`, `libphonenumber 9.0.42→9.0.43`.
- **Head** (working tree with this change): the same `✗ 6 finding(s) …` line and the same six refs.
- Gate trigger, base: `dependency-currency — FAILED (exit 1)`, `unchanged — failed here since 2026-11-03 15:03 (19 runs)`.

**Verdict: the red is inherited, not introduced.** It is the expired batch markers and the Class I
movers that row #1327 owns. The added alias names `shadow`, which is already current, so it adds no finding.
`currency-markers.toml` was not edited, as the brief requires.

## `distribution-boundary`

`node <servista-planning>/tools/check-distribution-boundary.mjs` -> exit 0, `✓ 0 findings.`

Caveat: this check reads the estate's `libs/servista-catalog` (version 0.96.0), not this worktree's head.
The alias it would judge is a public Gradle plugin, which is `distributable`, so it should have nothing to say.
The head verdict comes from run-job after the commit.

## `catalog-pins`

`node <servista-planning>/tools/check-catalog-pins.mjs` -> exit 0:

```
✓ catalog-pins: every live Gradle build pins the current catalog, no eu.servista coordinate carries a literal version, and every catalog ref names the version its library actually publishes.
```

Caveat: this check reads `libs/servista-catalog/gradle.properties` from the estate root, which still says
`0.96.0`. It does not see this worktree's `0.97.0`, so the release-commit allowance line the brief expects
does not appear here. The head verdict comes from run-job after the commit.

## `published-poms`

`node <servista-planning>/tools/check-published-poms.mjs --catalog catalog/libs.versions.toml` -> exit 2, **cannot run**
(a refusal, not a pass). The Forgejo registry answered HTTP 502 for 12 POMs of `servista/servista-service-runtime-database`
(0.10.0 to 0.21.0), so the check reports them as unread rather than clean. The controls fired 9/9, and every governed
pair it did read was clean. Re-run after the registry recovers.

## `tracker`

`node <servista-planning>/tools/check-tracker.mjs` -> exit 1 (red), **not caused by this car**. The car touches no tracker
file and no issue. The two reported problems:

```
⚠ check-tracker: generated files are STALE (839 newer comment(s)/58 newer row(s) since 2026-11-04T22:24:18Z); not a finding — the nightly sweep re-renders them. Hand-edit check: 3 file(s) match their own render.
✗ check-tracker: 1 structural problem(s).
  • #1333 (D1333) carries a bucket: label but no directive places it — add one to the fragment, or drop the label if it does not belong in the backlog
```

The stale-generated line is not a finding. The red is the `#1333` bucket-label problem, which is in another row's
fragment and not this car's. Not verified against a clean base run.

## `rebuild-set`

Not applicable: no `[versions]` ref moved, so there is no rebuild set. `git diff origin/main -- catalog/libs.versions.toml`
has no changed `[versions]` line.

## Not done, and why

- **No `./gradlew publish`, no tag**: the publish is a separate approved act after this lands (a car publishes nothing).
- **No re-pin** of any consumer; **no `servista-build-logic` edit**.
- **No marker edit** in `catalog/currency-markers.toml`: row #1327 owns the expired markers.
- **No test source** added or run: the build's gate line is `ktfmtCheck detekt test`, and this repository has no test source.
- **Gradle was not run locally** (no Gradle on the Mac; the gate runs on the cluster).
- **Gate trigger side effect**: one `gate.mjs --only dependency-currency --at <worktree>` run was made. Its output
  reports a remote run judged on a `car-scratch/gate-…` ref (run `20261105T224611Z-8xfbm2`). I ran no `git push`
  myself; what the trigger published remotely is for the gate owner to confirm.
