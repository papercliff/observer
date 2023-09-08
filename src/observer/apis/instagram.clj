(ns observer.apis.instagram
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [observer.apis.facebook :as facebook-api]
            [observer.attempt :as attempt]))

(defn image-post
  [image-url caption]
  (log/info "posting image to Instagram")
  (attempt/retry
    #(let [media-id (-> "https://graph.facebook.com/17841461607658091/media"
               (client/post
                 {:query-params
                  {:image_url    image-url
                   :caption      caption
                   :access_token facebook-api/access-token}})
               :body
               (json/read-str :key-fn keyword)
               :id)]
       (client/post
         "https://graph.facebook.com/17841461607658091/media_publish"
         {:query-params
          {:creation_id  media-id
           :access_token facebook-api/access-token}}))))
