
(assembly-load-file "lib/mymedialite/MyMediaLite.dll")

(ns clj-mml.core
  (:require [clj-mml.read :as read]
            [clj-mml.recommender :as recommender])
  (:gen-class)
  )

(defn example-rating-prediction [training-file]
  "Rating prediction"
  (let [oracle (recommender/init :useritembaseline)
        dt (read/ratingdata training-file)]
    (do 
      (recommender/train oracle dt)
      ;(recommender/evalute oracle test-dt)
      (recommender/predict oracle 1 1)
      )))


(defn example-item-prediction [training-file]
  "Item prediction from positive-only feedback"
  (let [oracle (recommender/init :mostpopular)
        dt (read/itemdata training-file)]
    (do 
      (recommender/train oracle dt)
      ;(recommender/evaluate oracle test-dt)
      (recommender/predict oracle 1 1)
      )))

(defn -main [& args]
  (let [training-file (str (first args))]
    (do
      (println (str "Using training file: `" training-file "`" ))
      (println (str "User #1 will rate product nr.1 " 
                    (example-rating-prediction training-file)))
      (println (str "Recommendating item.1  for user.1"  
                    (example-item-prediction training-file)))
      )))

(defn demo-run [] 
    (-main "data/u1.base" "data/u1.test"))

