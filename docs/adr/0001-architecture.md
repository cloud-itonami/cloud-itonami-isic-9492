# ADR-0001: PartyOps-LLM ⊣ Political Organization Governance Governor architecture

## Status

Accepted. `cloud-itonami-isic-9492` promoted from `:blueprint` to
`:implemented` in the `kotoba-lang/industry` registry.

## Context

`cloud-itonami-isic-9492` publishes an OSS business blueprint for
activities of political organizations: political party and political-
advocacy-group administration. Like every prior actor in this fleet,
the blueprint alone is not an implementation: this ADR records the
governed-actor architecture that promotes it to real, tested code,
following the same langgraph StateGraph + independent Governor +
Phase 0→3 rollout pattern established by `cloud-itonami-isic-6511`
(life insurance) and applied across seventy-three prior siblings,
most recently `cloud-itonami-isic-9312` (sports clubs).

## Decision

### Decision 1: single-actuation shape, and selection rationale

This blueprint's own README/business-model.md/operator-guide.md
consistently name only ONE real-world act: "publishing a public
political position or endorsement on the organization's behalf" --
both "position" and "endorsement" treated as ONE conceptual act,
`high-stakes` a one-member set `#{:actuation/publish-position}`, a
POSITIVE actuation (committing a real publication record). Following
`sportsclub`/9312's own precedent of surveying every remaining
`:blueprint`-tier candidate's `:itonami.blueprint/governor` keyword
for collisions before selecting, this build confirmed ISIC 9492's own
`:political-organization-governance-governor` has NO collision with
any already-implemented sibling (distinct from `association`/9412's,
`congregation`/9491's, `union`/9420's and `sportsclub`/9312's own
governor names) -- one of only two genuinely clean candidates
remaining at build time (the other being `9499`, other membership
organizations n.e.c.). ISIC 9492 was selected over 9499 for its
richer, more specific real-world regulatory grounding (campaign-
finance-disclosure law is a well-documented, universal concern;
9499's own "publishing a public position" text has no comparably
specific hook).

### Decision 2: entity and op shape

The primary entity is a `position` (covering both policy-position
statements and endorsements uniformly, since the actuation treats
them as one act), matching the business-model.md's own Offer language
("platform/policy-position proposal", "endorsement proposal"). Four
ops: `:member/intake` (directory upsert, no capital risk),
`:position/verify` (per-jurisdiction campaign-finance-disclosure
evidence checklist, never auto), `:disclaimer/screen` (campaign-
finance-disclaimer screening, unconditional-evaluation discipline,
never auto), and `:actuation/publish-position` (POSITIVE, high-stakes
-- publishing a real public political position or endorsement).

### Decision 3: `campaign-finance-disclaimer-missing-violations` -- the 59th unconditional-evaluation screening grounding, a genuinely new concept

Before writing this check, every prior sibling's governor/registry/
facts namespaces were grepped for "disclaimer", "imprint" and
"campaign-finance" -- zero hits, confirming this is a genuinely new
unconditional-evaluation concept. It reuses the unconditional-
evaluation DISCIPLINE (`casualty.governor/sanctions-violations`'s
original fix) for the 59th distinct application overall, continuing
the count established across this fleet's builds (most recently
`sportsclub.governor/disciplinary-complaint-unresolved-violations` at
58th). Grounded in real campaign-finance-disclosure law: US Federal
Election Campaign Act 52 U.S.C. §30120 ("paid for by" disclaimers),
UK Political Parties, Elections and Referendums Act 2000 imprint
rules, and Germany's Impressumspflicht (Medienstaatsvertrag §18).
Gates `:disclaimer/screen` and the actuation.

### Decision 4: `member-consensus-share-insufficient?` -- an honest reuse of this fleet's ratio-based check family, the fourth instance

`leasing.registry/collateral-coverage-ratio-insufficient?` established
the first instance of this fleet's ratio-based check family (MINIMUM-
floor direction); `behavioral.registry/supervision-ratio-
insufficient?` the second (MAXIMUM-ceiling direction);
`union.registry/strike-vote-share-insufficient?` the third (MINIMUM-
floor direction, votes-in-favor/votes-cast vs. required-majority-
share). `partyops.registry/member-consensus-share-insufficient?`
reuses the exact same quotient-comparison shape for the FOURTH
instance, MINIMUM-floor direction like `leasing`'s/`union`'s, applied
to a position's own recorded votes-in-favor/votes-cast against its own
recorded required-consensus-share. An honest reuse of the family
shape, not claimed as new -- grounded in the same real governance
concern `union`/9420's own check addresses (genuine member/governing-
body consensus, not an LLM-assumed one), applied here to endorsement/
position publication rather than strike authorization. Gates only the
actuation (a pure ground-truth recompute).

### Decision 5: dedicated double-actuation-guard boolean

