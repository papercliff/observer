(ns observer.apis.twitter
  (:require [environ.core :as env]
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

(defn image-tweet [abs-png-path]
  (timbre/info "posting image on twitter" abs-png-path)
  (Thread/sleep 5000)
  (restful/statuses-update-with-media
    :oauth-creds my-creds
    :body [(request/file-body-part abs-png-path)
           (request/status-body-part "#daily #news #keywords")]))
