(ns observer.apis.mastodon
  (:require [clj-http.client :as client]
            [environ.core :as env]
            [taoensso.timbre :as timbre]))

(defn post [text]
  (timbre/info "posting on mastodon" text)
  (Thread/sleep 5000)
  (client/post
    "https://newsie.social/api/v1/statuses"
    {:headers      {"Authorization"
                    (str
                      "Bearer "
                      (env/env :mastodon-access-token))}
     :form-params {:status text}}))
