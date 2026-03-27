# Phase 4: Rename Project - Research

**Researched:** 2026-03-27
**Domain:** Repository rename (Forgejo API, git remote, filesystem, project metadata)
**Confidence:** HIGH

## Summary

Phase 4 renames the project from `qabatz-gradle-plugins` to `qabatz-catalog` across four domains: the Gradle project name (`settings.gradle.kts`), the Forgejo repository (via API rename), the local git remote URL, and the local filesystem directory. Additionally, the existing push mirror must be replaced (delete old, create new pointing to `github.com/servista-eu/qabatz-catalog.git`), old Maven artifacts must be deleted from the Forgejo package registry, and project documentation (CLAUDE.md, README.md, `.planning/` files) must be updated.

All required Forgejo API endpoints have been verified as functional on the target instance (`git.hestia-ng.eu`). The existing push mirror is currently broken (authentication failure against `github.com/poupapaa/qabatz-gradle-plugins.git`), so replacing it carries no risk of disrupting a working mirror. Two old package entries exist in the registry (`eu.qabatz:qabatz-gradle-plugins-catalog` at versions 0.1.0 and 0.2.0, plus `eu.qabatz:qabatz-gradle-plugins` at 0.2.0) that need deletion per D-04.

**Primary recommendation:** Execute the rename via Forgejo API PATCH (D-01 primary path), which is the simplest and most reliable approach. The fallback migration path (create new repo, migrate, delete old) should only be needed if the API rename returns an error.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** Try API rename first (PATCH `/api/v1/repos/qabatz/qabatz-gradle-plugins`). If API rename fails, create a new `qabatz-catalog` repo without deleting the old one first -- copy all settings and config from the existing repo (except push mirror: use updated URL), migrate repo contents (all branches/tags), then delete the old repo only after migration is confirmed.
- **D-02:** Use the existing Forgejo token (embedded in git remote URL, user `heaphopdancer`) for API operations.
- **D-03:** Update the push mirror URL to `https://github.com/servista-eu/qabatz-catalog.git` as part of Phase 4 (during the rename operation). The GitHub repo `servista-eu/qabatz-catalog` already exists -- no GitHub-side creation needed. Phase 5 becomes verification-only for the mirror.
- **D-04:** Remove the old `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0` artifact from the Forgejo Maven registry in Phase 4. Clean break -- kotlin-commons will reference a non-existent artifact until Phase 6 updates it.
- **D-05:** Rename the local filesystem directory from `qabatz-gradle-plugins` to `qabatz-catalog`. Do this after the Forgejo rename and git remote update are complete (Forgejo first, local last).
- **D-06:** Update CLAUDE.md to reflect the new project name (`qabatz-catalog`) in its Project section and descriptions.
- **D-07:** Scan `.planning/` files for any absolute paths referencing `qabatz-gradle-plugins` and update them to `qabatz-catalog`.
- **D-08:** Verify rename via Forgejo API (confirm repo exists at `qabatz/qabatz-catalog`), test git clone with the new URL, and verify the old URL redirects (301) or is gone.
- **D-09:** Order: (1) settings.gradle.kts name change, (2) Forgejo repo rename/migration, (3) update push mirror URL, (4) update git remote URL locally, (5) remove old artifact from registry, (6) verify, (7) rename local directory, (8) update CLAUDE.md and .planning/ paths.

### Claude's Discretion
- Exact API calls and error handling for the Forgejo rename/migration
- How to discover and copy repo settings during the fallback migration path
- Specific curl commands for artifact removal and verification

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| REN-01 | Project name changed to `qabatz-catalog` in settings.gradle.kts | Trivial one-line edit; current value verified as `rootProject.name = "qabatz-gradle-plugins"` |
| REN-02 | Forgejo repository renamed (or recreated) as `qabatz/qabatz-catalog` | Forgejo API PATCH endpoint verified functional; fallback migration path documented |
| REN-03 | Git remote URL updated to new repository name | `git remote set-url` command after Forgejo rename completes |
</phase_requirements>

## Architecture Patterns

### Execution Flow (per D-09)

The rename must follow a strict sequence because each step depends on the previous:

