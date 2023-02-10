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
       (map first)
       (cons "breakingnews")))

(defn -main []
  (timbre/info "starting text task")
  (when-let [clusters-with-cliques
             (ppf-api/clusters-with-cliques
               (dt/now))]
    (doseq [[cluster clique] clusters-with-cliques]
      (let [key-words (s/join " · " cluster)
            link (str "https://papercliff.github.io/redirector/?q="
                      (s/join "+" clique))
            hashtags (->> clique
                          chosen-tags
                          (map #(str "#" %))
                          (s/join " "))
            keywords+hashtags+link (str
                                     key-words
                                     "\n"
                                     hashtags
                                     "\n"
                                     link)]
        (mastodon-api/text-twoot keywords+hashtags+link)
        (twitter-api/text-tweet keywords+hashtags+link)
        (facebook-api/text-post keywords+hashtags+link)
        (reddit-api/text-post key-words link)
        (linkedin-api/text-post keywords+hashtags+link))))
  (timbre/info "text task completed"))
