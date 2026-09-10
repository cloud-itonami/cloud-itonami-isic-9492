(ns partyops.store-contract-test
  "The Store contract, run against BOTH backends. Proving MemStore and
  the Datomic-backed (langchain.db) store satisfy the same contract is
  what makes 'swap the SSoT for Datomic / kotoba-server' a
  configuration change, not a rewrite -- see `cloud-itonami-isic-6511`'s
  `underwriting.store-contract-test` for the same pattern on the
  sibling actor."
  (:require [clojure.test :refer [deftest is testing]]
            [partyops.store :as store]))

(defn- backends []
  [["MemStore" (store/seed-db)] ["DatomicStore" (store/datomic-seed-db)]])

(deftest read-parity
  (doseq [[label s] (backends)]
    (testing label
      (is (= "climate-policy-platform" (:position-name (store/position s "position-1"))))
      (is (= "JPN" (:jurisdiction (store/position s "position-1"))))
      (is (true? (:campaign-finance-disclaimer-included? (store/position s "position-1"))))
      (is (= 45 (:votes-in-favor (store/position s "position-3"))))
      (is (false? (:campaign-finance-disclaimer-included? (store/position s "position-4"))))
      (is (false? (:published? (store/position s "position-1"))))
      (is (= ["position-1" "position-2" "position-3" "position-4"]
             (mapv :id (store/all-positions s))))
      (is (nil? (store/disclaimer-screen-of s "position-1")))
      (is (nil? (store/material-screen-of s "position-1")))
      (is (nil? (store/verify-of s "position-1")))
      (is (= [] (store/ledger s)))
      (is (= [] (store/publication-history s)))
      (is (zero? (store/next-sequence s "JPN")))
      (is (false? (store/position-already-published? s "position-1"))))))

(deftest write-and-ledger-parity
  (doseq [[label s] (backends)]
    (testing label
      (testing "partial upsert merges, preserving untouched fields"
        (store/commit-record! s {:effect :position/upsert
                                 :value {:id "position-1" :position-name "climate-policy-platform"}})
        (is (= "climate-policy-platform" (:position-name (store/position s "position-1"))))
        (is (true? (:campaign-finance-disclaimer-included? (store/position s "position-1"))) "unrelated field preserved"))
      (testing "verification / disclaimer-screen payloads commit and read back"
        (store/commit-record! s {:effect :verification/set :path ["position-1"]
                                 :payload {:jurisdiction "JPN" :checklist ["a" "b"]}})
        (is (= {:jurisdiction "JPN" :checklist ["a" "b"]} (store/verify-of s "position-1")))
        (store/commit-record! s {:effect :disclaimer-screen/set :path ["position-1"]
                                 :payload {:position-id "position-1" :disclaimer-included? true}})
        (is (= {:position-id "position-1" :disclaimer-included? true} (store/disclaimer-screen-of s "position-1"))))
      (testing "material-screen payload commits and reads back on both backends"
        (let [payload {:position-id "position-1" :medium :poster
                       :attrs {:within-campaign-period? true}
                       :intents [] :claimed-verdict :permitted-with-obligations
                       :required-evidence ["material-attribution-record"]}]
          (store/commit-record! s {:effect :material-screen/set :path ["position-1"]
                                   :payload payload})
          (is (= payload (store/material-screen-of s "position-1")))
          (is (nil? (store/material-screen-of s "position-2")))))
      (testing "position-publication drafts a record and advances the sequence"
        (store/commit-record! s {:effect :position/mark-published :path ["position-1"]})
        (is (= "JPN-POS-000000" (get (first (store/publication-history s)) "record_id")))
        (is (= "position-publication-draft" (get (first (store/publication-history s)) "kind")))
        (is (true? (:published? (store/position s "position-1"))))
        (is (= 1 (count (store/publication-history s))))
        (is (= 1 (store/next-sequence s "JPN")))
        (is (true? (store/position-already-published? s "position-1")))
        (is (false? (store/position-already-published? s "position-2"))))
      (testing "ledger is append-only and order-preserving"
        (store/append-ledger! s {:op :a :disposition :commit})
        (store/append-ledger! s {:op :b :disposition :hold})
        (is (= [:commit :hold] (mapv :disposition (store/ledger s))))))))

(deftest datomic-empty-store-is-usable
  (let [s (store/datomic-store)]
    (is (nil? (store/position s "nope")))
    (is (= [] (store/all-positions s)))
    (is (= [] (store/ledger s)))
    (is (= [] (store/publication-history s)))
    (is (zero? (store/next-sequence s "JPN")))
    (store/with-positions s {"x" {:id "x" :position-name "n"
                                  :campaign-finance-disclaimer-included? true
                                  :votes-in-favor 80 :votes-cast 100 :required-consensus-share 0.6
                                  :published? false :jurisdiction "JPN" :status :intake}})
    (is (= "n" (:position-name (store/position s "x"))))))
