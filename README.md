# clj-mml

It will be a ClojureCLR library for MyMediaLite, simple and fast recommender system.
Visit their homepage: http://www.ismll.uni-hildesheim.de/mymedialite/index.html .

Contact: @timgluz


## Prerequirements

I expect that you  already have installed given tools/libraries:

  * Mono3 - http://www.mono-project.com/Main_Page

  * ClojureCLR - https://github.com/clojure/clojure-clr

  * Lein2 - https://github.com/technomancy/leiningen

  * Lein2Clr - https://github.com/kumarshantanu/lein-clr



## Quick run

**Tested on OsX10.6, Mono3. should work also on Windows and Linux**. 

1. Clone this project:

  git clone https://github.com/timgluz/clj-mml.git

2. Download example data: 

  curl --remote-name http://www.grouplens.org/system/files/ml-100k.zip && unzip ml-100k.zip

3. Run project (currently broken, will fix very soon):

  lein clr run -m clj-mml.core ml-100k/u1.base


## Example usage:

```Clojure

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
  
 (.train oracle)
  
 ;;or use original method names
 (.set-ratings oracle training-dt)
 (= (read/size (.get-ratings oracle))
    (read/size training-dt))

 ;; or access methods & values via raw CLR interop:
 (.ToString (:model oracle))
 (.get_MaxRating (:model oracle))
 (.set_MaxRating (:model oracle) 10)
 (.get_MaxRating (:model oracle))

```

## License

Copyright © 2012 TimGluz

Distributed under the Eclipse Public License, the same as Clojure.