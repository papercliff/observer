(ns observer.apis.twitter
  (:require [environ.core :as env]
            [twitter.oauth :as oauth]
            [twitter.api.restful :as restful]))

(defn- my-creds []
  (oauth/make-oauth-creds (env/env :twitter-api-key)
                          (env/env :twitter-api-key-secret)
                          (env/env :twitter-api-access-token)
                          (env/env :twitter-api-access-token-secret)))

(defn tweet [text]
  (restful/statuses-update
    :oauth-creds (my-creds)
    :params {:status text}))
