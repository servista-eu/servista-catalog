# Phase 3: Publish Catalog - Research

**Researched:** 2026-03-27
**Domain:** Gradle version catalog publishing to Forgejo Maven registry
**Confidence:** HIGH

## Summary

This phase involves two file edits (artifactId in `build.gradle.kts`, version in `gradle.properties`) followed by a `./gradlew publish` to the Forgejo Maven registry and a curl-based verification. The publishing infrastructure is fully in place from the original project -- only the coordinates need changing.

The version catalog plugin publishes three files to Maven: a `.toml` file (the catalog itself), a `.pom` file (Maven coordinates with `<packaging>toml</packaging>`), and a `.module` file (Gradle Module Metadata with variant information). All three derive their artifact name from the `artifactId` property set in the `MavenPublication` block in `build.gradle.kts`.

**Primary recommendation:** Change `artifactId` to `"qabatz-catalog"` and `version` to `0.1.0`, then publish using the existing `./gradlew publish` task. Verify via curl against the Maven URL pattern on the Forgejo registry.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** Clean break -- change artifactId from `qabatz-gradle-plugins-catalog` to `qabatz-catalog`. No dual publishing under both old and new coordinates. kotlin-commons continues using the already-published old coordinate (`eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0`) until Phase 6 updates it.
- **D-02:** Set version to `0.1.0` in `gradle.properties`. This is groundwork -- the new artifact identity starts fresh at `0.1.0`, not carrying forward the old `0.2.0` version.
- **D-03:** Verify publish success by curling the Forgejo packages API at `https://git.hestia-ng.eu/api/packages/qabatz/maven` to confirm the artifact exists at the expected coordinates (`eu.qabatz:qabatz-catalog:0.1.0`).

### Claude's Discretion
- Order of implementation steps (artifactId change, version bump, publish, verify)
- Exact curl command and expected response format for registry verification

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CAT-05 | Catalog is published to Forgejo Maven registry as `eu.qabatz:qabatz-catalog` | Full publishing infrastructure exists; only `artifactId` and `version` edits needed, then `./gradlew publish` |
</phase_requirements>

## Standard Stack

No new libraries or dependencies are introduced in this phase. The existing Gradle plugins handle everything:

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `version-catalog` (Gradle plugin) | Built into Gradle 9.3.1 | Produces version catalog component for publishing | Standard Gradle mechanism for catalog publishing |
| `maven-publish` (Gradle plugin) | Built into Gradle 9.3.1 | Handles Maven repository upload | Standard Gradle mechanism for Maven publishing |

### Supporting
| Tool | Version | Purpose | When to Use |
|------|---------|---------|-------------|
| curl | System | Verify published artifact via HTTP GET | Post-publish verification |
| `./gradlew` | 9.3.1 | Build and publish | All build operations |

**No installation needed.** All tools are already configured.

## Architecture Patterns

### Publication Artifact Structure

When `./gradlew publish` runs for a version catalog, three files are uploaded to the Maven registry at path `{groupId-as-path}/{artifactId}/{version}/`:

```
eu/qabatz/qabatz-catalog/0.1.0/
  qabatz-catalog-0.1.0.toml       # The actual version catalog
  qabatz-catalog-0.1.0.pom        # Maven POM (packaging: toml)
  qabatz-catalog-0.1.0.module     # Gradle Module Metadata
```

Plus checksum files (`.md5`, `.sha1`, `.sha256`, `.sha512`) for each.

### Coordinate Flow

The Maven coordinates are assembled from three sources:

| Property | Source File | Current Value | New Value |
|----------|-----------|---------------|-----------|
| `groupId` | `gradle.properties` (`group=`) | `eu.qabatz` | `eu.qabatz` (unchanged) |
| `artifactId` | `build.gradle.kts` (`artifactId =`) | `qabatz-gradle-plugins-catalog` | `qabatz-catalog` |
| `version` | `gradle.properties` (`version=`) | `0.2.0` | `0.1.0` |

### Files to Edit

1. **`build.gradle.kts` line 19:** Change `artifactId = "qabatz-gradle-plugins-catalog"` to `artifactId = "qabatz-catalog"`
2. **`gradle.properties` line 2:** Change `version=0.2.0` to `version=0.1.0`
3. **`catalog/libs.versions.toml` line 2:** Update comment from `from("eu.qabatz:qabatz-gradle-plugins-catalog:<version>")` to `from("eu.qabatz:qabatz-catalog:<version>")`

### Publish Task Chain

The existing Gradle tasks (verified running):

| Task | Purpose |
|------|---------|
| `generateCatalogAsToml` | Generates the `.toml` from catalog DSL -- pre-publish validation |
| `generatePomFileForVersionCatalogPublication` | Generates POM with coordinates |
| `generateMetadataFileForVersionCatalogPublication` | Generates Gradle module metadata |
| `publishVersionCatalogPublicationToForgejoRepository` | Uploads to Forgejo |
| `publish` | Shortcut that runs all of the above |

`./gradlew publish` is the single command that orchestrates everything.

