# Requirements: qabatz-catalog

**Defined:** 2026-03-27
**Core Value:** Single source of truth for dependency versions across the entire Qabatz ecosystem

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Catalog

- [ ] **CAT-01**: Version catalog (`libs.versions.toml`) is the sole published artifact
- [ ] **CAT-02**: Catalog includes all current dependency versions (35 versions, 75 libraries, 6 plugins)
- [ ] **CAT-03**: Catalog includes qabatz-kotlin-commons library entries
- [ ] **CAT-04**: Catalog includes qabatz-kotlin-ktor library entries
- [ ] **CAT-05**: Catalog is published to Forgejo Maven registry as `eu.qabatz:qabatz-catalog`

### Cleanup

- [x] **CLN-01**: All 8 convention plugin classes are removed
- [x] **CLN-02**: Versions.kt and JooqExtension.kt are removed
- [x] **CLN-03**: Bundled detekt.yml configuration is removed
- [x] **CLN-04**: Plugin-related build configuration (gradlePlugin block, functional test source set) is removed
- [x] **CLN-05**: Build.gradle.kts only contains version catalog publishing logic

### Rename

- [ ] **REN-01**: Project name changed to `qabatz-catalog` in settings.gradle.kts
- [ ] **REN-02**: Forgejo repository renamed (or recreated) as `qabatz/qabatz-catalog`
- [ ] **REN-03**: Git remote URL updated to new repository name
- [ ] **REN-04**: Push mirror configured to `https://github.com/servista-eu/qabatz-catalog.git` using existing GitHub token

### Consumers

- [ ] **CON-01**: qabatz-kotlin-commons settings.gradle.kts updated to import `eu.qabatz:qabatz-catalog` instead of `eu.qabatz:qabatz-gradle-plugins-catalog`

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Automation

- **AUT-01**: CI/CD pipeline for automated catalog publishing
- **AUT-02**: Dependency update automation (Renovate/Dependabot)

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Convention plugins | Each project owns build-logic locally (pattern from kotlin-commons refactoring) |
| JooqPlugin preservation | Preserved in git history + migration doc in kotlin-ktor |
| kotlin-ktor refactoring | Separate project, separate milestone |
| Detekt config distribution | Each project maintains its own |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| CAT-01 | Phase 3 | Pending |
| CAT-02 | Phase 3 | Pending |
| CAT-03 | Phase 3 | Pending |
| CAT-04 | Phase 3 | Pending |
| CAT-05 | Phase 4 | Pending |
| CLN-01 | Phase 1 | Complete |
| CLN-02 | Phase 1 | Complete |
| CLN-03 | Phase 1 | Complete |
| CLN-04 | Phase 2 | Complete |
| CLN-05 | Phase 2 | Complete |
| REN-01 | Phase 5 | Pending |
| REN-02 | Phase 5 | Pending |
| REN-03 | Phase 5 | Pending |
| REN-04 | Phase 6 | Pending |
| CON-01 | Phase 7 | Pending |

**Coverage:**
- v1 requirements: 15 total
- Mapped to phases: 15
- Unmapped: 0

---
*Requirements defined: 2026-03-27*
*Last updated: 2026-03-27 after roadmap creation*
