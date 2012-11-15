(ns clj-mml.test-biasedmatrixfactorization
  (:use [clojure.test]
        [clj-mml.core])
  (:require [clj-mml.io.read :as read]
            [clj-mml.recommenders.biasedmatrixfactorization :as recommender] )
  )

(def training-dt (read/ratingdata "data/u1.base"))
(def test-dt (read/ratingdata "data/u1.test"))

(deftest recommender-init
  (testing "testing recommender based on biasedmatrixfactorization"
    (let [oracle (recommender/init)]
      (is (type? oracle :biasedmatrixfactorization))))
  (testing "init new recommender with data"
    (let [oracle (recommender/init training-dt)]
      (is (= (recommender/has-data? oracle) true))))

  ;(testing "init new recommender with settings")       
  )

(deftest recommender-predictability
  (let [oracle (recommender/init)]
    (testing "predicting with uninitialized model should return false"
      (is (= 
            (recommender/predictable? oracle 1 1)
            false)))
    (testing "predict with initialized model"
      (do 
        (recommender/set-data oracle training-dt)
        (is (= 
              (recommender/predictable? oracle 1 1)
              true))))
    ))

(deftest recommender-train
  (testing "train model without data"
    (let [oracle (recommender/init)]
      (is (nil? (recommender/train oracle)))
      ))
  (testing "train model with data"
    (let [oracle (recommender/init training-dt)]
      (is (type? (recommender/train oracle)
                 :biasedmatrixfactorization))))
  )


(deftest recommender-properties
  (let [oracle (recommender/train (recommender/init training-dt))]
    (testing "get model properties"
      (is (= 1
            (recommender/get-property oracle :MaxThreads))))
    (testing "set model properties"
      (is (= 2 
             (do 
               (recommender/set-property oracle :MaxThreads 2)
               (recommender/get-property oracle :MaxThreads)))))
    ))
