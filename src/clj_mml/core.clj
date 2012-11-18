
(assembly-load-file "lib/mymedialite/MyMediaLite.dll")

(ns clj-mml.core
  (:require [clojure.string :as string]
            [clj-mml.examples.ratingprediction :as ex])
  (:gen-class))

(defn -main [& args]
  "Runs examples "
  (let [training-file (str (first args))
        test-file "data/u1.test"]
    (do
      (println (str "Using training file: `" training-file "`" ))
      (ex/example training-file test-file)
      ;(println (str "Recommendating item.1  for user.1"
      ;              (example2/run training-file test-file 1 1)))
      )))

(defn demo-run []
    (-main "data/u1.base" "data/u1.test"))

