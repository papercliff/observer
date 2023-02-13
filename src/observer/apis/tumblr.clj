(ns observer.apis.tumblr
  (:require
    [clojure.java.io :as io]
    [environ.core :as env]
    [observer.fs :as fs]
    [taoensso.timbre :as timbre])
  (:import (com.tumblr.jumblr JumblrClient)
           (com.tumblr.jumblr.types LinkPost PhotoPost)))

(defn- client []
  (let [creds (JumblrClient.
                (env/env :tumblr-consumer-key)
                (env/env :tumblr-consumer-secret))]
    (.setToken
      creds
      (env/env :tumblr-oauth-token)
      (env/env :tumblr-oauth-token-secret))
    creds))

(defn text-post
  [title link tags]
  (timbre/info "posting text on tumblr" title link tags)
  (Thread/sleep 5000)
  (let [post (.newPost (client) "papercliff-api" LinkPost)]
    (.setTitle post title)
    (.setLinkUrl post link)
    (.setTags post (java.util.ArrayList. tags))
    (.save post)))

(defn image-post
  [caption tags]
  (timbre/info "posting image on tumblr")
  (Thread/sleep 5000)
  (let [post (.newPost (client) "papercliff-api" PhotoPost)]
    (.setCaption post caption)
    (.setData post (io/file fs/screenshot-abs-path))
    (.setTags post (java.util.ArrayList. tags))
    (.save post)))
