(ns clj-mml.io.write
  (:import [MyMediaLite.IO RatingData, TimedRatingData]))


(defn ratingdata [ratings target {:keys [users, items, format-str]
                                  :or [users nil, items nil, 
                                       format-str "{0}\t{1}\t{2}"]}]
  "Writes the predictions to a target file. If you want to use other separators
  then use parameter :format-str, example 
  (write/ratingdata ratings target :format-str {0},{1},{2})"
  (RatingData/WritePredictions ratings users items target format-str))

(defn timed-ratingdata [ratings target {:keys [users, items, format-str]
                                  :or [users nil, items nil, 
                                       format-str "{0}\t{1}\t{2}"]}]
  "Writes rating-data by preserving already existing timestamp data"
  (TimedRatingData/WritePredictions ratings users items target format-str))


