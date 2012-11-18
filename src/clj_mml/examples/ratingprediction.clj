(ns clj-mml.examples.ratingprediction
  (:require [clj-mml.recommenders.ratingprediction :as ratingprediction]
            [clj-mml.io.read :as read]))

(defn example [training-file test-file]
  "Rating prediction from main documentation"
  (let [training-data (read/ratingdata training-file)
        test-data (read/ratingdata test-file)
        params {:model :UserItemBaseline, :training-data training-data}
        oracle (ratingprediction/init params)
        delphi (ratingprediction/init (assoc params :model :BiasedMatrixFactorization))]
    (.train oracle)
    (.train delphi)
    (println "Can we do prediction between user.1 and item.1?" 
             (.can-predict? oracle 1 1)
             " Score by  UserItemBaseline: " (.predict oracle 1 1) 
             " Score by BiasedMatrixFactorization: " (.predict delphi 1 1))
    ))

(defn demo-usage []
  (example "data/u1.base" "data/u1.test"))
