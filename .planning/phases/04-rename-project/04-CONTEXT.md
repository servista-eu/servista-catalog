# Phase 4: Rename Project - Context

**Gathered:** 2026-03-27
**Status:** Ready for planning

<domain>
## Phase Boundary

Rename the project from `qabatz-gradle-plugins` to `qabatz-catalog` everywhere: settings.gradle.kts, Forgejo repository, git remote URL, local directory, and associated configuration. Also update the Forgejo push mirror to point to the already-created GitHub repo, remove the old artifact from the Forgejo registry, and verify the rename succeeded.

**Requirements covered:** REN-01, REN-02, REN-03

</domain>

<decisions>
## Implementation Decisions

### Forgejo Repository Transition
- **D-01:** Try API rename first (PATCH `/api/v1/repos/qabatz/qabatz-gradle-plugins`). If API rename fails, create a new `qabatz-catalog` repo without deleting the old one first — copy all settings and config from the existing repo (except push mirror: use updated URL), migrate repo contents (all branches/tags), then delete the old repo only after migration is confirmed.
- **D-02:** Use the existing Forgejo token (embedded in git remote URL, user `heaphopdancer`) for API operations.

### Push Mirror
- **D-03:** Update the push mirror URL to `https://github.com/servista-eu/qabatz-catalog.git` as part of Phase 4 (during the rename operation). The GitHub repo `servista-eu/qabatz-catalog` already exists — no GitHub-side creation needed. Phase 5 becomes verification-only for the mirror.

### Old Artifact Cleanup
- **D-04:** Remove the old `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0` artifact from the Forgejo Maven registry in Phase 4. Clean break — kotlin-commons will reference a non-existent artifact until Phase 6 updates it.

### Local Rename
- **D-05:** Rename the local filesystem directory from `qabatz-gradle-plugins` to `qabatz-catalog`. Do this after the Forgejo rename and git remote update are complete (Forgejo first, local last).
- **D-06:** Update CLAUDE.md to reflect the new project name (`qabatz-catalog`) in its Project section and descriptions.
- **D-07:** Scan `.planning/` files for any absolute paths referencing `qabatz-gradle-plugins` and update them to `qabatz-catalog`.

### Verification
- **D-08:** Verify rename via Forgejo API (confirm repo exists at `qabatz/qabatz-catalog`), test git clone with the new URL, and verify the old URL redirects (301) or is gone.

### Execution Order
- **D-09:** Order: (1) settings.gradle.kts name change, (2) Forgejo repo rename/migration, (3) update push mirror URL, (4) update git remote URL locally, (5) remove old artifact from registry, (6) verify, (7) rename local directory, (8) update CLAUDE.md and .planning/ paths.

### Claude's Discretion
- Exact API calls and error handling for the Forgejo rename/migration
- How to discover and copy repo settings during the fallback migration path
- Specific curl commands for artifact removal and verification

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Build Configuration
- `settings.gradle.kts` — Contains `rootProject.name = "qabatz-gradle-plugins"` (line to change to `qabatz-catalog`)
- `build.gradle.kts` — Already has `artifactId = "qabatz-catalog"` (no change needed)
- `gradle.properties` — Already has `version=0.1.0` (no change needed)

### Requirements
- `.planning/REQUIREMENTS.md` &sect;Rename — REN-01, REN-02, REN-03 requirement definitions

### Prior Phase Context
- `.planning/phases/03-publish-catalog/03-CONTEXT.md` — Phase 3 decisions on coordinate change and version (D-01, D-02)

### Forgejo API
- Forgejo API base: `https://git.hestia-ng.eu/api/v1`
- Repo rename endpoint: `PATCH /api/v1/repos/{owner}/{repo}` with `{"name": "new-name"}`
- Maven registry: `https://git.hestia-ng.eu/api/packages/qabatz/maven`

</canonical_refs>

<code_context>
## Existing Code Insights

### Current State
- `settings.gradle.kts`: single line `rootProject.name = "qabatz-gradle-plugins"` — trivial change
- `build.gradle.kts`: already updated to `artifactId = "qabatz-catalog"` (Phase 3)
- `gradle.properties`: already at `version=0.1.0` (Phase 3)
- Git remote: `https://heaphopdancer:<token>@git.hestia-ng.eu/qabatz/qabatz-gradle-plugins.git`

### Credential Pattern
- Forgejo token is embedded in the git remote URL (user `heaphopdancer`, token visible in URL)
- Same token can be used for Forgejo API calls via `Authorization: token <value>` header

### Integration Points
- Git remote URL must update from `qabatz-gradle-plugins.git` to `qabatz-catalog.git`
- Push mirror target changes from old GitHub URL to `https://github.com/servista-eu/qabatz-catalog.git`
- CLAUDE.md references `qabatz-gradle-plugins` in project description

</code_context>

<specifics>
## Specific Ideas

- GitHub repo `servista-eu/qabatz-catalog` already exists — user created it manually
- User prefers the safe migration path (create new before deleting old) over destructive operations
- Clean break philosophy carried forward from Phase 3 — remove old artifact, don't preserve backward compatibility

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 04-rename-project*
*Context gathered: 2026-03-27*
