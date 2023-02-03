(ns observer.apis.twitter
  (:require
    [clojure.string :as s]
    [environ.core :as env]
    [observer.fs :as fs]
    [taoensso.timbre :as timbre]
    [twitter.api.restful :as restful]
    [twitter.api.search :as search]
    [twitter.oauth :as oauth]
    [twitter.request :as request]))

(def my-creds
  (oauth/make-oauth-creds (env/env :twitter-api-key)
                          (env/env :twitter-api-key-secret)
                          (env/env :twitter-api-access-token)
                          (env/env :twitter-api-access-token-secret)))

(defn hashtag-popularity [tag]
  (timbre/info "getting tag stats from twitter")
  (Thread/sleep 5000)
  (let [res
        (->> {:q (str "%23" tag "%20-filter%3Aretweets")
              :result_type "recent"
              :lang "en"
              :count 100}
             (search/search
               :oauth-creds my-creds
               :params)
             :body
             :statuses
             (filter
               #(let [hashtag-set
                      (->> %
                           :entities
                           :hashtags
                           (map :text)
                           (map s/lower-case)
                           set)]
                  (hashtag-set tag)))
             (map :user)
             (map :id)
             distinct
             count)]
    (timbre/info tag "popularity in twitter is" res)
    res))

(defn text-tweet
  [text]
  (timbre/info "posting text on twitter" text)
  (Thread/sleep 5000)
  (restful/statuses-update
    :oauth-creds my-creds
    :params {:status text}))

(defn image-tweet [title]
  (timbre/info "posting image on twitter")
  (Thread/sleep 5000)
  (restful/statuses-update-with-media
    :oauth-creds my-creds
    :body [(request/file-body-part fs/screenshot-abs-path)
           (request/status-body-part title)]))