`:published?` is a dedicated boolean on the `position` record, never
a single `:status` value -- the same discipline every prior sibling
governor's guards establish, informed by `cloud-itonami-isic-6492`'s
status-lifecycle bug (ADR-2607071320).

### Decision 6: Store protocol, MemStore + DatomicStore parity

`partyops.store/Store` is implemented by both `MemStore` (atom-
backed, default for dev/tests/demo) and `DatomicStore` (`langchain.
db`-backed), proven to satisfy the same contract in `test/
partyops/store_contract_test.clj` -- the same seam every sibling
actor uses so swapping the SSoT backend is a configuration change,
not a rewrite. The protocol's per-entity accessor is named `position`
directly -- not a Clojure special form, so no `-of` suffix workaround
was needed.

### Decision 7: Phase 0→3 rollout

Phase 3's `:auto` set has exactly one member, `:member/intake` (no
capital risk). `:position/verify` and `:disclaimer/screen` are never
auto-eligible at any phase (matching every sibling's screening/
verification-op posture), and `:actuation/publish-position` is
permanently excluded from every phase's `:auto` set -- a structural
fact, not a rollout milestone, enforced by BOTH `partyops.phase` and
`partyops.governor`'s `high-stakes` set independently.

### Decision 8: no bespoke domain capability lib

This blueprint's own `:itonami.blueprint/required-technologies`
names no domain-specific capability beyond the generic robotics/
identity/forms/dmn/bpmn/audit-ledger stack -- there was no
capability-lib decision to make at all.

### Decision 9: mock + LLM advisor pair

`partyops.partyopsllm` provides `mock-advisor` (deterministic,
default everywhere -- the actor graph and governor contract run
offline) and `llm-advisor` (backed by `langchain.model/ChatModel`,
with a defensive EDN-proposal parser so a malformed LLM response
degrades to a safe low-confidence noop rather than ever auto-
publishing a position).

### Decision 10: no `blueprint.edn` field-sync fixes needed

Matching `photo`/7420's, `personalservice`/9609's, `edsupport`/8550's,
`headoffice`/7010's, `residential`/8790's, `cultural`/8542's,
`reserve`/6411's, `proserv`/7490's, `sportsevent`/9319's,
`recreation`/9329's and `sportsclub`/9312's own experience, this
repo's `blueprint.edn` already had the correct `isic-` prefixed `:id`
and correctly populated `:required-technologies`/`:optional-
technologies` matching the `kotoba-lang/industry` registry's own
entry for `"9492"` exactly -- only the `:maturity` field itself
needed adding.

## Alternatives considered

- **Building `cloud-itonami-isic-9499` (other membership organizations
  n.e.c.) instead.** Considered viable (no governor-name collision
  either), but rejected in favor of 9492 for richer, more specific
  real-world regulatory grounding -- campaign-finance-disclosure law
  is well-documented and universal, while 9499's own generic
  "publishing a public position" text offered no comparably specific
  hook to design a genuinely new check against.
- **Framing `member-consensus-share-insufficient?` as a new concept.**
  Rejected: it is structurally identical to `union.registry/strike-
  vote-share-insufficient?`'s own ratio-comparison shape; honestly
  characterizing it as a reuse matches this fleet's precedent-
  verification discipline.
- **A dual-actuation shape** (splitting "position" and "endorsement"
  into two acts). Rejected: the blueprint's own text consistently
  names only ONE real-world act.

## Consequences

- Seventy-fifth actor promoted in this fleet's registry (74
  implemented before this build).
- Establishes a genuinely NEW unconditional-evaluation-screening
  concept (campaign-finance-disclaimer-missing), grep-verified absent
  from every prior sibling before the claim was finalized.
- Documents an honest reuse of this fleet's ratio-based check family
  (member-consensus-share-insufficient, the 4th instance), not
  claimed as new.
- `MemStore` ‖ `DatomicStore` parity is proven by `test/partyops/
  store_contract_test.clj`.
- `blueprint.edn` required no field-sync fixes this time (already
  correct) -- only the `:maturity` flip itself.
- Only ONE genuinely clean `:blueprint`-tier candidate (`9499`) now
  remains that is not blocked by an exact governor-name collision
  with an already-implemented sibling, per the survey `sportsclub`/
  9312's own ADR-0001 recorded and this build re-confirmed.

## References

- `orgs/cloud-itonami/cloud-itonami-isic-9492/README.md`
- `orgs/cloud-itonami/cloud-itonami-isic-9492/docs/business-model.md`
- `orgs/cloud-itonami/cloud-itonami-isic-9420/src/union/registry.cljc` (`strike-vote-share-insufficient?` origin)
- `orgs/kotoba-lang/industry/resources/kotoba/industry/registry.edn` (entry `"9492"`)
