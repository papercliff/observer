(ns observer.text
  (:require [clojure.string :as s]
            [clojure.tools.logging :as log]
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
            [observer.markdown-templates :as md-templ]
            [observer.papercliff-data :as ppf-data]
            [observer.word-cloud :as wd-cloud])
  (:gen-class))

(defn- search-url [now clique]
  (str "https://papercliff.github.io/redirect/?q="
       (s/join "+" clique)
       "&tbs=cdr:1,cd_min:"
       (-> now dt/at-start-of-prev-day dt/->us-day-str)
       ",cd_max:"
       (-> now dt/at-start-of-next-day dt/->us-day-str)))

(defn- tag-candidates [key-words]
  (let [filtered-words
        (filter
          (fn [w]
            (every?
              (fn [chr]
                (Character/isLetter ^Character chr))
              w))
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
            (mastodon-api/hashtag-popularity %)))
       (sort-by second)
       reverse
       (take 2)
       (map first)
       (cons "news")))

(defn- go []
  (let [now (dt/now)
        date-hour-str (dt/->date-hour-str now)]

    (log/info
      "starting text task for"
      date-hour-str)

    (when-let [cliques-all (ppf-data/selected-cliques now)]

      ;; commit to news website
      (attempt/catch-all
        #(github-api/with-changeset
           "papercliff"
           "news"
           "main"
           (fn [changeset]
             (reduce
               (fn [acc clique]
                 (github-api/put-content
                   acc
                   (md-templ/text-post-path
                     now
                     clique)
                   (md-templ/text-post-content
                     now
                     clique
                     (search-url now clique))))
               changeset
               cliques-all))))

      ;; post to social media
      (doseq [clique cliques-all]
        (let [word-cloud-path (wd-cloud/create
                                (ppf-data/selected-context-keywords
                                 now
                                 clique))
              key-words (s/join " · " clique)
              link (search-url now clique)
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
          (doseq [f [#(mastodon-api/image-twoot word-cloud-path keywords+link+hashtags)
                     #(twitter-api/image-tweet word-cloud-path keywords+link+hashtags)
                     #(facebook-api/image-post word-cloud-path keywords+link+hashtags)
                     #(let [[image-url thing-id] (reddit-api/image-post word-cloud-path key-words)]
                        (reddit-api/write-comment thing-id link)
                        (insta-api/image-post image-url hashtags))
                     #(linkedin-api/image-post word-cloud-path keywords+link+hashtags)
                     #(tumblr-api/image-with-link-post word-cloud-path key-words link chosen-hashtags)]]
            (attempt/catch-all f)))))

    (log/info
      "text task completed for"
      date-hour-str)))

(defn -main []
  (attempt/catch-all go)
  (System/exit 0))
