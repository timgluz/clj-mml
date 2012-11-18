(ns clj-mml.test-ratingprediction
  (:use [clojure.test])
  (:require [clj-mml.recommenders.ratingprediction :as ratingprediction]
            [clj-mml.io.read :as read]))

(def not-nil? (comp not nil?))
(def training-data (read/ratingdata "data/u1.base"))
(def test-data (read/ratingdata "data/u1.test"))

(deftest recommender-init
  (testing "creating new UserItemBaseline without data and settings"
      (let [configs {:model :UserItemBaseline}
            oracle (ratingprediction/init configs)]
        (is (not-nil?
              (re-matches #"UserItemBaseline.*" (.to-string oracle))))
        (is (false? (.can-predict? oracle 1 1)))
        (is (= 80000
               (read/size (.set-data oracle training-data))))
        
        (is (not-nil? (.train oracle)))
        (is (true? (.can-predict? oracle 1 1)))
        ))
  (testing "initialize new recommender with training data"
      (let [configs {:model :UserItemBaseline, :training-data training-data}
            oracle (ratingprediction/init configs)]
        (is (= 80000
               (read/size (.get-data oracle))))
        (is (true? (.can-predict? oracle 1 1)))
        )))


(deftest recommender-recommend
  (let [configs {:model :UserItemBaseline, :training-data training-data}
        oracle (ratingprediction/init configs)]
    (.train oracle)
    (testing "recommending with limited results"
      (is (= 5
             (.size (.recommend oracle 1 5)))))
    ;(testing "recommending with ignored_items")
    ;(testing "recommending with candidate_items")
    ;(testing "recommending with all parameters are set")
  ))
