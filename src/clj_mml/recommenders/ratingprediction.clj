;------------------------------------------------------------------------------
; includes protocol and helpers, which are specific for RatingRecommenders
;
; -----------------------------------------------------------------------------

(ns clj-mml.recommenders.ratingprediction 
  (:refer-clojure :exclude [System.Random])
  (:use [clj-mml.recommenders.core])
  (:import [MyMediaLite.RatingPrediction BiasedMatrixFactorization, BiPolarSlopeOne
                CoClustering, Constant, EntityAverage,
                FactorWiseMatrixFactorization, GlobalAverage, 
                ItemAverage, ItemKNN, UserItemBaseline]))

(defn row2vect [row]
  [(.Item1 row) (.Item2 row)])
 
(defrecord RatingResults [rows]
  ResultProtocol
  (size [this] (count (:rows this)))
  (nth-result [this row-nr] 
    (row2vect
      (nth (:rows this) row-nr)))
  (to-vect [this] (map row2vect (seq (:rows this))))
  (to-map [this] 
    (reduce 
        (fn [coll, row] 
          (merge coll {(.Item1 row) (.Item2 row)}))
          {} (seq (:rows this)))
    ))

(defrecord RatingRecommender [model configs]
  RecommenderProtocol
  (load-model [this filename] (.LoadModel (:model this)))
  (train [this] 
    (do 
      (.Train (:model this))
      this))
  (can-predict? [this user-id item-id] 
    (.CanPredict (:model this) user-id item-id))
  (predict [this user-id item-id] 
    (.Predict (:model this) user-id item-id))
  (recommend [this user-id] (recommend this user-id -1 nil nil))
  (recommend [this user-id n] (recommend this user-id n nil nil))
  (recommend [this user-id n ignored_items] (recommend this user-id n ignored_items nil))
  (recommend [this user-id n ignored_items candidate_items] 
    (->RatingResults  
      (.Recommend (:model this) user-id n ignored_items candidate_items)
    ))
  (to-string [this] (.ToString (:model this)))
  RatingPredictorProtocol
  (get-ratings [this] (.Ratings (:model this)))
  (set-ratings [this ratings] (set! (.Ratings (:model this)) ratings))
  (get-max-rating [this] (.MaxRating (:model this)))
  (set-max-rating [this value] (.set_MaxRating (:model this) value))
  (get-data [this] (get-ratings this))
  (set-data [this value] (set-ratings this value))
  )

(defn base-init
  "Base initiliazer, which will be extended by child namespaces.
  Usage: 
    (base-init (ItemAverage.))
    (def init (partial base-init (ItemAverage.))) ;; in child namespaces
  "
  ([Model] (map->RatingRecommender {:model Model}))
  ([Model configs] 
      (let [oracle (->RatingRecommender Model, configs)]
         (when (contains? configs :training-data)
            (.set-data oracle (:training-data configs)))
        oracle
      )))

(defmulti init :model)
(defmethod init :BiasedMatrixFactorization  [configs]
  (base-init (BiasedMatrixFactorization.) configs))
(defmethod init :BiPolarSlopeOne [configs]
  (base-init (BiPolarSlopeOne.) configs))
(defmethod init :UserItemBaseline [configs]
  (base-init (UserItemBaseline.) configs))
(defmethod init :default [configs] 
  (println "Ouch, you forgot something: " configs ))


