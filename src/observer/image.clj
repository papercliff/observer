(ns observer.image
  (:require [etaoin.api :as e]
            [observer.apis.github :as github-api]
            [observer.apis.mastodon :as mastodon-api]
            [observer.apis.twitter :as twitter-api]
            [observer.date-time :as dt]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn take-screenshot []
  (timbre/info "taking screenshot")
  (let [driver (e/chrome
                 {:headless true,
                  :size [1080 1080]})]
    (->> "image.html"
         fs/res-path
         fs/absolute-path
         (str "file://")
         (e/go driver))
    (e/wait (+ 10 (rand 20)))
    (e/screenshot driver fs/screenshot-abs-path)
    (e/wait 10)
    (e/quit driver)))

(defn -main []
  (timbre/info "starting image task")
  (github-api/clone-animated-graph)
  (->> (dt/now)
       github-api/load-single-day-actions
       (format "const singleDayActions = %s;")
       (fs/save-content "single-day-actions.js"))
  (take-screenshot)
  (mastodon-api/image-twoot)
  (twitter-api/image-tweet)
  (timbre/info "text task completed"))
