(ns clj-mml.test-ratingprediction
  (:use [clojure.test])
  (:require [clj-mml.recommenders.ratingprediction :as ratingprediction]))

(deftest recommender-init
  (testing "creating new UserItemBaseline without data and settings"
      (let [configs {:model :UserItemBaseline}
            oracle (ratingprediction/init configs)]
        (is (= 
              (.to-string oracle) "UserBaseItemline"))
        )))
