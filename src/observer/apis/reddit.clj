(ns observer.apis.reddit
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :as env]
            [observer.apis.imgur :as imgur-api]
            [observer.attempt :as attempt]
            [taoensso.timbre :as timbre]))

(defn- access-token []
  (timbre/info "getting access token from reddit")
  (attempt/retry
    #(-> "https://www.reddit.com/api/v1/access_token"
         (client/post
           {:headers {"User-Agent" "papercliff-observer"}
            :basic-auth [(env/env :reddit-client-id) (env/env :reddit-secret)]
            :form-params {:grant_type "password"
                          :username "papercliff_api"
                          :password (env/env :reddit-password)}})
         :body
         (json/read-str :key-fn keyword)
         :access_token)))

(defn- post-headers []
  {"User-Agent" "papercliff-observer"
   "Authorization" (str "Bearer " (access-token))})

(defn text-post [title description]
  (let [headers (post-headers)]
    (timbre/info "posting text to reddit" title)
    (attempt/retry
      #(client/post
         "https://oauth.reddit.com/api/submit"
         {:headers headers
          :form-params {:title title
                        :kind "self"
                        :text description
                        :sr "r/papercliff"}}))))

(defn image-post [title]
  (let [headers (post-headers)
        image-url (imgur-api/upload-image
                    (str "News keywords for " title))]
    (timbre/info "posting image to reddit")
    (attempt/retry
      #(client/post
         "https://oauth.reddit.com/api/submit"
         {:headers headers
          :form-params {:title title
                        :kind "link"
                        :url image-url
                        :sr "r/papercliff"}}))
    image-url))
