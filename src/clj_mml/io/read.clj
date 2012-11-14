;(assembly-load-file "/lib/mymedialite/MyMediaLite.dll")

(ns clj-mml.io.read 
  (:import [MyMediaLite.IO RatingData, TimedRatingData,
              ItemData, ItemDataRatingThreshold]))

(defn ratingdata [filename & {:keys [header, timed, users, items] 
                              :or {header false, timed false, 
                                   users nil, items nil}}]
  "Reads rating data. 
  Rating data files have at least 3 columns: user-id, item-id, rating-value,
  date/time information or numerical timestamps will be used if necessary.
  NB! item recommendation tool also supports this rating format. 
  By default, every rating is interpreted as positive feedback, the 
  rating value will be ignored. 
  Use :timed true, if you want also read timestamp data"
  (if (= timed true)
    (TimedRatingData/Read filename users items header)
    (RatingData/Read filename users items header)))

(defn itemdata [filename & {:keys [header, users, items, threshold] 
                            :or {header false, users nil, items nil, threshold nil}}]
  "Read implicit feedback data. 
  Positive-only feedback files have at least 2 columns: the user-id, item-id.
  Additional columns are ignored. "
  (if (nil? threshold)
    (ItemData/Read filename users items  header)
    (ItemDataRatingThreshold/Read filename threshold users items header)
    ))

;; -- Helper functions -------------------
(defn size [dt]
  "Returns number of interaction events in the dataset"
  (.Count dt))


