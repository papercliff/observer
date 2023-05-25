(ns observer.apis.imgur
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :as env]
            [observer.attempt :as attempt]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre])
  (:import (java.io File FileInputStream)
           (java.util Base64)))

(defn- access-token []
  (timbre/info "getting access token from imgur")
  (attempt/retry
    #(-> "https://api.imgur.com/oauth2/token"
         (client/post
           {:form-params {:grant_type "refresh_token"
                          :client_id (env/env :imgur-client-id)
                          :client_secret (env/env :imgur-secret)
                          :refresh_token (env/env :imgur-refresh-token)}})
         :body
         (json/read-str :key-fn keyword)
         :access_token)))

(defn- encoded-image []
  (let [f (File. ^String fs/screenshot-abs-path)
        ary (byte-array (.length f))
        is (FileInputStream. f)]
    (.read is ary)
    (.close is)
    (.encodeToString (Base64/getEncoder) ary)))

(defn upload-image [title]
  (let [headers {"Authorization" (str "Bearer " (access-token))}]
    (timbre/info "uploading image on imgur")
    (attempt/retry
      #(-> "https://api.imgur.com/3/upload"
           (client/post
             {:headers headers
              :form-params {:image (encoded-image)
                            :title title
                            :type "base64"}})
           :body
           (json/read-str :key-fn keyword)
           :data
           :link))))
