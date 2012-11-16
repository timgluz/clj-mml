(require '[clj-mml.recommenders.itemaverage :as itemaverage] :reload)

(def oracle (itemaverage/init))
(can-predict? oracle 1 1)
