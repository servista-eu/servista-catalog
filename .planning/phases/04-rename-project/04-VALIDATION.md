---
phase: 4
slug: rename-project
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-03-27
---

# Phase 4 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Manual verification via shell commands (curl, grep, git) |
| **Config file** | N/A — no test framework needed |
| **Quick run command** | `grep 'qabatz-catalog' settings.gradle.kts && git remote get-url origin` |
| **Full suite command** | Quick run + Forgejo API verification + package deletion confirmation |
| **Estimated runtime** | ~5 seconds |

---

## Sampling Rate

- **After every task commit:** Run `grep 'qabatz-catalog' settings.gradle.kts && git remote get-url origin`
- **After every plan wave:** Run full API verification (repo exists, old packages gone, mirror configured)
- **Before `/gsd:verify-work`:** Full suite must be green
- **Max feedback latency:** 5 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 04-01-01 | 01 | 1 | REN-01 | smoke | `grep 'qabatz-catalog' settings.gradle.kts` | N/A (inline) | ⬜ pending |
| 04-01-02 | 01 | 1 | REN-02 | smoke | `curl -s -o /dev/null -w "%{http_code}" -H "Authorization: token $TOKEN" "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-catalog"` (expect 200) | N/A (inline) | ⬜ pending |
| 04-01-03 | 01 | 1 | REN-03 | smoke | `git remote get-url origin \| grep qabatz-catalog.git` | N/A (inline) | ⬜ pending |
| 04-02-01 | 02 | 2 | D-06 | smoke | `grep 'qabatz-catalog' CLAUDE.md \| head -1` | N/A (inline) | ⬜ pending |
| 04-02-02 | 02 | 2 | D-07 | smoke | `grep -r 'qabatz-gradle-plugins' .planning/codebase/ \| wc -l` (expect 0) | N/A (inline) | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. No test framework needed — all verification is done via shell commands against the Forgejo API and git.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Forgejo repo accessible at new URL | REN-02 | External API call required | `curl -s -H "Authorization: token $TOKEN" "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-catalog" \| python3 -c "import sys,json; print(json.load(sys.stdin)['name'])"` — expect `qabatz-catalog` |
| Old packages deleted | D-04 | External API call required | `curl -s -o /dev/null -w "%{http_code}" -H "Authorization: token $TOKEN" "https://git.hestia-ng.eu/api/v1/packages/qabatz/maven/eu.qabatz%3Aqabatz-gradle-plugins-catalog/0.2.0"` — expect 404 |
| Push mirror points to new target | D-03 | External API call required | `curl -s -H "Authorization: token $TOKEN" "https://git.hestia-ng.eu/api/v1/repos/qabatz/qabatz-catalog/push_mirrors"` — verify `remote_address` contains `servista-eu/qabatz-catalog` |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 5s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
