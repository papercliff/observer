(ns observer.apis.imgur
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :as env]
            [observer.date-time :as dt]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre]))

(defn- access-token []
  (timbre/info "getting access token from imgur")
  (Thread/sleep 5000)
  (-> "https://api.imgur.com/oauth2/token"
      (client/post
        {:form-params {:grant_type "refresh_token"
                       :client_id (env/env :imgur-client-id)
                       :client_secret (env/env :imgur-secret)
                       :refresh_token (env/env :imgur-refresh-token)}})
      :body
      (json/read-str :key-fn keyword)
      :access_token))

(defn- encoded-image []
  (let [f (java.io.File. fs/screenshot-abs-path)
        ary (byte-array (.length f))
        is (java.io.FileInputStream. f)]
    (.read is ary)
    (.close is)
    (.encodeToString (java.util.Base64/getEncoder) ary)))

(defn upload-image []
  (let [headers {"Authorization" (str "Bearer " (access-token))}]
    (timbre/info "uploading image on imgur")
    (Thread/sleep 5000)
    (-> "https://api.imgur.com/3/upload"
        (client/post
          {:headers headers
           :form-params {:image (encoded-image)
                         :title (->> (dt/now)
                                     dt/->prev-day-full-str
                                     (str "News keywords for "))
                         :type "base64"}})
        :body
        (json/read-str :key-fn keyword)
        :data
        :link)))
