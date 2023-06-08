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
    "all_collections/_posts/%s-%s.md"
    (dt/->hyphened-date-with-ms now)
    (s/join "-" tags)))

(defn- yesterday-str [now]
  (-> now
      dt/at-start-of-prev-day
      dt/->day-str))

(defn png-image-path [now]
  (format
    "assets/images/%s-daily-keywords.png"
    (yesterday-str now)))

(defn image-post-content [now]
  (format
    "---
layout: post
title: daily keywords
date: %s
content_url: %s
is_image: true
---
"
    (yesterday-str now)
    (png-image-path now)))

(defn image-post-path [now]
  (format
    "all_collections/_posts/%s-daily-keywords.md"
    (yesterday-str now)))
