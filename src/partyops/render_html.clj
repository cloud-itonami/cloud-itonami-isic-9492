(ns partyops.render-html
  "Build-time HTML renderer for `docs/samples/operator-console.html`.

  Closes flagship checklist item 2: this repo previously had NO demo
  page and no generator at all. This namespace drives the REAL actor
  stack (`partyops.operation` -> `partyops.governor` ->
  `partyops.store`, compiled as a langgraph StateGraph and run through
  `langgraph.graph/run*`) and renders the resulting SSoT + audit
  ledger.

  EVERY entity on the page traces to real declared seed data in this
  repo -- there is no hand-written HTML table content anywhere below:

    - positions, vote counts, jurisdictions   `partyops.store/demo-data`
    - jurisdiction authorities / legal bases  `partyops.facts/catalog`
    - campaign-conduct legality + citations   `kotoba-lang/senkyo`, via
                                              `partyops.material/recompute`
    - publication numbers                     `partyops.registry`
    - phase gate / auto-eligibility           `partyops.phase/phases`
    - every hold, rule and basis              the governor's own output

  This matters more than usual for THIS domain: the vertical is
  political-organization governance (ISIC 9492), so a plausible-looking
  but invented party name, donation figure or vote tally on a sample
  page would be indistinguishable from real political data. Nothing
  here is invented. The four positions are the repo's own fixtures
  (`position-1`..`position-4`); there are no donation figures in this
  actor's model at all, and none are shown.

  Deterministic: no timestamps, no random ids, byte-identical across
  reruns against the same seed (verify by diffing two consecutive
  runs into scratch dirs).

  Usage: `clojure -M:dev:render-html [out-file]`
  (default `docs/samples/operator-console.html`)."
  (:require [jp-go-dds.skin]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [partyops.store :as store]
            [partyops.facts :as facts]
            [partyops.governor :as governor]
            [partyops.phase :as phase]
            [partyops.operation :as op]
            [partyops.sim :as sim]
            [langgraph.graph :as g]))

;; The operator identity is the repo's OWN declared demo operator
;; (`partyops.sim/operator`) rather than a re-typed copy, so the
;; approver shown on the page ("op-1", a :governing-body-officer at
;; phase 3) traces to declared repo data like everything else.
(def ^:private operator sim/operator)

(defn- exec! [actor tid request]
  (g/run* actor {:request request :context operator} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by (:actor-id operator)}}
          {:thread-id tid :resume? true}))

;; ----------------------------- the scenario -----------------------------

