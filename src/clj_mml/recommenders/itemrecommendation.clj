;------------------------------------------------------------------------------
;ItemRecommendation
;
;this namespace contains item-recommendation protocol and multimethod to
;initialize new models.
;------------------------------------------------------------------------------

(ns clj-mml.recommenders.itemrecommendation
  (:use [clj-mml.recommenders.core])
  (:import [MyMediaLite.ItemRecommendation BPRLinear, BPRMF, CLiMF
            ItemKNN, ItemAttributeKNN, ItemAttributeSVM, MostPopular
            MostPopularByAttributes, MultiCoreBPRMF, SoftMarginRankingMF
            UserAttributeKNN, UserKNN, WRMF, WeightedBPRMF, Zero]
           [MyMediaLite.Eval Items, ItemsCrossValidation, ItemsOnline,
            CandidateItems, RepeatedEvents]))

(defrecord ItemRecommender [model configs]
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
  ItemRecommenderProtocol
  (get-feedback [this] (.Feedback (:model this)))
  (set-feedback [this feedback] (set! (.Ratings (:model this)) feedback))
  (get-data [this] (get-feedback this))
  (set-data [this feedback] (set-feedback this feedback))
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
  ItemEvaluateProtocol
  (measures [this] (set (map #(keyword %1) (Items/Measures))))
  (evaluate [this test-data training-data]
    (evaluate this test-data training-data nil nil :overlap :no -1))
  (evaluate [this test-data training-data test-users]
    (evaluate this test-data training-data test-users nil :overlap :no -1))
  (evaluate [this test-data training-data test-users candidate-items
             candidate-item-mode repeated-events n]
    (let [candidate-item-mode (keyword->ucase candidate-item-mode)
          repeated-events (keyword->ucase repeated-events)]
      (tuple->map
        (Items/Evaluate (:model this) test-data training-data test-users 
        candidate-items (enum-val CandidateItems candidate-item-mode) 
                        (enum-val RepeatedEvents repeated-events) n
        ))))
  (crossvalidate [this num-folds test-users candidate-items]
    (crossvalidate this num-folds test-users candidate-items
                   :overlap false false))
  (crossvalidate [this num-folds test-users candidate-items, 
                  candidate-item-mode compute-fit verbose]
    (let [candidate-item-mode (keyword->ucase candidate-item-mode)]
      (tuple->map
        (ItemsCrossValidation/DoCrossValidation (:model this) num-folds
        test-users candidate-items 
        (enum-val CandidateItems candidate-item-mode) compute-fit verbose))))
  (evaluate-online [this test-data training-data test-users
                    candidate-items candidate-item-mode]
    (let [candidate-item-mode (enum-val CandidateItems 
                                        (keyword->ucase candidate-item-mode))]
      (tuple->map
        (ItemsOnline/EvaluateOnline (:model this) test-data training-data
          test-users candidate-items candidate-item-mode))))
  )

(defn base-init
  "Base init"
  ([Model] (map->ItemRecommender {:model Model}))
  ([Model config-map]
   (let [oracle (base-init Model)
         training-data (get config-map :training-data nil)]
     (do 
       (when (not-nil? training-data)
          (.set-data oracle training-data))
       oracle
       ))))

(defmulti init :model)
(defmethod init :BPRLinear [configs]
  (base-init (BPRLinear.) configs))
(defmethod init :BPRMF [configs]
  (base-init (BPRMF. ) configs))
(defmethod init :CLiMF [configs]
  (base-init (CLiMF. ) configs))
(defmethod init :ItemAttributeKNN [configs]
  (base-init (ItemAttributeKNN.) configs))
(defmethod init :ItemAttributeSVM [configs]
  (base-init (ItemAttributeSVM.) configs))
(defmethod init :ItemKNN [configs]
  (base-init (ItemKNN.) configs))
(defmethod init :MostPopular [configs]
  (base-init (MostPopular.) configs))
(defmethod init :MostPopularByAttributes [configs]
  (base-init (MostPopularByAttributes.) configs))
(defmethod init :MultiCoreBPRMF [configs]
  (base-init (MultiCoreBPRMF.) configs))
(defmethod init :SoftMarginRankingMF [configs]
  (base-init (SoftMarginRankingMF.) configs))
(defmethod init :UserAttributeKNN [configs]
  (base-init (UserAttributeKNN.) configs))
(defmethod init :UserKNN [configs]
  (base-init (UserKNN.) configs))
(defmethod init :WeightedBPRMF [configs]
  (base-init (WeightedBPRMF.) configs))
(defmethod init :WRMF [configs]
  (base-init (WRMF.) configs))
(defmethod init :Zero [configs]
  (base-init (Zero.) configs))
(defmethod init :default [configs]
  (println "Ouch, you forgot something: ", configs))

