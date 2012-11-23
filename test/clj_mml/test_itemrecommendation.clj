(ns clj-mml.test-itemrecommendation
  (use [clojure.test]
       [clj-mml.recommenders.core])
  (require [clj-mml.recommenders.itemrecommendation :as itemrecommendation]
           [clj-mml.io.read :as read]))

(def training-data (read/itemdata "data/u1.base"))
(def test-data (read/itemdata "data/u1.base"))

(defn match-model-by-name [model-type]
  (let [name-pattern (re-pattern (format "%s.*" (name model-type)))]
  (->>
    (itemrecommendation/init model-type)
    (.to-string)
    (re-matches name-pattern))
  ))

(deftest recommender-init 
  (testing "creating new models by models name"
    (is (not-nil? (match-model-by-name :MostPopular)))
    (is (not-nil? (match-model-by-name :BPRMF)))
    (is (not-nil? (match-model-by-name :Zero)))
    (is (not-nil? (match-model-by-name :SoftMarginRankingMF))))

  (testing "creating new model by model name and initial training-data"
    (let [delphi (itemrecommendation/init :Zero :training-data training-data)]
      (is (not-nil? (:model delphi)))
      (is (= (read/size training-data)
             (read/size (.get-data delphi))))
      ))
  )

(deftest recommender-properties
  (let [oracle (itemrecommendation/init :BPRLinear :training-data training-data)]
    (testing "model getters"
        (is (= 0.0 (.getp oracle :InitMean)))
        (is (= (convert-type 0.05) 
               (.getp oracle :LearnRate)))
        (is (= (read/size training-data)
               (read/size (.getp oracle :Feedback)))))
    (testing "model setters"
        (is (= 10 (.setp oracle :NumIter 10)))
        (is (== (convert-type 0.1) 
               (.setp oracle :LearnRate 0.1))))
    ))

(deftest recommender-recommend
  (testing "can we predict without data"
    (let [oracle (itemrecommendation/init :MostPopular)]
      (is (false? (.can-predict? oracle 1 1)))))
  (testing "can we predict with data, which we gave during initialization"
    (let [oracle (itemrecommendation/init :BPRLinear :training-data training-data)]
      (is (true? (.can-predict? oracle 1 1)))))
  (let [oracle (itemrecommendation/init :MostPopular :training-data test-data)
        trained? (nil? (.train oracle))]
    (testing "do we have trained model"
      (is (not-nil? trained?)))
    (testing "recommending specific item.1 to user.1"
      (is (< 0.004 (double (.predict oracle 1 1)) 0.005)))
    (testing "recommend only n items to user.1"
      (is (= 11
             (.size (.recommend oracle 1 11)))))
    (testing "recommend 11 items but ignore some specific items"
      (is (contains-not?
            (-> (.recommend oracle 1 11 [98])(.to-map)) 98)))
    (testing "recommend 3 items from given candidate items"
      (let [results (-> (.recommend oracle 1 -1 nil [288 98 258])(.to-map))]
        (is (= 3 (count results)))
        (is (every? #(contains? results %1) [288 98 258]))))
    (testing "recommend 3 items from given candidate items, but ignore some of them"
      (let [banned_items [98 258 288]
            candidate_items [50 100 286 294]
            results (-> (.recommend oracle 1 -1 banned_items candidate_items)(.to-map))]
        (is (= (count candidate_items) 
               (count results)))
        (is (every? #(contains-not? results %1) banned_items))
        (is (every? #(contains? results %1) candidate_items))
        ))
  ))

(deftest recommender-evaluate
  (let [oracle (itemrecommendation/init :MostPopular :training-data training-data)
        trained? (nil? (.train oracle))]
    (testing "was training successful"
      (is (not-nil? trained?)))
    (testing "evaluate trained"
      (is (< 0 (count (.evaluate oracle test-data training-data)))))
    (testing "evaluate trained model by using specific users"
      (is (< 0 (count
                 (.evaluate oracle test-data training-data [1, 2 ,3])))))
    (testing "evaluate trained model by using specific items"
      (is (< 0 (count
                 (.evaluate oracle test-data training-data nil [288, 98 258])))))
    (testing "trained model evaluation by changing candidate item mode"
      (is (< 0 (count
                 (.evaluate oracle test-data training-data nil nil :test)))))
    (testing "trained model evaluation by changing repeated events value"
      (is (< 0 (count
                 (.evaluate oracle test-data training-data nil nil :overlap :yes -1)))))
    ))
