(ns partyops.governor
  "Political Organization Governance Governor -- the independent
  compliance layer that earns the PartyOps-LLM the right to commit.
  The LLM has no notion of jurisdictional campaign-finance-disclosure
  law, whether a position's own recorded member-consensus vote share
  has actually stayed above its own recorded required threshold, or
  when an act stops being a draft and becomes a real-world public
  political position or endorsement, so this MUST be a separate
  system able to *reject* a proposal and fall back to HOLD -- the
  political-organization analog of `cloud-itonami-isic-6512`'s
  `casualty.governor`.

  Five checks, in priority order, ALL HARD violations: a human approver
  CANNOT override them (you don't get to approve your way past a
  fabricated jurisdiction spec-basis, incomplete evidence, a missing
  campaign-finance disclaimer, an insufficient member-consensus vote
  share, or a double publication of the same position). The
  confidence/actuation gate is SOFT: it asks a human to look (low
  confidence / actuation), and the human may approve -- but see
  `partyops.phase`: for `:stake :actuation/publish-position` (a real
  public political act) NO phase ever allows auto-commit either. Two
  independent layers agree that actuation is always a human call.

    1. Spec-basis                  -- did the jurisdiction proposal cite
                                       an OFFICIAL source (`partyops.
                                       facts`), or invent one?
    2. Evidence incomplete         -- for `:actuation/publish-
                                       position`, has the jurisdiction
                                       actually been assessed with a
                                       full position-publication
                                       evidence checklist (member-
                                       consensus/disclaimer-compliance/
                                       governing-body-approval/
                                       publication-notice) on file?
    3. Campaign-finance disclaimer
       missing                       -- reported by THIS proposal
                                       itself (a `:disclaimer/screen`
                                       that just found a missing
                                       disclaimer), or already on file
                                       for the position (`:disclaimer/
                                       screen`/`:actuation/publish-
                                       position`). Evaluated
                                       UNCONDITIONALLY (not scoped to a
                                       specific op), the SAME discipline
                                       `casualty.governor/sanctions-
                                       violations`'s original fix
                                       establishes -- GENUINELY NEW
                                       (grep-verified absent -- zero
                                       hits for 'disclaimer'/'imprint'/
                                       'campaign-finance' across every
                                       prior sibling), the 59th
                                       distinct application of this
                                       discipline overall (most
                                       recently `sportsclub.governor/
                                       disciplinary-complaint-
                                       unresolved-violations` at 58th).
                                       Grounded in real campaign-
                                       finance-disclosure law: US FECA
                                       52 U.S.C. §30120, UK PPERA
                                       imprint rules, Germany's
                                       Impressumspflicht (MStV §18).
    4. Member-consensus share
       insufficient                  -- for `:actuation/publish-
                                       position`, INDEPENDENTLY
                                       recompute whether the
                                       position's own votes-in-favor
                                       divided by its own votes-cast
                                       falls below its own required-
                                       consensus-share
                                       (`partyops.registry/member-
                                       consensus-share-insufficient?`)
                                       -- needs no proposal inspection
                                       or stored-verdict lookup at
                                       all. The FOURTH instance of
                                       this fleet's ratio-based check
                                       family (`leasing.registry/
                                       collateral-coverage-ratio-
                                       insufficient?`/`behavioral.
                                       registry/supervision-ratio-
                                       insufficient?`/`union.registry/
                                       strike-vote-share-insufficient?`
                                       established the first three),
                                       MINIMUM-floor direction like
                                       `leasing`'s/`union`'s -- an
                                       honest reuse of the family
                                       shape, not claimed as new.
    5. Campaign-conduct not
       permitted                     -- for `:material/screen`,
                                       INDEPENDENTLY re-run the
                                       `kotoba-lang/senkyo` legality
                                       screen from the POSITION's own
                                       jurisdiction and the proposal's
                                       declared medium/attributes, and
                                       HOLD when the recomputed verdict
                                       blocks (`:prohibited` /
                                       `:no-spec-basis` /
                                       `:undetermined` /
                                       `:out-of-scope`). A SECOND,
                                       distinct violation fires when the
                                       advisor's CLAIMED verdict
                                       disagrees with the recomputed one
                                       -- a disagreement is itself
                                       evidence the advisor authored a
                                       conclusion rather than reporting
                                       one. Same discipline as check 4:
                                       recompute from the actor's own
                                       records, never read the proposal's
                                       answer.
    6. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:actuation/
                                       publish-position` (a REAL public
                                       political act) -> escalate.

  One more guard, double-publication prevention, is enforced but NOT
  listed as a numbered HARD check above because it needs no upstream
  comparison at all -- `already-published-violations` refuses to
  publish the SAME position twice, off a dedicated `:published?` fact
  (never a `:status` value) -- the SAME 'check a dedicated boolean,
  not status' discipline every prior governor's guards establish,
  informed by `cloud-itonami-isic-6492`'s status-lifecycle bug
  (ADR-2607071320)."
  (:require [partyops.facts :as facts]
            [partyops.material :as material]
            [partyops.registry :as registry]
            [partyops.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Publishing a real public political position or endorsement is the
  ONE real-world actuation event this actor performs -- a single-
  member set, matching every other single-actuation sibling's shape."
  #{:actuation/publish-position})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:position/verify` (or `:actuation/publish-position`) proposal
  with no spec-basis citation is a HARD violation -- never invent a
  jurisdiction's campaign-finance-disclosure requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:position/verify :actuation/publish-position} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は法域要件として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:actuation/publish-position`, the jurisdiction's required
  member-consensus/disclaimer-compliance/governing-body-approval/
  publication-notice evidence must actually be satisfied -- do not
  trust the advisor's self-reported confidence alone."
  [{:keys [op subject]} st]
  (when (= op :actuation/publish-position)
    (let [p (store/position st subject)
          verification (store/verify-of st subject)]
      (when-not (and verification
                     (facts/required-evidence-satisfied?
                      (:jurisdiction p) (:checklist verification)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(会員合意記録/表示義務遵守記録/運営機関承認記録/公表通知記録等)が充足していない状態での公表提案"}]))))

(defn- campaign-finance-disclaimer-missing-violations
  "A missing campaign-finance disclaimer -- reported by THIS proposal
  (e.g. a `:disclaimer/screen` that itself just found one missing), or
  already on file in the store for the position (`:disclaimer/
  screen`/`:actuation/publish-position`) -- is a HARD, un-overridable
  hold. Evaluated UNCONDITIONALLY (not scoped to a specific op) so the
  screening op itself can HARD-hold on its own finding."
  [{:keys [op subject]} proposal st]
  (let [hit-in-proposal? (false? (get-in proposal [:value :disclaimer-included?]))
        position-id (when (contains? #{:disclaimer/screen :actuation/publish-position} op) subject)
        hit-on-file? (and position-id (false? (:campaign-finance-disclaimer-included? (store/position st position-id))))]
    (when (or hit-in-proposal? hit-on-file?)
      [{:rule :campaign-finance-disclaimer-missing
        :detail "選挙運動用/政治活動用表示義務を欠いた状態での公表提案は進められない"}])))

(defn- member-consensus-share-insufficient-violations
  "For `:actuation/publish-position`, INDEPENDENTLY recompute whether
  the position's own votes-in-favor divided by its own votes-cast
  falls below its own required-consensus-share via `partyops.
  registry/member-consensus-share-insufficient?` -- needs no proposal
  inspection or stored-verdict lookup at all, an honest reuse of this
  fleet's ratio-based check family SHAPE."
  [{:keys [op subject]} st]
  (when (= op :actuation/publish-position)
    (let [p (store/position st subject)]
      (when (registry/member-consensus-share-insufficient? p)
        [{:rule :member-consensus-share-insufficient
          :detail (str subject " の賛成票比率(" (:votes-in-favor p) "/" (:votes-cast p)
                      ")が必要合意比率(" (:required-consensus-share p) ")を下回っている")}]))))

(defn- campaign-conduct-violations
  "For `:material/screen`: re-run the senkyo legality screen
  INDEPENDENTLY -- the jurisdiction comes from the POSITION record (not
  the proposal, or the advisor could choose its own jurisdiction), and
  the advisor's claimed verdict is never read as an input.

  Two distinct HARD rules:
  - `:campaign-conduct-not-permitted` -- the recomputed verdict blocks.
    `:undetermined` blocks too: 'approve your way past not knowing' is
    exactly the path this actor must not have. The remedy is to supply
    the missing attribute and re-run, not to escalate.
  - `:screen-verdict-mismatch` -- the advisor claimed a verdict that
    disagrees with the recomputed one. Not knowing is a hold; claiming
    the wrong answer is a separate, named violation."
  [{:keys [op subject]} proposal st]
  (when (= op :material/screen)
    (let [p (store/position st subject)
          value (:value proposal)
          result (material/recompute p value)
          claimed (:claimed-verdict value)
          vs (cond-> []
               (material/blocking? result)
               (conj {:rule :campaign-conduct-not-permitted
                      :detail (material/hold-detail result)})

               (material/verdict-mismatch? claimed result)
               (conj {:rule :screen-verdict-mismatch
                      :detail (str "advisor の主張 " claimed
                                   " と独立再計算 " (:verdict result) " が食い違う")}))]
      (seq vs))))

(defn- already-published-violations
  "For `:actuation/publish-position`, refuses to publish the SAME
  position twice, off a dedicated `:published?` fact -- see ns
  docstring for why this sidesteps the status-lifecycle risk `cloud-
  itonami-isic-6492`'s ADR-0001 documents."
  [{:keys [op subject]} st]
  (when (= op :actuation/publish-position)
    (when (store/position-already-published? st subject)
      [{:rule :already-published
        :detail (str subject " は既に公表済み")}])))

(defn check
  "Censors a PartyOps-LLM proposal against the governor rules. Returns
   {:ok? bool :violations [..] :confidence c :escalate? bool :high-stakes? bool
    :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (campaign-finance-disclaimer-missing-violations request proposal st)
                           (member-consensus-share-insufficient-violations request st)
                           (campaign-conduct-violations request proposal st)
                           (already-published-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
