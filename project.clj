(defproject observer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[aysylu/loom "1.0.2"]
                 [clj-http "3.12.3"]
                 [clj-time "0.15.2"]
                 [environ "1.2.0"]
                 [etaoin "1.0.39"]
                 [com.taoensso/timbre "6.0.2"]
                 [dev.nubank/clj-github "0.6.2"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.slf4j/slf4j-log4j12 "2.0.5"]
                 [twitter-api "1.8.0"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "observer-standalone.jar"
  :profiles {:production {:env {:production true}}})