(defn run-demo!
  "Runs a fresh seeded store through a scenario that reaches EVERY
  check the Political Organization Governance Governor implements, so
  the console shows the real enforcement surface rather than a happy
  path.

  position-1 (JPN, climate-policy-platform, disclaimer included,
  consensus 80/100 >= 0.6) walks a full clean lifecycle -- intake
  (auto-commits: the only op in phase 3's `:auto` set), jurisdiction
  verification (approved), campaign-finance-disclaimer screening
  (approved), an online-ad campaign-material legality screen that
  senkyo recomputes as `:permitted-with-obligations` (approved), and
  the position publication itself (ALWAYS escalates -- no phase ever
  auto-commits `:actuation/publish-position` -- approved, producing
  publication JPN-POS-000000).

  Nine HARD holds, none of which ever reaches a human:

    :evidence-incomplete             position-1 publication attempted
                                     BEFORE its jurisdiction evidence
                                     checklist was on file
    :campaign-conduct-not-permitted  position-1 door-to-door canvassing
                                     (公職選挙法 第138条 bans the medium
                                     outright in JPN -- not resolvable
                                     by supplying attributes)
    + :screen-verdict-mismatch       the same door-to-door screen with
                                     the advisor FABRICATING a
                                     `:permitted` verdict: the governor
                                     never reads the claim, recomputes
                                     independently, and fires a second,
                                     separately-named violation because
                                     claiming a wrong answer is a
                                     distinct offence from not knowing
    :campaign-conduct-not-permitted  position-1 poster screen carrying an
                                     `:voter-targeting` intent -- a
                                     structurally out-of-scope intent
                                     this stack has no construction path
                                     for
    :already-published               position-1 published a second time
    :no-spec-basis                   position-2's jurisdiction (ATL) has
                                     no official spec-basis; the advisor
                                     must not invent requirements
    :campaign-conduct-not-permitted  position-2 online-ad -- senkyo has no
                                     catalog for ATL, which is
                                     `:no-spec-basis`, NOT 'unregulated'
    :member-consensus-share-insufficient
                                     position-3 published with its own
                                     recorded 45/100 below its own
                                     recorded 0.6 threshold, recomputed
                                     from the position's own fields
    :campaign-finance-disclaimer-missing
                                     position-4 screened and found
                                     missing its required disclaimer

  Returns the resulting store. Every field the renderer reads is real
  governor/store output."
  []
  (let [db (store/seed-db)
        actor (op/build db)]

    ;; --- position-1: clean lifecycle -------------------------------
    (exec! actor "p1-intake" {:op :member/intake :subject "position-1"
                              :patch {:id "position-1"
                                      :position-name "climate-policy-platform"}})

    ;; publication attempted before the evidence checklist exists
    (exec! actor "p1-early-publish" {:op :actuation/publish-position :subject "position-1"})

    (exec! actor "p1-verify" {:op :position/verify :subject "position-1"})
    (approve! actor "p1-verify")

    (exec! actor "p1-disclaimer" {:op :disclaimer/screen :subject "position-1"})
    (approve! actor "p1-disclaimer")

    ;; a legality screen that actually clears: senkyo returns
    ;; :permitted-with-obligations once the two required attributes exist
    (exec! actor "p1-material" {:op :material/screen :subject "position-1"
                                :medium :online-ad
                                :attrs {:within-campaign-period? true
                                        :sender-is-candidate-or-party true}})
    (approve! actor "p1-material")

    (exec! actor "p1-publish" {:op :actuation/publish-position :subject "position-1"})
    (approve! actor "p1-publish")

    ;; --- HARD holds ------------------------------------------------
    (exec! actor "p1-doortodoor" {:op :material/screen :subject "position-1"
                                  :medium :door-to-door})

    (exec! actor "p1-fabricate" {:op :material/screen :subject "position-1"
                                 :medium :door-to-door
                                 :fabricate-verdict? true})

    (exec! actor "p1-intent" {:op :material/screen :subject "position-1"
                              :medium :poster
                              :intents [:voter-targeting]})

    (exec! actor "p1-republish" {:op :actuation/publish-position :subject "position-1"})

    (exec! actor "p2-verify" {:op :position/verify :subject "position-2" :no-spec? true})

    (exec! actor "p2-material" {:op :material/screen :subject "position-2"
                                :medium :online-ad})

    (exec! actor "p3-verify" {:op :position/verify :subject "position-3"})
    (approve! actor "p3-verify")

    (exec! actor "p3-publish" {:op :actuation/publish-position :subject "position-3"})

    (exec! actor "p4-disclaimer" {:op :disclaimer/screen :subject "position-4"})

    db))

;; ----------------------------- rendering helpers -----------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")))

(defn- nm
  "Render a keyword/string uniformly (keywords lose their colon)."
  [v]
  (if (keyword? v) (name v) (str v)))

(defn- join-rows [rows] (str/join "\n" rows))

(defn- td [& cells]
  (str "        <tr>" (apply str (map #(str "<td>" % "</td>") cells)) "</tr>"))

(defn- code [v] (str "<code>" (esc (nm v)) "</code>"))

(defn- holds
  "Every HARD governor hold this run produced."
  [ledger]
  (filterv #(= :governor-hold (:t %)) ledger))

;; ----------------------------- sections -----------------------------

(defn- position-row
  "One SSoT position. Consensus is shown as the position's OWN recorded
  numbers so a reader can check the governor's ratio arithmetic by eye."
  [ledger {:keys [id position-name jurisdiction campaign-finance-disclaimer-included?
                  votes-in-favor votes-cast required-consensus-share
                  published? publication-number]}]
  (let [share (when (and votes-cast (pos? votes-cast))
                (/ (double votes-in-favor) votes-cast))
        met? (and share (>= share required-consensus-share))
        last-fact (last (filter #(= (:subject %) id) ledger))]
    (td (code id)
        (esc position-name)
        (esc jurisdiction)
        (if campaign-finance-disclaimer-included?
          "<span class=\"ok\">included</span>"
          "<span class=\"critical\">MISSING</span>")
        (str "<span class=\"num\">" votes-in-favor "/" votes-cast "</span> vs "
             "<span class=\"num\">" required-consensus-share "</span> "
             (if met?
               "<span class=\"ok\">met</span>"
               "<span class=\"critical\">below threshold</span>"))
        (if published?
          (str "<span class=\"ok\">published</span> " (code publication-number))
          "<span class=\"muted\">not published</span>")
        (case (:t last-fact)
          :committed "<span class=\"ok\">committed</span>"
          :governor-hold (str "<span class=\"critical\">HARD hold &middot; "
                              (esc (nm (-> last-fact :violations first :rule))) "</span>")
          :approval-granted "<span class=\"ok\">approved &amp; committed</span>"
          "<span class=\"muted\">no activity</span>"))))

(defn- phase-row
  "One rollout phase, straight from `partyops.phase/phases`."
  [[ph {:keys [label writes auto]}]]
  (td (str "<span class=\"num\">" ph "</span>")
      (esc label)
      (if (seq writes)
        (str/join " " (map #(code %) (sort writes)))
        "<span class=\"muted\">none</span>")
      (if (seq auto)
        (str/join " " (map #(code %) (sort auto)))
        "<span class=\"muted\">none</span>")))

(defn- op-gate-row
  "Per-op gate, DERIVED from `partyops.phase/phases` rather than
  hand-described: an op that appears in no phase's `:auto` set is
  structurally always-human, and that is a fact about the data, not a
  claim in prose."
  [ph-map op]
  (let [auto-in (sort (keep (fn [[ph {:keys [auto]}]] (when (contains? auto op) ph)) ph-map))
        write-in (sort (keep (fn [[ph {:keys [writes]}]] (when (contains? writes op) ph)) ph-map))]
    (td (code op)
        (if (seq write-in) (str/join ", " write-in) "<span class=\"muted\">never</span>")
        (if (seq auto-in)
          (str "<span class=\"warn\">phase " (str/join ", " auto-in) "</span>")
          "<span class=\"critical\">never &mdash; always a human decision</span>"))))

(defn- hold-row [{:keys [op subject violations confidence]}]
  (td (code subject)
      (code op)
      (join-rows (map #(str "<span class=\"critical\">" (esc (nm (:rule %))) "</span>") violations))
      (join-rows (map #(esc (:detail %)) violations))
      (str "<span class=\"num\">" confidence "</span>")))

(defn- material-row
  "One campaign-material legality screen attempt. `:basis` on a
  committed screen is the set of legal citations senkyo's own findings
  carried, so the citations shown are the library's, not this page's."
  [{:keys [t subject summary basis violations]}]
  (td (code subject)
      (if (= t :committed)
        "<span class=\"ok\">committed</span>"
        "<span class=\"critical\">HARD hold</span>")
      (if (= t :committed)
        (esc summary)
        (join-rows (map #(str "<span class=\"critical\">" (esc (nm (:rule %))) "</span>") violations)))
      (if (= t :committed)
        (if (seq basis) (join-rows (map #(esc (nm %)) basis)) "<span class=\"muted\">&mdash;</span>")
        (join-rows (map #(esc (:detail %)) violations)))))

(defn- jurisdiction-row [[iso3 {:keys [name owner-authority legal-basis provenance required-evidence]}]]
  (td (code iso3)
      (esc name)
      (esc owner-authority)
      (esc legal-basis)
      (str "<span class=\"num\">" (count required-evidence) "</span>")
      (str "<a href=\"" (esc provenance) "\">" (esc provenance) "</a>")))

;; --- approver attribution (DERIVED, not asserted) ---------------------

(defn- register-for
  "The SSoT register a committed op writes. Used to CHECK whether
  approver attribution actually survived the commit, rather than
  assuming it did."
  [db op subject]
  (case op
    :position/verify            (store/verify-of db subject)
    :disclaimer/screen          (store/disclaimer-screen-of db subject)
    :material/screen            (store/material-screen-of db subject)
    (:actuation/publish-position
     :member/intake)            (store/position db subject)
    nil))

(defn- approver-row
  "Joins each `:approval-granted` audit fact to the SSoT register the
  op wrote, and reports whether `:approved-by` is actually PRESENT
  there.

  This is derived at render time on purpose. `partyops.operation`
  attaches the approver to the commit record's `:payload`, but
  `partyops.store/commit-record!` reads `:payload` for only three of
  its five effects -- `:position/upsert` reads `:value` (which has no
  approver) and `:position/mark-published` reads neither. Rather than
  hardcode 'this one is broken', the page WALKS the register and
  reports what is there, so it self-corrects the day the store keeps
  the payload for every effect.

  Silently omitting the approver would be dishonest: a reader could
  not tell 'nobody approved this' from 'the store did not keep it'."
  [db {:keys [op subject by]}]
  (let [reg (register-for db op subject)
        retained? (contains? reg :approved-by)]
    (td (code subject)
        (code op)
        (esc by)
        (if retained?
          (str "<span class=\"ok\">retained in record</span> &middot; <code>:approved-by "
               (esc (:approved-by reg)) "</code>")
          (str "<span class=\"warn\">audit only &mdash; not retained in record</span>")))))

(defn- publication-row [record]
  (td (code (get record "record_id"))
      (code (get record "position_id"))
      (esc (get record "jurisdiction"))
      (esc (get record "kind"))
      (if (get record "immutable")
        "<span class=\"ok\">immutable</span>"
        "<span class=\"muted\">mutable</span>")))

(defn- ledger-row [{:keys [t op subject basis]}]
  (td (esc (nm t))
      (code (or op :n-a))
      (code subject)
      (if (seq basis)
        (join-rows (map #(esc (nm %)) basis))
        "<span class=\"muted\">&mdash;</span>")))

;; ----------------------------- document -----------------------------

(defn- section [title lead headers rows]
  (str "  <section class=\"card\">\n"
       "    <h2>" title "</h2>\n"
       "    <p class=\"muted\">" lead "</p>\n"
       "    <table>\n"
       "      <thead><tr>" (apply str (map #(str "<th>" % "</th>") headers)) "</tr></thead>\n"
       "      <tbody>\n" (join-rows rows) "\n"
       "      </tbody>\n"
       "    </table>\n"
       "  </section>\n"))

(defn render
  "Renders the operator console from a store `db` that has already run
  `run-demo!` (or any other real scenario)."
  [db]
  (let [ledger (vec (store/ledger db))
        positions (store/all-positions db)
        hard (holds ledger)
        material-facts (filterv #(= :material/screen (:op %)) ledger)
        approvals (filterv #(= :approval-granted (:t %)) ledger)
        cov (facts/coverage)]
    (str
     "<!doctype html>\n<html lang=\"en\"><head><meta charset=\"utf-8\">"
     "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, viewport-fit=cover\">"
     "<meta name=\"color-scheme\" content=\"light\">"
     "<title>cloud-itonami-isic-9492 &middot; partyops operator console</title><style>"
     (jp-go-dds.skin/dds+skin)
     "</style></head><body>\n"
     "<div class=\"dds-ext-container\">\n"
     "<header class=\"bar\">\n"
     "  <h1>Activities of political organizations (ISIC 9492) &mdash; Operator Console</h1>\n"
     "</header>\n"
     "<p><span class=\"badge\">read-only sample</span> "
     "<span class=\"badge\">governor-gated</span> "
     "<span class=\"badge\">position publication always human-approved</span></p>\n"
     "<p class=\"muted\">Build-time generated from the REAL actor stack "
     "(<code>partyops.operation</code> &rarr; <code>partyops.governor</code> &rarr; "
     "<code>partyops.store</code>) by <code>partyops.render-html</code> via "
     "<code>clojure -M:dev:render-html</code>. Every row below is this run's own output "
     "against the repo's declared seed data &mdash; no hand-written table content, no "
     "invented organizations, positions or figures.</p>\n"
     "<main>\n"

     (section "Positions (SSoT)"
              (str "The four seeded positions from <code>partyops.store/demo-data</code>. "
                   "Consensus shows each position's OWN recorded votes against its OWN "
                   "recorded threshold &mdash; the governor recomputes this ratio itself and "
                   "never reads the advisor's claim about it.")
              ["Position" "Name" "Jurisdiction" "Finance disclaimer" "Consensus (in favor/cast vs required)" "Publication" "Last op"]
              (map (partial position-row ledger) positions))

     (section "Rollout phase gate"
              (str "Straight from <code>partyops.phase/phases</code>. Note that "
                   "<code>:actuation/publish-position</code> appears in no phase's auto set, "
                   "including phase 3 &mdash; a structural fact, not a milestone still to come. "
                   "This run executed at phase "
                   "<span class=\"num\">" (:phase operator) "</span> as <code>"
                   (esc (:actor-id operator)) "</code> (" (esc (nm (:actor-role operator))) ").")
              ["Phase" "Label" "Write-enabled ops" "Auto-commit ops"]
              (map phase-row (sort-by key phase/phases)))

     (section "Action gate (Political Organization Governance Governor)"
              (str "Derived from the phase table, not hand-described: an op that appears in "
                   "no phase's auto set is structurally always a human decision. The governor "
                   "independently escalates the "
                   "<span class=\"num\">" (count governor/high-stakes) "</span> high-stakes stake "
                   "(<code>" (esc (nm (first governor/high-stakes))) "</code>) as well, so two "
                   "layers agree. Confidence floor: <span class=\"num\">"
                   governor/confidence-floor "</span>.")
              ["Op" "Write-enabled at phase" "Auto-commit eligible"]
              (map (partial op-gate-row phase/phases) (sort phase/write-ops)))

     (section (str "HARD governor holds this run (<span class=\"num\">" (count hard) "</span>)")
              (str "HARD violations cannot be overridden by a human approver &mdash; none of "
                   "these ever reached the approval node. Details are the governor's own text.")
              ["Position" "Op" "Rule" "Detail" "Advisor confidence"]
              (map hold-row hard))

     (section "Campaign-material legality screens (senkyo, independently recomputed)"
              (str "<code>:material/screen</code> re-runs the "
                   "<code>kotoba-lang/senkyo</code> legality screen from the POSITION's own "
                   "jurisdiction &mdash; never the proposal's &mdash; and never reads the "
                   "advisor's claimed verdict. A disagreement between the claim and the "
                   "recomputation is itself a separately-named violation. "
                   "<code>:undetermined</code> blocks too: the remedy is to supply the missing "
                   "attribute and re-run, not to approve past not knowing.")
              ["Position" "Outcome" "Verdict / rules fired" "Legal basis / detail"]
              (map material-row material-facts))

     (section "Jurisdiction spec-basis catalog"
              (str "<code>partyops.facts/catalog</code> &mdash; the official sources the "
                   "governor requires before any <code>:position/verify</code> proposal may "
                   "commit. Coverage is reported honestly: "
                   "<span class=\"num\">" (:covered cov) "</span> of "
                   "<span class=\"num\">" (:requested cov) "</span> requested jurisdictions "
                   "seeded. A jurisdiction absent from this table has NO spec-basis, and the "
                   "advisor must not invent one &mdash; that is exactly what position-2's "
                   "hold above demonstrates.")
              ["ISO3" "Jurisdiction" "Owner authority" "Legal basis" "Required evidence" "Provenance"]
              (map jurisdiction-row (sort-by key facts/catalog)))

     (section "Approver attribution"
              (str "Each human approval joined to the SSoT register its op actually wrote. "
                   "The retention column is DERIVED at render time by checking whether "
                   "<code>:approved-by</code> is present in the stored record, so it "
                   "self-corrects if the store changes. Where it says "
                   "<span class=\"warn\">audit only</span>, the approver is real and is in the "
                   "audit ledger below, but <code>partyops.store/commit-record!</code> did not "
                   "carry it into the record &mdash; that effect reads <code>:value</code> "
                   "(or neither), not <code>:payload</code>. Shown explicitly rather than "
                   "omitted, so 'nobody approved' and 'the store did not keep it' stay "
                   "distinguishable.")
              ["Position" "Op" "Approved by (audit)" "Retained in SSoT record?"]
              (map (partial approver-row db) approvals))

     (section "Position-publication records"
              (str "Drafts built by <code>partyops.registry</code>. Publication numbers are "
                   "jurisdiction-scoped sequences, not an invented national registry format; "
                   "every certificate this actor produces is UNSIGNED &mdash; signing is the "
                   "organization operator's own act.")
              ["Publication number" "Position" "Jurisdiction" "Kind" "Immutability"]
              (if (seq (store/publication-history db))
                (map publication-row (store/publication-history db))
                [(td "<span class=\"muted\">none</span>" "" "" "" "")]))

     (section (str "Audit ledger this run (<span class=\"num\">" (count ledger) "</span> facts)")
              "Append-only decision-fact log &mdash; every commit and hold this scenario produced, in order."
              ["Fact" "Op" "Position" "Basis"]
              (map ledger-row ledger))

     "</main>\n"
     "<footer>\n"
     "  <p>cloud-itonami-isic-9492 &middot; ISIC 9492 Activities of political organizations. "
     "Regenerate with <code>clojure -M:dev:render-html</code>. Deterministic: identical seed "
     "produces a byte-identical page.</p>\n"
     "</footer>\n"
     "</div>\n"
     "</body></html>\n")))

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        db (run-demo!)
        ledger (vec (store/ledger db))
        hard (holds ledger)]
    ;; Build-time INVARIANT, not a convention: a console that shows no
    ;; HARD hold has not demonstrated that the governor can actually
    ;; refuse anything, which is the only claim this page exists to
    ;; make. Fail the build rather than emit a page that looks fine.
    (when (zero? (count hard))
      (throw (ex-info (str "render-html: scenario produced 0 :governor-hold records. "
                           "The operator console must demonstrate at least one HARD, "
                           "un-overridable governor hold; refusing to write a page that "
                           "would imply the governor never refuses anything.")
                      {:ledger-facts (count ledger)
                       :fact-types (frequencies (map :t ledger))})))
    (io/make-parents out)
    (spit out (render db))
    (println "wrote" out
             (str "(" (count ledger) " ledger facts, "
                  (count hard) " HARD governor holds, "
                  (count (mapcat :violations hard)) " rule violations, "
                  (count (store/publication-history db)) " position publications)"))
    (println "  rules fired:" (str/join ", " (sort (distinct (map (comp name :rule)
                                                                  (mapcat :violations hard))))))))
