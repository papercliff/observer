(ns observer.fs
  (:require [clojure.java.io :as io]
            [taoensso.timbre :as timbre]))

(defn res-path [rel-path]
  (str "resources/" rel-path))

(defn absolute-path [rel-path]
  (.getAbsolutePath
    (io/file rel-path)))

(defn save-content
  [rel-path content]
  (timbre/info
    "saving contents to"
    (res-path rel-path))
  (spit
    (res-path rel-path)
    content))

(def screenshot-abs-path
  (-> "screenshot.png"
      res-path
      absolute-path))
