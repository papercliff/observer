(ns observer.date-time
  (:require [clj-time.core :as time]
            [clj-time.format :as time-f]
            [clojure.string :as s]))

(defn- unparse [dt fmt]
  (time-f/unparse
    (time-f/formatter fmt)
    dt))

(defn ->date-hour-str [dt]
  (unparse dt :date-hour-minute))

(defn ->day-str [dt]
  (unparse dt :date))

(defn ->hyphened-date-with-ms [dt]
  (-> dt
      (unparse :date-hour-minute-second-ms)
      (s/replace #"\D" "-")))

(defn ->full-day-str [dt]
  (unparse dt "EEEEE, MMMMM d, yyyy"))

(defn ->us-day-str [dt]
  (unparse dt "M/d/yyyy"))

(defn minutes-ago [dt hours]
  (time/minus
    dt
    (time/minutes hours)))

(defn at-start-of-prev-day [dt]
  (->> 1
       time/days
       (time/minus dt)
       time/with-time-at-start-of-day))

(defn at-start-of-next-day [dt]
  (->> 1
       time/days
       (time/plus dt)
       time/with-time-at-start-of-day))

(defn now []
  (time/now))
