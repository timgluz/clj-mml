(ns clj-mml.test-ratingprediction
  (:use [clojure.test]
        [clj-mml.recommenders.core])
  (:require [clj-mml.recommenders.ratingprediction :as ratingprediction]
            [clj-mml.io.read :as read]))

(def training-data (read/ratingdata "data/u1.base"))
(def test-data (read/ratingdata "data/u1.test"))

(deftest recommender-init
  (testing "creating new UserItemBaseline without data and settings"
      (let [oracle (ratingprediction/init :UserItemBaseline)]
        (is (not-nil?
              (re-matches #"UserItemBaseline.*" (.to-string oracle))))
        (is (false? (.can-predict? oracle 1 1)))
        (is (= 80000
               (read/size (.set-data oracle training-data))))
        
        (is (not-nil? (.train oracle)))
        (is (true? (.can-predict? oracle 1 1)))
        ))
  (testing "initialize new recommender with training data"
      (let [oracle (ratingprediction/init :UserItemBaseline 
                                          :training-data training-data)]
        (is (= 80000 (read/size (.get-data oracle))))
        (is (true? (.can-predict? oracle 1 1)))
        ))
  (testing "initializing new recommender with configuration"
    (let [oracle (ratingprediction/init :UserItemBaseline
                                        :Ratings training-data
                                        :MaxRating 12
                                        :RegU 0.07)]
      (is (= (read/size training-data)
             (read/size (.getp oracle :Ratings))))
      (is (= (float 12) (float (.getp oracle :MaxRating))))
      (is (= (float 0.07) (float (.getp oracle :RegU))))
     ))       
  )


(deftest recommender-recommend
  (let [oracle (ratingprediction/init :UserItemBaseline :training-data training-data)
        trained? (nil? (.train oracle))]
    (testing "success of training process"
      (is (not-nil? trained?)))
    (testing "recommending with limited results"
      (is (= 5 (.size (.recommend oracle 1 5)))))
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
  (let [oracle (ratingprediction/init :UserItemBaseline)]
    (testing "access to propertynames"
      (is (every? (.properties oracle) [:MaxRating :MinRating :NumIter])))
    (testing "accessing to read property name"
      (is (= 10 (.getp oracle :NumIter))))
    (testing "Setting property value"
      (is (= 10.0 (.setp oracle :MaxRating 10))))
    ))

(deftest recommender-evaluation
  (let [oracle (ratingprediction/init :UserItemBaseline :training-data training-data)
        trained? (.train oracle)]
    (testing "success of training process"
      (is (not-nil? trained?)))
    (testing "get possible evaluation metrics for given recommender"
      (is (contains? (.measures oracle) :RMSE)))
    (testing "calculating values for all evaluation metrics by using test-data"
       (is (every? (.evaluate oracle test-data)
                   (.measures oracle))))
    (testing "calculating values for eval-metrics for given test and training-data"
        (is (every? (.evaluate oracle test-data training-data)
                    (.measures oracle))))

    (testing "crossvalidation with all arguments"
        (is (every? (.crossvalidate oracle 10 false false)
                    (.measures oracle))))
    (testing "crossvalidation without specifying any argument"
        (is (every? (.crossvalidate oracle)
                    (.measures oracle))))
    (testing "online evaluation"
        (is (every? (.evaluate-online oracle test-data)
                    (.measures oracle))))
    ))
