(ns observer.apis.linkedin
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [environ.core :as env]
            [observer.attempt :as attempt]
            [observer.fs :as fs]))

(def author
  "urn:li:organization:90787929")

(defn- access-token []
  (log/info "getting access token from linkedin")
  (attempt/retry
    #(-> "https://www.linkedin.com/oauth/v2/accessToken"
         (client/post
           {:form-params {:grant_type "refresh_token"
                          :client_id (env/env :linkedin-client-id)
                          :client_secret (env/env :linkedin-secret)
                          :refresh_token (env/env :linkedin-refresh-token)}})
         :body
         (json/read-str :key-fn keyword)
         :access_token)))

(defn- headers []
  {"LinkedIn-Version" "202301"
   "Authorization" (str "Bearer " (access-token))})

(def headers-memo
  (memoize headers))

(defn text-post
  ([text]
   (text-post nil text))
  ([media-id text]
   (log/info "posting text on linkedin" text)
   (attempt/retry
     #(->> {:author author
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
                     :headers (headers-memo)
                     :body)
           (client/post "https://api.linkedin.com/rest/posts")))))

(defn- prepare-image []
  (log/info "preparing image upload on linkedin")
  (attempt/retry
    #(-> "https://api.linkedin.com/rest/images?action=initializeUpload"
         (client/post
           {:content-type :json
            :headers (headers-memo)
            :body (json/write-str
                    {:initializeUploadRequest
                     {:owner author}})})
         :body
         (json/read-str :key-fn keyword)
         :value)))

(defn- upload-image [upload-url]
  (log/info "uploading image on linkedin")
  (attempt/retry
    #(client/put
       upload-url
       {:headers (headers-memo)
        :body (io/file fs/screenshot-abs-path)})))

(defn image-post [title]
  (let [{:keys [uploadUrl image]} (prepare-image)]
    (upload-image uploadUrl)
    (text-post image title)))
