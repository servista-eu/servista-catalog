# Roadmap: qabatz-catalog

## Overview

Transform `qabatz-gradle-plugins` into `qabatz-catalog` by stripping all convention plugins and build configuration, verifying and publishing the version catalog as the sole artifact under new coordinates, renaming the project and repositories, and updating all consuming projects.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Strip Plugin Sources & Simplify Build** - Remove all convention plugin code, extensions, bundled configuration, and strip build to catalog-only
- [x] **Phase 2: Verify Catalog** - Confirm version catalog content is complete and correct
- [x] **Phase 3: Publish Catalog** - Publish catalog to Forgejo under new artifact coordinates
- [ ] **Phase 4: Rename Project** - Rename project locally and on Forgejo, update git remote
- [ ] **Phase 5: Configure Mirror** - Set up GitHub push mirror for the renamed repository
- [ ] **Phase 6: Update Consumers** - Migrate consuming projects to the new catalog coordinates

## Phase Details

### Phase 1: Strip Plugin Sources & Simplify Build
**Goal**: All convention plugin code, bundled configuration, and plugin-related build config are gone; the project is a pure catalog publisher that compiles
**Depends on**: Nothing (first phase)
**Requirements**: CLN-01, CLN-02, CLN-03, CLN-04, CLN-05
**Success Criteria** (what must be TRUE):
  1. No convention plugin Kotlin classes exist in the source tree
  2. Versions.kt and JooqExtension.kt no longer exist
  3. Bundled detekt.yml is removed from the project
  4. The gradlePlugin block and functional test source set are removed from build.gradle.kts
  5. build.gradle.kts contains only version catalog publishing logic
  6. The project still compiles after all deletions
**Plans:** 2 plans

Plans:
- [x] 01-01-PLAN.md -- Delete all source files and strip build.gradle.kts to catalog-only
- [x] 01-02-PLAN.md -- Clean up internal version catalog and verify final build

### Phase 2: Verify Catalog
**Goal**: The version catalog is confirmed as complete, correct, and the sole published artifact
**Depends on**: Phase 1
**Requirements**: CAT-01, CAT-02, CAT-03, CAT-04
**Success Criteria** (what must be TRUE):
  1. The version catalog is the only artifact produced by the build
  2. libs.versions.toml contains all 35 versions, 75 library aliases, and 6 plugin aliases
  3. qabatz-kotlin-commons library entries are present in the catalog
  4. qabatz-kotlin-ktor library entries are present in the catalog
**Plans:** 1 plan

Plans:
- [x] 02-01-PLAN.md -- Verify catalog integrity and update documentation with correct counts

### Phase 3: Publish Catalog
**Goal**: The catalog is published to the Forgejo Maven registry under its new artifact coordinates
**Depends on**: Phase 2
**Requirements**: CAT-05
**Success Criteria** (what must be TRUE):
  1. Running `./gradlew publish` publishes the catalog to the Forgejo Maven registry
  2. The published artifact coordinates are `eu.qabatz:qabatz-catalog`
  3. The catalog artifact is retrievable from `https://git.hestia-ng.eu/api/packages/qabatz/maven`
**Plans:** 1 plan

Plans:
- [x] 03-01-PLAN.md -- Update artifact coordinates and publish catalog to Forgejo registry

### Phase 4: Rename Project
**Goal**: The project is named qabatz-catalog everywhere -- locally, on Forgejo, and in the git remote
**Depends on**: Phase 3
**Requirements**: REN-01, REN-02, REN-03
**Success Criteria** (what must be TRUE):
  1. settings.gradle.kts declares the project name as `qabatz-catalog`
  2. The Forgejo repository is accessible at `qabatz/qabatz-catalog`
  3. The local git remote URL points to the renamed Forgejo repository
**Plans**: TBD

Plans:

### Phase 5: Configure Mirror
**Goal**: The GitHub push mirror is configured for the renamed repository
**Depends on**: Phase 4
**Requirements**: REN-04
**Success Criteria** (what must be TRUE):
  1. Push mirror is configured to target `https://github.com/servista-eu/qabatz-catalog.git`
  2. Pushes to Forgejo are mirrored to the GitHub repository
**Plans**: TBD

Plans:

### Phase 6: Update Consumers
**Goal**: All consuming projects reference the new catalog coordinates and work correctly
**Depends on**: Phase 3
**Requirements**: CON-01
**Success Criteria** (what must be TRUE):
  1. qabatz-kotlin-commons settings.gradle.kts imports `eu.qabatz:qabatz-catalog` instead of `eu.qabatz:qabatz-gradle-plugins-catalog`
  2. qabatz-kotlin-commons builds successfully with the new catalog reference
**Plans**: TBD

Plans:

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3 -> 4 -> 5 -> 6

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Strip Plugin Sources & Simplify Build | 2/2 | Complete | 2026-03-27 |
| 2. Verify Catalog | 1/1 | Complete | 2026-03-27 |
| 3. Publish Catalog | 1/1 | Complete | 2026-03-27 |
| 4. Rename Project | 0/0 | Not started | - |
| 5. Configure Mirror | 0/0 | Not started | - |
| 6. Update Consumers | 0/0 | Not started | - |
