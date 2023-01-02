(ns observer.text
  (:require [clojure.string :as s]))

(defn post [words]
  (str
    (s/join " · " words)
    "\n"
    "https://google.com/search?q="
    (s/join "+" words)))