```
Step 1: settings.gradle.kts name change  (code change, commit, push)
Step 2: Forgejo API repo rename           (external API call)
Step 3: Delete old push mirror + create new  (external API calls)
Step 4: Update local git remote URL       (git remote set-url)
Step 5: Delete old Maven packages         (external API calls)
Step 6: Verify rename                     (API + git clone test)
Step 7: Rename local directory            (mv command)
Step 8: Update CLAUDE.md, README.md, .planning/ paths  (code changes)
```

**Important sequencing notes:**
- Step 1 must be committed and pushed BEFORE Step 2 (so the rename operates on up-to-date repo)
- Step 2 must complete BEFORE Step 4 (remote URL depends on Forgejo rename)
- Step 4 must complete BEFORE Step 7 (git operations need working remote)
- Step 7 changes the working directory path, so Step 8 must use the NEW path
- Step 8 is the last code change and should be committed from the new directory

### Recommended Plan Structure

This phase should be split into two plans:

**Plan 1: Rename and Migrate (Steps 1-6)**
- Change `settings.gradle.kts`, commit, push
- Forgejo API rename
- Push mirror replacement
- Git remote update
- Old artifact deletion
- Verification

**Plan 2: Local Cleanup (Steps 7-8)**
- Local directory rename
- Update CLAUDE.md, README.md, and `.planning/` files
- Final commit from new directory

This split is important because:
- Plan 1 involves irreversible external operations (API calls)
- Plan 2 is local-only and can be re-executed if interrupted
- The directory rename (Step 7) changes the working directory, affecting all subsequent file paths

## Forgejo API Reference (Verified)

All endpoints verified against `https://git.hestia-ng.eu` on 2026-03-27.

### Repository Rename

```bash
# PATCH /api/v1/repos/{owner}/{repo}
# Body: {"name": "new-name"}
# Response: 200 (success), 403 (forbidden), 404 (not found), 422 (validation error)

curl -s -X PATCH \
  -H "Authorization: token $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "qabatz-catalog"}' \
  "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-gradle-plugins"
```

**Confidence:** HIGH -- endpoint format confirmed by Gitea/Forgejo API documentation; the `name` field in `EditRepoOption` is the standard way to rename repositories.

### Push Mirror Management

```bash
# LIST: GET /api/v1/repos/{owner}/{repo}/push_mirrors
# Returns: array of push mirror objects
# Verified: HTTP 200, returns current mirror data

# GET BY NAME: GET /api/v1/repos/{owner}/{repo}/push_mirrors/{name}
# Verified: HTTP 200, returns single mirror by remote_name

# DELETE: DELETE /api/v1/repos/{owner}/{repo}/push_mirrors/{name}
# Verified: endpoint accepts DELETE method (confirmed via OPTIONS)

# CREATE: POST /api/v1/repos/{owner}/{repo}/push_mirrors
# Body: {"remote_address": "url", "remote_username": "user", "remote_password": "token", "interval": "8h0m0s", "sync_on_commit": true}
# Verified: endpoint accepts POST method (confirmed via OPTIONS)
```

**Current push mirror state (verified):**
- `remote_name`: `remote_mirror_PnJFYFp9VQX`
- `remote_address`: `https://github.com/poupapaa/qabatz-gradle-plugins.git`
- `sync_on_commit`: true
- `interval`: 8h0m0s
- **Status: BROKEN** -- authentication failure ("Invalid username or token. Password authentication is not supported for Git operations.")

**Note on rename behavior:** After the repo rename (Step 2), the push mirror endpoints will be at the NEW path: `/api/v1/repos/qabatz/qabatz-catalog/push_mirrors`. The old path will return 404 (or redirect).

### Package Deletion

```bash
# DELETE /api/v1/packages/{owner}/{type}/{name}/{version}
# Note: package name must be URL-encoded (colons become %3A)

# Verified via GET (same endpoint pattern returns package details):
# GET /api/v1/packages/qabatz/maven/eu.qabatz%3Aqabatz-gradle-plugins-catalog/0.2.0
# Returns: HTTP 200 with full package metadata
```

### Repository Verification

