(ns clj-mml.core-test
  (:use clojure.test
        clj-mml.core
        clj-mml.read
        clj-mml.recommender))

(def test-file "data/u1.base")

(deftest data-reading
  (testing "reading existing ratingdata"
    (let [dt (ratingdata test-file)]
      (is (size dt) 80000)))

  (testing "reading existing itemdata"
      (let [dt (itemdata test-file)]
        (is (size dt) 80000))))

(deftest model-init
  (testing "initialise rating predictor"
    (let [oracle (init :useritembaseline)]
      (is (type? oracle :useritembaseline))
      ))
         
  (testing "initialise itempredictor"
    (let [oracle (init :mostpopular)]
      (is (type? oracle :mostpopular))
      )))

(deftest model-setdata
  (testing "set data of rating predictor"
    (let [oracle (init :useritembaseline)
          dt (ratingdata test-file)]
      (do
        (set-data oracle dt)
        (is (= 
              (size (get-data oracle)) 
              80000))
        )))
         
  (testing "set data of item predictor"
    (let [oracle (init :mostpopular)
          dt (itemdata test-file)]
      (do 
        (set-data oracle dt)
        (is (= 
              (size (get-data oracle)) 
              80000))
      ))))

(deftest main-tests 
  (data-reading)
  (model-init)
  (model-setdata))

(defn test-ns-hook []
  (main-tests))
