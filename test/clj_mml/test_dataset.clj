;-------------------------------------------------------------------------------
;Tests for dataset namespace
;
;-------------------------------------------------------------------------------

(ns clj-mml.test-dataset
  (:use [clojure.test])
  (:require [clj-mml.dataset :as dataset]))

(def not-nil? (comp not nil?))

(deftest dataset-items 
  (testing "initialization of new items dataset"
    (is (not-nil? (dataset/new-items))))
  (testing "adding new items to dataset"
    (let [dt-set (dataset/new-items)]
      (dataset/add-item! dt-set 1 1)
      (is (= 1 (.Count dt-set)))
      (dataset/add-item! dt-set 1 2)
      (is (= 2 (.Count dt-set)))
      ))
  (testing "building new dataset of items from Clojure data-collection"
    (let [dt-set (dataset/build-items [[1 1] [1 2] [2 3]])]
      (is (= 3 (.Count dt-set)))
      )))

(deftest dataset-ratings
  (testing "initialization of new ratings dataset"
    (is (not-nil? (dataset/new-ratings))))
  (testing "adding new ratings to dataset"
    (let [dt-set (dataset/new-ratings)]
      (dataset/add-rating! dt-set 1 1 3.0)
      (is (= 1 (.Count dt-set)))
      (dataset/add-rating! dt-set 1 3 4.0)
      (is (= 2 (.Count dt-set)))
      ))
  (testing "building new dataset from Clojure data-collection"
    (let [dt-set (dataset/build-ratings [[2 1 1.0] [2 3 4.0] [1 2 0]])]
      (is (= 3 (.Count dt-set))))))
