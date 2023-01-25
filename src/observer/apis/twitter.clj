(ns observer.apis.twitter
  (:require [environ.core :as env]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre]
            [twitter.api.restful :as restful]
            [twitter.oauth :as oauth]
            [twitter.request :as request]))

(def my-creds
  (oauth/make-oauth-creds (env/env :twitter-api-key)
                          (env/env :twitter-api-key-secret)
                          (env/env :twitter-api-access-token)
                          (env/env :twitter-api-access-token-secret)))

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
