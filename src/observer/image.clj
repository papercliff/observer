(ns observer.image
  (:require
    [clojure.string :as s]
    [etaoin.api :as e]
    [observer.apis.facebook :as facebook-api]
    [observer.apis.github :as github-api]
    [observer.apis.linkedin :as linkedin-api]
    [observer.apis.mastodon :as mastodon-api]
    [observer.apis.reddit :as reddit-api]
    [observer.apis.tumblr :as tumblr-api]
    [observer.apis.twitter :as twitter-api]
    [observer.attempt :as attempt]
    [observer.date-time :as dt]
    [observer.fs :as fs]
    [taoensso.timbre :as timbre])
  (:gen-class))

(def tags
  ["daily" "news" "keywords"])

(def hashtags-str
  (->> tags
       (map #(str "#" %))
       (s/join " ")))

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
        full-day-str (-> now
                         dt/at-start-of-prev-day
                         dt/->full-day-str)
        full-day-with-hashtags (str full-day-str
                                    "\n"
                                    hashtags-str)]
    (->> now
         github-api/load-single-day-actions
         (format "const singleDayActions = %s;")
         (fs/save-content "single-day-actions.js"))
    (take-screenshot)
    (doseq [f [#(mastodon-api/image-twoot full-day-with-hashtags)
               #(twitter-api/image-tweet full-day-with-hashtags)
               #(facebook-api/image-post full-day-with-hashtags)
               #(let [image-url (reddit-api/image-post full-day-str)]
                  (github-api/save-content
                    "mrdimosthenis"
                    "BlindfoldChessTraining"
                    "sponsor"
                    "sponsor.json"
                    {:SponsorName "papercliff"
                     :SponsorImage image-url}
                    "Auto-commit by Papercliff observer"))
               #(linkedin-api/image-post full-day-with-hashtags)
               #(tumblr-api/image-post full-day-str tags)]]
      (attempt/catch-all f)))
  (timbre/info "image task completed")
  (System/exit 0))

;; Comment to trigger rebuild again
