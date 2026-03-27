---
phase: 2
slug: verify-catalog
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-27
---

# Phase 2 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | None — no test framework; Gradle tasks + shell commands |
| **Config file** | none — no test infrastructure needed |
| **Quick run command** | `./gradlew generateCatalogAsToml` |
| **Full suite command** | `./gradlew generateCatalogAsToml outgoingVariants` |
| **Estimated runtime** | ~5 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew generateCatalogAsToml`
- **After every plan wave:** Run `./gradlew generateCatalogAsToml outgoingVariants`
- **Before `/gsd:verify-work`:** Full suite must be green
- **Max feedback latency:** 5 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | CAT-01 | smoke | `./gradlew outgoingVariants 2>&1 \| grep -c "Variant "` (expect: 1) | N/A (CLI) | ⬜ pending |
| 02-01-02 | 01 | 1 | CAT-02 | smoke | `awk` count commands on catalog/libs.versions.toml | N/A (CLI) | ⬜ pending |
| 02-01-03 | 01 | 1 | CAT-03 | smoke | `grep -c "qabatz-commons" catalog/libs.versions.toml` (expect: 9) | N/A (CLI) | ⬜ pending |
| 02-01-04 | 01 | 1 | CAT-04 | smoke | `grep -c "qabatz-ktor" catalog/libs.versions.toml` (expect: 1) | N/A (CLI) | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. This phase uses Gradle tasks and shell commands for verification, not a test framework. No test infrastructure needed.

---

## Manual-Only Verifications

All phase behaviors have automated verification.

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 5s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
