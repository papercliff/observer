(ns observer.attempt
  (:require [taoensso.timbre :as timbre]))

(def sleep-time
  5000)

(def exhaust-msg
  "retry exhausted")

(def max-attempts
  3)

(defn sleep-and-execute [f]
  (timbre/infof
    "sleeping for about %s milliseconds"
    sleep-time)
  (Thread/sleep
    (+ sleep-time
       (rand 3000)))
  (f))

(defn- retry-go [f i]
  (if (<= i max-attempts)
    (try
      (sleep-and-execute f)
      (catch Exception e
        (timbre/warnf
          "attempt %s failed with message %s"
          i
          (.getMessage e))
        (retry-go
          f
          (inc i))))
    (do (timbre/error exhaust-msg)
        (throw (Exception. ^String exhaust-msg)))))

(defn retry [f]
  (retry-go f 1))

(defn catch-all [f]
  (try
    (f)
    (catch Exception e
      (timbre/error
        (.getMessage e)
        "occurred and suppressed"))))
