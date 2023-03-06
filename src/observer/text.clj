(ns observer.text
  (:require [clojure.string :as s]
            [observer.apis.facebook :as facebook-api]
            [observer.apis.linkedin :as linkedin-api]
            [observer.apis.mastodon :as mastodon-api]
            [observer.apis.papercliff :as ppf-api]
            [observer.apis.reddit :as reddit-api]
            [observer.apis.tumblr :as tumblr-api]
            [observer.apis.twitter :as twitter-api]
            [observer.date-time :as dt]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn- tag-candidates [key-words]
  (let [filtered-words
        (filter
          (fn [w]
            (every? #(Character/isLetter %) w))
          key-words)]
    (concat
      filtered-words
      (for [a filtered-words
            b filtered-words
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
       (take 2)
       (map first)))

(defn -main []
  (timbre/info "starting text task")
  (doseq [clique (ppf-api/selected-cliques (dt/now))]
    (let [key-words (s/join " · " clique)
          link (str "https://papercliff.github.io/redirect/?q="
                    (s/join "+" clique))
          chosen-hashtags (chosen-tags clique)
          hashtags (->> chosen-hashtags
                        (cons "breakingnews")
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
      (reddit-api/text-post key-words link)
      (linkedin-api/text-post keywords+link+hashtags)
      (tumblr-api/text-post
        key-words
        link
        (cons "breaking news" chosen-hashtags))))
  (timbre/info "text task completed")
  (System/exit 0))
