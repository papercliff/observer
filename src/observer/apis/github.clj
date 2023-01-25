(ns observer.apis.github
  (:require [clj-github.httpkit-client :as github-client]
            [clj-github.repository :as github-repo]
            [clj-http.client :as client]
            [environ.core :as env]
            [me.raynes.fs :as raynes]
            [observer.date-time :as dt]
            [observer.fs :as fs]
            [taoensso.timbre :as timbre]))

(def single-day-actions-fmt
  "https://raw.githubusercontent.com/papercliff/historical-data/master/transformed/%s-single-day-actions.json")

(defn load-single-day-actions [now]
  (let [url (format
              single-day-actions-fmt
              (dt/->start-of-prev-day-str now))]
    (timbre/info "loading" url)
    (-> url client/get :body)))

(defn clone-animated-graph []
  (timbre/info "cloning github repo")
  (-> {:token (env/env :github-token)}
      github-client/new-client
      (github-repo/clone "papercliff" "animated-graph")
      (raynes/copy-dir fs/res-dir-path)))
