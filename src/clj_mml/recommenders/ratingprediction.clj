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

(defrecord RatingRecommender [model settings]
  RecommenderProtocol
  (can-predict? [this user-id item-id] 
    (.CanPredict (:model this) user-id item-id))
  (load-model [this filename] (.LoadModel (:model this)))
  (recommend [this user-id settings]
      (.Recommend (:model this) user-id -1 nil nil))
  (to-string [this] (.ToString (:model this)))
  (train [this] (.Train (:model this)))
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
  ([Model training-data] 
      (let [oracle (base-init Model)]
        (.set-data oracle training-data)
        oracle
      )))

(defmulti init :model)
(defmethod init :BiasedMatrixFactorization  [params]
  (base-init (BiasedMatrixFactorization.)))
(defmethod init :BiPolarSlopeOne [params]
  (base-init (BiPolarSlopeOne.)))
(defmethod init :UserItemBaseline [params]
  (base-init (UserItemBaseline.)))
(defmethod init :default [params] 
  (println "Ouch, you forgot something: " params ))


