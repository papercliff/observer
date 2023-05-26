(ns observer.apis.github
  (:require [clj-github.changeset :as github-change]
            [clj-github.httpkit-client :as github-client]
            [clj-github.repository :as github-repo]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :as env]
            [me.raynes.fs :as raynes]
            [observer.attempt :as attempt]
            [observer.date-time :as dt]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre]))

(def single-day-actions-fmt
  "https://raw.githubusercontent.com/papercliff/historical-data/master/transformed/%s-single-day-actions.json")

(def gh-token-map
  {:token (env/env :github-token)})

(defn load-single-day-actions [now]
  (let [url (->> now
                 dt/at-start-of-prev-day
                 dt/->day-str
                 (format single-day-actions-fmt))]
    (timbre/info "loading" url)
    (attempt/retry
      #(-> url client/get :body))))

(defn clone-animated-graph []
  (timbre/info "cloning github repo")
  (attempt/retry
    #(-> gh-token-map
         github-client/new-client
         (github-repo/clone "papercliff" "animated-graph")
         (raynes/copy-dir fs/res-dir-path))))

(defn save-content
  [org repo branch path content commit-msg]
  (timbre/infof "saving github contents to %s/%s/%s/%s" org repo branch path)
  (attempt/retry
    #(-> gh-token-map
         github-client/new-client
         (github-change/from-branch! org repo branch)
         (github-change/put-content
           path
           (json/write-str
             content
             :indent true))
         (github-change/commit! commit-msg)
         (github-change/update-branch!))))
