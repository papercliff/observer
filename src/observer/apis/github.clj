(ns observer.apis.github
  (:require [clj-github.changeset :as github-change]
            [clj-github.httpkit-client :as github-client]
            [clj-github.repository :as github-repo]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [environ.core :as env]
            [me.raynes.fs :as raynes]
            [observer.attempt :as attempt]
            [observer.date-time :as dt]
            [observer.fs :as fs]))

(def put-content
  "[changeset path content]

  Returns a new changeset with the file under path with new content.
  `content` can be a string or a byte-array.
  It creates a new file if it does not exist yet."
  github-change/put-content)

(def single-day-actions-fmt
  "https://raw.githubusercontent.com/papercliff/historical-data/master/transformed/%s-single-day-actions.json")

(def gh-token-map
  {:token (env/env :github-token)})

(defn load-single-day-actions [now]
  (let [url (->> now
                 dt/at-start-of-prev-day
                 dt/->day-str
                 (format single-day-actions-fmt))]
    (log/info "loading" url)
    (attempt/retry
      #(-> url client/get :body))))

(defn clone-graph-vis []
  (log/info "cloning github repo")
  (attempt/retry
    #(-> gh-token-map
         github-client/new-client
         (github-repo/clone "papercliff" "graph-vis")
         (raynes/copy-dir fs/res-dir-path))))

(defn with-changeset [org repo branch f]
  (attempt/retry
    (fn []
      (log/infof
        "committing github changes to %s/%s/%s"
        org repo branch)
      (-> gh-token-map
          github-client/new-client
          (github-change/from-branch!
            org
            repo
            branch)
          f
          (github-change/commit!
            "Auto-commit from papercliff observer")
          (github-change/update-branch!)))))

(defn put-content-once
  [org repo branch path content]
  (with-changeset
    org repo branch
    (fn [changeset]
      (put-content
        changeset
        path
        content))))
