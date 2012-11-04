# clj-mml

It will be a ClojureCLR library for MyMediaLite, simple and fast recommender system . 
Visit their homepage: http://www.ismll.uni-hildesheim.de/mymedialite/index.html .


This project currently includes just  simple usage examples to get MyMediaLite run.


Contact: @timgluz


## Prerequirements

I expect you have already installed given tools:

  * Mono3 - http://www.mono-project.com/Main_Page

  * ClojureCLR - https://github.com/clojure/clojure-clr

  * Lein2 - https://github.com/technomancy/leiningen

  * Lein2Clr - https://github.com/kumarshantanu/lein-clr



## Usage

**Tested on OsX10.6, Mono3**. 

1. Clone this project:
  git clone https://github.com/timgluz/clj-mml.git

2. Download example data: 
  
  curl --remote-name http://www.grouplens.org/system/files/ml-100k.zip && unzip ml-100k.zip

3. Run project:

  lein clr run -m clj-mml.core ml-100k/u1.data



## License

Copyright © 2012 FIXME

Distributed under the Eclipse Public License, the same as Clojure.