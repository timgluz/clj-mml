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
  (recommend [this user-id settings] "Recommends items for a given user.
                                     If you want to use default settings, 
                                     then use nil, else append map with keys, 
                                     like :n, :ignore-items, :candidate-items")
  (save-model [this filename] "Save the model parameters to a file.")
  (to-string [this] "returns string representation of the recommender")
  (train [this] "Learn the model parameters of the recommender from the training data.")
  )
(defprotocol RatingPredictorProtocol
  "Generic protocol for simple predictors"
  (get-ratings [this] "returns ratings, which were used for last training")
  (set-ratings [this ratings] "sets rating-data for given predictor")
  (get-max-rating [this] "gets maximum rating of predictor")
  (set-max-rating [this value] "sets maximum rating of predictor")
  (get-data [this] "Wrapper function to unify data reading")
  (set-data [this ratings] "Wrapper function to unify data setting") 
)
;; macros  -----------------------------
(defn build-setter-name [property]
  "Builds proper setter-name for C# objects"
  (symbol (format ".set_%s" (name property))))

(defn build-getter-name [property]
  "Builds proper getter-name for C# objects"
  (symbol (format ".%s" (name property))))

(defmacro set-property [model property value]
  (let [method-name (build-setter-name property)]
    `(~method-name ~model ~value)))

;(macroexpand-1 '(set-property "model" :MaxThreads 41))

(defmacro get-property [model property]
  `(~(build-getter-name property) ~model))

;(macroexpand-1 '(get-property "model" :MaxThreads))

