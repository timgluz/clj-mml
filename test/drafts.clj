
(require  '[clj-mml.recommenders.ratingprediction :as ratingprediction] 
          '[clj-mml.io.read :as read])

(def training-dt (read/ratingdata "data/u1.base"))

(def oracle (ratingprediction/init :UserItemBaseline))

(.to-string oracle)
(.can-predict? oracle 1 1)

(.can-predict? oracle2 1 1)
