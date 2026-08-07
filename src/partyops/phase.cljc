(ns partyops.phase
  "Phase 0->3 staged rollout -- the political-organization analog of
  `cloud-itonami-isic-6512`'s `casualty.phase`.

    Phase 0  read-only        -- no writes, still governor-gated.
    Phase 1  assisted-intake  -- member/supporter intake allowed,
                                 every write needs human approval.
    Phase 2  assisted-verify  -- adds jurisdiction verification +
                                 disclaimer screening writes, still
                                 approval.
    Phase 3  supervised auto  -- governor-clean, high-confidence
                                 `:member/intake` (no capital risk yet)
                                 may auto-commit. `:actuation/publish-
                                 position` NEVER auto-commits, at any
                                 phase.

  `:actuation/publish-position` is deliberately ABSENT from every
  phase's `:auto` set, including phase 3 -- a permanent structural
  fact, not a rollout milestone still to come. Publishing a real
  public political position or endorsement is the ONE real-world,
  reputation-critical legal act this actor performs; it is always a
  human governing-body officer's call. `partyops.governor`'s
  `:actuation/publish-position` high-stakes gate enforces the same
  invariant independently -- two layers, not one, agree on this.
  `:disclaimer/screen` is likewise never auto-eligible, at any phase --
  the same posture every sibling's KYC/conflict/independence/
  surveillance/calibration/credential/integrity/patron/authorization/
  safety/anti-doping/egress/conduct screening op has. Like every prior
  sibling's phase 3 `:auto` set, this domain has only ONE member
  (`:member/intake`) -- no separate no-capital-risk 'file' lifecycle
  distinct from the member itself.

  `:material/screen` (campaign-material/conduct legality screening
  against `kotoba-lang/senkyo`) joins phase 2's `:writes` and is
  likewise NEVER auto-eligible at any phase, for the same reason every
  screening op in this fleet is not: a screening op that can
  auto-commit is a screening op whose finding nobody read. Its
  governor check re-runs the screen INDEPENDENTLY of the advisor's
  claim, so an auto path would also mean nobody compared the two.")

(def read-ops  #{})
(def write-ops #{:member/intake :position/verify :disclaimer/screen
                 :material/screen :actuation/publish-position})

;; NOTE the invariant: `:actuation/publish-position` is a member of
;; `write-ops` (governor-gated like any write) but is NEVER a member
;; of any phase's `:auto` set below. Do not add it there.
(def phases
  "phase -> {:label .. :writes <ops allowed to write> :auto <ops allowed to
  auto-commit when governor-clean>}."
  {0 {:label "read-only"        :writes #{}                                                              :auto #{}}
   1 {:label "assisted-intake"  :writes #{:member/intake}                                                :auto #{}}
   2 {:label "assisted-verify"  :writes #{:member/intake :position/verify :disclaimer/screen
                                          :material/screen}                                              :auto #{}}
   3 {:label "supervised-auto"  :writes write-ops
      :auto #{:member/intake}}})

(def default-phase 3)

(defn gate
  "Adjust a governor disposition for the rollout phase. Returns
  {:disposition kw :reason kw|nil}.

  - a governor HOLD always stays HOLD (compliance wins).
  - a write op not yet enabled in this phase -> HOLD (:phase-disabled).
  - a write op enabled but not auto-eligible -> ESCALATE (:phase-approval),
    even if the governor was clean.
  - `:actuation/publish-position` is never auto-eligible at any phase,
    so it always escalates once the governor clears it (or holds if
    the governor doesn't)."
  [phase {:keys [op]} governor-disposition]
  (let [{:keys [writes auto]} (get phases phase (get phases default-phase))]
    (cond
      (= :hold governor-disposition)       {:disposition :hold :reason nil}
      (contains? read-ops op)              {:disposition governor-disposition :reason nil}
      (not (contains? writes op))          {:disposition :hold :reason :phase-disabled}
      (and (= :commit governor-disposition)
           (not (contains? auto op)))      {:disposition :escalate :reason :phase-approval}
      :else                                {:disposition governor-disposition :reason nil})))

(defn verdict->disposition
  "Map a Political Organization Governance Governor verdict to a base
  disposition before the phase gate."
  [verdict]
  (cond (:hard? verdict) :hold
        (:escalate? verdict) :escalate
        :else :commit))
