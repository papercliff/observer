(ns observer.apis.tumblr
  (:require
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [environ.core :as env]
    [observer.attempt :as attempt])
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
  (log/info "posting text on tumblr" title link tags)
  (attempt/retry
    #(let [post (.newPost (client) "papercliff-api" LinkPost)]
       (.setTitle post title)
       (.setLinkUrl post link)
       (.setTags post (java.util.ArrayList. tags))
       (.save post))))

(defn image-post
  [image-abs-path caption tags]
  (log/info "posting image on tumblr")
  (attempt/retry
    #(let [post (.newPost (client) "papercliff-api" PhotoPost)]
       (.setCaption post caption)
       (.setData post (io/file image-abs-path))
       (.setTags post (java.util.ArrayList. tags))
       (.save post))))