```bash
# GET /api/v1/repos/{owner}/{repo}
# Returns: 200 (exists) or 404 (not found)

curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: token $TOKEN" \
  "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-catalog"
```

## Runtime State Inventory

This is a rename phase -- runtime state must be explicitly inventoried.

| Category | Items Found | Action Required |
|----------|-------------|------------------|
| Stored data | None -- this project has no databases, no persistent state stores. It is a build-tool-only project. | None |
| Live service config | **Forgejo push mirror**: remote_name `remote_mirror_PnJFYFp9VQX`, pointing to `github.com/poupapaa/qabatz-gradle-plugins.git` (broken). **Forgejo repo name**: `qabatz/qabatz-gradle-plugins`. **Maven registry**: 3 old packages (see below). | Delete old mirror, create new. Rename repo. Delete old packages. |
| OS-registered state | None -- no cron jobs, systemd units, or OS registrations reference this project name | None |
| Secrets/env vars | Forgejo token embedded in git remote URL (user `heaphopdancer`). The token itself does not change -- only the repo path in the URL changes. No `.env` files in this project. | Update git remote URL only |
| Build artifacts | `.gradle/` and `build/` directories contain cached state with old project name. These are gitignored and will auto-regenerate. | None -- Gradle handles this automatically |

### Old Maven Packages to Delete (D-04)

Verified via Forgejo API on 2026-03-27:

| Package Name | Version | ID | Action |
|---|---|---|---|
| `eu.qabatz:qabatz-gradle-plugins-catalog` | 0.2.0 | 467 | DELETE (per D-04) |
| `eu.qabatz:qabatz-gradle-plugins-catalog` | 0.1.0 | 421 | DELETE (also old) |
| `eu.qabatz:qabatz-gradle-plugins` | 0.2.0 | 464 | DELETE (old plugin artifact) |

**Note:** D-04 specifically mentions `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0`, but there are also an older version (0.1.0) and the plugin artifact itself (`eu.qabatz:qabatz-gradle-plugins:0.2.0`). All three carry the old name and should be deleted for a clean break. The other old packages (individual plugin marker artifacts like `qabatz.testing:qabatz.testing.gradle.plugin`) are separate concerns -- they use different group IDs and are unrelated to the rename.

## Files Requiring Update

### Source Files (in-scope for this phase)

| File | Current Value | New Value | Decision |
|------|---------------|-----------|----------|
| `settings.gradle.kts` | `rootProject.name = "qabatz-gradle-plugins"` | `rootProject.name = "qabatz-catalog"` | REN-01 |
| `README.md` | `# qabatz-gradle-plugins` | `# qabatz-catalog` | D-06 (update project name) |
| `CLAUDE.md` | Multiple references to `qabatz-gradle-plugins` throughout | Update to `qabatz-catalog` where it refers to the current project name | D-06 |

### Documentation Files with Old Name References

**Active documentation (update per D-06/D-07):**

| File | Type of Reference | Update? |
|------|-------------------|---------|
| `.planning/PROJECT.md` | Current project description | YES -- update current-state references |
| `.planning/codebase/STRUCTURE.md` | Directory tree header, consumer references | YES |
| `.planning/codebase/ARCHITECTURE.md` | Artifact references, data flow | YES |
| `.planning/codebase/INTEGRATIONS.md` | Consumer integration examples | YES |
| `.planning/codebase/STACK.md` | Artifact coordinate references | YES |

**Historical documentation (do NOT update):**

| File | Why Leave Unchanged |
|------|---------------------|
| `.planning/ROADMAP.md` | Overview text is historical narrative; phase descriptions are locked |
| `.planning/STATE.md` | Decision log records what happened; changing history is misleading |
| `.planning/REQUIREMENTS.md` | Phase traceability table references are historical |
| `.planning/phases/01-*/` | Completed phase artifacts -- historical record |
| `.planning/phases/02-*/` | Completed phase artifacts -- historical record |
| `.planning/phases/03-*/` | Completed phase artifacts -- historical record |
| `.planning/phases/04-*/` | Current phase context/discussion -- references old name correctly |

**Absolute paths in `.planning/` files (D-07):**