### Verification Pattern

The Forgejo Maven registry serves artifacts at standard Maven URL paths. To verify the published artifact:

```bash
# Verify the TOML file is accessible
curl -sf -u "token:$FORGEJO_TOKEN" \
  "https://git.hestia-ng.eu/api/packages/qabatz/maven/eu/qabatz/qabatz-catalog/0.1.0/qabatz-catalog-0.1.0.toml" \
  -o /dev/null -w "%{http_code}"
# Expected: 200

# Verify the POM is accessible
curl -sf -u "token:$FORGEJO_TOKEN" \
  "https://git.hestia-ng.eu/api/packages/qabatz/maven/eu/qabatz/qabatz-catalog/0.1.0/qabatz-catalog-0.1.0.pom" \
  -o /dev/null -w "%{http_code}"
# Expected: 200
```

**Important:** The Forgejo instance at `git.hestia-ng.eu` requires authentication even for reading packages (verified: unauthenticated requests return `"Only signed in user is allowed to call APIs."`).

### Anti-Patterns to Avoid
- **Do NOT change `settings.gradle.kts`:** The root project name (`qabatz-gradle-plugins`) stays unchanged in this phase -- that is Phase 5 (REN-01) scope.
- **Do NOT dual-publish:** Decision D-01 explicitly forbids publishing under both old and new coordinates.
- **Do NOT forget the comment in `catalog/libs.versions.toml`:** It still references the old coordinate and should be updated for consistency.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Maven publishing | Custom upload scripts or curl-based PUT | `./gradlew publish` | Handles checksums, POM generation, module metadata, authentication automatically |
| Artifact verification | Custom scripts parsing HTML | Standard Maven URL pattern with curl | Forgejo serves standard Maven repo layout |

## Common Pitfalls

### Pitfall 1: Missing Forgejo Credentials
**What goes wrong:** `./gradlew publish` fails with 401 Unauthorized.
**Why it happens:** `FORGEJO_TOKEN` environment variable not set, or the token has expired.
**How to avoid:** Verify credentials before publishing: `echo $FORGEJO_TOKEN | head -c 5` to confirm it is set. The credential resolution chain is: `publishToken` gradle property -> `forgejoToken` gradle property -> `FORGEJO_TOKEN` env var -> empty string (will fail).
**Warning signs:** Empty password field in Gradle output, HTTP 401 response.

### Pitfall 2: Old Artifact Still Published
**What goes wrong:** Running `publish` with the old artifactId still configured somewhere.
**Why it happens:** Forgetting to change the `artifactId` in `build.gradle.kts` before publishing.
**How to avoid:** After editing, run `./gradlew generatePomFileForVersionCatalogPublication` and check `build/publications/versionCatalog/pom-default.xml` -- confirm `<artifactId>qabatz-catalog</artifactId>` appears.
**Warning signs:** POM still shows `qabatz-gradle-plugins-catalog`.

### Pitfall 3: Version Already Exists
**What goes wrong:** Forgejo returns an error on publish because a package with the same name and version already exists.
**Why it happens:** Re-running publish after a successful first publish.
**How to avoid:** This is a one-shot operation for a given version. If it needs to be re-published, the existing package version must be deleted first via the Forgejo UI or API.
**Warning signs:** HTTP 409 Conflict or similar error during publish.

### Pitfall 4: Verification Fails Due to Auth
**What goes wrong:** curl returns the JSON message `"Only signed in user is allowed to call APIs."` instead of the artifact.
**Why it happens:** The Forgejo instance requires authentication for all API access including package downloads.
**How to avoid:** Always include `-u "token:$FORGEJO_TOKEN"` in curl verification commands.
**Warning signs:** Getting a JSON error response instead of TOML/XML content.

## Code Examples

### Current build.gradle.kts (before changes)
```kotlin
// Source: build.gradle.kts (verified in working tree)
publications {
    create<MavenPublication>("versionCatalog") {
        from(components["versionCatalog"])
        artifactId = "qabatz-gradle-plugins-catalog"  // <-- change this
    }
}
```

### After changes
```kotlin
publications {
    create<MavenPublication>("versionCatalog") {
        from(components["versionCatalog"])
        artifactId = "qabatz-catalog"
    }
}
```

### Verification: Pre-publish POM check
```bash
# After editing, before publishing -- verify generated POM has correct coordinates
./gradlew generatePomFileForVersionCatalogPublication
cat build/publications/versionCatalog/pom-default.xml
# Should show:
#   <groupId>eu.qabatz</groupId>
#   <artifactId>qabatz-catalog</artifactId>
#   <version>0.1.0</version>
```

### Verification: Post-publish artifact retrieval
```bash
# Verify the artifact is retrievable from the registry
curl -sf -u "token:$FORGEJO_TOKEN" \
  "https://git.hestia-ng.eu/api/packages/qabatz/maven/eu/qabatz/qabatz-catalog/0.1.0/qabatz-catalog-0.1.0.toml" \
  | head -5
# Expected output: first few lines of the TOML catalog
```

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK | Gradle build | Yes | 21.0.10 (Temurin) | -- |
| Gradle wrapper | Build and publish | Yes | 9.3.1 | -- |
| curl | Post-publish verification | Yes | System | -- |
| FORGEJO_TOKEN | Forgejo authentication | No (not in env) | -- | Must be provided at publish time |

