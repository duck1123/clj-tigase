(defproject clj-tigase "0.1.0-SNAPSHOT"
  :description "Clojure library for working with Tigase XMPP Server"
  :url "http://github.com/duck1123/clj-tigase"
  :min-lein-version "2.0.0"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"tigase-snapshots" "http://maven.tigase.org/"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [tigase/tigase-server "5.1.0-SNAPSHOT"]]
  :profiles {:dev
             {:dependencies
              [[midje "1.5.0-SNAPSHOT"]]}}
  :plugins [[codox "0.6.1"]
            [lein-midje "2.0.0-SNAPSHOT"]])
