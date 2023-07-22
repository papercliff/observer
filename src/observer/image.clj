(ns observer.image
  (:require
    [clojure.data.json :as json]
    [clojure.string :as s]
    [clojure.tools.logging :as log]
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
    [observer.markdown-templates :as md-templ])
  (:gen-class))

(def tags
  ["daily" "news" "keywords"])

(def hashtags-str
  (->> tags
       (map #(str "#" %))
       (s/join " ")))

(defn take-screenshot []
  (log/info "taking screenshot")
  (let [driver (e/firefox-headless
                 {:size [1080 1080]})]
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
  (log/info "starting image task")
  (fs/delete-res-dir)
  (github-api/clone-graph-vis)
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

    ;; commit to news website
    (attempt/catch-all
      #(github-api/with-changeset
         "papercliff"
         "news"
         "main"
         (fn [changeset]
           (-> changeset
               (github-api/put-content
                 (md-templ/png-image-path now)
                 (fs/image-byte-array))
               (github-api/put-content
                 (md-templ/image-post-path now)
                 (md-templ/image-post-content now))))))

    ;; post to social media
    (doseq [f [#(mastodon-api/image-twoot full-day-with-hashtags)
               #(twitter-api/image-tweet full-day-with-hashtags)
               #(facebook-api/image-post full-day-with-hashtags)
               #(let [image-url (reddit-api/image-post full-day-str)]
                  (github-api/put-content-once
                    "mrdimosthenis"
                    "BlindfoldChessTraining"
                    "sponsor"
                    "sponsor.json"
                    (json/write-str
                      {:SponsorName "papercliff"
                       :SponsorImage image-url}
                      :indent true)))
               #(linkedin-api/image-post full-day-with-hashtags)
               #(tumblr-api/image-post full-day-str tags)]]
      (attempt/catch-all f)))

  (log/info "image task completed")
  (System/exit 0))

;; Comment to trigger rebuild again
