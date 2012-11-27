;--------------------------------------------------------------------------------
; Methodmaps for Recommender records
;
; here is collection of methodmaps to use in Recommender records
;--------------------------------------------------------------------------------
(in-ns 'clj-mml.recommenders.core)

(def recommender-method-map {
  :load-model (fn [this filename] (.LoadModel (:model this)))
  :train      (fn [this] (do (.Train (:model this)) this))
  :can-predict? (fn [this user-id item-id] 
    (.CanPredict (:model this) user-id item-id))
  :predict    (fn [this user-id item-id] 
    (.Predict (:model this) user-id item-id))
  :recommend  (fn [this user-id n ignored_items candidate_items] 
    (let [ignored_items (list->generic ignored_items)
          candidate_items (list->generic candidate_items)]
      (->RecommendationResults  
        (.Recommend (:model this) user-id n ignored_items candidate_items))
      ))
})

(def model-property-method-map {
  :to-string (fn [this] (.ToString (:model this)))
  :properties (fn [this] 
    (let [ props  (-> (:model this) (.GetType)(.GetProperties))]
      (set 
        (map (fn [prop] (keyword (.Name prop))) props))))
  :getp (fn [this property]
    (if (contains? (properties this) property)
      (let [prop (-> (:model this) (.GetType) (#(.GetProperty %1 (name property))))]
        (.GetValue prop (:model this)))
      (println "Model dont have property: " property)))
  :setp (fn [this property value]
    (if (contains? (properties this) property)
      (let [klass (-> (:model this)(.GetType))
            prop (.GetProperty klass (name property))
            typed-value (convert-type value)]
        (if (true? (.CanWrite prop))
          (do
            (.SetValue prop (:model this) typed-value nil)
            (.getp this property))
          (println "Property `" property "`isnot mutable.")
        ))
      (println "Property: " property " dont exists.")))
  :get-properties (fn [this]
    (->> 
      (.properties this)
      (map (fn [prop] 
             (try
               {prop (.getp this prop)}
               (catch Exception e {prop nil}))))
      (apply merge)
     ))
  :set-properties (fn [this config-map]
    (let [properties (.properties this)]
     (->> 
        (filter #(contains? properties (key %1)) config-map)
        (map (fn [row] (.setp this (first row) (second row))))
        (doall) ;;process lazy-lists
       )))
})



