(ns observer.image
  (:require [etaoin.api :as e]
            [observer.apis.facebook :as facebook-api]
            [observer.apis.github :as github-api]
            [observer.apis.linkedin :as linkedin-api]
            [observer.apis.mastodon :as mastodon-api]
            [observer.apis.reddit :as reddit-api]
            [observer.apis.twitter :as twitter-api]
            [observer.date-time :as dt]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre])
  (:gen-class))

(def hashtags-str
  "#daily #news #keywords")

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
    (e/wait (+ 60 (rand 20)))
    (e/screenshot driver fs/screenshot-abs-path)
    (e/wait 10)
    (e/quit driver)))

(defn -main []
  (timbre/info "starting image task")
  (fs/delete-res-dir)
  (github-api/clone-animated-graph)
  (let [now (dt/now)
        full-day-str (dt/->prev-day-full-str now)
        full-day-with-hashtags (str full-day-str
                                    "\n"
                                    hashtags-str)]
    (->> now
         github-api/load-single-day-actions
         (format "const singleDayActions = %s;")
         (fs/save-content "single-day-actions.js"))
    (take-screenshot)
    (mastodon-api/image-twoot full-day-with-hashtags)
    (twitter-api/image-tweet full-day-with-hashtags)
    (facebook-api/image-post full-day-with-hashtags)
    (reddit-api/image-post full-day-str)
    (linkedin-api/image-post full-day-with-hashtags))
  (timbre/info "image task completed"))
