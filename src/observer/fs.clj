(ns observer.fs
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as raynes]
            [taoensso.timbre :as timbre]))

(def res-dir-path
  "resources")

(defn res-path [rel-path]
  (str res-dir-path "/" rel-path))

(defn absolute-path [rel-path]
  (.getAbsolutePath
    (io/file rel-path)))

(defn delete-res-dir []
  (timbre/info
    "deleting resources directory")
  (raynes/delete-dir res-dir-path))

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
