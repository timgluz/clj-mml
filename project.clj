(defproject clj-mml "org.clojars.timgluz/0.1.0"
  :description "ClojureCLR wrapper for MyMediaLite recommendation systems library."
  :url "https://github.com/timgluz/clj-mml"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :min-lein-version "2.0.0"
  :plugins [[lein-clr "0.2.0"]]
  :aot [clj-mml]
  :main clj-mml.core
  :clr {:cmd-templates  {:clj-exe   ["mono" [CLJCLR14_40 %1]]
                         :clj-dep   ["mono" ["target/clr/clj/Debug 4.0" %1]]
                         :clj-url   "https://github.com/downloads/clojure/clojure-clr/clojure-clr-1.4.1-Debug-4.0.zip"
                         :clj-zip   "clojure-clr-1.4.1-Debug-4.0.zip"
                         :curl      ["curl" "--insecure" "-f" "-L" "-o" %1 %2]
                         :nuget-ver ["mono" [*PATH "nuget.exe"] "install" %1 "-Version" %2]
                         :nuget-any ["mono" [*PATH "nuget.exe"] "install" %1]
                         :unzip     ["unzip" "-d" %1 %2]
                         :wget      ["wget" "--no-check-certificate" "--no-clobber" "-O" %1 %2]}
        ;; for automatic download/unzip of ClojureCLR,
        ;; 1. make sure you have curl or wget installed and on PATH,
        ;; 2. uncomment deps in :deps-cmds, and
        ;; 3. use :clj-dep instead of :clj-exe in :main-cmd and :compile-cmd
        :deps-cmds      [; [:wget  :clj-zip :clj-url] ; edit to use :curl instead of :wget
                         ; [:unzip "../clj" :clj-zip]
                         ; copy mymedialite libs to project libs
                         ;["cp" "-r" ".lein-git-deps/clj-mml/lib" "."]
                         ]
        :main-cmd      [:clj-exe "Clojure.Main.exe"]
        :compile-cmd   [:clj-exe "Clojure.Compile.exe"]
        :assembly-paths [["lib/"]]
  })
