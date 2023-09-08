(ns observer.image
  (:require
    [clojure.data.json :as json]
    [clojure.string :as s]
    [clojure.tools.logging :as log]
    [etaoin.api :as e]
    [observer.apis.facebook :as facebook-api]
    [observer.apis.github :as github-api]
    [observer.apis.instagram :as insta-api]
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

(defn take-screenshot [image-abs-path]
  (log/info "taking screenshot")
  (let [driver (e/firefox-headless
                 {:size [1080 1165]})]
    (->> "image.html"
         fs/res-path
         fs/absolute-path
         (str "file://")
         (e/go driver))
    (e/wait (+ 60 (rand 20)))
    (e/screenshot driver image-abs-path)
    (e/wait 10)
    (e/quit driver)))

(defn- gh-files-to-delete [now]
  (let [posts-dir "all_collections/_posts"
        images-dir "assets/images"

        image-del-pred
        (fn [filename]
          (some-> filename
                  (subs 0 10)
                  dt/safe-parse
                  (dt/interval-in-days now)
                  (> 7)))

        posts-to-delete
        (->> posts-dir
             (github-api/get-dir-filenames!
               "papercliff"
               "news")
             (filter
               (fn [filename]
                 (if (.contains filename "daily-keywords.md")
                   (image-del-pred filename)
                   (some-> filename
                           (subs 0 10)
                           dt/safe-parse
                           (dt/interval-in-days now)
                           (> 90)))))
             (map
               (fn [filename]
                 (str posts-dir "/" filename))))

        images-to-delete
        (->> images-dir
             (github-api/get-dir-filenames!
               "papercliff"
               "news")
             (filter image-del-pred)
             (map
               (fn [filename]
                 (str images-dir "/" filename))))]
    (concat posts-to-delete images-to-delete)))

(defn- update-gh-news-site [now image-abs-path]
  (attempt/catch-all
    #(github-api/with-changeset
       "papercliff"
       "news"
       "main"
       (fn [changeset]
         (-> (fn [acc path]
               (github-api/delete
                 acc
                 path))
             (reduce
               changeset
               (gh-files-to-delete now))
             (github-api/put-content
               (md-templ/png-image-path now)
               (fs/image-byte-array image-abs-path))
             (github-api/put-content
               (md-templ/image-post-path now)
               (md-templ/image-post-content now)))))))

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
                                    hashtags-str)
        image-abs-path (-> "screenshot.png"
                           fs/res-path
                           fs/absolute-path)]
    (->> now
         github-api/load-single-day-actions
         (format "const singleDayActions = %s;")
         (fs/save-content "single-day-actions.js"))
    (take-screenshot image-abs-path)

    (update-gh-news-site now image-abs-path)

    ;; post to social media
    (doseq [f [#(mastodon-api/image-twoot image-abs-path full-day-with-hashtags)
               #(twitter-api/image-tweet image-abs-path full-day-with-hashtags)
               #(facebook-api/image-post image-abs-path full-day-with-hashtags)
               #(let [[image-url _] (reddit-api/image-post image-abs-path full-day-str)]
                  (insta-api/image-post image-url full-day-with-hashtags)
                  (github-api/put-content-once
                    "mrdimosthenis"
                    "BlindfoldChessTraining"
                    "sponsor"
                    "sponsor.json"
                    (json/write-str
                      {:SponsorName "papercliff"
                       :SponsorImage image-url}
                      :indent true)))
               #(linkedin-api/image-post image-abs-path full-day-with-hashtags)
               #(tumblr-api/image-post image-abs-path full-day-str tags)]]
      (attempt/catch-all f)))

  (log/info "image task completed")
  (System/exit 0))

;; Comment to trigger rebuild again
