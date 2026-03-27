# qabatz-catalog

## What This Is

A shared Gradle version catalog providing centralized dependency version alignment for all Qabatz Kotlin projects. Published as `eu.qabatz:qabatz-catalog` to the Forgejo Maven registry, consumed by projects like qabatz-kotlin-commons, qabatz-kotlin-ktor, and future Qabatz services. Each consuming project owns its own build-logic convention plugins locally; this project only manages the version catalog.

## Core Value

Single source of truth for dependency versions across the entire Qabatz ecosystem — preventing version drift and dependency conflicts between projects.

## Requirements

### Validated

- ✓ Published version catalog with 35 versions, 75 library aliases, 6 plugin aliases — existing
- ✓ Consumed by qabatz-kotlin-commons (0.2.0) — existing
- ✓ Includes qabatz-kotlin-commons (0.2.0) and qabatz-kotlin-ktor (0.1.1) library entries — existing
- ✓ Strip all convention plugins (8 plugins, Versions.kt, JooqExtension.kt, bundled detekt.yml) — Validated in Phase 1: Strip Plugin Sources
- ✓ Simplify build.gradle.kts to only publish the version catalog — Validated in Phase 1: Strip Plugin Sources
- ✓ Version catalog confirmed as sole published artifact (CAT-01) — Validated in Phase 2: Verify Catalog
- ✓ Catalog entry counts verified: 35 versions, 75 libraries, 6 plugins (CAT-02) — Validated in Phase 2: Verify Catalog
- ✓ 9 qabatz-kotlin-commons entries present in catalog (CAT-03) — Validated in Phase 2: Verify Catalog
- ✓ 1 qabatz-kotlin-ktor entry present in catalog (CAT-04) — Validated in Phase 2: Verify Catalog

### Active

- [ ] Rename project from `qabatz-gradle-plugins` to `qabatz-catalog`
- [ ] Rename Forgejo repository (or delete and recreate as `qabatz-catalog`)
- [ ] Update push mirror to `https://github.com/servista-eu/qabatz-catalog.git` (reuse existing GitHub token)
- [ ] Update published artifact coordinates to `eu.qabatz:qabatz-catalog`
- [ ] Update consuming projects' references (kotlin-commons settings.gradle.kts catalog import)

### Out of Scope

- Convention plugins — each project owns its own build-logic locally (pattern established by kotlin-commons refactoring)
- JooqPlugin logic — preserved in git history, migration instructions written to kotlin-ktor's TODO-BUILD-LOGIC.md
- Bundled detekt configuration — each project maintains its own
- CI/CD pipeline — not in scope for this milestone

## Context

- This project was originally `qabatz-gradle-plugins` containing 8 convention plugins + a version catalog
- After heavy refactoring of qabatz-kotlin-commons, the pattern shifted: each project owns its build-logic locally (included build with precompiled script plugins)
- qabatz-kotlin-commons already uses only the version catalog from this project, not the plugins
- qabatz-kotlin-ktor will follow the same pattern when refactored (JooqPlugin migration instructions prepared)
- The Forgejo instance is at `git.hestia-ng.eu` under the `qabatz` organization
- Publishing target: Forgejo Maven registry at `https://git.hestia-ng.eu/api/packages/qabatz/maven`
- Push mirror exists to GitHub under `servista-eu` organization

## Constraints

- **Backward compatibility**: Consuming projects reference `eu.qabatz:qabatz-gradle-plugins-catalog:0.2.0` — coordinate change requires updating all consumers
- **Forgejo credentials**: Repository uses embedded token in git remote URL (user `heaphopdancer`)
- **GitHub mirror**: Push mirror token must be reused from current Forgejo repo configuration

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Remove all 8 convention plugins | Each project owns build-logic locally; plugins are unused by kotlin-commons and will be unused by kotlin-ktor after its refactoring | ✓ Done (Phase 1) |
| Rename to qabatz-catalog | Reflects the project's actual purpose (version catalog only, not plugins) | — Pending |
| Preserve JooqPlugin via docs, not code | Complex logic (177 lines) preserved in git history + migration doc in kotlin-ktor; no active consumers after ktor refactoring | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-03-27 after Phase 2 completion*
