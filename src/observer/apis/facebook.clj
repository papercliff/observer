(ns observer.apis.facebook
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [environ.core :as env]
            [observer.attempt :as attempt]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre]))

(def access-token
  (env/env :fb-page-access-token))

(defn text-post
  ([text]
   (text-post nil text))
  ([media-id text]
   (timbre/info "posting text on facebook" text)
   (attempt/retry
     #(client/post
        "https://graph.facebook.com/112918271679891/feed"
        {:query-params
         (merge
           (if media-id
             {"attached_media[0]"
              (format "{\"media_fbid\":\"%s\"}" media-id)}
             {})
           {:message text
            :access_token access-token})}))))

(defn image-post [title]
  (timbre/info "posting image on facebook")
  (attempt/retry
    #(-> "https://graph.facebook.com/me/photos"
         (client/post
           {:query-params
            {:published "false"
             :access_token access-token}
            :multipart [{:name "file"
                         :mime-type "image/png"
                         :content (io/file fs/screenshot-abs-path)}]})
         :body
         (json/read-str :key-fn keyword)
         :id
         (text-post title))))
