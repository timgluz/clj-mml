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
                ItemAverage, ItemKNN, UserItemBaseline]
           [MyMediaLite.Eval Ratings RatingsOnline RatingsCrossValidation]))


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
    (let [ignored_items (list->generic ignored_items)
          candidate_items (list->generic candidate_items)]
      (->RecommendationResults  
        (.Recommend (:model this) user-id n ignored_items candidate_items))
      ))
  (to-string [this] (.ToString (:model this)))
  RatingPredictorProtocol
  (get-ratings [this] (.Ratings (:model this)))
  (set-ratings [this ratings] (set! (.Ratings (:model this)) ratings))
  (get-max-rating [this] (.MaxRating (:model this)))
  (set-max-rating [this value] (.set_MaxRating (:model this) value))
  (get-data [this] (get-ratings this))
  (set-data [this value] (set-ratings this value))
  ModelPropertyProtocol
  (properties [this] 
    (let [ props  (-> (:model this) (.GetType)(.GetProperties))]
      (set 
        (map (fn [prop] (keyword (.Name prop))) 
          props))))
  (getp [this property]
    (if (contains? (properties this) property)
      (let [prop (-> (:model this) (.GetType) (#(.GetProperty %1 (name property))))]
        (.GetValue prop (:model this)))
      (println "Model dont have property: " property)))
  (setp [this property value]
    (if (contains? (properties this) property)
      (let [klass (-> (:model this)(.GetType))
            prop (.GetProperty klass (name property))]
        (if (true? (.CanWrite prop))
          (do 
            (.SetValue prop (:model this) value nil)
            (.GetValue prop (:model this)))
          (println "Property `" property "`isnot mutable.")
        ))
      (println "Property: " property " dont exists.")))
  EvaluateProtocol
  (measures [this] (set (map #(keyword %1) (Ratings/Measures))))
  (evaluate [this test-data]
    (evaluate this test-data nil))
  (evaluate [this test-data training-data]
    (->> 
      (Ratings/Evaluate (:model this) test-data training-data)
      (map (fn [row] {(keyword (.Key row)) (.Value row)}))
      (apply merge)
      ))
  (crossvalidate [this] (crossvalidate this 5 false false))
  (crossvalidate [this num-iter] (crossvalidate this num-iter false false))
  (crossvalidate [this num-iter compute-fit]
    (crossvalidate this num-iter compute-fit false))
  (crossvalidate [this num-iter compute-fit verbose] 
    (->>
      (RatingsCrossValidation/DoCrossValidation (:model this) num-iter compute-fit verbose)
      (map (fn [row] {(keyword (.Key row)) (.Value row)}))
      (apply merge)))

  (evaluate-online [this data]
    (->>
      (RatingsOnline/EvaluateOnline (:model this) data)
      (map (fn [row] {(keyword (.Key row)) (.Value row)}))
      (apply merge)))
  )

(defn base-init
  "Base init"
  ([Model] (map->RatingRecommender {:model Model}))
  ([Model config-map]
   (let [oracle (->RatingRecommender Model config-map)
         training-data (get config-map :training-data nil)]
     (when (not-nil? training-data)
       (.set-data oracle training-data))
     oracle
     ))
  )

(defmulti init :model)
(defmethod init :BiasedMatrixFactorization  [configs]
  (base-init (BiasedMatrixFactorization.) configs))
(defmethod init :BiPolarSlopeOne [configs]
  (base-init (BiPolarSlopeOne.) configs))
(defmethod init :UserItemBaseline [configs]
  (base-init (UserItemBaseline.) configs))
(defmethod init :default [configs] 
  (println "Ouch, you forgot something: " configs ))


