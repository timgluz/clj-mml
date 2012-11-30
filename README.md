# clj-mml

It will be a ClojureCLR library for MyMediaLite, simple and fast recommender system.
Visit their homepage: http://www.ismll.uni-hildesheim.de/mymedialite/index.html .

Contact: @timgluz [twitter](https://twitter.com/timgluz)|[gmail.com](timgluz+cljmml@gmail.com)


## Prerequirements

I expect that you  already have installed given tools&libraries:

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
  
3. Rename ml-100k directory to data:

 mv ml-100k data

4. Run tests:

 lein clr test 
 lein clr test clj-mml.test-ratingprediction

5. Run project:

 lein clr run -m clj-mml.core data/u1.base
 lein clr run data/u1.base


## Wiki pages
 * [main wiki page & usage](https://github.com/timgluz/clj-mml/wiki/CLJ-MML)
 * [ratingprediction api & usage](https://github.com/timgluz/clj-mml/wiki/RatingPrediction)
 * [itemrecommendation api](https://github.com/timgluz/clj-mml/wiki/itemrecommendation)


## Example usage:

```Clojure

(ns clj-mml.examples.sugarexample
  (:require [clj-mml.recommenders.itemrecommendation :as itemrecommendation]
            [clj-mml.recommenders.ratingprediction :as ratingprediction])
  (:use [clj-mml.recommenders.recommender]))

(def training-file "data/u1.base")
(def test-file "data/u1.test")

(def oracle (itemrecommendation/init :MostPopular))
(def delphi (ratingprediction/init :UserAverage))

;;train recommenders
(train oracle training-file)
(train delphi [[1 1 4.0][1 2 5.0][1 3 4.0]])

(can-predict? delphi 1 1)
(predict delphi 1 1)
(recommend oracle 1 -1 [100 101] [100 101 102 103 104 105])

(evaluate oracle test-file training-file)
(evaluate delphi test-file training-file)


```

## Usage on own project
 
Check out [Starter-MML](https://github.com/timgluz/starter-mml) to fasten up starting new project. 


## License

Copyright © 2012 TimGluz

Distributed under the Eclipse Public License, the same as Clojure.