Four files contain absolute paths with `/home/sven/.../qabatz-gradle-plugins`:
- `.planning/phases/01-strip-plugin-sources/01-01-PLAN.md` (line 212)
- `.planning/phases/01-strip-plugin-sources/01-02-PLAN.md` (lines 113, 150)
- `.planning/phases/02-verify-catalog/02-01-PLAN.md` (lines 116, 174)

These are in completed phase plan files. They are historical records of commands that were already executed. **Recommendation:** Do NOT update these. They are frozen artifacts from past phases. Changing them would falsify history. The planner should note D-07 is satisfied by scanning and finding only historical references.

### Files NOT Needing Update

| File | Why |
|------|-----|
| `build.gradle.kts` | Already uses `artifactId = "qabatz-catalog"` (updated in Phase 3) |
| `gradle.properties` | Contains `group=eu.qabatz` and `version=0.1.0` (no project name reference) |
| `catalog/libs.versions.toml` | Already references `eu.qabatz:qabatz-catalog` (updated in Phase 3) |

### .claude/worktrees/ Directory

The `.claude/worktrees/agent-a597d091/` directory contains a copy of many project files with old references. This directory is:
- Untracked (`git status` shows `?? .claude/`)
- A Claude Code internal worktree artifact
- Should NOT be modified as part of this phase -- it will be cleaned up naturally

## Common Pitfalls

### Pitfall 1: Push Mirror Endpoint Path Changes After Rename
**What goes wrong:** After renaming the repo from `qabatz-gradle-plugins` to `qabatz-catalog`, the push mirror API endpoints move to `/repos/qabatz/qabatz-catalog/push_mirrors`. Attempting to delete/create mirrors at the old path fails with 404.
**Why it happens:** Forgejo redirects the web UI but API endpoints use the canonical name.
**How to avoid:** After the rename API call succeeds, use the NEW repo name in all subsequent API calls.
**Warning signs:** HTTP 404 responses to push mirror API calls.

### Pitfall 2: Push Mirror Requires GitHub Token
**What goes wrong:** Creating a new push mirror to `github.com/servista-eu/qabatz-catalog.git` requires authentication. The current mirror is broken because the old token no longer works.
**Why it happens:** GitHub requires a personal access token (PAT) for HTTPS push operations. The old mirror used an expired/invalid token.
**How to avoid:** The user must provide a valid GitHub PAT with `repo` scope for the `servista-eu` organization. The Forgejo token (heaphopdancer) is for Forgejo only -- it cannot authenticate to GitHub.
**Warning signs:** Mirror creation succeeds but first sync fails with "Invalid username or token".

### Pitfall 3: Committing Before vs After Directory Rename
**What goes wrong:** After renaming the local directory (Step 7), the git working directory path changes. Any uncommitted changes from before the rename become confusing.
**Why it happens:** Git tracks content, not directory names, but the shell working directory reference breaks.
**How to avoid:** Commit and push ALL changes before the directory rename. After the rename, `cd` into the new directory and verify `git status` is clean.
**Warning signs:** `fatal: not a git repository` errors after the rename.

### Pitfall 4: Package Name URL Encoding
**What goes wrong:** Maven package names contain colons (e.g., `eu.qabatz:qabatz-gradle-plugins-catalog`). If not URL-encoded in the API path, the request fails.
**Why it happens:** Colons are special characters in URLs.
**How to avoid:** URL-encode the package name: `eu.qabatz%3Aqabatz-gradle-plugins-catalog`.
**Warning signs:** HTTP 400 or 404 when calling package deletion endpoint.

### Pitfall 5: CLAUDE.md Update Scope
**What goes wrong:** CLAUDE.md contains many references to `qabatz-gradle-plugins` in multiple contexts: project name, artifact coordinates, plugin IDs, consumer import statements. Blindly replacing all occurrences breaks accuracy.
**Why it happens:** Some references are to the OLD artifact coordinates (which are historical context), and some are to the current project identity.
**How to avoid:** CLAUDE.md needs a careful rewrite -- update the project name and description at the top, but ensure artifact coordinate references reflect the CURRENT state (`eu.qabatz:qabatz-catalog`). Historical references to old coordinates in context/constraints sections should be updated to reflect reality.
**Warning signs:** CLAUDE.md states the project is called `qabatz-catalog` but still says artifacts are `qabatz-gradle-plugins:0.2.0`.