**Missing dependencies with no fallback:**
- `FORGEJO_TOKEN` environment variable is not currently set in the shell environment. It MUST be available when `./gradlew publish` and curl verification are executed. The user needs to export it or pass it as a Gradle property (`-PpublishToken=...`).

**Missing dependencies with fallback:**
- Local Maven publish (`publishToMavenLocal`) cannot be tested due to `.m2` directory permissions (owned by root). Fallback: use `generatePomFileForVersionCatalogPublication` to verify coordinates without actual publish.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | None (no test infrastructure in project) |
| Config file | None |
| Quick run command | `./gradlew generateCatalogAsToml` (validates catalog generation) |
| Full suite command | `./gradlew generatePomFileForVersionCatalogPublication` + POM inspection |

### Phase Requirements to Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| CAT-05 | Catalog published as `eu.qabatz:qabatz-catalog` | smoke | `./gradlew generatePomFileForVersionCatalogPublication && grep -q 'qabatz-catalog' build/publications/versionCatalog/pom-default.xml` | N/A (build output) |
| CAT-05 | Artifact retrievable from Forgejo registry | manual | `curl -sf -u "token:$FORGEJO_TOKEN" "https://git.hestia-ng.eu/api/packages/qabatz/maven/eu/qabatz/qabatz-catalog/0.1.0/qabatz-catalog-0.1.0.toml" -o /dev/null -w "%{http_code}"` | N/A (manual verification) |

### Sampling Rate
- **Per task commit:** `./gradlew generateCatalogAsToml` (ensures catalog still generates)
- **Per wave merge:** `./gradlew generatePomFileForVersionCatalogPublication` + POM coordinate check
- **Phase gate:** Successful publish + curl verification returning HTTP 200

### Wave 0 Gaps
None -- no test framework needed. Validation is done through build output inspection and HTTP verification.

## Open Questions

1. **Forgejo Token Availability**
   - What we know: `FORGEJO_TOKEN` is not set in the current shell environment. The credential resolution chain in `build.gradle.kts` falls back to empty string if not provided.
   - What's unclear: Whether the user will export it before running publish, or pass it as a Gradle property.
   - Recommendation: The plan should note that the token must be available but should NOT attempt to set it programmatically. The user provides credentials.

2. **Existing Package Conflict**
   - What we know: Forgejo does not allow publishing if a package of the same name and version already exists.
   - What's unclear: Whether `eu.qabatz:qabatz-catalog:0.1.0` has ever been published before (unlikely, but possible from earlier experiments).
   - Recommendation: If publish fails with a conflict error, delete the existing version via the Forgejo web UI before retrying.

## Sources

### Primary (HIGH confidence)
- Project files: `build.gradle.kts`, `gradle.properties`, `catalog/libs.versions.toml` -- directly inspected
- Generated publication files: `build/publications/versionCatalog/pom-default.xml`, `build/publications/versionCatalog/module.json` -- generated and inspected
- Gradle task listing: `./gradlew tasks --all | grep publish` -- verified available tasks
- Forgejo instance probe: `curl https://git.hestia-ng.eu/api/v1/packages/qabatz` -- confirmed auth required

### Secondary (MEDIUM confidence)
- [Forgejo Maven Package Registry docs](https://forgejo.org/docs/next/user/packages/maven/) -- standard Maven URL patterns
- [Gitea Maven Package Registry docs](https://docs.gitea.com/usage/packages/maven) -- Forgejo inherits Gitea's Maven registry implementation
- [Gitea maven.go router](https://github.com/go-gitea/gitea/blob/main/routers/api/packages/maven/maven.go) -- Maven URL path pattern: `{groupId-as-path}/{artifactId}/{version}/{filename}`

### Tertiary (LOW confidence)
- None

## Project Constraints (from CLAUDE.md)

- **Backward compatibility**: Consuming projects reference `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0` -- this is why D-01 uses clean break (old artifact stays published, consumers update later in Phase 6)
- **Forgejo credentials**: Repository uses embedded token in git remote URL (user `heaphopdancer`) -- publish uses separate `FORGEJO_TOKEN` env var
- **Kotlin DSL**: All build scripts use Kotlin DSL (`.kts`)
- **GSD Workflow**: Changes must go through GSD workflow
- **ktfmt**: Code formatted with `kotlinLangStyle()` -- applies if Kotlin source files are edited (not applicable here, only `.kts` build script changes)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- no new dependencies, all infrastructure verified in working tree
- Architecture: HIGH -- publication files generated and inspected, coordinates verified
- Pitfalls: HIGH -- credential behavior verified, auth requirement confirmed via live probe

**Research date:** 2026-03-27
**Valid until:** 2026-04-27 (stable -- Gradle publishing infrastructure rarely changes)
