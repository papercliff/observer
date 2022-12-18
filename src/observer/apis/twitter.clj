(ns observer.apis.twitter
  (:require [environ.core :as env]
            [taoensso.timbre :as timbre]
            [twitter.oauth :as oauth]
            [twitter.api.restful :as restful]))

(defn- my-creds []
  (oauth/make-oauth-creds (env/env :twitter-api-key)
                          (env/env :twitter-api-key-secret)
                          (env/env :twitter-api-access-token)
                          (env/env :twitter-api-access-token-secret)))

(defn tweet [text]
  (timbre/info "posting on twitter" text)
  (Thread/sleep 5000)
  (restful/statuses-update
    :oauth-creds (my-creds)
    :params {:status text}))
