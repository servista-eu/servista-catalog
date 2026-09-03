# DISTRIBUTION — what may leave the estate, and why

✅ **`distribution: distributable`.** This catalog is resolved by every consumer of the distributable
set, and that is exactly why what it NAMES is a boundary question rather than a convenience one.

⛔ **It names only distributable coordinates.** The 25 `commons-infra-*` and
`commons-wiring-koin-infra-*` aliases left it at **0.3.0**
([`F419 #595`](https://git.hestia-ng.eu/servista/servista-planning/issues/595), 2026-09-03): a
catalog a contractor holds is an inventory a contractor reads, and typing
`libs.servista.commons.infra.` into an editor would have listed the whole tenant-provisioning
control plane — and then resolved coordinates their credential is not meant to reach.

✅ Those aliases are now `eu.servista.infra:servista-catalog-infra`, published to the
**`servista-internal`** package owner from
[`servista-kotlin-commons-infra`](../servista-kotlin-commons-infra). The eight `backend/infra`
services declare both catalogs; nothing else declares the second.

⚠️ **`eu.servista:servista-catalog:0.2.0` still resolves and still names them.** Versions are
immutable; it is frozen, not corrected. The residue is stated in
[`07-STAGE-3-DISTRIBUTION-BOUNDARY.md`](https://git.hestia-ng.eu/servista/servista-planning/src/branch/main/work/shared-libraries/07-STAGE-3-DISTRIBUTION-BOUNDARY.md)
§5.

## What this repository must never do

⛔ **Add an alias for a coordinate whose module is `distribution: internal`.** `node
tools/check-distribution-boundary.mjs` in `servista-planning` asserts it in both directions: every
alias here resolves to a distributable module, and every internal module is absent.
