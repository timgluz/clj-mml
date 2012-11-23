(ns clj-mml.examples.itemrecommendation
  (:require [clj-mml.recommenders.itemrecommendation :as itemrecommendation]
            [clj-mml.io.read :as read]))

(defn- demo [oracle training-data test-data]
  "Simple example to demo usage of itemrecommendation"
  (do 
    (println "Using model: " (.to-string oracle))
    (println "Is model trained: " (nil? (.train oracle)))
    (println "Can we calculate recommendation between user.1 and item.1?"
             (.can-predict? oracle 1 1))
    (println "Score between user.1 and item.1:" (.predict oracle 1 1))
    (println "Recommend top7 product to user.1: ")
    (println (-> (.recommend oracle 1 7) (.to-map)))
    (println "Evaluation metrics of current model:")
    (println (.evaluate oracle test-data training-data))
    ))


(defn run-example [training-file test-file]
  (let [training-data (read/itemdata training-file)
        test-data (read/itemdata test-file)
        oracle (itemrecommendation/init :MostPopular 
                                        :training-data training-data)]
    (demo oracle training-data test-data)
    ))
