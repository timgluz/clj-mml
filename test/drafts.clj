;example usage on REPL
(require  '[clj-mml.recommenders.ratingprediction :as ratingprediction] 
          '[clj-mml.io.read :as read])

(def training-dt (read/ratingdata "data/u1.base"))
(def test-dt (read/ratingdata "data/u1.test"))

(def oracle (ratingprediction/init :UserAverage))

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

(def d (->> 
  (.properties oracle)
  (map (fn [prop] 
         (try
           {prop (.getp oracle prop)}
           (catch Exception e 
              {prop :requires-argument})
           )))
  doall
  ))

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

(import '[MyMediaLite.Eval Ratings])

(def values (->>
            (Ratings/Evaluate (:model oracle) test-dt nil)
            (map (fn [row] {(keyword (.Key row)) (.Value row)}))
            (apply merge))) 

;; using itemrecommendation

(require '[clj-mml.recommenders.itemrecommendation :as itemrecommendation]
         '[clj-mml.io.read :as read])

(def training-data (read/itemdata "data/u1.base"))
(def test-data (read/itemdata "data/u1.test"))

(def delphi (itemrecommendation/init :MostPopular))

(defprotocol P1 (p [this] ""))
(defprotocol P2 (t [this] ""))
(deftype T []
  P1 
  (p [this] (println "from p.")))

(extend T 
  P2 {:t (fn [this] (println "from t."))})

(def p (T.))

(def oracle (itemrecommendation/init :BPRLinear 
                                     :training-data training-data
                                     :LearnRate 0.03 :RegU 0.9))

(->> 
  (itemrecommendation/init :MostPopular)
  (.to-string)
  (re-matches #"MostPopular.*"))


