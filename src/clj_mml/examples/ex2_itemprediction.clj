(ns clj-mml.examples.ex2-itemprediction
  (:require [clj-mml.recommenders.mostpopular :as recommender]
            [clj-mml.read :as read]))

(defn run [training-file test-file user-id item-id]
  "Example: Item prediction from positive only feedback"
  (let [training-data (read/itemdata training-file)
        test-data (read/itemdata test-file)
        oracle (recommender/init)]
    (do
      (recommender/train oracle training-data)
      (println (str "Test metrics: " (recommender/test oracle test-data)))
      (recommender/predict oracle user-id item-id)
      )))


(defn demo-run []
  "Hard-coded example for quick demonstration"
  (println (str "User.1 will like item.1 :"
                (run "data/u1.base" "data/u1.test" 1 1))))
