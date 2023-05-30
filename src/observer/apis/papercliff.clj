(ns observer.apis.papercliff
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.set :as st]
            [clojure.string :as s]
            [environ.core :as env]
            [loom.graph :as loom]
            [loom.alg :as loom-alg]
            [observer.attempt :as attempt]
            [observer.date-time :as dt]
            [taoensso.timbre :as timbre]))

(defn- combinations [query-params]
  (timbre/info "getting combinations" query-params)
  (attempt/retry
    #(let [res (-> (env/env :papercliff-combinations-url)
                   (client/get
                     {:content-type :json
                      :headers {(env/env :papercliff-secret-key)
                                (env/env :papercliff-secret-value)}
                      :query-params query-params})
                   :body
                   (json/read-str :key-fn keyword))]
       (timbre/info "combinations" res)
       res)))

(defn- combinations-since [now]
  (let [from (dt/->date-hour-str
               (dt/minutes-ago
                 now
                 (* 24.25 60)))
        to (dt/->date-hour-str
             (dt/minutes-ago
               now
               (* 0.25 60)))]
    (->> 100
         (iterate
           (partial + 100))
         (map
           (fn [offset]
             {:from from
              :to to
              :offset offset}))
         (cons
           {:from from
            :to to})
         (mapcat combinations)
         (take-while
           (fn [{:keys [agencies]}]
             (> agencies 2))))))

(defn- combinations-until
  [combinations-memo now terms]
  (combinations-memo
    {:to (dt/->date-hour-str
           (dt/minutes-ago
             now
             (* 1.25 60)))
     :terms (s/join "-" terms)}))

(defn- story->term-pairs [story]
  (let [[a b c] (s/split story #"-")]
    [[a b]
     [a c]
     [b c]]))

(defn- loom-graph [now]
  (let [combinations-memo
        (memoize combinations)]
    (->> now
         combinations-since
         (map :story)
         (filter
           (fn [story]
             (->> story
                  story->term-pairs
                  (mapcat
                    (fn [terms]
                      (combinations-until
                        combinations-memo
                        now
                        terms)))
                  (every?
                    (fn [{:keys [agencies]}]
                      (< agencies 3))))))
         (mapcat story->term-pairs)
         (apply loom/graph))))

(defn selected-cliques [now]
  (let [graph (loom-graph now)
        components (->> graph
                        loom-alg/connected-components
                        (map set))
        _ (timbre/info "reaching components" components)
        cliques (->> graph
                     loom-alg/maximal-cliques
                     (filter
                       (fn [clq]
                         (> (count clq) 3)))
                     (sort-by count)
                     reverse
                     (map set))
        _ (timbre/info "forming cliques" cliques)
        res (->> components
                 (mapcat
                   (fn [comp-set]
                     (if-let [cl (->> cliques
                                      (filter
                                        (fn [clq]
                                          (st/subset? clq comp-set)))
                                      first)]
                       [cl]
                       [])))
                 (map sort))]
    (timbre/info "selecting cliques" res)
    (seq res)))
