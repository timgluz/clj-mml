(ns clj-mml.recommenders.mostpopular
  (:import [MyMediaLite.ItemRecommendation MostPopular]
           [MyMediaLite.Eval Items]))

(defn init []
  (MostPopular. ))

(defn set-data [model data]
  "Sets predictor data"
  (set! (.Feedback model) data))

(defn get-data [model data]
  (.Feedback model))

(defn train [model training-data]
  "Returns trained model"
  (do
    (set-data model training-data)
    (.Train model)
    model
    ))

;; TODO: fix - it needs more argument
(defn test [model, test-data]
  (let [metrics (Items/Evaluate model test-data nil)]
    metrics))

(defn predict [model, user-id, item-id]
  "Makes a score prediction for a certain user and item"
  (.Predict model user-id item-id))
