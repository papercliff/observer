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
            link (str "https://news.google.com/search?q="
                      (s/join "+" words)
                      "&hl=en-US&gl=US&ceid=US:en")
            keywords+link (str key-words "\n" link)]
        (mastodon-api/text-twoot keywords+link)
        (twitter-api/text-tweet keywords+link)
        (facebook-api/text-post keywords+link)
        (reddit-api/text-post
          key-words
          link)
        (linkedin-api/text-post keywords+link))))
  (timbre/info "text task completed"))
