(ns clj-mml.test-ratingprediction
  (:use [clojure.test])
  (:require [clj-mml.recommenders.ratingprediction :as ratingprediction]
            [clj-mml.io.read :as read]))

(def not-nil? (comp not nil?))
(def contains-not? (comp not contains?))
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
    (testing "recommending with ignored_items"
      (is (contains-not? 
            (.to-map (.recommend oracle 1 5 [169])) 
            169
          )))
    (testing "recommending with candidate_items"
      (is (contains? 
            (.to-map (.recommend oracle 1 5 nil [64, 169, 318]))
            169
          )))
    (testing "recommending with all parameters are set"
      (is (-> 
            (.recommend oracle 1 5 [169][345 567 169 318 483 64])
            (.to-map)
            (#(contains-not? %1 169))
          )))
  ))

(deftest recommender-properties
  (let [configs {:model :UserItemBaseline}
        oracle (ratingprediction/init configs)]
    (testing "access to propertynames"
      (is (every? (.properties oracle) [:MaxRating :MinRating :NumIter])))
    (testing "accessing to read property name"
      (is (= 10 (.getp oracle :NumIter))))
    (testing "Setting property value"
      (is (= 10.0 (.setp oracle :MaxRating 10))))
    ))

(deftest recommender-evaluation
  (let [configs {:model :UserItemBaseline, :training-data training-data}
        oracle (ratingprediction/init configs)]
    (.train oracle)
    (testing "get possible evaluation metrics for given recommender"
      (is (contains? (.measures oracle) :RMSE)))
    (testing "calculating values for all evaluation metrics by using test-data"
       (is (every? (.evaluate oracle test-data)
                   (.measures oracle))))
    (testing "calculating values for eval-metrics for given test and training-data"
        (is (every? (.evaluate oracle test-data training-data)
                    (.measures oracle))))
    ))
