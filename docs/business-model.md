# Business Model: Activities of political organizations

## Classification

- Repository: `cloud-itonami-isic-9492`
- ISIC Rev.5: `9492`
- Activity: activities of political organizations -- political party and political-advocacy-group administration
- Social impact: community access, data sovereignty, transparent audit

## Customer

- independent political parties/associations
- cooperative advocacy coalitions
- community civic-engagement programs

## Offer

- member/supporter intake
- platform/policy-position proposal
- endorsement proposal
- immutable audit ledger

## Revenue

- self-host setup: one-time implementation fee
- managed hosting: monthly subscription per organization
- support: monthly retainer with SLA
- migration: import from an incumbent membership-management system
- per-member dues-processing fee

## Trust Controls

- no public political position or endorsement is published on the organization's behalf without human sign-off
- a fabricated member-consensus claim forces a hold, not an override
- every publication path is auditable
- member/supporter data stays outside Git
- emergency manual override paths remain outside LLM control
- a missing campaign-finance disclaimer, or a position whose own
  recorded member-consensus vote share has not met its own recorded
  required threshold, forces a hold, not an override
- position publication is logged and escalated, and cannot be
  published twice for the same position: a double-publication attempt
  is held off this actor's own position facts alone, with no upstream
  comparison needed

## Political Organization Governance Governor: decision rule

`blueprint.edn` fixes `:itonami.blueprint/governor` to `:political-
organization-governance-governor` -- this is not a generic "review
step," it is the one gate the ONE real-world act this business
performs (publishing a real public political position or endorsement)
must pass. The governor sits between the PartyOps-LLM and execution,
per the README's Core Contract:

```text
PartyOps-LLM -> Political Organization Governance Governor -> hold, proceed, or human approval
```

**Approves**: routine political-organization actions proposed against
a position that already has a consented jurisdiction evidence
checklist on file, satisfied required evidence, an included campaign-
finance disclaimer, and a member-consensus vote share meeting its own
recorded required threshold. These proceed straight to the position
ledger.

**Rejects or escalates**: the governor refuses to let the advisor
publish a position on its own authority when any of the following
hold -- a fabricated jurisdiction spec-basis; incomplete evidence; a
missing campaign-finance disclaimer; an insufficient member-consensus
vote share; a double-publication attempt. A clean publication proposal
still always routes to a human -- `:actuation/publish-position` is
never auto-committed, at any rollout phase.
