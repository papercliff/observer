(ns observer.text
  (:require [clojure.string :as s]
            [taoensso.timbre :as timbre]))

(defn tweet [stories]
  (let [res (str
              (->> stories
                   (map (partial s/join " · "))
                   (s/join "\n"))
              "\n"
              "#hourly #news #keywords")]
    (timbre/info "tweet" res)
    res))
