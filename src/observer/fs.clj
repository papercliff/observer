(ns observer.fs
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [me.raynes.fs :as raynes])
  (:import (java.io File FileInputStream)))

(def res-dir-path
  "resources")

(defn res-path [rel-path]
  (str res-dir-path "/" rel-path))

(defn absolute-path [rel-path]
  (.getAbsolutePath
    (io/file rel-path)))

(defn delete-res-dir []
  (log/info
    "deleting resources directory")
  (raynes/delete-dir res-dir-path))

(defn save-content
  [rel-path content]
  (log/info
    "saving contents to"
    (res-path rel-path))
  (spit
    (res-path rel-path)
    content))

(defn image-byte-array [image-abs-path]
  (let [f (File. ^String image-abs-path)
        ary (byte-array (.length f))
        is (FileInputStream. f)]
    (.read is ary)
    (.close is)
    ary))
