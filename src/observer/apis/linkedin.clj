(ns observer.apis.linkedin
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [environ.core :as env]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre]))

(def author
  "urn:li:organization:90787929")

(defn- access-token []
  (timbre/info "getting access token from linkedin")
  (Thread/sleep 5000)
  (-> "https://www.linkedin.com/oauth/v2/accessToken"
      (client/post
        {:form-params {:grant_type "refresh_token"
                       :client_id (env/env :linkedin-client-id)
                       :client_secret (env/env :linkedin-secret)
                       :refresh_token (env/env :linkedin-refresh-token)}})
      :body
      (json/read-str :key-fn keyword)
      :access_token))

(defn- headers []
  {"LinkedIn-Version" "202301"
   "Authorization" (str "Bearer " (access-token))})

(defn text-post
  ([text]
   (text-post nil text))
  ([media-id text]
   (timbre/info "posting text on linkedin" text)
   (Thread/sleep 5000)
   (->> {:author author
         :commentary text
         :visibility "PUBLIC"
         :distribution {:feedDistribution "MAIN_FEED"
                        :targetEntities []
                        :thirdPartyDistributionChannels []}
         :lifecycleState "PUBLISHED"
         :isReshareDisabledByAuthor false}
        (merge
          (if media-id
            {:content {:media {:id media-id}}}
            {}))
        json/write-str
        (hash-map :content-type :json
                  :headers (headers)
                  :body)
        (client/post "https://api.linkedin.com/v2/posts"))))

(defn- prepare-image []
  (timbre/info "preparing image upload on linkedin")
  (Thread/sleep 5000)
  (-> "https://api.linkedin.com/rest/images?action=initializeUpload"
      (client/post
        {:content-type :json
         :headers (headers)
         :body (json/write-str
                 {:initializeUploadRequest
                  {:owner author}})})
      :body
      (json/read-str :key-fn keyword)
      :value))

(defn- upload-image [upload-url]
  (timbre/info "uploading image on linkedin")
  (Thread/sleep 5000)
  (client/put
    upload-url
    {:headers (headers)
     :body (io/file fs/screenshot-abs-path)}))

(defn image-post [title]
  (let [{:keys [uploadUrl image]} (prepare-image)]
    (upload-image uploadUrl)
    (text-post image title)))
