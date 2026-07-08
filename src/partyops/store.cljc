(ns partyops.store
  "SSoT for the political-organization-governance actor, behind a
  `Store` protocol so the backend is a swap, not a rewrite -- the same
  seam every prior `cloud-itonami-isic-*` actor in this fleet uses:

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps).
    - `DatomicStore` -- backed by `langchain.db`, a Datomic-API-compatible
                        EAV store (datalog q / pull / upsert). Pure `.cljc`,
                        so it runs offline AND can be pointed at a real
                        Datomic Local or a kotoba-server pod by swapping
                        `langchain.db`'s `:db-api` (see langchain.kotoba-db).

  Both implement the same protocol and pass the same contract
  (test/partyops/store_contract_test.clj), which is the whole point:
  the actor, the Political Organization Governance Governor and the
  audit ledger never know which SSoT they run on.

  Like `union.store`'s ballot-bearing entity, a POSITION is acted on
  directly by the ONE actuation op -- no dynamically-filed sub-record,
  and the double-actuation guard checks a dedicated `:published?`
  boolean rather than a `:status` value, the same discipline every
  prior governor's guards establish.

  The ledger stays append-only on every backend: 'which position was
  screened for a missing campaign-finance disclaimer, which position
  was published, on what jurisdictional basis, approved by whom' is
  always a query over an immutable log -- the audit trail a member
  trusting this organization needs, and the evidence the organization
  needs if a publication is later disputed."
  (:require #?(:clj  [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            [partyops.registry :as registry]
            [langchain.db :as d]))

(defprotocol Store
  (position [s id])
  (all-positions [s])
  (disclaimer-screen-of [s position-id] "committed campaign-finance-disclaimer screening verdict for a position, or nil")
  (verify-of [s position-id] "committed jurisdiction verification, or nil")
  (ledger [s])
  (publication-history [s] "the append-only position-publication history (partyops.registry drafts)")
  (next-sequence [s jurisdiction] "next position-publication-number sequence for a jurisdiction")
  (position-already-published? [s position-id] "has this position already been published?")
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-positions [s positions] "replace/seed the position directory (map id->position)"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained position set so the actor + tests run offline."
  []
  {:positions
   {"position-1" {:id "position-1" :position-name "climate-policy-platform"
                  :campaign-finance-disclaimer-included? true
                  :votes-in-favor 80 :votes-cast 100 :required-consensus-share 0.6
                  :published? false :jurisdiction "JPN" :status :intake}
    "position-2" {:id "position-2" :position-name "atlantis-platform"
                  :campaign-finance-disclaimer-included? true
                  :votes-in-favor 80 :votes-cast 100 :required-consensus-share 0.6
                  :published? false :jurisdiction "ATL" :status :intake}
    "position-3" {:id "position-3" :position-name "tax-reform-endorsement"
                  :campaign-finance-disclaimer-included? true
                  :votes-in-favor 45 :votes-cast 100 :required-consensus-share 0.6
                  :published? false :jurisdiction "JPN" :status :intake}
    "position-4" {:id "position-4" :position-name "candidate-endorsement"
                  :campaign-finance-disclaimer-included? false
                  :votes-in-favor 80 :votes-cast 100 :required-consensus-share 0.6
                  :published? false :jurisdiction "JPN" :status :intake}}})

;; ----------------------------- shared commit logic -----------------------------

