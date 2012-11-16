;------------------------------------------------------------------------------
;ItemAverage ratingpredictor
;
;Uses the average rating value of an item for prediction.
;------------------------------------------------------------------------------

(ns clj-mml.recommenders.itemaverage
  (:use [clj-mml.recommenders.core])
  (:import [MyMediaLite.RatingPrediction ItemAverage]
           [clj_mml.recommenders.core MMLRecommender]))

;;TODO: refactor it core-function inner-init, with params model&settings 
(defn init []
  (let [model (ItemAverage.)]
    (MMLRecommender. model nil)))

