;-------------------------------------------------------------------------------
; Recommender.clj
;
; sugar-candy around record of recommender
; Purpuse of this library is to hide point front of func-names and support 
; multiple arguments and default values for function arguments
;-------------------------------------------------------------------------------

(ns clj-mml.recommenders.recommender
  (:use [clj-mml.recommenders.core 
         :exclude [get-data, set-data, train, can-predict?, predict, 
                   recommend, evaluate, crossvalidate, evaluate-online]])
  (:require [clj-mml.io.read :as read]
            [clj-mml.dataset :as dataset]
            [clojure.string :as string]))

(defn matches-classname? [match obj]
  (not-nil? (re-find match
              (str (class obj)))))

(defn get-classname [obj]
  (last (string/split (-> obj class str) #"\.")))

;-- for IO 
(defmulti read-file (fn [oracle source] (get-classname oracle)))
(defmethod read-file "RatingRecommender" [oracle source]
  (read/ratingdata source))
(defmethod read-file "ItemRecommender" ItemRecommenderProtocol [oracle source]
  (read/itemdata source))
(defmethod read-file :default [oracle source]
  (println "Error: your recommender dont implement required Protocol."))

(defmulti build-dataset (fn [oracle datacoll](get-classname oracle)))
(defmethod build-dataset "RatingRecommender" [oracle datacoll]
    (dataset/build-ratings datacoll))
(defmethod build-dataset "ItemRecommender" [oracle datacoll]
    (dataset/build-items datacoll))

(defn get-data [oracle]
  (.get-data oracle))

(defn to-dataset [oracle source]
  (cond
    (true? (matches-classname? #"Data.*" oracle)) source ;existing dataset
    (string? source) (read-file oracle source) ;from file
    (coll? source) (build-dataset oracle source);from Clojure datacoll
    ))

(defn set-data [oracle source]
  (.set-data oracle (to-dataset oracle source)))

;-- for Recommendation&prediction
(defn train
  ([oracle] (.train oracle))
  ([oracle training-data] 
    (do 
      (set-data oracle training-data)
      (train oracle))))

(defn can-predict? [oracle user-id item-id]
  (.can-predict? oracle user-id item-id))

(defn predict [oracle user-id item-id]
  (.predict oracle user-id item-id))

(defn recommend 
  ([oracle user-id] (recommend oracle user-id -1 nil nil))
  ([oracle user-id n] (recommend oracle user-id n nil nil))
  ([oracle user-id n ignore-items] (recommend oracle user-id n ignore-items nil))
  ([oracle user-id n ignore-items candidate-items]
    (.to-map
        (.recommend oracle user-id n ignore-items candidate-items))))

;-- for Evaluation
(defn evaluate 
  ([oracle test-data] (.evaluate oracle (to-dataset oracle test-data)))
  ([oracle test-data training-data] 
    (.evaluate oracle (to-dataset oracle test-data) (to-dataset oracle training-data)))
  ([oracle test-data training-data & 
    {:keys [test-users, candidate-items, candidate-item-mode, repeated-events, n]
     :or {test-users nil, candidate-items nil, candidate-item-mode :overlap
          repeated-events :no, n -1}}]
   (let [test-data (to-dataset oracle test-data)
         training-data (to-dataset oracle training-data)]
      (.evaluate oracle test-data training-data test-users candidate-items
        candidate-item-mode repeated-events n)
    )))

(defmulti crossvalidate (fn [oracle & configs] (get-classname oracle)))
(defmethod crossvalidate "ItemRecommender" [oracle & 
          {:keys [num-folds test-users candidate-items candidate-item-mode compute-fit verbose]
           :or {num-folds 5, test-users [], candidate-items []
                candidate-item-mode :overlap, compute-fit false, verbose false}}] 
  (.crossvalidate oracle num-folds test-users candidate-items candidate-item-mode
                  compute-fit verbose))
(defmethod crossvalidate "RatingPredictor" [oracle & 
            {:keys [num-folds compute-fit verbose]
             :or {num-folds 5, compute-fit false, verbose false}}]
  (.crossvalidate oracle num-folds compute-fit verbose))

(defmethod crossvalidate :default [oracle & configs]
  (println "Error: given recommender dont support crossvalidation:" 
           "Given class: " (get-classname oracle)))

(defmulti evaluate-online (fn [oracle & configs] (get-classname oracle)))
(defmethod evaluate-online "RatingPredictor" [oracle & {:keys [test-data] 
                                                        :or {test-data nil}}]
  (when (not (nil? test-data))
    (.evaluate oracle (to-dataset oracle test-data))))

(defmethod evaluate-online "ItemRecommender" [oracle & 
          {:keys [test-data training-data test-users candidate-items candidate-item-mode]
           :or {test-data nil, training-data nil test-users nil candidate-items nil
                candidate-item-mode :overlap}}]
  (let [test-data (to-dataset oracle test-data)
        training-data (to-dataset oracle training-data)]
    
    (.evaluate-online oracle test-data training-data candidate-items candidate-item-mode)))


