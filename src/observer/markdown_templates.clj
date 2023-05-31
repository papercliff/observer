(ns observer.markdown-templates
  (:require [clojure.string :as s]
            [observer.date-time :as dt]))

(defn text-post-content
  [now tags search-url]
  (format
    "---
layout: post
title: %s
date: %s
categories: [%s]
content_url: %s
---
"
    (s/join " · " tags)
    (dt/->day-str now)
    (s/join ", " tags)
    search-url))

(defn text-post-path
  [now tags]
  (format
    "all_collections/_posts/%s-%s.md")
  (dt/->day-str now)
  (s/join "-" tags))
