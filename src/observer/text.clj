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

(defn- tag-candidates [key-words]
  (let [filtered-capitalized
        (->> key-words
             (filter
               (fn [w]
                 (every? #(Character/isLetter %) w)))
             (map s/capitalize))]
    (concat
      filtered-capitalized
      (for [a filtered-capitalized
            b filtered-capitalized
            :when (not= a b)]
        (str a b)))))

(defn- chosen-tags [key-words]
  (->> key-words
       tag-candidates
       (map
         #(vector
            %
            (+ (mastodon-api/hashtag-popularity %)
               (twitter-api/hashtag-popularity %))))
       (sort-by second)
       reverse
       (take 3)
       (map first)))

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
            hashtags (->> words
                          chosen-tags
                          (map #(str "#" %))
                          (s/join " "))
            keywords+link+hashtags (str
                                     key-words
                                     "\n"
                                     link
                                     "\n"
                                     hashtags)]
        (mastodon-api/text-twoot keywords+link+hashtags)
        (twitter-api/text-tweet keywords+link+hashtags)
        (facebook-api/text-post keywords+link+hashtags)
        (reddit-api/text-post
          key-words
          link)
        (linkedin-api/text-post keywords+link+hashtags))))
  (timbre/info "text task completed"))
