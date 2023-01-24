(ns observer.text
  (:require [clojure.string :as s]
            [observer.apis.facebook :as facebook-api]
            [observer.apis.linkedin :as linkedin-api]
            [observer.apis.mastodon :as mastodon-api]
            [observer.apis.papercliff :as ppf-api]
            [observer.apis.reddit :as reddit-api]
            [observer.apis.twitter :as twitter-api]
            [observer.date-time :as dt]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn -main []
  (timbre/info "starting text task")
  (when-let [stories
             (ppf-api/new-important-clusters
               (dt/now))]
    (timbre/info "stories" stories)
    (doseq [words stories]
      (let [key-words (s/join " · " words)
            base-link (str "https://news.google.com/search?q="
                           (s/join "+" words))
            single-base-text (str key-words "\n" base-link)]
        (mastodon-api/text-twoot single-base-text)
        (twitter-api/text-tweet single-base-text)
        (facebook-api/text-post
          (str single-base-text "&hl=en-US&gl=US&ceid=US:en"))
        (reddit-api/text-post
          key-words
          base-link)
        (linkedin-api/text-post single-base-text))))
  (timbre/info "text task completed"))
