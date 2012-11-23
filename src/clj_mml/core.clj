
(assembly-load-file "lib/mymedialite/MyMediaLite.dll")

(ns clj-mml.core
  (:require [clojure.string :as string]
            [clj-mml.examples.ratingprediction :as ex1]
            [clj-mml.examples.itemrecommendation :as ex2])
  (:gen-class))

(defn -main [& args]
  "Runs examples "
  (let [
        ;training-file (str (first args))
        training-file "data/u1.base"
        test-file "data/u1.test"]
    (do
      (println (str "Using training file: `" training-file "`" ))
      (ex1/run-example training-file test-file)
      (ex2/run-example training-file test-file)
      )))

(defn demo-run []
    (-main "data/u1.base" "data/u1.test"))

