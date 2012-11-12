# clj-mml

It will be a ClojureCLR library for MyMediaLite, simple and fast recommender system.
Visit their homepage: http://www.ismll.uni-hildesheim.de/mymedialite/index.html .


This project currently includes just  simple usage examples to get MyMediaLite run.


Contact: @timgluz


## Prerequirements

I expect that you  already have installed given tools/libraries:

  * Mono3 - http://www.mono-project.com/Main_Page

  * ClojureCLR - https://github.com/clojure/clojure-clr

  * Lein2 - https://github.com/technomancy/leiningen

  * Lein2Clr - https://github.com/kumarshantanu/lein-clr



## Usage

**Tested on OsX10.6, Mono3. should work also on Windows and Linux**. 

1. Clone this project:

  git clone https://github.com/timgluz/clj-mml.git

2. Download example data: 

  curl --remote-name http://www.grouplens.org/system/files/ml-100k.zip && unzip ml-100k.zip

3. Run project:

  lein clr run -m clj-mml.core ml-100k/u1.base

Also look into examples & test directory.


## License

Copyright © 2012 TimGluz

Distributed under the Eclipse Public License, the same as Clojure.