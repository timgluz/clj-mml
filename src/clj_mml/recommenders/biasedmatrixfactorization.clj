; -----------------------------------------------------------------------------
; BiasedMatrixFactorization
; Matrix factorization model for item prediction (ranking) optimized for BPR,
; which reduces ranking to pairwise classification. 
; ----------------------------------------------------------------------------

(ns clj-mml.recommenders.biasedmatrixfactorization
    (:use  [clj-mml.recommenders.core :refer :all])
    (:import [MyMediaLite.RatingPrediction BiasedMatrixFactorization])
  )

(in-ns 'clj-mml.recommender.core)

; -- HELPERS --------------------------
(defn set-data [model ratings]
  (set! (.Ratings model) ratings))

;TODO: test class of model, before accessing value
(defn get-data [model]
  (.Ratings model))

(defn has-data? [model]
  "Tests does model have data"
  (not 
    (nil? (get-data model)))) 

; -- CORE METHODS ---------------------

(defn init
  "Initializes BMF, you can add training-file as first argument"
  ([] (BiasedMatrixFactorization.))
  ([training-data] 
    (let [bmf (init)]
      (set-data bmf training-data)
      bmf
      )))

; name collision , can we refactor out core? 
;(defn iterate [model]
;  "Performs one iteration of stochastic gradient ascent over the training data.
;  One iteration is samples number of positive entries in the training matrix."
;  (.Iterate model))

(defn train
  "trains given model. After successfull training will return model else just nil"
  ([model] 
    (if (has-data? model)
      (do 
        (.Train model)
        model)
      (do 
        (println "Cant train model without data.")
        nil
        )))
  ([model training-data] 
    (do 
      (set-data model training-data)
      (train model))))

;(defn crossvalidate ([] ))
;(defn test [])

(defn predict [model user-id item-id]
  "Predict the weight for given user-item combination.
  If the user or the item are not known for recommender, zero is returned.
  To avoid this behavior for unknown entities, use predictable? method"
  (.Predict model user-id item-id))


;;TODO: add & test items and ignore-items
(defn recommend [model, user-id & {:keys [n items ignore-items]
                                   :or {n -1, items nil, ignore-items nil}}]
  "Recommends items for a given user. 
  Required Parameters:  model, user-id
  Optional params:
    :n - the number of items to recommend, default value is -1 = as many as possible
    :items - candidate items to choose from; default value is  nil = use all
    :ignore-items - collection of items, that shouldnt return  
  "
  (.Recommend model user-id n ignore-items items))


;(defn score-items []) - TODO: problem creating with generic Lists

; -- methods for  model information and statistics 
(defn compute-objective [model]
  "Computes the regularized loss"
  (.ComputeObjective model))

; -- Functions to manage data of the model
(defn retrain-item [model item-id]
  "Retrains the latent factors of a given item."
  (.RetrainItem model item-id))

(defn retrain-user [model user-id]
  (.RetrainItem model user-id))

(defn add-ratings [model ratings-obj]
  "Adds new rating object to model and preforms incremental training"
  (.AddRatings model ratings-obj))

(defn update-ratings [model ratings-obj]
  "Updates existing ratings and performs incremental training"
  (.UpdateRatings model ratings-obj))

(defn remove-item [model item-id]
  "Remove all feedback by one item."
  (.RemoveItem model item-id))

(defn remove-user [model user-id]
  "Remove all feedback by one user."
  (.RemoveUser model user-id))