## Code Examples

### Step 1: Change settings.gradle.kts

```kotlin
// FROM:
rootProject.name = "qabatz-gradle-plugins"

// TO:
rootProject.name = "qabatz-catalog"
```

### Step 2: Forgejo API Rename

```bash
TOKEN="<from git remote URL>"

# Primary: PATCH rename
curl -s -X PATCH \
  -H "Authorization: token $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "qabatz-catalog"}' \
  "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-gradle-plugins"

# Verify: check new name exists
curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: token $TOKEN" \
  "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-catalog"
# Expected: 200
```

### Step 3: Push Mirror Replacement

```bash
# NOTE: After rename, use new repo name in path!
REPO="qabatz/qabatz-catalog"

# Delete old mirror
curl -s -X DELETE \
  -H "Authorization: token $TOKEN" \
  "https://git.hestia-ng.eu/api/v1/repos/$REPO/push_mirrors/remote_mirror_PnJFYFp9VQX"

# Create new mirror (requires GitHub PAT)
curl -s -X POST \
  -H "Authorization: token $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "remote_address": "https://github.com/servista-eu/qabatz-catalog.git",
    "remote_username": "<github-username>",
    "remote_password": "<github-pat>",
    "interval": "8h0m0s",
    "sync_on_commit": true
  }' \
  "https://git.hestia-ng.eu/api/v1/repos/$REPO/push_mirrors"
```

### Step 4: Update Local Git Remote

```bash
TOKEN="<forgejo-token>"
git remote set-url origin "https://heaphopdancer:${TOKEN}@git.hestia-ng.eu/qabatz/qabatz-catalog.git"
```

### Step 5: Delete Old Packages

```bash
# Delete old catalog artifact (both versions)
curl -s -X DELETE \
  -H "Authorization: token $TOKEN" \
  "https://git.hestia-ng.eu/api/v1/packages/qabatz/maven/eu.qabatz%3Aqabatz-gradle-plugins-catalog/0.2.0"

curl -s -X DELETE \
  -H "Authorization: token $TOKEN" \
  "https://git.hestia-ng.eu/api/v1/packages/qabatz/maven/eu.qabatz%3Aqabatz-gradle-plugins-catalog/0.1.0"

# Delete old plugin artifact
curl -s -X DELETE \
  -H "Authorization: token $TOKEN" \
  "https://git.hestia-ng.eu/api/v1/packages/qabatz/maven/eu.qabatz%3Aqabatz-gradle-plugins/0.2.0"
```

### Step 7: Local Directory Rename

