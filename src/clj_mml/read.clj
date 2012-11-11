;(assembly-load-file "/lib/mymedialite/MyMediaLite.dll")

(ns clj-mml.read 
  (:import [MyMediaLite.IO RatingData, ItemData]))

(defn ratingdata [filename & {:keys [header] :or {header false}}]
  "Read ratings data. "
  (RatingData/Read filename nil nil header))


(defn itemdata [filename & {:keys [header] :or {header false}}]
  "Read implicit feedback data for ItemRecommender"
  (ItemData/Read filename nil nil header))

;; -- Helper functions -------------------
(defn size [dt]
  "Returns number of interaction events in the dataset"
  (.Count dt))


