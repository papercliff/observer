(ns observer.apis.papercliff
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.set :as st]
            [clojure.string :as s]
            [environ.core :as env]
            [loom.graph :as loom]
            [loom.alg :as loom-alg]
            [observer.date-time :as dt]
            [taoensso.timbre :as timbre]))

(defn- combinations [query-params]
  (timbre/info "getting combinations" query-params)
  (Thread/sleep 5000)
  (let [res (-> "https://papercliff.p.rapidapi.com/combinations"
                (client/get
                  {:content-type :json
                   :headers {"X-RapidAPI-Key"
                             (env/env :x-rapidapi-key)

                             "X-RapidAPI-Host"
                             "papercliff.p.rapidapi.com"}
                   :query-params query-params})
                :body
                (json/read-str :key-fn keyword))]
    (timbre/info "combinations" res)
    res))

(def combinations-memo
  (memoize combinations))

(defn- combinations-since [now]
  (combinations-memo
    {:from (dt/->date-hour-str
             (dt/minutes-ago
               now
               (* 2.5 60)))}))

(defn- combinations-until [now terms]
  (combinations-memo
    {:to (dt/->date-hour-str
           (dt/minutes-ago
             now
             60))
     :terms (s/join "-" terms)}))

(defn- story->term-pairs [story]
  (let [[a b c] (s/split story #"-")]
    [[a b]
     [a c]
     [b c]]))

(defn- loom-graph [now]
  (->> now
       combinations-since
       (filter
         (fn [{:keys [agencies]}]
           (>= agencies 3)))
       (map :story)
       (filter
         (fn [story]
           (->> story
                story->term-pairs
                (mapcat #(combinations-until now %))
                (every?
                  (fn [{:keys [agencies]}]
                    (< agencies 3))))))
       (mapcat story->term-pairs)
       (apply loom/graph)))

(defn- components [graph]
  (let [connected-components
        (loom-alg/connected-components graph)

        res
        (->> connected-components
             (filter #(> (count %) 3))
             (map sort)
             (sort-by #(vector (/ 1 (count %)) (s/join " " %)))
             (map set))]
    (timbre/info "selecting components from" connected-components)
    (timbre/info "reaching components" res)
    res))

(defn- cliques [graph]
  (let [res (->> graph
                 loom-alg/maximal-cliques
                 (sort-by count)
                 reverse)]
    (timbre/info "forming cliques" res)
    res))

(defn clusters-with-cliques [now]
  (let [graph (loom-graph now)
        selected-components (components graph)
        sorted-cliques (cliques graph)
        res (map
              (fn [component]
                [component
                 (->> sorted-cliques
                      (filter #(st/subset? % component))
                      first)])
              selected-components)]
    (timbre/info "reaching cluster-clique pairs" res)
    (seq res)))
