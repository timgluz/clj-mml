;------------------------------------------------------------------------------
; includes protocol and helpers, which are specific for RatingRecommenders
;
; -----------------------------------------------------------------------------

(ns clj-mml.recommenders.ratingprediction 
  (:refer-clojure :exclude [System.Random])
  (:use [clj-mml.recommenders.core])
  (:import [MyMediaLite.RatingPrediction BiasedMatrixFactorization, BiPolarSlopeOne
            CoClustering, Constant, EntityAverage, FactorWiseMatrixFactorization 
            GlobalAverage, ItemAttributeKNN, ItemAverage, ItemKNN
            LatentFeatureLogLinearModel, MatrixFactorization, NaiveBayes
            SigmoidCombinedAsymmetricFactorModel, SigmoidItemAsymmetricFactorModel
            SigmoidSVDPlusPlus, SigmoidUserAsymmetricFactorModel, SlopeOne 
            SocialMF, SVDPlusPlus, TimeAwareBaseline, TimeAwareBaselineWithFrequencies
            UserAttributeKNN, UserAverage, UserItemBaseline, UserKNN]
           [MyMediaLite.Eval Ratings RatingsOnline RatingsCrossValidation]))


(defrecord RatingRecommender [model configs]
  RatingPredictorProtocol
  (get-ratings [this] (.Ratings (:model this)))
  (set-ratings [this ratings] (set! (.Ratings (:model this)) ratings))
  (get-max-rating [this] (.MaxRating (:model this)))
  (set-max-rating [this value] (.set_MaxRating (:model this) value))
  RecommenderProtocol
  (get-data [this] (get-ratings this))
  (set-data [this value] (set-ratings this value))
  (load-model [this filename] (.LoadModel (:model this)))
  (train [this] 
    (do 
      (.Train (:model this))
      this))
  (train [this training-data]
    (do
      (.set-data this training-data)
      (.train this)))
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
  ModelPropertyProtocol
  (to-string [this] (.ToString (:model this)))
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
            prop (.GetProperty klass (name property))
            typed-value (convert-type value)]
        (if (true? (.CanWrite prop))
          (do 
            (.SetValue prop (:model this) typed-value nil)
            (.GetValue prop (:model this)))
          (println "Property `" property "`is not mutable.")
        ))
        (println "Property: " property " dont exists.")))
    (get-properties [this]
      (->> 
        (.properties this)
        (map (fn [prop] 
               (try
                 {prop (.getp this prop)}
                 (catch Exception e
                   {prop nil}))))
        (apply merge)
       ))
  (set-properties [this config-map]
    (let [properties (.properties this)]
     (->> 
       (filter #(contains? properties (key %1)) config-map)
       (map (fn [row] (.setp this (first row) (second row))))
       (doall) ;;run lazy-lists
       )))
 EvaluateProtocol
  (measures [this] (set (map #(keyword %1) (Ratings/Measures))))
  (evaluate [this test-data]
    (evaluate this test-data nil))
  (evaluate [this test-data training-data]
    (tuple->map
      (Ratings/Evaluate (:model this) test-data training-data)))
  (crossvalidate [this] (crossvalidate this 5 false false))
  (crossvalidate [this num-iter] (crossvalidate this num-iter false false))
  (crossvalidate [this num-iter compute-fit]
    (crossvalidate this num-iter compute-fit false))
  (crossvalidate [this num-iter compute-fit verbose] 
    (tuple->map
      (RatingsCrossValidation/DoCrossValidation (:model this) num-iter compute-fit verbose)))

  (evaluate-online [this data]
    (->>
      (RatingsOnline/EvaluateOnline (:model this) data)
      (map (fn [row] {(keyword (.Key row)) (.Value row)}))
      (apply merge)))
  )

(defn base-init
  "Base init"
  ([Model] (map->RatingRecommender {:model Model}))
  ([Model configs]
   (let [config-map (apply hash-map configs)
         oracle (base-init Model)
         training-data (get config-map :training-data nil)]
     (when (not-nil? training-data)
       (.set-data oracle training-data))
     (.set-properties oracle config-map)
     oracle
     )))

;;TODO: refactor it
(defmulti init (fn [x & _] (keyword x)))
(defmethod init :BiasedMatrixFactorization  [model-name & configs]
  (base-init (BiasedMatrixFactorization.) configs))
(defmethod init :BiPolarSlopeOne [model-name & configs]
  (base-init (BiPolarSlopeOne.) configs))
(defmethod init :CoClustering [model-name & configs]
  (base-init (CoClustering.) configs))
(defmethod init :Constant [model-name & configs]
  (base-init (Constant.) configs))
(defmethod init :FactorWiseMatrixFactorization [model-name & configs]
  (base-init (FactorWiseMatrixFactorization.) configs))
(defmethod init :GlobalAverage [model-name & configs]
  (base-init (GlobalAverage.) configs))
(defmethod init :ItemAttributeKNN [model-name & configs]
  (base-init (ItemAttributeKNN.) configs))
(defmethod init :ItemAverage [model-name & configs]
  (base-init (ItemAverage.) configs))
(defmethod init :ItemKNN [model-name & configs]
  (base-init (ItemKNN.) configs))
(defmethod init :LatentFeatureLogLinearModel [model-name & configs]
  (base-init (LatentFeatureLogLinearModel.) configs))
(defmethod init :MatrixFactorization [model-name & configs]
  (base-init (MatrixFactorization.) configs))
(defmethod init :NaiveBayes [model-name & configs]
  (base-init (NaiveBayes.) configs))
(defmethod init :SigmoidCombinedAsymmetricFactorModel [model-name & configs]
  (base-init (SigmoidCombinedAsymmetricFactorModel.) configs))
(defmethod init :SigmoidItemAsymmetricFactorModel [model-name & configs]
  (base-init (SigmoidItemAsymmetricFactorModel.) configs))
(defmethod init :SigmoidSVDPlusPlus [model-name & configs]
  (base-init (SigmoidSVDPlusPlus.) configs))
(defmethod init :SigmoidUserAsymmetricFactorModel [model-name & configs]
  (base-init (SigmoidUserAsymmetricFactorModel.) configs))
(defmethod init :SlopeOne [model-name & configs]
  (base-init (SlopeOne.) configs))
(defmethod init :SocialMF [model-name & configs]
  (base-init (SocialMF.) configs))
(defmethod init :SVDPlusPlus [model-name & configs]
  (base-init (SVDPlusPlus.) configs))
(defmethod init :TimeAwareBaseline [model-name & configs]
  (base-init (TimeAwareBaseline.) configs))
(defmethod init :TimeAwareBaselineWithFrequencies [model-name & configs]
  (base-init (TimeAwareBaselineWithFrequencies.) configs))
(defmethod init :UserAttributeKNN [model-name & configs]
  (base-init (UserAttributeKNN.) configs))
(defmethod init :UserAverage [model-name & configs]
  (base-init (UserAverage.) configs))
(defmethod init :UserItemBaseline [model-name & configs]
  (base-init (UserItemBaseline.) configs))
(defmethod init :UserKNN [model-name & configs]
  (base-init (UserKNN.) configs))
(defmethod init :default [model-name & configs] 
  (println "Ouch, unknown model: " model-name))


