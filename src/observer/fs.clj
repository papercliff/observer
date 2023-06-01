(ns observer.fs
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as raynes]
            [taoensso.timbre :as timbre])
  (:import (java.io File FileInputStream)))

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

(defn image-byte-array []
  (let [f (File. ^String screenshot-abs-path)
        ary (byte-array (.length f))
        is (FileInputStream. f)]
    (.read is ary)
    (.close is)
    ary))
