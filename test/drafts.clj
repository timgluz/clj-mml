;example usage on REPL
(require  '[clj-mml.recommenders.ratingprediction :as ratingprediction] 
          '[clj-mml.io.read :as read])

(def training-dt (read/ratingdata "data/u1.base"))
(def params {:model :UserItemBaseline})

(def oracle (ratingprediction/init params))

(read/size training-dt)
(.to-string oracle)
;;can we can predict without data?
(.can-predict? oracle 1 1)
;; initialize rating data
(.set-data oracle training-dt)
(.get-data oracle)
(.can-predict? oracle 1 1)

(def oracle (ratingprediction/init (assoc params :training-data training-dt)))

(.train oracle)
(def results (.recommend oracle 1 5))
(.size results) 
(.to-map results) ;transform Tuple<int, int> to map<item-id score>

(reduce (fn [coll, row] (merge coll {(first row) (rest row)}))
        {}, [[:a 1][:b 2]])

;;or use original method names
(.set-ratings oracle training-dt)
(= (read/size (.get-ratings oracle))
   (read/size training-dt))

;; or access methods & values via raw CLR interop:
(.ToString (:model oracle))
(.get_MaxRating (:model oracle))
(.set_MaxRating (:model oracle) 10)
(.get_MaxRating (:model oracle))

(defmacro new-generic-list [datatype]
  (let [constructor (format "|System.Collections.Generic.List`1[%s]|" 
                              (str datatype))]
    `(new ~(symbol constructor))))
(macroexpand-1 '(new-generic-list System.Int32))

(def props (-> (:model oracle) (.GetType)(.GetProperties)))
(map (fn [prop] (-> (.Name prop) println)) props)

(def prop (-> (:model oracle) (.GetType) (#(.GetProperty %1 "MaxRating"))))


