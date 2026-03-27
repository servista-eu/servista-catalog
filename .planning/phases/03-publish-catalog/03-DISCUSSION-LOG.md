# Phase 3: Publish Catalog - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-03-27
**Phase:** 03-publish-catalog
**Areas discussed:** Coordinate transition, Version number, Publish verification

---

## Coordinate Transition

| Option | Description | Selected |
|--------|-------------|----------|
| Clean break (Recommended) | Change to eu.qabatz:qabatz-catalog only. kotlin-commons keeps working on the already-published old coordinate until Phase 6 updates it. No dual publishing. | ✓ |
| Dual publish | Publish under BOTH old and new coordinates. Ensures consumers never break, but adds complexity. | |
| Old coordinate + defer | Keep publishing as qabatz-gradle-plugins-catalog for now. Defer coordinate change to Phase 4. | |

**User's choice:** Clean break
**Notes:** Single consumer (kotlin-commons) can stay on already-published 0.2.0 of old coordinate until Phase 6 migrates it.

---

## Version Number

| Option | Description | Selected |
|--------|-------------|----------|
| 1.0.0 (Recommended) | Signals definitive catalog project. Clean starting point for new artifact identity. | |
| Keep 0.2.0 | Same content, same version. Simple but potentially confusing. | |
| Bump to 0.3.0 | Incremental bump signals change but doesn't convey significance. | |

**User's choice:** Other — 0.1.0
**Notes:** "We are still doing the groundwork, there should not even be a version yet. If you must use one, then use version 0.1.0"

---

## Publish Verification

| Option | Description | Selected |
|--------|-------------|----------|
| curl registry API (Recommended) | Hit Forgejo packages API to confirm artifact exists at expected coordinates. Automatable and scriptable. | ✓ |
| Gradle publish output | Trust ./gradlew publish exit code and output. Simplest but no registry confirmation. | |
| Manual Forgejo UI check | Check Forgejo web UI packages page. Visual but not automatable. | |

**User's choice:** curl registry API
**Notes:** None

---

## Claude's Discretion

- Order of implementation steps
- Exact curl command and response format for verification

## Deferred Ideas

None
