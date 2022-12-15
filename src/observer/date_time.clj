(ns observer.date-time
  (:require [clj-time.core :as time]
            [clj-time.format :as time-f]))

(defn ->date-hour-str [dt]
  (time-f/unparse
    (time-f/formatter
      :date-hour-minute)
    dt))

(defn minutes-ago [dt minutes]
  (time/minus
    dt
    (time/minutes minutes)))

(defn now []
  (time/now))
