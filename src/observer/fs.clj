(ns observer.fs
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre]))

(defn- res-path [rel-path]
  (str "resources/" rel-path))

(defn absolute-path [rel-path]
  (.getAbsolutePath
    (io/file rel-path)))

(defn load-content [rel-path]
  (timbre/info
    "loading contents from"
    (res-path rel-path))
  (-> rel-path res-path slurp))

(defn save-content
  [rel-path content]
  (timbre/info
    "saving contents to"
    (res-path rel-path))
  (spit
    (res-path rel-path)
    content))
