(ns clj-mml.examples.ex1-ratingprediction
  (:use [clj-mml.recommenders.useritembaseline])
  (:require [clj-mml.io.read :as read]))


(defn run [training-file, test-file, user-id, item-id]
  "Basic usage of useritembaseline to predict score to 
  specific user and product-id"
  (let [oracle (init)
        training-data (read/ratingdata training-file)
        test-data (read/ratingdata test-file)]
    (do
      (train oracle training-data)
      (println (str "Evaluation results: " (test oracle test-data)))
      (predict oracle user-id item-id)
      )))

(defn demo-run []
  "Runs hard-coded example"
  (run "data/u1.base", "data/u1.test", 1 1))
