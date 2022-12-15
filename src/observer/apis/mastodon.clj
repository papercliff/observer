(ns observer.apis.mastodon
  (:require [clj-http.client :as client]
            [environ.core :as env]))

(defn post [text]
  (client/post
    "https://newsie.social/api/v1/statuses"
    {:headers      {"Authorization"
                    (str
                      "Bearer "
                      (env/env :mastodon-access-token))}
     :form-params {:status text}}))
