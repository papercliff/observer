(ns observer.apis.mastodon
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [environ.core :as env]
            [observer.attempt :as attempt]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre]))

(def headers
  {"Authorization"
   (str
     "Bearer "
     (env/env :mastodon-access-token))})

(defn hashtag-popularity [tag]
  (timbre/info "getting tag stats from mastodon")
  (Thread/sleep attempt/sleep-time)
  (or
    (attempt/catch-all
      #(let [history
             (-> "https://newsie.social/api/v1/tags/"
                 (str tag)
                 (client/get {:headers headers})
                 :body
                 (json/read-str :key-fn keyword)
                 :history)

             res (->> history
                      (map :accounts)
                      (map (fn [s] (Integer/parseInt s)))
                      (apply +))]
         (timbre/info tag "popularity in mastodon is" res)
         res))
    0))

(defn text-twoot
  ([text]
   (text-twoot nil text))
  ([media-id text]
   (timbre/info "posting text on mastodon" text)
   (attempt/retry
     #(client/post
        "https://newsie.social/api/v1/statuses"
        {:headers headers
         :form-params (merge
                        (if media-id
                          {"media_ids[]" media-id}
                          {})
                        {:status text})}))))

(defn image-twoot [title]
  (timbre/info "posting image on mastodon")
  (attempt/retry
    #(-> "https://newsie.social/api/v1/media"
         (client/post
           {:headers headers
            :multipart [{:name "file"
                         :mime-type "image/png"
                         :content (io/file fs/screenshot-abs-path)}]})
         :body
         (json/read-str :key-fn keyword)
         :id
         (text-twoot title))))
