---
phase: 3
slug: publish-catalog
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-27
---

# Phase 3 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | None (no test infrastructure in project) |
| **Config file** | None |
| **Quick run command** | `./gradlew generateCatalogAsToml` |
| **Full suite command** | `./gradlew generatePomFileForVersionCatalogPublication` + POM inspection |
| **Estimated runtime** | ~5 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew generateCatalogAsToml`
- **After every plan wave:** Run `./gradlew generatePomFileForVersionCatalogPublication` + POM coordinate check
- **Before `/gsd:verify-work`:** Successful publish + curl verification returning HTTP 200
- **Max feedback latency:** 10 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 03-01-01 | 01 | 1 | CAT-05 | smoke | `./gradlew generatePomFileForVersionCatalogPublication && grep -q 'qabatz-catalog' build/publications/versionCatalog/pom-default.xml` | N/A (build output) | ⬜ pending |
| 03-01-02 | 01 | 1 | CAT-05 | manual | `curl -sf -u "token:$FORGEJO_TOKEN" "https://git.hestia-ng.eu/api/packages/qabatz/maven/eu/qabatz/qabatz-catalog/0.1.0/qabatz-catalog-0.1.0.toml" -o /dev/null -w "%{http_code}"` | N/A (manual) | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. No test framework needed — validation is done through build output inspection and HTTP verification.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Artifact retrievable from Forgejo registry | CAT-05 | Requires live HTTP access to Forgejo with valid credentials | Run curl with auth token against the package URL; expect HTTP 200 |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 10s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
