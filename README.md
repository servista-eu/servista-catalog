# servista-catalog

Shared Gradle version catalog providing centralized dependency version alignment for all servista
Kotlin projects. Published as `eu.servista:servista-catalog` to the Forgejo Maven registry.

## Usage

Import the catalog in your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://git.hestia-ng.eu/api/packages/servista/maven")
            credentials {
                username = System.getenv("FORGEJO_USER") ?: "token"
                password = System.getenv("FORGEJO_TOKEN") ?: ""
            }
        }
        mavenCentral()
    }
    versionCatalogs {
        create("libs") { from("eu.servista:servista-catalog:<version>") }  // see gradle.properties
    }
}
```

Then reference libraries and plugins in `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.servista.commons.core)
    implementation(libs.servista.service.runtime.core)
    implementation(libs.ktor.server.core)
    implementation(libs.koin.core)
}
```

## What the catalog pins

⛔ **This section used to list every version, and every one of them was wrong.** Measured
2026-09-05 while releasing `0.15.0`: it claimed `servista-kotlin-commons` at `0.3.0` (it was
`0.9.0`), `servista-service-runtime` at `0.2.0` (`0.8.0`) with *"ten named slices"* (there are
eleven), `servista-avro-schemas` at `0.2.0` (`0.3.0`), and Kafka at 4.2.0 against a `4.3.1` ref.
⭐ **A quantity that varies is a COMMAND, not a sentence** — the same remedy `A436 #611` applied to
`CLAUDE.md` in this repository on the same day, one file over, which is why this one was still
wrong.

```bash
# every version this catalog states, from the file that states it
sed -n '/^\[versions\]/,/^\[libraries\]/p' catalog/libs.versions.toml
# and the version this catalog itself publishes
grep '^version=' gradle.properties
```

Categories, which do not go stale: the servista libraries (`servista-kotlin-commons`,
`servista-service-runtime`'s core plus its slices, `servista-avro-schemas`); Kotlin, Ktor and Koin;
database (jOOQ, Flyway, HikariCP, PostgreSQL and Oracle JDBC); messaging (Kafka, Avro); the
lakehouse client (Trino JDBC); cache; observability (OpenTelemetry, Micrometer); security (Nimbus
JOSE+JWT, Tink, BouncyCastle, OpenFGA); cloud (AWS SDK); validation; testing (JUnit, Kotest,
Testcontainers, MockK); analysis (Detekt, Dokka, ktfmt); and the Gradle plugin artifacts
`build-logic` consumes.

⛔ **This catalog names only what is DISTRIBUTABLE.** The 25 `commons-infra-*` and
`commons-wiring-koin-infra-*` aliases left it at `0.3.0` (`F419 #595`, 2026-09-03): they name the
tenant-provisioning control plane, and this catalog is resolved by every consumer of the
distributable set. They are now `eu.servista.infra:servista-catalog-infra`, published to the
**`servista-internal`** package owner, and declared as a second catalog by the eight `backend/infra`
services alone. See
[`DISTRIBUTION.md`](https://git.hestia-ng.eu/servista/servista-kotlin-commons-infra/src/branch/main/DISTRIBUTION.md).

⚠️ **`servista-service-runtime` has no aggregate alias** — it is `-core` plus its named slices since
`F418 #594`. `eu.servista:servista-service-runtime:0.1.0` still resolves; it simply has no 0.2.0.

## Publishing

```bash
FORGEJO_USER=<user> FORGEJO_TOKEN=<token> ./gradlew publish
```

Forgejo does not support overwriting published Maven artifacts. Bump the version in `gradle.properties` before each publish.

### Credential resolution

| Parameter | Resolution order |
|-----------|-----------------|
| Username | `forgejoUser` Gradle property > `FORGEJO_USER` env var > `"token"` |
| Password | `publishToken` Gradle property > `forgejoToken` Gradle property > `FORGEJO_TOKEN` env var |
| Registry URL | `publishUrl` Gradle property > `https://git.hestia-ng.eu/api/packages/servista/maven` |

## Project structure

```
servista-catalog/
  catalog/libs.versions.toml   -- the published artifact (consumed by downstream projects)
  gradle/libs.versions.toml    -- project-internal build catalog (empty)
  build.gradle.kts             -- version-catalog + maven-publish plugins
  settings.gradle.kts          -- rootProject.name = "servista-catalog"
  gradle.properties            -- group (eu.servista) + version
```

This project has no source code. The `test` and `detekt` tasks are registered as no-ops for CI pipeline compatibility.

## License

Proprietary. Internal use only.
