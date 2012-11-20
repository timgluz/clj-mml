(ns clj-mml.examples.ratingprediction
  (:require [clj-mml.recommenders.ratingprediction :as ratingprediction]
            [clj-mml.io.read :as read]))

(defn- demo [oracle test-data]
  "Demo simple usage of recommender via ClojureCLR"
   (do
      (println "Example for:\n" (.to-string oracle)) 
      (.train oracle) ;;grow some muscles
      (println "Can we do prediction between user.1 and item.1?" 
                (.can-predict? oracle 1 1))
      (println "Score by model:  " (.predict oracle 1 1))
      (println "Recommend 5 products to user.1: " 
               (.to-map (.recommend oracle 1 5)))
      (println "Evaluation metrics: " (.evaluate oracle test-data))
      (println "10-Fold Crossvalidation metrics:" (.crossvalidate oracle 10)) 
    ))

(defn example [training-file test-file]
  "Rating prediction from main documentation"
  (let [training-data (read/ratingdata training-file)
        test-data (read/ratingdata test-file)
        params {:model :UserItemBaseline, :training-data training-data}
        oracle (ratingprediction/init params)
        delphi (ratingprediction/init (assoc params :model :BiasedMatrixFactorization))]
      (demo oracle test-data)
      (demo delphi test-data)
    ))

(defn demo-usage []
  (example "data/u1.base" "data/u1.test"))
