(ns partyops.sim
  "Demo driver -- `clojure -M:dev:run`. Walks a clean position through
  intake -> jurisdiction verification -> campaign-finance-disclaimer
  screening -> position-publication proposal (always escalates) ->
  human approval -> commit, then shows four HARD holds (a jurisdiction
  with no spec-basis, a position whose own recorded member-consensus
  vote share has NOT met its own recorded required threshold, a
  position whose own recorded campaign-finance disclaimer has NOT
  been included [screened directly via `:disclaimer/screen` -- never
  via an actuation op against an unscreened position -- see this
  actor's own governor ns docstring / the lesson `parksafety`'s
  ADR-2607071922 Decision 5, `eldercare`'s, `museum`'s,
  `conservation`'s, `salon`'s, `entertainment`'s, `casework`'s,
  `hospital`'s, `facility`'s, `school`'s, `association`'s, `leasing`'s,
  `behavioral`'s, `secondary`'s, `card`'s, `water`'s, `telecom`'s,
  `aerospace`'s, `recovery`'s, `consulting`'s, `union`'s,
  `congregation`'s, `fab`'s, `energy`'s, `care`'s, `navigator`'s,
  `learning`'s, `banking`'s, `advertising`'s, `polling`'s, `research`'s,
  `design`'s, `nursing`'s, `sports`'s, `alliedhealth`'s, `laundry`'s,
  `holdco`'s, `photo`'s, `personalservice`'s, `edsupport`'s,
  `headoffice`'s, `residential`'s, `cultural`'s, `reserve`'s,
  `proserv`'s, `sportsevent`'s, `recreation`'s and `sportsclub`'s
  ADR-0001s already recorded], and a double publication of an already-
  processed position) that never reach a human at all, and prints the
  audit ledger + the draft position-publication records."
  (:require [langgraph.graph :as g]
            [partyops.store :as store]
            [partyops.operation :as op]))

(def operator {:actor-id "op-1" :actor-role :governing-body-officer :phase 3})

(defn- exec! [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn -main [& _]
  (let [db (store/seed-db)
        actor (op/build db)]
    (println "== member/intake position-1 (JPN, clean; disclaimer included, consensus met) ==")
    (println (exec! actor "t1" {:op :member/intake :subject "position-1"
                                :patch {:id "position-1" :position-name "climate-policy-platform"}} operator))

    (println "== position/verify position-1 (escalates -- human approves) ==")
    (println (exec! actor "t2" {:op :position/verify :subject "position-1"} operator))
    (println (approve! actor "t2"))

    (println "== disclaimer/screen position-1 (clean; escalates -- human approves) ==")
    (println (exec! actor "t3" {:op :disclaimer/screen :subject "position-1"} operator))
    (println (approve! actor "t3"))

    (println "== actuation/publish-position position-1 (always escalates -- actuation/publish-position) ==")
    (let [r (exec! actor "t4" {:op :actuation/publish-position :subject "position-1"} operator)]
      (println r)
      (println "-- human governing-body officer approves --")
      (println (approve! actor "t4")))

    (println "== position/verify position-2 (no spec-basis -> HARD hold) ==")
    (println (exec! actor "t5" {:op :position/verify :subject "position-2" :no-spec? true} operator))

    (println "== position/verify position-3 (escalates -- human approves; sets up the consensus test) ==")
    (println (exec! actor "t6" {:op :position/verify :subject "position-3"} operator))
    (println (approve! actor "t6"))

    (println "== actuation/publish-position position-3 (consensus 45/100 < required 0.6 -> HARD hold) ==")
    (println (exec! actor "t7" {:op :actuation/publish-position :subject "position-3"} operator))

    (println "== disclaimer/screen position-4 (missing -> HARD hold, never reaches a human) ==")
    (println (exec! actor "t8" {:op :disclaimer/screen :subject "position-4"} operator))

    (println "== actuation/publish-position position-1 AGAIN (double-publication -> HARD hold) ==")
    (println (exec! actor "t9" {:op :actuation/publish-position :subject "position-1"} operator))

    (println "== audit ledger ==")
    (doseq [f (store/ledger db)] (println f))

    (println "== draft position-publication records ==")
    (doseq [r (store/publication-history db)] (println r))))
