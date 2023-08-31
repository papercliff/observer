(ns observer.apis.papercliff
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [environ.core :as env]
            [observer.attempt :as attempt]))

(defn- api-data [endpoint query-params]
  (log/info "getting" endpoint query-params)
  (attempt/retry
    #(let [res (-> :papercliff-core-url
                   env/env
                   (str endpoint)
                   (client/get
                     {:content-type :json
                      :headers      {(env/env :papercliff-core-header-name)
                                     (env/env :papercliff-core-header-value)}
                      :query-params query-params})
                   :body
                   (json/read-str :key-fn keyword))]
       (log/info "resulting" endpoint res)
       res)))

(defn combinations [query-params]
  (api-data "/api/v1/combinations" query-params))
