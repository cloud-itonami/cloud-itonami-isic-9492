(ns partyops.registry
  "Pure-function position-publication record construction -- an
  append-only organization book-of-record draft.

  Like every sibling actor's registry, there is no single international
  check-digit standard for a position-publication reference number --
  every organization/jurisdiction assigns its own reference format.
  This namespace does NOT invent one; it builds a jurisdiction-scoped
  sequence number and validates the record's required fields, the
  same honest, non-fabricating discipline `partyops.facts` uses.

  `member-consensus-share-insufficient?` is the FOURTH instance of
  this fleet's ratio-based check family (`leasing.registry/collateral-
  coverage-ratio-insufficient?` established the first, MINIMUM-floor
  direction; `behavioral.registry/supervision-ratio-insufficient?`
  the second, MAXIMUM-ceiling direction; `union.registry/strike-vote-
  share-insufficient?` the third, MINIMUM-floor direction), applying
  the SAME quotient-comparison shape -- MINIMUM-floor direction, like
  `leasing`'s and `union`'s -- to a position's own recorded votes-in-
  favor divided by its own recorded votes-cast against its own
  recorded required-consensus-share threshold. An honest reuse of the
  family shape, not claimed as new.

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real party-management/balloting system. It builds the
  RECORD an organization would keep, not the act of publishing the
  position itself (that is `partyops.operation`'s `:actuation/
  publish-position`, always human-gated -- see README `Actuation`)."
  (:require [clojure.string :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is
  the organization operator's own act, not this actor's. See README
  `Actuation`."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(defn member-consensus-share-insufficient?
  "Does `position`'s own `:votes-in-favor` divided by its own `:votes-
  cast` fall BELOW its own recorded `:required-consensus-share`
  threshold? A pure ground-truth check against the position's own
  permanent fields -- no upstream comparison needed. The FOURTH
  instance of this fleet's ratio-based check family (see ns
  docstring), MINIMUM-floor direction like `leasing`'s/`union`'s."
  [{:keys [votes-in-favor votes-cast required-consensus-share]}]
  (and (number? votes-in-favor) (number? votes-cast) (pos? votes-cast)
       (number? required-consensus-share)
       (< (/ (double votes-in-favor) votes-cast) required-consensus-share)))

(defn register-position-publication
  "Validate + construct the POSITION-PUBLICATION registration DRAFT --
  the organization's own act of publishing a real political position
  or endorsement on its behalf. Pure function -- does not touch any
  real party-management system; it builds the RECORD an organization
  would keep. `partyops.governor` independently re-verifies the
  position's own campaign-finance-disclaimer status and member-
  consensus share, and blocks a double-publication of the same
  position, before this is ever allowed to commit."
  [position-id jurisdiction sequence]
  (when-not (and position-id (not= position-id ""))
    (throw (ex-info "position-publication: position_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "position-publication: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "position-publication: sequence must be >= 0" {})))
  (let [publication-number (str (str/upper-case jurisdiction) "-POS-" (zero-pad sequence 6))
        record {"record_id" publication-number
                "kind" "position-publication-draft"
                "position_id" position-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "publication_number" publication-number
     "certificate" (unsigned-certificate "PositionPublication" publication-number publication-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
