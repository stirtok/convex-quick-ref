(ns stirtok.convex-ref-docs
  (:refer-clojure :exclude [ns-map])
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [scicloj.clay.v2.api :as clay]))

(def ns-list
  ["convex.core"
   "convex.fungible"
   "stirtok.coin"])

(def ns-map ; stringified Convex Lisp
  (as-> ns-list x
    (interleave (map #(str ":" %) x)
                (map #(str "@" %) x))
    (str/join " " x)
    (str "{" x "}")))

(def mk-doc-tree ; stringified Convex Lisp
  (format "(do
             (def ns-map %s)
             (defn ns-doc
               [ns-addr]
               (when (address? ns-addr)
                 (into {}
                       (for [[sym m] (:metadata (account ns-addr))]
                         [sym m]))))
             (into {}
                   (for [[ns-sym ns-addr] ns-map]
                     [ns-sym (ns-doc ns-addr)])))"
          ns-map))

(def http-body
  (format "{:address #12
            :source %s}"
          mk-doc-tree))




(def query-cvx-path "scratch/query.cvx")
(def doc-tree-json-path "scratch/doc-tree.json")
(def data-clj-path "scratch/notebooks/data.clj")
(def index-templ-path "resources/index.clj")
(def index-src-doc-path "scratch/notebooks/index.clj")



(defn write-query-cvx
  [_]
  (io/make-parents query-cvx-path)
  (spit query-cvx-path http-body)
  (System/exit 0))

(defn write-notebooks
  [_]
  (let [doc-tree (get (json/read-str (slurp doc-tree-json-path)) "value")]
    (io/make-parents data-clj-path)
    (spit data-clj-path (format "(ns data) 
                                 (def doc-tree %s)" 
                                doc-tree))
    (io/copy (io/file index-templ-path) (io/file index-src-doc-path))
    (System/exit 0)))

(defn write-html 
  [_]
  (clay/make! {:source-path index-src-doc-path
               :format [#_:quarto :html]
               :hide-info-line true
               :base-target-path "scratch/docs"
               :clean-up-target-dir true
               :show false})
  (System/exit 0))





