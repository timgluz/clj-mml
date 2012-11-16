;------------------------------------------------------------------------------
;Predictors main namespace, which includes just recommender 
;specific methods and helpers
;
;------------------------------------------------------------------------------

(assembly-load-file "lib/mymedialite/MyMediaLite.dll")

(ns clj-mml.recommenders.core
  (:refer-clojure :exclude [test])
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

(defrecord MMLRecommender [model settings]
  RecommenderProtocol
  (can-predict? [this user-id item-id] 
    (.CanPredict (:model this) user-id item-id))
  (load-model [this filename] (.LoadModel (:model this)))
  (recommend [this user-id settings]
      (.Recommend (:model this) user-id -1 nil nil))
  (to-string [this] (.ToString (:model this)))
  (train [this] (.Train (:model this)))
  )

(defn get-class [model]
  "Return type of recommender"
  (let [class-name (str (class model))]
    (->> 
      (string/split class-name #"\.")
      (#(nth % 2)) ;classname is 3rd element
      (string/lower-case)
      (keyword)
    )))

(defn type? [model recommender-type]
  "Tests does given model is same type"
  (= (get-class model)
     (keyword recommender-type)))


(defn predictable? [model user-id item-id]
  "Checks whether a useful prediction can be made for a given user-item combination"
  (.CanPredict model user-id item-id))

; -- get/setters model attributes ----------------------------
(defn to-str [value]
  "transform value to string, if and handle keywords"
  (if (keyword? value)
    (name value)
    (str value)))

(defn build-setter-name [property]
  "Builds proper setter-name for C# objects"
  (symbol (format ".set_%s" (to-str property))))

(defn build-getter-name [property]
  "Builds proper getter-name for C# objects"
  (symbol (format ".%s" (to-str property))))

(defmacro set-property [model property value]
  (let [method-name (build-setter-name property)]
    `(~method-name ~model ~value)))

;(macroexpand-1 '(set-property "model" :MaxThreads 41))

(defmacro get-property [model property]
  `(~(build-getter-name property) ~model))

;(macroexpand-1 '(get-property "model" :MaxThreads))


; -- manage model -----------------
  
(defn clone [model]
  "Creates a shallow copy of the object"
  (.Clone model))

(defn save-params [model filename]
  "Saves the model parameters to a file"
  (.SaveModel model filename))

(defn load-params [model filename]
  "Get the model parameters from a file."
  (.LoadModel model filename))

