(ns clj-mml.recommenders.useritembaseline
  (:import [MyMediaLite.RatingPrediction UserItemBaseline]
           [MyMediaLite.Eval Ratings]))


(defn init []
  "Returns initialized new model."
  (UserItemBaseline.))

(defn set-data [model, data]
  ""
  (do
    (set! (.Ratings model) data)
    model))

(defn get-data [model]
  (.Ratings model))

(defn train [model, dt]
  "Trains mdoel with given data"
  (do
    (set-data model dt)
    (.Train model)
    model
    ))

;; TODO: refactor it to eval namespace 
(defn test [model, test-dt]
  "Returns evaluation map with RMSE, MAE, CBD values."
  (let [metrics (Ratings/Evaluate model test-dt nil)]
    (hash-map 
      :RMSE (get metrics "RMSE")
      :MAE (get metrics "MAE")
      :CBD (get metrics "CBD")
      )))

(defn predict [model user-id item-id]
  "Makes prediction for a certain user and item"
  (.Predict model user-id item-id))
