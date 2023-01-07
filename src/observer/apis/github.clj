(ns observer.apis.github
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [taoensso.timbre :as timbre]))

(defn load-content [repo path]
  (timbre/info "loading github contents from" repo path)
  (-> "https://raw.githubusercontent.com/papercliff/%s/master/%s"
      (format repo path)
      (client/get {:throw-exceptions false})
      :body
      (json/read-str :key-fn keyword)))
