(ns clj-mml.examples.sugarexample
  (:require [clj-mml.recommenders.itemrecommendation :as itemrecommendation]
            [clj-mml.recommenders.ratingprediction :as ratingprediction])
  (:use [clj-mml.recommenders.recommender]))

(def training-file "data/u1.base")
(def test-file "data/u1.test")

(def oracle (itemrecommendation/init :MostPopular))
(def delphi (ratingprediction/init :UserAverage))

;;train recommenders
(train oracle training-file)
(train delphi [[1 1 4.0][1 2 5.0][1 3 4.0]])

(can-predict? delphi 1 1)
(predict delphi 1 1)
(recommend oracle 1 -1 [100 101] [100 101 102 103 104 105])

(evaluate oracle test-file training-file)
(evaluate delphi test-file training-file)