```bash
cd /home/sven/work/clients/hestia/projects/qabatz/libs/
mv qabatz-gradle-plugins qabatz-catalog
cd qabatz-catalog
git status  # verify git still works
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Repository rename | Manual Forgejo UI operations or repo recreation | `PATCH /api/v1/repos/{owner}/{repo}` | Atomic, preserves stars/forks/settings, scriptable |
| Push mirror config | Forgejo web UI manual entry | Push mirror API endpoints (DELETE old + POST new) | Scriptable, verifiable, part of the same execution flow |
| Package deletion | Forgejo web UI manual deletion | `DELETE /api/v1/packages/{owner}/{type}/{name}/{version}` | Scriptable, verifiable, handles URL encoding |

## Open Questions

1. **GitHub PAT for Push Mirror**
   - What we know: The existing push mirror is broken (auth failure). A new mirror to `github.com/servista-eu/qabatz-catalog.git` requires a valid GitHub PAT.
   - What's unclear: Where will the GitHub PAT come from? It cannot be the Forgejo `heaphopdancer` token. The user needs to provide or generate a GitHub PAT with appropriate scope.
   - Recommendation: The plan should include a step where the user provides the GitHub PAT. If unavailable, the push mirror creation should be deferred to Phase 5 (which is already "Configure Mirror").

2. **Old Plugin Marker Artifacts**
   - What we know: The registry contains 8 old plugin marker artifacts (`qabatz.testing:qabatz.testing.gradle.plugin:0.2.0`, etc.) that reference the old plugin system.
   - What's unclear: D-04 specifically mentions only `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0`. Should the other old plugin artifacts also be cleaned up?
   - Recommendation: At minimum delete the 3 artifacts with `qabatz-gradle-plugins` in the name (per D-04's clean-break philosophy). The plugin marker artifacts use different group IDs and are a separate cleanup concern -- defer to a future cleanup task.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| curl | Forgejo API calls | Verified | (system) | -- |
| git | Remote URL update, commits | Verified | (system) | -- |
| python3 | JSON parsing in verification | Verified | (system) | Use jq or grep |
| Forgejo API | Repo rename, mirror, packages | Verified | v14.0+ | Forgejo web UI (manual) |
| GitHub PAT | Push mirror authentication | UNKNOWN | -- | Defer mirror to Phase 5 |

**Missing dependencies with no fallback:**
- None (all core operations use available tools)

**Missing dependencies with fallback:**
- GitHub PAT: If unavailable, defer push mirror creation to Phase 5 (already planned as "Configure Mirror"). The mirror deletion and all other operations proceed without it.

## Validation Architecture

### Test Framework

This phase involves no code that can be unit-tested in the traditional sense. All changes are either:
- Single-line config edits (settings.gradle.kts)
- External API calls (Forgejo)
- Git operations (remote URL)
- Documentation updates (markdown files)

| Property | Value |
|----------|-------|
| Framework | Manual verification via API and git commands |
| Config file | N/A |
| Quick run command | `curl -s -o /dev/null -w "%{http_code}" -H "Authorization: token $TOKEN" "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-catalog"` |
| Full suite command | Verify API + git clone + package deletion confirmation |

### Phase Requirements to Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REN-01 | settings.gradle.kts has `qabatz-catalog` | smoke | `grep 'qabatz-catalog' settings.gradle.kts` | N/A (inline) |
| REN-02 | Forgejo repo accessible at new name | smoke | `curl -s -o /dev/null -w "%{http_code}" -H "Authorization: token $TOKEN" "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-catalog"` (expect 200) | N/A (inline) |
| REN-03 | Git remote points to new URL | smoke | `git remote get-url origin` (expect `qabatz-catalog.git`) | N/A (inline) |

### Sampling Rate
- **Per task commit:** Verify `settings.gradle.kts` content; verify git remote URL
- **Per wave merge:** Full API verification (repo exists, old packages gone, mirror configured)
- **Phase gate:** All three REN requirements verified via automated commands

### Wave 0 Gaps
None -- no test framework infrastructure needed. All verification is done via shell commands against the Forgejo API and git.

## Sources

### Primary (HIGH confidence)
- Forgejo API at `git.hestia-ng.eu` -- direct endpoint testing (PATCH repos, push_mirrors, packages)
- `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties` -- direct file inspection
- `git remote -v` -- current remote URL verified
- Forgejo package registry API -- all 3 old packages identified with IDs

### Secondary (MEDIUM confidence)
- [Gitea API EditRepoOption](https://github.com/go-gitea/gitea/issues/25683) -- confirms `name` field for rename
- [Gitea push mirrors API PR #19841](https://github.com/go-gitea/gitea/issues/19796) -- confirms push mirror endpoints exist
- [Gitea package deletion](https://github.com/go-gitea/gitea/issues/25064) -- confirms DELETE endpoint for Maven packages

### Tertiary (LOW confidence)
- None

## Metadata

**Confidence breakdown:**
- Forgejo API rename: HIGH -- PATCH endpoint confirmed by Gitea/Forgejo docs; request body format standard
- Push mirror API: HIGH -- endpoints verified live (GET returns 200, OPTIONS confirms POST/DELETE)
- Package deletion: HIGH -- GET endpoint verified with URL-encoded package name; DELETE confirmed by Gitea docs
- File update scope: HIGH -- full grep audit completed across all project files
- GitHub PAT requirement: MEDIUM -- known requirement but availability unclear

**Research date:** 2026-03-27
**Valid until:** 2026-04-27 (stable -- Forgejo API is versioned and unlikely to change)
