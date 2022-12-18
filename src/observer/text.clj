(ns observer.text
  (:require [clojure.string :as s]))

(defn post [words]
  (str
    (s/join " · " words)
    "\n"
    "https://news.google.com/search?q="
    (s/join "+" words)))
