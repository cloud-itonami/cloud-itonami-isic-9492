(ns partyops.material-test
  "`:material/screen` の契約 —— 選挙運動用文書図画・行為の適法性を
  `kotoba-lang/senkyo` に対して**独立に再計算**し、通らないものを HARD hold
  する。この repo が既に持っていた `:disclaimer/screen`（表示義務の有無という
  1 点の boolean）では捕まらなかった側面を覆う。

  中心の不変条件:

  1. 法域は **position 側**から取る。提案に法域を書かせない。
  2. advisor が主張した verdict は**読まない**。読んだら再計算の意味が無い。
  3. `:undetermined` も HARD。『分からないまま承認する』経路を作らない。
  4. `:material/screen` はどの phase でも auto にならない。"
  (:require [clojure.test :refer [deftest is testing]]
            [langgraph.graph :as g]
            [partyops.governor :as governor]
            [partyops.material :as material]
            [partyops.operation :as op]
            [partyops.phase :as phase]
            [partyops.store :as store]
            [senkyo.screen :as senkyo-screen]))

(def operator {:actor-id "op-1" :actor-role :governing-body-officer :phase 3})

(def positions
  {"jp-1"  {:id "jp-1" :position-name "jp-platform" :jurisdiction "JPN"
            :campaign-finance-disclaimer-included? true
            :votes-in-favor 80 :votes-cast 100 :required-consensus-share 0.6
            :published? false :status :intake}
   "can-1" {:id "can-1" :position-name "can-platform" :jurisdiction "CAN"
            :campaign-finance-disclaimer-included? true
            :votes-in-favor 80 :votes-cast 100 :required-consensus-share 0.6
            :published? false :status :intake}
   "atl-1" {:id "atl-1" :position-name "atlantis-platform" :jurisdiction "ATL"
            :campaign-finance-disclaimer-included? true
            :votes-in-favor 80 :votes-cast 100 :required-consensus-share 0.6
            :published? false :status :intake}})

(defn- fresh []
  (let [db (store/with-positions (store/seed-db) positions)]
    [db (op/build db)]))

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn- dispo [res] (get-in res [:state :disposition]))

