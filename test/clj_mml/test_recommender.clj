;--------------------------------------------------------------------------------
; Recommender 
;
; test-cases for recommender namespace
;--------------------------------------------------------------------------------

(ns clj-mml.test-recommender
  (:use [clojure.test]
        [clj-mml.recommenders.core])
  (:require [clj-mml.recommenders.recommender :as recommender]
            [clj-mml.recommenders.ratingprediction :as ratingprediction]
            [clj-mml.recommenders.itemrecommendation :as itemrecommendation]))

(def training-file "data/u1.base")
(def test-file "data/u1.test")
(def delphi (itemrecommendation/init :MostPopular))
(def oracle (ratingprediction/init :UserAverage))

(deftest recommender-init 
  (testing "reading files for different recommenders"
    (is (true? (recommender/matches-classname? #"PosOnlyFeedback.*" 
                 (recommender/read-file delphi training-file))))      
    (is (true? (recommender/matches-classname? #"Rating.*"
                 (recommender/read-file oracle training-file)))))
         
  (testing "creating specific datasets for recommenders"
    (let [dt-set (recommender/build-dataset delphi [[1 1 0.0][1 2 1.0]])
          dt-set2 (recommender/build-dataset oracle [[1 1 0.0][1 2 2.0]])]
      (is (true? (recommender/matches-classname? #"PosOnlyFeedback.*" dt-set)))
      (is (= 2 (.Count dt-set)))
      (is (true? (recommender/matches-classname? #"Rating.*" dt-set2)))
      (is (= 2 (.Count dt-set2)))
    ))
  (testing "adding new dataset from file and adding it to ItemRecommender"
    (let [oracle (itemrecommendation/init :MostPopular)]
      (is (nil? (recommender/get-data oracle)))
      (recommender/set-data oracle training-file)
      (is (true? (recommender/matches-classname? #"PosOnlyFeedback.*"
                  (recommender/get-data oracle))))
      ))
 
  (testing "adding new dataset from file and adding it to RatingPredictor"
    (let [oracle (ratingprediction/init :UserAverage)]
      (is (nil? (recommender/get-data oracle)))
      (recommender/set-data oracle training-file)
      (is (true? (recommender/matches-classname? #"Rating.*"
                  (recommender/get-data oracle))))
      )))

(deftest recommender-usage
  (testing "set training data and test recommender predictability"
    (let [oracle (itemrecommendation/init :MostPopular)]
      (is (false? (recommender/can-predict? oracle 1 1)))
      (recommender/set-data oracle training-file)
      (recommender/train oracle)
      (is (true? (recommender/can-predict? oracle 1 1)))
      ))
  (testing "set training data for Recommender by run"
    (let [oracle (itemrecommendation/init :MostPopular)]
      (is (false? (recommender/can-predict? oracle 1 1)))
      (recommender/train oracle training-file)
      (is (true? (recommender/can-predict? oracle 1 1)))  
    ))

  (testing "predict rating score with ItemRecommender:"
    (let [oracle (itemrecommendation/init :MostPopular)
          trained? (recommender/train oracle training-file)]
      (is (< 0.003 (recommender/predict oracle 1 1) 0.01))))
  
  (testing "predict score with RatingPrediction:"
    (let [oracle (ratingprediction/init :UserAverage)]
      (recommender/train oracle training-file)
      (is (< 3.0 (recommender/predict oracle 1 1) 5.0))
      ))
  (testing "recommending N items to User.1 with RatingPredictor"
    (let [oracle (ratingprediction/init :UserAverage)
          trained? (recommender/train oracle training-file)
          recommendations (recommender/recommend oracle 1 11)]
      (is (= (count recommendations) 11))
      (is (contains? recommendations 1))
      )) 
  (testing "recommending N items to User.1 with ItemRecommender"
    (let [oracle (itemrecommendation/init :MostPopular)
          trained? (recommender/train oracle training-file)
          recommendations (recommender/recommend oracle 1 -1 [100 101] [100 101 102 103 104])]
      (is (= (count recommendations) 3))
      (is (false? (contains? recommendations 101)))
      (is (contains? recommendations 103))
      ))
)

(deftest recommender-evaluation
  (testing "evaluating ItemRecommender just giving path to  test-files"
    (let [oracle (itemrecommendation/init :MostPopular)
          trained? (recommender/train oracle training-file)]
      (is (map? (recommender/evaluate oracle test-file training-file)))
    ))
  (testing "evaluating RatingPredictor just giving path to test-file"
    (let [oracle (ratingprediction/init :UserAverage)
          trained? (recommender/train oracle training-file)]
      (is (map? (recommender/evaluate oracle test-file)))
      ))
  )
