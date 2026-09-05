## Project

**servista-catalog**

A pure Gradle version catalog providing centralized dependency version alignment for all servista Kotlin projects. Published as `eu.servista:servista-catalog` to the Forgejo Maven registry under the **`servista`** package owner, consumed by servista-kotlin-commons (root + build-logic), servista-kotlin-commons-infra, servista-service-runtime, and all servista services.

⛔ **The version this repository publishes is in `gradle.properties`, and it is deliberately not repeated here.** It has moved nine times since this file first named it and every copy of it in prose went stale silently. `node tools/check-catalog-pins.mjs` in `servista-planning` reads it from that file — *not typed* — and asserts that every live Gradle build pins it.

**Core Value:** Single source of truth for dependency versions across the entire servista ecosystem — preventing version drift and dependency conflicts between projects.

### Constraints

- **Forgejo credentials**: Registry requires `FORGEJO_USER`/`FORGEJO_TOKEN` for publish and resolve
- **No source code**: Pure catalog — no Kotlin, no convention plugins, no compilation
- **Build-logic sharing**: The catalog is imported by both main builds and their `build-logic` included builds via `versionCatalogs { create("libs") { from(...) } }` in settings

## Technology Stack

## Languages
- TOML - Version catalog definition (`catalog/libs.versions.toml`)

## Runtime
- Gradle 9.3.1 (via wrapper)

## Build System
- `maven-publish` + `version-catalog` plugins
- `catalog/libs.versions.toml` — **The published artifact** consumed by all downstream projects
- `gradle.properties` — Group (`eu.servista`) and the published `version`, which is the single
  source of truth for it. ⛔ **Immutable from 0.2.0: published once, never `DELETE`d, never
  re-uploaded. Forgejo refuses a re-upload `409` — measured 2026-09-03 with a control.** A release
  is a version bump, and a catalog with a wrong alias costs a patch release rather than an edit.

## Configuration
- `FORGEJO_USER` / `FORGEJO_TOKEN` — Maven registry authentication
- `publishUrl` — Override Maven publish URL (default: `https://git.hestia-ng.eu/api/packages/servista/maven`)
- `forgejoUser` / `forgejoToken` / `publishToken` — Alternative Gradle properties

## Conventions

## Version Catalog Structure
- `[versions]` section: version strings keyed by dependency short-name (e.g., `ktor = "3.4.3"`)
- `[libraries]` section: module coordinates referencing version keys via `version.ref`
- `[plugins]` section: plugin IDs referencing version keys
- Naming: lowercase kebab-case for version keys (e.g., `postgresql-jdbc`, `aws-sdk`, `otel-agent`)
- Servista internal libraries use `servista-` prefix in keys
- Gradle plugin artifacts prefixed with `gradle-` (e.g., `gradle-kotlin`, `gradle-detekt`) for build-logic consumption

## Dependency Management
- `catalog/libs.versions.toml` is the **single source of truth** for all dependency versions
- When bumping versions, only this file needs updating
- Consuming projects pull versions via:
  `versionCatalogs { create("libs") { from("eu.servista:servista-catalog:<version>") } }` — the
  version being whatever `gradle.properties` currently publishes. ⛔ **Every live Gradle build must
  pin the CURRENT one, in the same sitting as the release**, and 13 of the pin sites are in a
  `build-logic` COMPOSITE BUILD whose failure names a *plugin* rather than a version.
  `node tools/check-catalog-pins.mjs` is what says so; under immutable versions a repository nobody
  re-pins keeps working, for ever, on the old catalog.
- After modifying, must republish since Forgejo doesn't support overwriting Maven artifacts

## Architecture

## What the catalog pins

⛔ **This section used to list every version, and every one of them was wrong.** Measured
2026-09-05: it claimed commons/service-runtime/avro-schemas at `0.2.0` (they were `0.5.0`, `0.7.0`
and `0.3.0`) and Kafka at 4.2.0 against a `4.3.1` ref — a stale inventory that reads as
authoritative because it is in a file every session loads. ⭐ **A quantity that varies is a
COMMAND, not a sentence.**

```bash
# every version this catalog states, from the file that states it
sed -n '/^\[versions\]/,/^\[libraries\]/p' catalog/libs.versions.toml
```

Categories, which do not go stale: the servista libraries (commons, service-runtime's core plus its
ten slices, avro-schemas); Kotlin, Ktor and Koin; database (jOOQ, Flyway, HikariCP, PostgreSQL and Oracle JDBC); messaging (Kafka, Avro);
cache; observability (OpenTelemetry, Micrometer); security (Nimbus JOSE+JWT, Tink, BouncyCastle,
OpenFGA); cloud (AWS SDK); testing (JUnit, Kotest, Testcontainers, MockK); analysis (Detekt, Dokka,
ktfmt); and the Gradle plugin artifacts that `build-logic` consumes.

⛔ **It names ONLY distributable coordinates.** The 25 `commons-infra-*` aliases left this catalog
at 0.3.0 (`F419 #595`) for `eu.servista.infra:servista-catalog-infra`, published from
`servista-kotlin-commons-infra` to the **`servista-internal`** owner — because a catalog a
contractor holds is an inventory a contractor reads.
