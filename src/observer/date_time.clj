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

(defn- at-start-of-prev-day [dt]
  (->> 1
       time/days
       (time/minus dt)
       time/with-time-at-start-of-day))

(defn ->start-of-prev-day-str [dt]
  (time-f/unparse
    (time-f/formatter :date)
    (at-start-of-prev-day dt)))

(defn now []
  (time/now))
