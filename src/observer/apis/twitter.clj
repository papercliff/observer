(ns observer.apis.twitter
  (:require
    [clj-http.client :as client]
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [environ.core :as env]
    [observer.attempt :as attempt]
    [oauth.client :as oauth]))

(def text-endpoint-url
  "https://api.twitter.com/2/tweets")

(def image-endpoint-url
  "https://upload.twitter.com/1.1/media/upload.json")

(defn- consumer []
  (oauth/make-consumer
    (env/env :twitter-consumer-key)
    (env/env :twitter-consumer-secret)
    "https://api.twitter.com/oauth/request_token"
    "https://api.twitter.com/oauth/access_token"
    "https://api.twitter.com/oauth/authorize"
    :hmac-sha1))

(defn- credentials [method url]
  (oauth/credentials
    (consumer)
    (env/env :twitter-access-token)
    (env/env :twitter-token-secret)
    method
    url))

(defn- headers
  [method url]
  {"Authorization"
   (oauth/authorization-header
     (credentials
       method url))})

(defn text-tweet
  ([text]
   (text-tweet nil text))
  ([media-id text]
   (log/info "posting text on twitter" text)
   (attempt/retry
     #(client/post
        text-endpoint-url
        {:headers      (headers
                         :POST
                         text-endpoint-url)
         :content-type :json
         :body         (json/write-str
                         (merge
                           (if media-id
                             {:media
                              {:media_ids
                               [(str media-id)]}}
                             {})
                           {:text text}))}))))

(defn image-tweet [image-abs-path title]
  (log/info "posting image on twitter")
  (attempt/retry
    #(-> (client/post
           image-endpoint-url
           {:headers
            (headers
              :POST
              image-endpoint-url)
            :multipart
            [{:name    "media"
              :content (io/file image-abs-path)}]})
         :body
         (json/read-str :key-fn keyword)
         :media_id
         (text-tweet title))))
