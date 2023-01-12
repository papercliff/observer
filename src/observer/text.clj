(ns observer.text
  (:require [clojure.string :as s]
            [observer.apis.facebook :as facebook-api]
            [observer.apis.mastodon :as mastodon-api]
            [observer.apis.papercliff :as ppf-api]
            [observer.apis.twitter :as twitter-api]
            [observer.date-time :as dt]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn- content [words]
  (str
    (s/join " · " words)
    "\n"
    "https://news.google.com/search?q="
    (s/join "+" words)))

(defn -main []
  (timbre/info "starting text task")
  (when-let [stories
             (ppf-api/new-important-clusters
               (dt/minutes-ago
                 (dt/now)
                 60))]
    (timbre/info "stories" stories)
    (doseq [words stories]
      (let [post (content words)]
        (mastodon-api/text-twoot post)
        (twitter-api/text-tweet post)
        (facebook-api/text-post (str post "&hl=en-US&gl=US&ceid=US:en")))))
  (timbre/info "text task completed"))
