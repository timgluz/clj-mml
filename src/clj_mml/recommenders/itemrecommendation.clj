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

(def models #{:BPRLinear, :BPRMF, :CLiMF :ItemKNN, :ItemAttributeKNN, 
              :ItemAttributeSVM, :MostPopular :MostPopularByAttributes, 
              :MultiCoreBPRMF, :SoftMarginRankingMF :UserAttributeKNN, 
              :UserKNN, :WRMF, :WeightedBPRMF, :Zero})


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

(defmacro init-new-model [model-name]
  (let [sym (gensym)]
    `(let [~sym (new ~(symbol (name model-name)))]
       ~sym)))

(macroexpand-1 '(init-new-model :Zero))

(defn init
  "Base init"
  ([model-name] 
    (when (contains? models model-name)
      (let [model (init-new-model model-name)]
        (map->ItemRecommender {:model model}))))
  ([model-name & configs]
   (let [config-map (apply hash-map configs)
         oracle (init model-name)
         training-data (get config-map :training-data nil)]
     (when (not-nil? oracle)
       (when (not-nil? training-data) (.set-data oracle training-data))
       (.set-properties oracle config-map)
       oracle
       ))))

