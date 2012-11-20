;------------------------------------------------------------------------------
;Predictors main namespace, which includes just recommender 
;specific methods and helpers
;
;------------------------------------------------------------------------------

(assembly-load-file "lib/mymedialite/MyMediaLite.dll")

(ns clj-mml.recommenders.core
  (:require [clojure.string :as string]))

(defprotocol RecommenderProtocol
  "Generic protocol for simple recommenders"
  (can-predict? [this user-id item-id] "Checks whether a usefule prediction can be 
                                       made for a given user-item combination")
  (load-model [this filename] "Get the model parameters from file")
  (predict [this user-id item-id] "Predict rating or score for a given user-item 
                              combination")
  (recommend [this user-id] [this user-id n] [this user-id n ignored_items]
             [this user-id n ignored_items candidate_items]
             "Recommends items for a given user.")
  (save-model [this filename] "Save the model parameters to a file.")
  (to-string [this] "returns string representation of the recommender")
  (train [this] "Learn the model parameters of the recommender from the training data."))

(defprotocol RatingPredictorProtocol
  "Generic protocol for simple predictors"
  (get-ratings [this] "returns ratings, which were used for last training")
  (set-ratings [this ratings] "sets rating-data for given predictor")
  (get-max-rating [this] "gets maximum rating of predictor")
  (set-max-rating [this value] "sets maximum rating of predictor")
  (get-data [this] "Wrapper function to unify data reading")
  (set-data [this ratings] "Wrapper function to unify data setting") 
)

(defprotocol ResultProtocol 
  "Protocol to handle results of recommendations"
  (size [this] "returns count of results")
  (nth-result [this row-nr] "Returns nth row, which are transformed to list")
  (to-vect [this] "Turns results to native Clojure vector")
  (to-map [this] "Turns list of results to native Clojure map")
  )

(defprotocol EvaluateProtocol
  "Protocol that describe evaluation interface"
  (measures [this] "Returns list of available evaluation measures")
  (evaluate [this test-data] [this test-data training-dat] 
            "Evaluates a predictor and returns map of metrics")
  (crossvalidate [this] [this num-folds] [this num-folds compute-fit]
                 [this num-folds compute-fit verbose] 
                 "Evaluates on the folds of a dataset splits")
  (evaluate-online [this data] "Online evaluation for recommender"))


;; macros  -----------------------------
(defn build-setter-name [property]
  "Builds proper setter-name for C# objects"
  (symbol (format ".set_%s" (name property))))
(defmacro set-property [model property value]
  (let [method-name (build-setter-name property)]
    `(~method-name ~model ~value)))

;(macroexpand-1 '(set-property "model" :MaxThreads 41))

(defn build-getter-name [property]
  "Builds proper getter-name for C# objects"
  (symbol (format ".get_%s" (name property))))

(defmacro get-property [model property]
  `(~(build-getter-name property) ~model))

;(macroexpand-1 '(get-property "model" :MaxThreads))
(defprotocol ModelPropertyProtocol 
  "Protocol that handles get/setters of given model"
  (properties [this] "Returns set of possible properties")
  (getp [this property] "Access to models properties")
  (setp [this property value] "Set value of public property.")
  ;(- [this property] [this property value] "get/setters shorthand notation")
  )

(defn new-generic-list []
  (let [lst (|System.Collections.Generic.List`1[System.Int32]|.)]
    lst))

(defmulti list->generic (fn [x] (seq? (seq x))))
(defmethod list->generic false [lst] lst)
(defmethod list->generic true  [lst]
  "Transforms Clojure list to Generic.IList<System.Int32>"
  (let [generic-coll (new-generic-list)]
    (dorun 
      (map (fn [item] (.Add generic-coll item)) lst))
    generic-coll
    ))

