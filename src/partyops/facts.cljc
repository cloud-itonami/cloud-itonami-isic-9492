(ns partyops.facts
  "Per-jurisdiction political-communication disclosure/imprint
  regulatory catalog -- the G2-style spec-basis table the Political
  Organization Governance Governor checks every position/verify
  proposal against ('did the advisor cite an OFFICIAL public source
  for this jurisdiction's campaign-finance-disclosure/imprint
  requirements, or did it invent one?').

  This blueprint's own actuation ('publishing a public political
  position or endorsement') and its own Trust Controls ('a fabricated
  member-consensus claim forces a hold, not an override') point at
  two genuinely real, distinct regulatory concerns for THIS vertical:
  (1) campaign-finance-disclosure/imprint labeling requirements for
  published political communications -- a well-documented, universal
  concern (US FECA 'paid for by' disclaimers, UK PPERA imprints,
  Japan's political-funds/election-law disclosure requirements,
  Germany's Impressumspflicht) -- and (2) genuine member/governing-
  body consensus before a position or endorsement may be published on
  the organization's behalf, not an LLM-assumed consensus.

  Coverage is reported HONESTLY (see `coverage`), the same discipline
  every sibling actor's `facts` namespace uses: a jurisdiction not in
  this table has NO spec-basis, full stop -- the advisor must not
  fabricate one, and the governor holds if it tries.

  Seed values are drawn from each jurisdiction's official election/
  political-finance regulator (see `:provenance`); they are a
  STARTING catalog, not a from-scratch survey of all ~194
  jurisdictions.")

(def catalog
  "iso3 -> requirement map. `:required-evidence` mirrors the generic
  member-consensus-record/disclaimer-compliance-record/governing-
  body-approval-record/publication-notice-record evidence set
  submitted in some form; `:legal-basis` / `:owner-authority` /
  `:provenance` are the G2 citation the governor requires before any
  :position/verify proposal can commit."
  {"JPN" {:name "Japan"
          :owner-authority "総務省 (Ministry of Internal Affairs and Communications, MIC)"
          :legal-basis "政治資金規正法 (Political Funds Control Act); 公職選挙法 (Public Offices Election Act) -- 政治的表示物の表示義務"
          :national-spec "政治団体の政治活動に係る文書図画の表示義務基準"
          :provenance "https://www.soumu.go.jp/senkyo/seiji_s/"
          :required-evidence ["会員合意記録 (member-consensus record)"
                              "表示義務遵守記録 (disclaimer-compliance record)"
                              "運営機関承認記録 (governing-body-approval record)"
                              "公表通知記録 (publication-notice record)"]}
   "USA" {:name "United States"
          :owner-authority "Federal Election Commission (FEC)"
          :legal-basis "Federal Election Campaign Act (FECA), 52 U.S.C. §30120 -- 'paid for by' disclaimer requirement"
          :national-spec "FEC disclaimer requirements for political communications (11 CFR §110.11)"
          :provenance "https://www.fec.gov/help-candidates-and-committees/making-disbursements/disclaimers/"
          :required-evidence ["Member-consensus record"
                              "Disclaimer-compliance record"
                              "Governing-body-approval record"
                              "Publication-notice record"]}
   "GBR" {:name "United Kingdom"
          :owner-authority "The Electoral Commission"
          :legal-basis "Political Parties, Elections and Referendums Act 2000 (PPERA) -- imprint requirements"
          :national-spec "Electoral Commission imprint rules for campaign/party political communications"
          :provenance "https://www.electoralcommission.org.uk/"
          :required-evidence ["Member-consensus record"
                              "Disclaimer-compliance record"
                              "Governing-body-approval record"
                              "Publication-notice record"]}
   "DEU" {:name "Germany"
          :owner-authority "Präsident des Deutschen Bundestages (party-finance oversight authority)"
          :legal-basis "Parteiengesetz (PartG) -- Transparenzpflichten; Impressumspflicht nach Medienstaatsvertrag (MStV) §18"
          :national-spec "PartG Rechenschaftspflicht; MStV Impressumspflicht für politische Kommunikation"
          :provenance "https://www.bundestag.de/parteienfinanzierung"
          :required-evidence ["Mitgliederkonsensprotokoll (member-consensus record)"
                              "Impressumsnachweis (disclaimer-compliance record)"
                              "Vorstandsbeschluss (governing-body-approval record)"
                              "Veröffentlichungsmitteilung (publication-notice record)"]}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO spec-basis,
  and the governor must hold any proposal that tries to publish a
  position on it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions actually
  have a spec-basis entry. Never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-isic-9492 R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog, not a survey of all ~194 "
                 "jurisdictions -- extend `partyops.facts/catalog`, "
                 "never fabricate a jurisdiction's requirements.")})))

(defn required-evidence-satisfied?
  "Does `submitted` (a set/coll of evidence keywords or strings) satisfy
  every evidence item listed for `iso3`? Missing spec-basis -> never
  satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (let [need (count required-evidence)
          have (count (filter (set submitted) required-evidence))]
      (= need have))))

(defn evidence-checklist [iso3]
  (:required-evidence (spec-basis iso3) []))
