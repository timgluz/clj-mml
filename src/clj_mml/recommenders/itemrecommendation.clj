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

(def models #{:BPRLinear :BPRMF :CLiMF :ItemAttributeKNN :ItemAttributeSVM
              :ItemKNN :MostPopular :MostPopularByAttributes 
              :MultiCoreBPRMF :SoftMarginRankingMF :UserAttributeKNN
             :UserKNN :WeightedBPRMF :Zero})


(defrecord ItemRecommender [model configs]
  ItemRecommenderProtocol
  (get-feedback [this] (.Feedback (:model this)))
  (set-feedback [this feedback] (set! (.Feedback (:model this)) feedback))

  RecommenderProtocol
  (get-data [this] (get-feedback this))
  (set-data [this feedback] (set-feedback this feedback))
  (load-model [this filename] (.LoadModel (:model this)))
  (train [this] (do 
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
            prop (.GetProperty klass (name property))
            typed-value (convert-type value)]
        (if (true? (.CanWrite prop))
          (do
            (.SetValue prop (:model this) typed-value nil)
            (.getp this property))
          (println "Property `" property "`isnot mutable.")
        ))
      (println "Property: " property " dont exists.")))

  (set-properties [this config-map]
    (let [properties (.properties this)]
     (->> 
        (filter #(contains? properties (key %1)) config-map)
        (map (fn [row] (.setp this (first row) (second row))))
        (doall) ;;process lazy-lists
       )))
  EvaluateProtocol
  (measures [this] (set (map #(keyword %1) (Items/Measures))))
  (evaluate [this test-data training-data]
    (evaluate this test-data training-data nil nil :overlap :no -1))
  (evaluate [this test-data training-data test-users]
    (evaluate this test-data training-data test-users nil :overlap :no -1))
  (evaluate [this test-data training-data test-users candidate-items]
    (evaluate this test-data training-data test-users candidate-items :overlap :no -1))
  (evaluate [this test-data training-data test-users candidate-items candidate-item-mode]
    (evaluate this test-data training-data test-users candidate-items candidate-item-mode :no -1))
  (evaluate [this test-data training-data test-users candidate-items
             candidate-item-mode repeated-events n]
    (let [test-users (list->generic test-users)
          candidate-items (list->generic candidate-items)
          candidate-item-mode (keyword->ucase candidate-item-mode)
          repeated-events (keyword->capitalize repeated-events)]
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
  ([Model configs]
   (let [config-map (apply hash-map configs)
         oracle (base-init Model)
         training-data (get config-map :training-data nil)]
     (do 
       (when (not-nil? training-data)
          (.set-data oracle training-data))
       (.set-properties oracle config-map)
       oracle
       ))))

;;TODO: refactor to macro + use models collection to check existance
(defmulti init (fn [x & _] (keyword x)))
(defmethod init :BPRLinear [model-name & configs]
  (base-init (BPRLinear.) configs))
(defmethod init :BPRMF [model-name & configs]
  (base-init (BPRMF. ) configs))
(defmethod init :CLiMF [model-name & configs]
  (base-init (CLiMF. ) configs))
(defmethod init :ItemAttributeKNN [model-name & configs]
  (base-init (ItemAttributeKNN.) configs))
(defmethod init :ItemAttributeSVM [model-name & configs]
  (base-init (ItemAttributeSVM.) configs))
(defmethod init :ItemKNN [model-name & configs]
  (base-init (ItemKNN.) configs))
(defmethod init :MostPopular [model-name & configs]
  (base-init (MostPopular.) configs))
(defmethod init :MostPopularByAttributes [model-name & configs]
  (base-init (MostPopularByAttributes.) configs))
(defmethod init :MultiCoreBPRMF [model-name & configs]
  (base-init (MultiCoreBPRMF.) configs))
(defmethod init :SoftMarginRankingMF [model-name & configs]
  (base-init (SoftMarginRankingMF.) configs))
(defmethod init :UserAttributeKNN [model-name & configs]
  (base-init (UserAttributeKNN.) configs))
(defmethod init :UserKNN [model-name & configs]
  (base-init (UserKNN.) configs))
(defmethod init :WeightedBPRMF [model-name & configs]
  (base-init (WeightedBPRMF.) configs))
(defmethod init :WRMF [model-name & configs]
  (base-init (WRMF.) configs))
(defmethod init :Zero [model-name & configs]
  (base-init (Zero.) configs))
(defmethod init :default [model-name & configs]
  (println "Ouch, you forgot something: ", configs))

