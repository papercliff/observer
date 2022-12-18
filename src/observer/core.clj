(ns observer.core
  (:require [observer.apis.mastodon :as mastodon-api]
            [observer.apis.papercliff :as ppf-api]
            [observer.apis.twitter :as twitter-api]
            [observer.date-time :as dt]
            [observer.text :as text]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn -main []
  (when-let [stories
             (ppf-api/new-important-clusters
               (dt/minutes-ago
                 (dt/now)
                 60))]
    (timbre/info "stories" stories)
    (doseq [words stories]
      (let [post (text/post words)]
        (twitter-api/tweet post)
        (mastodon-api/post post)))))
