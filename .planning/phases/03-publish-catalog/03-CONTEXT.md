# Phase 3: Publish Catalog - Context

**Gathered:** 2026-03-27
**Status:** Ready for planning

<domain>
## Phase Boundary

Change the published artifact coordinates from `eu.qabatz:qabatz-gradle-plugins-catalog` to `eu.qabatz:qabatz-catalog` and publish to the Forgejo Maven registry. Verify the artifact is retrievable via the registry API.

**Requirements covered:** CAT-05

</domain>

<decisions>
## Implementation Decisions

### Coordinate Transition
- **D-01:** Clean break — change artifactId from `qabatz-gradle-plugins-catalog` to `qabatz-catalog`. No dual publishing under both old and new coordinates. kotlin-commons continues using the already-published old coordinate (`eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0`) until Phase 6 updates it.

### Version Number
- **D-02:** Set version to `0.1.0` in `gradle.properties`. This is groundwork — the new artifact identity starts fresh at `0.1.0`, not carrying forward the old `0.2.0` version. The catalog content is unchanged from Phase 2 verification (35 versions, 75 libraries, 6 plugins).

### Publish Verification
- **D-03:** Verify publish success by curling the Forgejo packages API at `https://git.hestia-ng.eu/api/packages/qabatz/maven` to confirm the artifact exists at the expected coordinates (`eu.qabatz:qabatz-catalog:0.1.0`).

### Claude's Discretion
- Order of implementation steps (artifactId change, version bump, publish, verify)
- Exact curl command and expected response format for registry verification

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Build Configuration
- `build.gradle.kts` — Contains the artifactId to change (line 19: `qabatz-gradle-plugins-catalog` -> `qabatz-catalog`) and full publishing configuration
- `gradle.properties` — Version to change (`0.2.0` -> `0.1.0`) and group (`eu.qabatz`)
- `settings.gradle.kts` — Project name (unchanged in this phase, stays `qabatz-gradle-plugins`)

### Published Catalog
- `catalog/libs.versions.toml` — The catalog content being published (unchanged in this phase)

### Requirements
- `.planning/REQUIREMENTS.md` §Catalog — CAT-05 requirement definition

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Publishing infrastructure fully configured in `build.gradle.kts` — Forgejo Maven repository, credential resolution (env vars + gradle properties), URL configuration
- `generateCatalogAsToml` Gradle task available for pre-publish validation

### Established Patterns
- Phase 1 used `generateCatalogAsToml` as the primary build verification command
- Credential resolution chain: `forgejoUser`/`publishToken` gradle properties -> `FORGEJO_USER`/`FORGEJO_TOKEN` env vars -> defaults

### Integration Points
- `build.gradle.kts` line 19: `artifactId = "qabatz-gradle-plugins-catalog"` — the single line to change for coordinate rename
- `gradle.properties` line 2: `version=0.2.0` — the single line to change for version
- Forgejo registry API: `https://git.hestia-ng.eu/api/packages/qabatz/maven` — publish target and verification endpoint

</code_context>

<specifics>
## Specific Ideas

- Version 0.1.0 chosen because this is groundwork — the project isn't "done" yet, so starting fresh makes more sense than carrying forward 0.2.0 or jumping to 1.0.0

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 03-publish-catalog*
*Context gathered: 2026-03-27*
