;------------------------------------------------------------------------------
;ItemRecommendation
;
;this namespace contains item-recommendation protocol and multimethod to
;initialize new models.
;------------------------------------------------------------------------------

(ns clj-mml.recommenders.itemrecommendation
  (:use [clj-mml.recommenders.core])
  (:import [MyMediaLite.ItemRecommendation BPRLinear, BPRMF, CLiMF,
            ItemAttributeKNN, ItemAttributeSVM, MostPopular,
            MostPopularByAttributes, UserAttributeKNN, UserKNN, WRMF, Zero]))

;TODO: finish
(defrecord ItemRecommender [model settings])


(defn base-init
  "Base init"
  ([Model] (map->ItemRecommender {:model Model}))
  )
