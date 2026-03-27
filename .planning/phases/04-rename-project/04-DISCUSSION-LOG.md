# Phase 4: Rename Project - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-03-27
**Phase:** 04-rename-project
**Areas discussed:** Forgejo repo transition, Existing mirror handling, Local directory name, GitHub repo rename, Old artifact cleanup, Forgejo redirect verification

---

## Forgejo Repo Transition

| Option | Description | Selected |
|--------|-------------|----------|
| Rename via Forgejo UI | Rename existing repo in Forgejo settings. Preserves all settings, mirror config, stars, issue history. Auto-redirects old URL. | |
| Rename via Forgejo API | Use PATCH /api/v1/repos/qabatz/qabatz-gradle-plugins to rename programmatically. Same preservation benefits, but scriptable. | Preferred |
| Delete and recreate | Delete old repo, create fresh qabatz-catalog, push all branches/tags. Clean slate but loses settings. | Fallback |

**User's choice:** Try API rename first. If that fails, create new repo without deleting old one first — copy all settings and config (except push mirror: use updated URL), migrate contents, then delete old repo.
**Notes:** User explicitly preferred the safe migration path — never delete old before new is confirmed working.

---

## Existing Mirror Handling

| Option | Description | Selected |
|--------|-------------|----------|
| Update mirror in Phase 4 | Update push mirror URL to new GitHub target during rename. Avoids broken mirror gap. Phase 5 verifies. | ✓ |
| Leave for Phase 5 | Phase 4 only handles rename. Mirror may break temporarily. Phase 5 handles all mirror setup. | |
| Disable mirror in Phase 4, configure in Phase 5 | Explicitly disable old mirror during rename. Phase 5 sets up new mirror cleanly. | |

**User's choice:** Update mirror in Phase 4
**Notes:** None

---

## Local Directory Name

| Option | Description | Selected |
|--------|-------------|----------|
| Rename local directory | Rename local directory to match project name. Keeps everything consistent. | ✓ |
| Leave as-is | Keep local directory as qabatz-gradle-plugins. Less disruption. | |

**User's choice:** Rename local directory
**Notes:** User also confirmed: rename after Forgejo rename (not before), update CLAUDE.md to reflect new name, and scan .planning/ for absolute paths to update.

---

## GitHub Repo Rename

| Option | Description | Selected |
|--------|-------------|----------|
| Rename GitHub repo in Phase 4 | Rename via GitHub API in same phase. | |
| Create new, delete old | Safe migration approach for GitHub side. | |
| Leave for Phase 5 | Phase 5 handles all GitHub-side work. | |

**User's choice:** Not needed — user has already created `servista-eu/qabatz-catalog` on GitHub.
**Notes:** No GitHub-side rename or creation needed. Just point the Forgejo mirror to it.

---

## Old Artifact Cleanup

| Option | Description | Selected |
|--------|-------------|----------|
| Remove after consumer migration | Leave old artifact until Phase 6 updates consumers. | |
| Remove in Phase 4 | Clean break — delete old artifact now. kotlin-commons may break. | ✓ |
| Leave indefinitely | Don't remove. Harmless in registry. | |

**User's choice:** Remove in Phase 4
**Notes:** Clean break philosophy consistent with Phase 3 approach.

---

## Forgejo Redirect Verification

| Option | Description | Selected |
|--------|-------------|----------|
| API + clone test | Hit API to confirm repo at new path, test git clone, verify old URL redirect. | ✓ |
| API check only | Minimal — just confirm repo exists at new path. | |
| You decide | Claude picks verification steps. | |

**User's choice:** API + clone test
**Notes:** None

---

## Claude's Discretion

- Exact API calls and error handling for Forgejo rename/migration
- How to discover and copy repo settings during fallback path
- Specific curl commands for artifact removal and verification
- Execution order within each step

## Deferred Ideas

None — discussion stayed within phase scope
