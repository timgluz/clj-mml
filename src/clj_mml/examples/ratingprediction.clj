(ns clj-mml.examples.ratingprediction
  (:require [clj-mml.recommenders.ratingprediction :as ratingprediction]
            [clj-mml.io.read :as read]))

(defn- demo [oracle training-data test-data]
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

(defn run-example [training-file test-file]
  "Rating prediction from main documentation"
  (let [training-data (read/ratingdata training-file)
        test-data (read/ratingdata test-file)
        oracle (ratingprediction/init :UserItemBaseline :training-data training-data)
        delphi (ratingprediction/init :BiasedMatrixFactorization
                                      :training-data training-data)]
      (demo oracle training-data test-data)
      (demo delphi training-data test-data)
    ))

(defn demo-usage []
  (run-example "data/u1.base" "data/u1.test"))
