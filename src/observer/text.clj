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
       (map first)
       (cons "breakingnews")))

(defn -main []
  (timbre/info "starting text task")
  (when-let [clusters-with-cliques
             (ppf-api/clusters-with-cliques
               (dt/now))]
    (doseq [[cluster clique] clusters-with-cliques]
      (let [key-words (s/join " · " cluster)
            link (str "https://papercliff.github.io/redirect/?q="
                      (s/join "+" cluster))
            chosen-hashtags (chosen-tags clique)
            hashtags (->> chosen-hashtags
                          (map #(str "#" %))
                          (s/join " "))
            keywords+link+hashtags (str
                                     key-words
                                     "\n"
                                     link
                                     "\n"
                                     hashtags)]
        (try
          (mastodon-api/text-twoot keywords+link+hashtags)
          (catch Exception e
            (timbre/error "caught exception" (.getMessage e))))
        (try
          (twitter-api/text-tweet keywords+link+hashtags)
          (catch Exception e
            (timbre/error "caught exception" (.getMessage e))))
        (try
          (facebook-api/text-post keywords+link+hashtags)
          (catch Exception e
            (timbre/error "caught exception" (.getMessage e))))
        (try
          (reddit-api/text-post key-words link)
          (catch Exception e
            (timbre/error "caught exception" (.getMessage e))))
        (try
          (linkedin-api/text-post keywords+link+hashtags)
          (catch Exception e
            (timbre/error "caught exception" (.getMessage e))))
        (try
          (tumblr-api/text-post key-words link chosen-hashtags)
          (catch Exception e
            (timbre/error "caught exception" (.getMessage e)))))))
  (timbre/info "text task completed"))
