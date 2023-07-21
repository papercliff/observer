(ns observer.apis.twitter
  (:require
    [clojure.string :as s]
    [clojure.tools.logging :as log]
    [environ.core :as env]
    [observer.attempt :as attempt]
    [observer.fs :as fs]
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
  (log/info "getting tag stats from twitter")
  (Thread/sleep attempt/sleep-time)
  (or
    (attempt/catch-all
      #(let [res
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
                    (fn [status]
                      (let [hashtag-set
                            (->> status
                                 :entities
                                 :hashtags
                                 (map :text)
                                 (map s/lower-case)
                                 set)]
                        (hashtag-set tag))))
                  (map :user)
                  (map :id)
                  distinct
                  count)]
         (log/info tag "popularity in twitter is" res)
         res))
    0))

(defn text-tweet
  [text]
  (log/info "posting text on twitter" text)
  (attempt/retry
    #(restful/statuses-update
       :oauth-creds my-creds
       :params {:status text})))

(defn image-tweet [title]
  (log/info "posting image on twitter")
  (attempt/retry
    #(restful/statuses-update-with-media
       :oauth-creds my-creds
       :body [(request/file-body-part fs/screenshot-abs-path)
              (request/status-body-part title)])))