(defn- publish-position!
  "Backend-agnostic `:position/mark-published` -- looks up the
  position via the protocol and drafts the position-publication
  record, and returns {:result .. :position-patch ..} for the caller
  to persist."
  [s position-id]
  (let [p (position s position-id)
        seq-n (next-sequence s (:jurisdiction p))
        result (registry/register-position-publication position-id (:jurisdiction p) seq-n)]
    {:result result
     :position-patch {:published? true
                      :publication-number (get result "publication_number")}}))

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (position [_ id] (get-in @a [:positions id]))
  (all-positions [_] (sort-by :id (vals (:positions @a))))
  (disclaimer-screen-of [_ id] (get-in @a [:disclaimer-screens id]))
  (verify-of [_ position-id] (get-in @a [:verifications position-id]))
  (ledger [_] (:ledger @a))
  (publication-history [_] (:publications @a))
  (next-sequence [_ jurisdiction] (get-in @a [:sequences jurisdiction] 0))
  (position-already-published? [_ position-id] (boolean (get-in @a [:positions position-id :published?])))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :position/upsert
      (swap! a update-in [:positions (:id value)] merge value)

      :verification/set
      (swap! a assoc-in [:verifications (first path)] payload)

      :disclaimer-screen/set
      (swap! a assoc-in [:disclaimer-screens (first path)] payload)

      :position/mark-published
      (let [position-id (first path)
            {:keys [result position-patch]} (publish-position! s position-id)
            jurisdiction (:jurisdiction (position s position-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:sequences jurisdiction] (fnil inc 0))
                       (update-in [:positions position-id] merge position-patch)
                       (update :publications registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-positions [s positions] (when (seq positions) (swap! a assoc :positions positions)) s))

(defn seed-db
  "A MemStore seeded with the demo position set. The deterministic
  default."
  []
  (->MemStore (atom (assoc (demo-data)
                           :verifications {} :disclaimer-screens {} :ledger [] :sequences {}
                           :publications []))))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------

(def ^:private schema
  "DataScript/Datomic-style schema: only constraint attrs are declared.
  Map/compound values (verification/disclaimer-screen payloads,
  ledger facts, position-publication records) are stored as EDN
  strings so `langchain.db` doesn't expand them into sub-entities --
  the same convention every sibling actor's store uses."
  {:position/id                  {:db/unique :db.unique/identity}
   :verification/position-id     {:db/unique :db.unique/identity}
   :disclaimer-screen/position-id {:db/unique :db.unique/identity}
   :ledger/seq                   {:db/unique :db.unique/identity}
   :publication/seq              {:db/unique :db.unique/identity}
   :sequence/jurisdiction        {:db/unique :db.unique/identity}})

(defn- enc [v] (pr-str v))
(defn- dec* [s] (when s (edn/read-string s)))

(defn- position->tx [{:keys [id position-name campaign-finance-disclaimer-included? votes-in-favor
                             votes-cast required-consensus-share published? jurisdiction status publication-number]}]
  (cond-> {:position/id id}
    position-name                                     (assoc :position/position-name position-name)
    (some? campaign-finance-disclaimer-included?)      (assoc :position/campaign-finance-disclaimer-included? campaign-finance-disclaimer-included?)
    votes-in-favor                                     (assoc :position/votes-in-favor votes-in-favor)
    votes-cast                                          (assoc :position/votes-cast votes-cast)
    required-consensus-share                             (assoc :position/required-consensus-share required-consensus-share)
    (some? published?)                                    (assoc :position/published? published?)
    jurisdiction                                            (assoc :position/jurisdiction jurisdiction)
    status                                                    (assoc :position/status status)
    publication-number                                          (assoc :position/publication-number publication-number)))

(def ^:private position-pull
  [:position/id :position/position-name :position/campaign-finance-disclaimer-included?
   :position/votes-in-favor :position/votes-cast :position/required-consensus-share
   :position/published? :position/jurisdiction :position/status :position/publication-number])

(defn- pull->position [m]
  (when (:position/id m)
    {:id (:position/id m) :position-name (:position/position-name m)
     :campaign-finance-disclaimer-included? (boolean (:position/campaign-finance-disclaimer-included? m))
     :votes-in-favor (:position/votes-in-favor m)
     :votes-cast (:position/votes-cast m)
     :required-consensus-share (:position/required-consensus-share m)
     :published? (boolean (:position/published? m))
     :jurisdiction (:position/jurisdiction m) :status (:position/status m)
     :publication-number (:position/publication-number m)}))

(defrecord DatomicStore [conn]
  Store
  (position [_ id]
    (pull->position (d/pull (d/db conn) position-pull [:position/id id])))
  (all-positions [_]
    (->> (d/q '[:find [?id ...] :where [?e :position/id ?id]] (d/db conn))
         (map #(pull->position (d/pull (d/db conn) position-pull [:position/id %])))
         (sort-by :id)))
  (disclaimer-screen-of [_ id]
    (dec* (d/q '[:find ?p . :in $ ?pid
                :where [?k :disclaimer-screen/position-id ?pid] [?k :disclaimer-screen/payload ?p]]
              (d/db conn) id)))
  (verify-of [_ position-id]
    (dec* (d/q '[:find ?p . :in $ ?pid
                :where [?a :verification/position-id ?pid] [?a :verification/payload ?p]]
              (d/db conn) position-id)))
  (ledger [_]
    (->> (d/q '[:find ?s ?f :where [?e :ledger/seq ?s] [?e :ledger/fact ?f]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (publication-history [_]
    (->> (d/q '[:find ?s ?r :where [?e :publication/seq ?s] [?e :publication/record ?r]] (d/db conn))
         (sort-by first)
         (mapv (comp dec* second))))
  (next-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :sequence/jurisdiction ?j] [?e :sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (position-already-published? [s position-id]
    (boolean (:published? (position s position-id))))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :position/upsert
      (d/transact! conn [(position->tx value)])

      :verification/set
      (d/transact! conn [{:verification/position-id (first path) :verification/payload (enc payload)}])

      :disclaimer-screen/set
      (d/transact! conn [{:disclaimer-screen/position-id (first path) :disclaimer-screen/payload (enc payload)}])

      :position/mark-published
      (let [position-id (first path)
            {:keys [result position-patch]} (publish-position! s position-id)
            jurisdiction (:jurisdiction (position s position-id))
            next-n (inc (next-sequence s jurisdiction))]
        (d/transact! conn
                     [(position->tx (assoc position-patch :id position-id))
                      {:sequence/jurisdiction jurisdiction :sequence/next next-n}
                      {:publication/seq (count (publication-history s)) :publication/record (enc (get result "record"))}])
        result)
      nil)
    s)
  (append-ledger! [s fact]
    (d/transact! conn [{:ledger/seq (count (ledger s)) :ledger/fact (enc fact)}])
    fact)
  (with-positions [s positions]
    (when (seq positions) (d/transact! conn (mapv position->tx (vals positions)))) s))

(defn datomic-store
  "A DatomicStore (langchain.db backend) seeded from `data`
  ({:positions ..}); empty when omitted."
  ([] (datomic-store {}))
  ([{:keys [positions]}]
   (let [s (->DatomicStore (d/create-conn schema))]
     (with-positions s positions))))

(defn datomic-seed-db
  "A DatomicStore seeded with the demo position set -- the Datomic-
  backed analog of `seed-db`, used to prove protocol parity."
  []
  (datomic-store (demo-data)))
