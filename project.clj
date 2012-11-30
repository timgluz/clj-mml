(defproject clj-mml "org.clojars.timgluz/0.1.0"
  :description "ClojureCLR wrapper for MyMediaLite recommendation systems library."
  :url "https://github.com/timgluz/clj-mml"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :min-lein-version "2.0.0"
  :plugins [[lein-clr "0.1.0"]]
  :aot [clj-mml]
  :main clj-mml.core
  :clr {:main-cmd    ["mono" [CLJCLR14_40 "Clojure.Main.exe"]]
        :compile-cmd ["mono" [CLJCLR14_40 "Clojure.Compile.exe"]]
        ;:target-path "target/clr"
        :load-paths  ["lib/mymedialite"]})
