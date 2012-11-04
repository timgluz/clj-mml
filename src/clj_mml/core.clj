(assembly-load-file "lib/mymedialite/MyMediaLite.dll")

(ns clj-mml.core
  (:import [MyMediaLite.IO RatingData])
  (:import [MyMediaLite.RatingPrediction UserItemBaseline])
  (:gen-class)
  )

(defn read-data [filename]
  "Reads training-data"
  (RatingData/Read filename nil nil false))

(defn init-recommender []
  "Initialize and returns recommender objecty"
  (UserItemBaseline. ))

(defn train-recommender [recommender training-data]
  "Runs training cycles on given recommender"
  (do 
    (set! (.Ratings recommender) training-data)
    (.Train recommender)
    recommender
    ))

(defn test-recommender [recommender test-data]
  "Evaluates trained recommender with given test data"
  (.Evaluate recommender test-data)
  )

(defn- predictable? [recommender  user-id item-id]
  (.CanPredict  recommender user-id item-id))

(defn predict-rating [recommender user-id item-id]
  (if (predictable? recommender user-id item-id)
    (.Predict recommender user-id item-id)
    -1.0 ;if model cant rpedict then return just -1.0, aka smt went very wrong 
    ))

(defn -main [& args]
  (let [
        default-training-file "data/u1.base"
        training-file (str (first args))
        item-id 1
        user-id 1
        oracle (init-recommender)]
    (do
      (println (str "Using training file: `" training-file "`" ))
      (train-recommender oracle (read-data training-file))
      ;(test-recommender oracle test-data)
      (println (str 
                 "User #" user-id
                 " likes product #" item-id
                 ": " (predict-rating oracle user-id item-id)))
      )))

(defn demo-run [] 
    (-main "data/u1.base" "data/u1.test"))

