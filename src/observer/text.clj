(ns observer.text
  (:require [clojure.string :as s]
            [observer.apis.facebook :as facebook-api]
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
            link-suffix "&hl=en-US&gl=US&ceid=US:en"]
        (mastodon-api/text-twoot
          (str key-words
               "\n"
               base-link))
        (twitter-api/text-tweet
          (str key-words
               "\n"
               base-link))
        (facebook-api/text-post
          (str key-words
               "\n"
               base-link
               link-suffix))
        (reddit-api/text-post
          key-words
          base-link))))
  (timbre/info "text task completed"))