(defn- rules [db]
  (into #{} (mapcat :basis (filter #(= :governor-hold (:t %)) (store/ledger db)))))

;; ---------------------------------------------------------------- 1

(deftest jurisdiction-comes-from-the-position-not-the-proposal
  (testing "提案に法域を書いても無視され、position の法域で screen される"
    (let [p (get positions "jp-1")
          r (material/recompute p {:medium :door-to-door
                                   :jurisdiction "USA"        ; ← 提案側の主張
                                   :attrs {:within-campaign-period? true}})]
      (is (= "JPN" (:iso3 r)) "advisor が法域を選べてはならない")
      (is (= :prohibited (:verdict r))))))

;; ---------------------------------------------------------------- 2

(deftest japan-door-to-door-is-a-hard-hold
  (let [[db actor] (fresh)
        res (exec-op actor "m1"
                     {:op :material/screen :subject "jp-1" :medium :door-to-door
                      :attrs {:within-campaign-period? true}}
                     operator)]
    (is (= :hold (dispo res)))
    (is (contains? (rules db) :campaign-conduct-not-permitted))
    (testing "SSoT には何も書かれない"
      (is (nil? (store/material-screen-of db "jp-1"))))
    (testing "hold でも ledger には 1 事実残る"
      (is (= 1 (count (store/ledger db)))))))

(deftest canada-door-to-door-is-not-a-hold
  (testing "同じ手段でも法域が違えば結論が変わる —— 表が実際に効いている証拠"
    (let [[db actor] (fresh)
          res (exec-op actor "m2"
                       {:op :material/screen :subject "can-1" :medium :door-to-door
                        :attrs {:within-campaign-period? true}}
                       operator)]
      (is (not= :hold (dispo res)))
      (is (not (contains? (rules db) :campaign-conduct-not-permitted))))))

;; ---------------------------------------------------------------- 3

(deftest fabricated-verdict-is-its-own-named-violation
  (testing "advisor が :permitted を主張しても、再計算と食い違えば別建ての違反になる"
    (let [[db actor] (fresh)
          res (exec-op actor "m3"
                       {:op :material/screen :subject "jp-1" :medium :door-to-door
                        :attrs {:within-campaign-period? true}
                        :fabricate-verdict? true}
                       operator)]
      (is (= :hold (dispo res)))
      (let [rs (rules db)]
        (is (contains? rs :screen-verdict-mismatch))
        (is (contains? rs :campaign-conduct-not-permitted)
            "禁止と虚偽主張は別の違反として同時に立つ")))))

(deftest mismatch-is-not-raised-when-nothing-was-claimed
  (testing "主張していないものは偽れない —— claimed が nil なら mismatch にしない"
    (let [p (get positions "can-1")
          r (material/recompute p {:medium :leaflet
                                   :attrs {:within-campaign-period? true}})]
      (is (false? (material/verdict-mismatch? nil r)))
      (is (true? (material/verdict-mismatch? :prohibited r))))))

;; ---------------------------------------------------------------- 4

(deftest out-of-scope-intent-stops-the-op
  (doseq [intent material/out-of-scope-intents]
    (let [[db actor] (fresh)
          res (exec-op actor (str "m4-" (name intent))
                       {:op :material/screen :subject "can-1" :medium :leaflet
                        :attrs {:within-campaign-period? true}
                        :intents [intent]}
                       operator)]
      (is (= :hold (dispo res)) (str intent " が素通りした"))
      (is (contains? (rules db) :campaign-conduct-not-permitted)))))

(deftest boundary-is-not-redefined-in-this-actor
  (testing "境界は共有ライブラリ側の集合をそのまま再輸出する（actor ごとに分岐させない）"
    (is (identical? senkyo-screen/out-of-scope-intents material/out-of-scope-intents))
    (is (contains? material/out-of-scope-intents :voter-targeting))
    (is (contains? material/out-of-scope-intents :distribution-dispatch))))

;; ---------------------------------------------------------------- 5

(deftest undetermined-cannot-be-approved-past
  (testing "属性を欠いた screen は escalate ではなく HOLD —— 承認で通せない"
    (let [[db actor] (fresh)
          ;; attrs を渡さない = 運動期間内かが決まらない
          res (exec-op actor "m5"
                       {:op :material/screen :subject "jp-1" :medium :leaflet}
                       operator)]
      (is (= :hold (dispo res)))
      (is (contains? (rules db) :campaign-conduct-not-permitted))
      (testing "verdict が :undetermined であることを直接も確認する"
        (is (= :undetermined
               (:verdict (material/recompute (get positions "jp-1") {:medium :leaflet}))))))))

(deftest uncovered-jurisdiction-holds-rather-than-passing
  (testing "senkyo に無い法域は :no-spec-basis で HOLD —— 「規制が無い」と扱わない"
    (let [[db actor] (fresh)
          res (exec-op actor "m6"
                       {:op :material/screen :subject "atl-1" :medium :poster
                        :attrs {:within-campaign-period? true}}
                       operator)]
      (is (= :hold (dispo res)))
      (is (false? (material/jurisdiction-supported? "ATL"))))))

;; ---------------------------------------------------------------- 6

(deftest a-clean-screen-escalates-then-commits
  (let [[db actor] (fresh)
        res (exec-op actor "m7"
                     {:op :material/screen :subject "jp-1" :medium :poster
                      :attrs {:within-campaign-period? true}}
                     operator)]
    (testing "clean でも auto では通らない（screening op は常に人を通す）"
      (is (= :interrupted (:status res))))
    (let [after (approve! actor "m7")]
      (is (= :commit (dispo after)))
      (let [payload (store/material-screen-of db "jp-1")]
        (is (some? payload))
        (is (= :permitted-with-obligations (:claimed-verdict payload)))
        (testing "要求される証跡が記録に残る"
          (is (seq (:required-evidence payload))))))))

;; ---------------------------------------------------------------- 7

(deftest material-screen-never-auto-at-any-phase
  (doseq [[n {:keys [auto]}] phase/phases]
    (is (not (contains? auto :material/screen))
        (str "phase " n " must not auto-commit :material/screen"))))

(deftest material-screen-is-a-write-op-gated-from-phase-2
  (is (contains? phase/write-ops :material/screen))
  (is (not (contains? (:writes (get phase/phases 1)) :material/screen)))
  (is (contains? (:writes (get phase/phases 2)) :material/screen))
  (testing "phase 1 では :phase-disabled で HOLD"
    (is (= {:disposition :hold :reason :phase-disabled}
           (phase/gate 1 {:op :material/screen} :commit)))))

;; ---------------------------------------------------------------- 8

(deftest governor-check-is-callable-directly
  (testing "governor 単体でも禁止を検出する（graph 経由に依存しない）"
    (let [db (store/with-positions (store/seed-db) positions)
          proposal {:value {:position-id "jp-1" :medium :door-to-door
                            :attrs {:within-campaign-period? true}
                            :claimed-verdict :permitted}
                    :cites ["公職選挙法 第138条"] :confidence 0.9}
          v (governor/check {:op :material/screen :subject "jp-1"} operator proposal db)]
      (is (true? (:hard? v)))
      (is (= #{:campaign-conduct-not-permitted :screen-verdict-mismatch}
             (into #{} (map :rule (:violations v))))))))

(deftest blocking-verdicts-are-named-and-closed
  (is (= #{:out-of-scope :no-spec-basis :prohibited :undetermined}
         material/blocking-verdicts)))
