# cloud-itonami-isic-9492

Open Business Blueprint for **ISIC Rev.5 9492**: Activities of
political organizations.

This repository publishes a political-organization-governance actor --
member/supporter intake, per-jurisdiction campaign-finance-disclosure
regulatory assessment, campaign-finance-disclaimer screening and
public-position/endorsement publication -- as an OSS business that
any qualified operator can fork, deploy, run, improve and sell, so a
community or independent provider never surrenders member/supporter
data and ledgers to a closed SaaS.

Built on this workspace's
[`langgraph`](https://github.com/kotoba-lang/langgraph)
StateGraph runtime (portable `.cljc`, supervised superstep loop,
interrupts, Datomic/in-mem checkpoints) -- the same actor pattern as
every prior actor in this fleet
([`cloud-itonami-isic-6511`](https://github.com/cloud-itonami/cloud-itonami-isic-6511),
[`6512`](https://github.com/cloud-itonami/cloud-itonami-isic-6512),
[`6621`](https://github.com/cloud-itonami/cloud-itonami-isic-6621),
[`6622`](https://github.com/cloud-itonami/cloud-itonami-isic-6622),
[`6629`](https://github.com/cloud-itonami/cloud-itonami-isic-6629),
[`6520`](https://github.com/cloud-itonami/cloud-itonami-isic-6520),
[`6530`](https://github.com/cloud-itonami/cloud-itonami-isic-6530),
[`6820`](https://github.com/cloud-itonami/cloud-itonami-isic-6820),
[`6612`](https://github.com/cloud-itonami/cloud-itonami-isic-6612),
[`6492`](https://github.com/cloud-itonami/cloud-itonami-isic-6492),
[`6920`](https://github.com/cloud-itonami/cloud-itonami-isic-6920),
[`6611`](https://github.com/cloud-itonami/cloud-itonami-isic-6611),
[`7120`](https://github.com/cloud-itonami/cloud-itonami-isic-7120),
[`8620`](https://github.com/cloud-itonami/cloud-itonami-isic-8620),
[`8530`](https://github.com/cloud-itonami/cloud-itonami-isic-8530),
[`9200`](https://github.com/cloud-itonami/cloud-itonami-isic-9200),
[`7500`](https://github.com/cloud-itonami/cloud-itonami-isic-7500),
[`9603`](https://github.com/cloud-itonami/cloud-itonami-isic-9603),
[`9521`](https://github.com/cloud-itonami/cloud-itonami-isic-9521),
[`9321`](https://github.com/cloud-itonami/cloud-itonami-isic-9321),
[`8730`](https://github.com/cloud-itonami/cloud-itonami-isic-8730),
[`9102`](https://github.com/cloud-itonami/cloud-itonami-isic-9102),
[`9103`](https://github.com/cloud-itonami/cloud-itonami-isic-9103),
[`9602`](https://github.com/cloud-itonami/cloud-itonami-isic-9602),
[`9000`](https://github.com/cloud-itonami/cloud-itonami-isic-9000),
[`8890`](https://github.com/cloud-itonami/cloud-itonami-isic-8890),
[`8610`](https://github.com/cloud-itonami/cloud-itonami-isic-8610),
[`9311`](https://github.com/cloud-itonami/cloud-itonami-isic-9311),
[`8510`](https://github.com/cloud-itonami/cloud-itonami-isic-8510),
[`9412`](https://github.com/cloud-itonami/cloud-itonami-isic-9412),
[`6491`](https://github.com/cloud-itonami/cloud-itonami-isic-6491),
[`8720`](https://github.com/cloud-itonami/cloud-itonami-isic-8720),
[`8521`](https://github.com/cloud-itonami/cloud-itonami-isic-8521),
[`6619`](https://github.com/cloud-itonami/cloud-itonami-isic-6619),
[`3600`](https://github.com/cloud-itonami/cloud-itonami-isic-3600),
[`6190`](https://github.com/cloud-itonami/cloud-itonami-isic-6190),
[`3030`](https://github.com/cloud-itonami/cloud-itonami-isic-3030),
[`3830`](https://github.com/cloud-itonami/cloud-itonami-isic-3830),
[`7020`](https://github.com/cloud-itonami/cloud-itonami-isic-7020),
[`9420`](https://github.com/cloud-itonami/cloud-itonami-isic-9420),
[`9491`](https://github.com/cloud-itonami/cloud-itonami-isic-9491),
[`2610`](https://github.com/cloud-itonami/cloud-itonami-isic-2610),
[`3512`](https://github.com/cloud-itonami/cloud-itonami-isic-3512),
[`8810`](https://github.com/cloud-itonami/cloud-itonami-isic-8810),
[`8691`](https://github.com/cloud-itonami/cloud-itonami-isic-8691),
[`8569`](https://github.com/cloud-itonami/cloud-itonami-isic-8569),
[`6419`](https://github.com/cloud-itonami/cloud-itonami-isic-6419),
[`7310`](https://github.com/cloud-itonami/cloud-itonami-isic-7310),
[`7320`](https://github.com/cloud-itonami/cloud-itonami-isic-7320),
[`7210`](https://github.com/cloud-itonami/cloud-itonami-isic-7210),
[`7410`](https://github.com/cloud-itonami/cloud-itonami-isic-7410),
[`8710`](https://github.com/cloud-itonami/cloud-itonami-isic-8710),
[`8541`](https://github.com/cloud-itonami/cloud-itonami-isic-8541),
[`8690`](https://github.com/cloud-itonami/cloud-itonami-isic-8690),
[`9601`](https://github.com/cloud-itonami/cloud-itonami-isic-9601),
[`6420`](https://github.com/cloud-itonami/cloud-itonami-isic-6420),
[`7420`](https://github.com/cloud-itonami/cloud-itonami-isic-7420),
[`9609`](https://github.com/cloud-itonami/cloud-itonami-isic-9609),
[`8550`](https://github.com/cloud-itonami/cloud-itonami-isic-8550),
[`7010`](https://github.com/cloud-itonami/cloud-itonami-isic-7010),
[`8790`](https://github.com/cloud-itonami/cloud-itonami-isic-8790),
[`8542`](https://github.com/cloud-itonami/cloud-itonami-isic-8542),
[`6411`](https://github.com/cloud-itonami/cloud-itonami-isic-6411),
[`7490`](https://github.com/cloud-itonami/cloud-itonami-isic-7490),
[`9319`](https://github.com/cloud-itonami/cloud-itonami-isic-9319),
[`9329`](https://github.com/cloud-itonami/cloud-itonami-isic-9329),
[`9312`](https://github.com/cloud-itonami/cloud-itonami-isic-9312)) --
here it is **PartyOps-LLM ⊣ Political Organization Governance
Governor**.

> **Why an actor layer at all?** An LLM is great at drafting a
> member/supporter intake summary, normalizing records, and checking
> whether a position's own recorded member-consensus vote share
> actually stays above its own recorded required threshold -- but it
> has **no notion of which jurisdiction's campaign-finance-disclosure
> law is official, no authority to publish a real public political
> position or endorsement, and no way to know on its own whether a
> mandatory campaign-finance disclaimer has actually been included**.
> Letting it publish a position directly invites fabricated regulatory
> citations, a position being published without a legally required
> disclaimer, and a fabricated member-consensus claim being quietly
> accepted -- and liability, and reputational risk, for whoever runs
> it. This project seals the PartyOps-LLM into a single node and wraps
> it with an independent **Political Organization Governance
> Governor**, a human **approval workflow**, and an immutable **audit
> ledger**.

## Scope: what this actor does and does not do

This actor covers member/supporter intake through campaign-finance-
disclosure regulatory assessment, disclaimer screening and position/
endorsement publication. It does **not**, by itself, hold any license
or registration required to operate a political organization in a
given jurisdiction, and it does not claim to. It also does not
adjudicate the underlying policy debate itself --
`partyops.registry/member-consensus-share-insufficient?` is a pure
ground-truth recompute against the position's own recorded vote
fields, not a political judgment. Whoever deploys and operates a live
instance (a licensed political organization) supplies any
jurisdiction-specific registration, the real campaign-finance-
compliance process and the real membership-management-system
integrations, and bears that jurisdiction's liability -- the software
supplies the governed, spec-cited, audited execution scaffold so that
operator does not have to build the compliance layer from scratch.

### Actuation

**Publishing a real public political position or endorsement is never
autonomous, at any phase, by construction.** Two independent layers
enforce this (`partyops.governor`'s `:actuation/publish-position`
high-stakes gate and `partyops.phase`'s phase table, which never puts
`:actuation/publish-position` in any phase's `:auto` set) -- see
`partyops.phase`'s docstring and `test/partyops/phase_test.clj`'s
`publish-position-never-auto-at-any-phase`. The actor may draft,
check and recommend; a human governing-body officer is always the
one who actually publishes a position. Matching `leasing`'s/
`underwriting`'s/`testlab`'s/`clinic`'s/`veterinary`'s/`funeral`'s/
`parksafety`'s/`salon`'s/`entertainment`'s/`facility`'s/
`consulting`'s/`advertising`'s/`polling`'s/`research`'s/`design`'s/
`sports`'s/`alliedhealth`'s/`photo`'s/`personalservice`'s/
`edsupport`'s/`cultural`'s/`proserv`'s/`sportsevent`'s/`recreation`'s/
`sportsclub`'s single-actuation shape, grounded directly in this
blueprint's own README text ("No automated proposal, by itself, can
complete the following without governor approval and audit evidence:
publishing a public political position or endorsement on the
organization's behalf") -- both "position" and "endorsement" are
treated as ONE conceptual act (`:actuation/publish-position`), a
POSITIVE actuation (committing a real publication record), matching
this fleet's majority actuation shape (`3600`/`6190` are the fleet's
two NEGATIVE-actuation exceptions).

### Campaign-conduct legality (`:material/screen`)

`:disclaimer/screen` answers **one boolean** -- is the required
disclaimer present. It does not answer whether the campaign method
itself is lawful, and those are genuinely different questions:
a leaflet can carry a perfect imprint and still be illegal to
distribute, and door-to-door canvassing is a **crime in Japan**
(公職選挙法 §138) while being constitutionally **protected** in the
United States (Watchtower v. Village of Stratton, 536 U.S. 150 (2002))
and affirmatively **guaranteed access** in Canada (Canada Elections
Act s.81). An advisor carrying one jurisdiction's campaign norms into
another does not produce a suboptimal plan; it produces a criminal one.

`:material/screen` re-runs `kotoba-lang/senkyo`'s legality screen
through `partyops.material` and HARD-holds on the result. Three
disciplines, each with a test that fails when it is removed:

- **The jurisdiction comes from the POSITION record, never the
  proposal.** If the advisor supplied the jurisdiction it could choose
  the law it is judged under.
- **The advisor's claimed verdict is never read as an input.** The
  governor recomputes, then compares -- a disagreement raises its own
  named violation (`:screen-verdict-mismatch`) separate from the
  underlying illegality (`:campaign-conduct-not-permitted`). Same
  discipline as check 4's independent ratio recompute.
- **`:undetermined` HARD-holds too.** An unresolved screen is not an
  escalation a human may approve; the remedy is to supply the missing
  attribute and re-run. There is no "approve your way past not
  knowing" path.

**Structurally absent, not unimplemented.** `partyops.material/out-of-
scope-intents` re-exports `senkyo.screen/out-of-scope-intents`
verbatim -- this actor does not restate the boundary, because a
boundary restated per actor drifts per actor. A request carrying any
of `:voter-targeting`, `:vote-persuasion-script`, `:candidate-ranking`,
`:turnout-operation`, `:distribution-dispatch`, `:opinion-profiling` or
`:eligibility-adjudication` is stopped before any screening runs. These
are not gated by risk level, cannot be escalated for human override,
and the proposal vocabulary has no path to construct them.

## The core contract

```
member/supporter intake + jurisdiction facts (partyops.facts, spec-cited)
        |
        v
   ┌───────────────────────┐   proposal      ┌───────────────────────┐
   │ PartyOps-LLM          │ ─────────────▶ │ Political Org.                 │  (independent system)
   │ (sealed)              │  + citations    │ Governance Governor:         │
   └───────────────────────┘                 │ spec-basis · evidence-       │
          │                 commit ◀┼ incomplete · campaign-finance-    │
          │                         │ disclaimer-missing                    │
    record + ledger        escalate ┼ (unconditional, NEW) · member-         │
          │              (ALWAYS for│ consensus-share-insufficient             │
          │               :actuation│ (ratio-based, honest reuse) ·             │
          │               /publish- │ already-published                          │
          ▼               position) └───────────────────────┘
      human approval
```

**The PartyOps-LLM never publishes a position the Political
Organization Governance Governor would reject, and never does so
without a human sign-off.** Hard violations (fabricated regulatory
requirements; unsupported evidence; a missing campaign-finance
disclaimer; an insufficient member-consensus vote share; a double
publication) force **hold** and *cannot* be approved past; a clean
publication proposal still always routes to a human.

## Run

```bash
clojure -M:dev:run     # walk one clean single-actuation lifecycle + four HARD-hold cases through the actor
clojure -M:dev:test    # governor contract · phase invariants · store parity · registry conformance · facts coverage
clojure -M:lint        # clj-kondo (errors fail; CI mirrors this)
```

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot
performs the physical domain work**. Here a document-courier robot
handles physical member-mailing fulfillment where used, under the
actor, gated by the independent **Political Organization Governance
Governor**. The governor never dispatches hardware itself;
`:high`/`:safety-critical` actions require human sign-off.

## Open business

This repository is not only source code. It is a public, forkable
business model:

| Layer | What is open |
|---|---|
| OSS core | Actor runtime, Political Organization Governance Governor, position-publication draft records, audit ledger |
| Business blueprint | Customer, offer, pricing, unit economics, sales motion |
| Operator playbook | How to fork, license, deploy and support the service in a jurisdiction |
| Trust controls | Governance, security reporting, actuation invariant, audit requirements |

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md) to start this as an
open business on itonami.cloud, and
[`docs/adr/0001-architecture.md`](docs/adr/0001-architecture.md) for the
full architecture and decision record.

## Capability layer

This blueprint resolves its technology stack via
[`kotoba-lang/industry`](https://github.com/kotoba-lang/industry) (ISIC
`9492`). This vertical's member/operational records are practice-
specific rather than a shared cross-operator data contract, so
`partyops.*` runs on the generic robotics/identity/forms/dmn/bpmn/
audit-ledger stack only -- no bespoke domain capability lib to
reference at all.

## Layout

| File | Role |
|---|---|
| `src/partyops/store.cljc` | **Store** protocol -- `MemStore` ‖ `DatomicStore` (`langchain.db`) + append-only audit ledger + position-publication history. No dynamically-filed sub-record -- the actuation op acts directly on a pre-seeded position, and the double-actuation guard checks a dedicated `:published?` boolean rather than a `:status` value |
| `src/partyops/registry.cljc` | Position-publication draft records, plus `member-consensus-share-insufficient?` -- an HONEST reuse of this fleet's ratio-based check family (the FOURTH instance, MINIMUM-floor direction like `leasing`'s/`union`'s), not claimed as new |
| `src/partyops/facts.cljc` | Per-jurisdiction campaign-finance-disclosure/imprint catalog with an official spec-basis citation per entry, honest coverage reporting |
| `src/partyops/material.cljc` | **Campaign-conduct bridge** to `kotoba-lang/senkyo` -- independent re-screen of a method's legality from the POSITION's jurisdiction, plus the re-exported out-of-scope intent set (the boundary is defined once, in the shared library) |
| `src/partyops/partyopsllm.cljc` | **PartyOps-LLM** -- `mock-advisor` ‖ `llm-advisor`; intake/jurisdiction-verification/disclaimer-screening/material-screening/publication proposals |
| `src/partyops/governor.cljc` | **Political Organization Governance Governor** -- 6 HARD checks (spec-basis · evidence-incomplete · campaign-finance-disclaimer-missing, unconditional evaluation, GENUINELY NEW, the 59th grounding of this discipline · member-consensus-share-insufficient, ratio-based reuse, the 4th instance, not claimed as new · **campaign-conduct-not-permitted + screen-verdict-mismatch**, independent re-screen against `senkyo` · already-published guard) + 1 soft (confidence/actuation gate) |
| `src/partyops/phase.cljc` | **Phase 0→3** -- read-only → assisted intake → assisted verify → supervised (position publication always human; member intake is the ONLY auto-eligible op, no direct capital risk) |
| `src/partyops/operation.cljc` | **OperationActor** -- langgraph-clj StateGraph |
| `src/partyops/sim.cljc` | demo driver |
| `test/partyops/*_test.clj` | governor contract · phase invariants · store parity · registry conformance · facts coverage · **campaign-conduct screening** (`material_test.clj`) |

## Business-process coverage (honest)

This actor covers member/supporter intake through campaign-finance-
disclosure regulatory assessment, disclaimer screening and position/
endorsement publication -- the core governed lifecycle this
blueprint's own `docs/business-model.md` names as its Offer:

| Covered | Not covered (out of scope for this R0) |
|---|---|
| Member/supporter intake + per-jurisdiction evidence checklisting, HARD-gated on an official spec-basis citation (`:member/intake`/`:position/verify`) | Real membership-management-system integration, real campaign-finance-compliance filing itself (see `partyops.facts`'s docstring) |
| Campaign-finance-disclaimer screening, evaluated unconditionally so the screening op itself can HARD-hold on its own finding (`:disclaimer/screen`) | Any political-judgment or policy-merit determination itself -- deliberately outside this actor's competence |
| Campaign-material/conduct **legality** screening against `kotoba-lang/senkyo` (`:material/screen`) -- is this method lawful at all in this position's jurisdiction, on what basis, with what evidence | Voter targeting, vote-persuasion scripting, candidate ranking, turnout operations, distribution dispatch, opinion profiling, eligibility adjudication -- **not unimplemented; structurally absent** (see below) |
| Position/endorsement publication, HARD-gated on full evidence, an included disclaimer and a sufficient member-consensus vote share, plus a double-publication guard (`:actuation/publish-position`) | |
| Immutable audit ledger for every intake/verification/screening/publication decision | |

Extending coverage is additive: add the next gate (e.g. a campaign-
contribution-limit check) as its own governed op with its own HARD
checks and tests, following the SAME "an independent governor
re-verifies against the actor's own records before any real-world
act" pattern this repo's flagship op already establishes.

## Jurisdiction coverage (honest)

`partyops.facts/coverage` reports how many requested jurisdictions
actually have an official spec-basis in `partyops.facts/catalog` --
currently **5** seeded (JPN, USA, GBR, DEU, CAN) out of ~194
jurisdictions worldwide. This is a starting catalog to prove the
governor contract end-to-end, not a claim of global coverage. Adding a
jurisdiction is additive: one map entry in `partyops.facts/catalog`,
citing a real official source -- never fabricate a jurisdiction's
requirements to make coverage look bigger.

**Two catalogs, not one.** `partyops.facts` covers campaign-finance
**disclosure/imprint** requirements; `kotoba-lang/senkyo` (consumed by
`:material/screen`, see below) covers **which campaign methods are
lawful at all** in a jurisdiction. They are separate tables with
separate coverage, and a jurisdiction can be in one and not the other
-- `partyops.material/jurisdiction-supported?` answers the senkyo side.
Do not read one catalog's coverage as the other's.

## Maturity

`:implemented` -- `PartyOps-LLM` + `Political Organization Governance
Governor` run as real, tested code (see `Run` above), promoted from
the originally-published `:blueprint`-tier scaffold, modeled closely
on the seventy-three prior actors' architecture. See
`docs/adr/0001-architecture.md` for the history and design.

## License

Code and implementation templates are AGPL-3.0-or-later.
