(ns observer.apis.mastodon
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [environ.core :as env]
            [taoensso.timbre :as timbre]))

(def headers
  {"Authorization"
   (str
     "Bearer "
     (env/env :mastodon-access-token))})

(defn text-twoot
  ([text]
   (text-twoot nil text))
  ([media-id text]
   (timbre/info "posting text on mastodon" text)
   (Thread/sleep 5000)
   (client/post
     "https://newsie.social/api/v1/statuses"
     {:headers headers
      :form-params (merge
                     (if media-id
                       {"media_ids[]" media-id}
                       {})
                     {:status text})})))

(defn image-twoot [abs-png-path]
  (timbre/info "posting image on twitter" abs-png-path)
  (Thread/sleep 5000)
  (-> "https://newsie.social/api/v1/media"
      (client/post
        {:headers headers
         :multipart [{:name "file"
                      :mime-type "image/png"
                      :content (io/file abs-png-path)}]})
      :body
      (json/read-str :key-fn keyword)
      :id
      (text-twoot "#daily #news #keywords")))
