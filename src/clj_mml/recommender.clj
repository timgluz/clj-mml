(ns clj-mml.recommender
  (:require [clojure.string :as string])
  (:import [MyMediaLite.RatingPrediction UserItemBaseline]
           [MyMediaLite.ItemRecommendation MostPopular]))

(defn init [predictor]
  "Initializes predictor"
  (cond 
    (= predictor :useritembaseline) (UserItemBaseline.)
    (= predictor :mostpopular) (MostPopular.)
    ))

(defn-  predictable? [model])

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
  "returns model of predictor"
  (= (get-class model)
     (keyword recommender-type)))

(defn set-data [model data]
  "Sets rating predictor"
  (cond 
    (= (type? model :useritembaseline) true) (set! (.Ratings model) data)
    (= (type? model :mostpopular) true) (set! (.Feedback model) data)
    ))

(defn get-data [model]
  "Returns dataset of given predictor"
  (cond
    (type? model :useritembaseline) (.Ratings model)
    (type? model :mostpopular) (.Feedback model)
    ))


(defn train [model data]
  "Runs training cycles on given recommender"
  (do
    (set-data model data)
    (.Train model)
    model
    ))

(defn predict [model user-id item-id]
  "Does score prediction to specific item and user"
  (.Predict model user-id item-id))


