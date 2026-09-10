(ns partyops.material
  "選挙運動用文書図画・行為の**適法性 screen** への橋渡し —— `kotoba-lang/senkyo`。

  この actor が既に持っていた `:disclaimer/screen` は「表示義務が入っているか」
  という **1 点の boolean** で、その position が置かれた法域で**その手段を使って
  よいか**は誰も見ていなかった。ポスターを貼ってよいか、ビラを配ってよいか、
  戸別訪問してよいかは法域ごとに反対を向いており（JPN §138 は戸別訪問を全面禁止、
  USA は許可制自体が違憲、CAN は管理者が妨げることを禁じる）、**表示義務が満たされて
  いても手段が違法**ということが普通に起きる。

  ## この ns が持たないもの

  規制の中身は持たない。それは `senkyo.facts` の仕事で、ここは:

  1. position の法域 + 提案が主張する手段/属性 から **screen を独立に再計算**する
  2. その結果が commit してよいものかを **語彙として返す**

  だけを行う。**advisor が主張した verdict は一切参照しない** ——
  `partyops.registry/member-consensus-share-insufficient?` が提案を見ずに
  position 自身の票数から比率を計算し直すのと同じ規律である。

  ## 構造的に持たない意図

  `senkyo.screen/out-of-scope-intents` をそのまま deny 側の閉じた集合として使う。
  有権者ターゲティング・投票誘導の話法・候補者の優劣評価・投票率運用・配布実行指揮・
  信条プロファイリング・当落の裁定 —— これらは risk level で緩和されるゲートでは
  なく、**提案語彙に構築経路が無い**。境界を actor ごとに書き直さず、共有ライブラリ
  側の 1 つの集合を参照する（actor が増えるたびに文言が分岐しないため）。"
  (:require [senkyo.screen :as screen]
            [senkyo.facts :as senkyo-facts]
            [senkyo.medium :as senkyo-medium]))

(def out-of-scope-intents
  "共有ライブラリ側の境界をそのまま再輸出する。**この actor で再定義しない。**"
  screen/out-of-scope-intents)

(def blocking-verdicts
  "commit させてはならない verdict。

  - `:out-of-scope`   扱わない意図が混ざっている
  - `:no-spec-basis`  その法域のカタログが無い（「規制が無い」ではない）
  - `:prohibited`     その手段自体が禁じられている
  - `:undetermined`   決まらない項目が残っている

  **`:undetermined` も HARD にする。** 人が承認して通せる種類の未決ではなく、
  属性を補って再実行すべきものだからである。『分からないまま承認する』経路を
  作らない。"
  #{:out-of-scope :no-spec-basis :prohibited :undetermined})

(defn screenable-media
  "senkyo が手段として知っているもの。"
  []
  (senkyo-medium/medium-ids))

(defn recompute
  "position と提案から screen を**独立に**再計算する。
  提案が主張した verdict は読まない（読むと再計算の意味が無い）。

  `proposal-value` は `{:medium kw :attrs {..} :intents [..]
  :require-high-confidence? bool}`。法域は **position 側**から取る ——
  提案に法域を書かせると、advisor が法域を選べてしまう。"
  [position {:keys [medium attrs intents require-high-confidence?]}]
  (screen/screen {:iso3 (:jurisdiction position)
                  :medium medium
                  :attrs (or attrs {})
                  :intents (set (or intents #{}))
                  :require-high-confidence? require-high-confidence?}))

(defn blocking?
  "再計算した screen 結果が commit を止めるべきものか。"
  [screen-result]
  (boolean (blocking-verdicts (:verdict screen-result))))

(defn verdict-mismatch?
  "advisor が主張した verdict と、独立に再計算した verdict が食い違うか。
  食い違いは**それ自体が違反**である（advisor が結論を作った証拠）。
  主張が無い（nil）ときは mismatch としない —— 主張していないものは偽れない。"
  [claimed recomputed]
  (boolean (and claimed (not= claimed (:verdict recomputed)))))

(defn hold-detail
  "HOLD 理由の人間向け説明。screen 結果の note をそのまま運ぶ（governor が
  文言を作り直すと、規制の説明が 2 か所に分かれる）。"
  [screen-result]
  (str "法域 " (:iso3 screen-result) " / 手段 " (:medium screen-result)
       " の screen 結果が " (:verdict screen-result) ": " (:note screen-result)))

(defn jurisdiction-supported?
  "senkyo にその法域の規制カタログがあるか。`partyops.facts` の
  campaign-finance-disclosure カタログとは**別の表**なので、片方にあって
  もう片方に無いことがありうる。"
  [iso3]
  (senkyo-facts/jurisdiction-covered? iso3))

(defn coverage
  "senkyo 側のカバレッジをそのまま返す（この actor が自前で数え直さない）。"
  []
  (senkyo-facts/coverage))
