# Phase 1: Strip Plugin Sources - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-03-27
**Phase:** 01-strip-plugin-sources
**Areas discussed:** Compilation boundary, Directory cleanup, JooqPlugin migration doc

---

## Compilation Boundary

| Option | Description | Selected |
|--------|-------------|----------|
| Minimal build edits (Recommended) | Phase 1 removes just enough from build.gradle.kts to keep compilation working | |
| Delete sources only, accept broken build | Phase 1 strictly deletes source files. Build breaks temporarily | |
| Merge Phase 1 and 2 | Combine source deletion and build simplification into a single phase | |

**User's choice:** Initially deferred to Claude's recommendation ("Please advise"). Claude recommended "Minimal build edits."

**Revision:** User asked Claude to check if any recommendations had conflicts. Claude identified that "Minimal build edits" contradicts requirement CLN-04 (mapped to Phase 2 in REQUIREMENTS.md). Revised options presented.

| Option (Revised) | Description | Selected |
|--------|-------------|----------|
| Merge Phases 1 & 2 (Recommended) | Combine source deletion + build simplification into one phase | ✓ |
| Keep separate, broken build OK | Phase 1 deletes sources, build breaks temporarily, Phase 2 fixes it | |
| Keep original advice | Phase 1 does minimal build edits, pulling some CLN-04 work into Phase 1 | |

**User's choice:** Merge Phases 1 & 2
**Notes:** Eliminates the artificial boundary between tightly coupled cleanup work. CLN-01 through CLN-05 all handled together.

---

## Directory Cleanup

| Option | Description | Selected |
|--------|-------------|----------|
| Remove everything under src/ (Recommended) | Delete the entire src/ directory tree. No source code remains after stripping plugins | ✓ |
| Remove only empty leaf directories | Delete empty directories but keep src/main/ structure | |
| Leave directories in place | Only delete files, leave empty directory skeleton | |

**User's choice:** Remove everything under src/
**Notes:** Clean slate. Project becomes a pure catalog publisher with no source code.

---

## JooqPlugin Migration Doc

| Option | Description | Selected |
|--------|-------------|----------|
| Already exists | TODO-BUILD-LOGIC.md is already written in kotlin-ktor | ✓ |
| Create before deleting | Extract JooqPlugin logic into a migration guide before deletion | |
| Not needed — git history is enough | Code preserved in git history, no migration doc needed | |

**User's choice:** Already exists (verified by Claude)
**Notes:** User said "I think it already exists, but double-check to make sure." Claude verified the file exists at `qabatz-kotlin-ktor/TODO-BUILD-LOGIC.md` with comprehensive content covering full plugin logic, XML config template, known issues, and migration approach.

---

## Claude's Discretion

- Order of deletions within the phase (source files first vs build config first)
- Whether to verify compilation incrementally or only at the end

## Deferred Ideas

- Roadmap renumbering required due to Phase 1/2 merge (subsequent phases shift down)
