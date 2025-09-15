(ns stirtok.convex-quick-ref
  (:refer-clojure :exclude [ns-map ns])
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))


;; -- write-query-cvx ----------------------------------------------

(def ns-list
  ["convex.core"
   "convex.metadata"
   "convex.registry"
   "convex.trust"
   "convex.asset"
   "convex.fungible"
   "asset.nft.basic"
   "asset.nft.simple"
   "asset.box"
   "asset.box.actor"
   "asset.multi-token"
   "asset.wrap.convex"
   "convex.trust.delegate"
   ;; TODO: More trust.*
   "torus.exchange"])

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

(defn build-query-cvx
  [_]
  (io/make-parents query-cvx-path)
  (spit query-cvx-path http-body)
  (System/exit 0))


;; -- write-adoc ----------------------------------------------


(def doc-tree-json-path "scratch/doc-tree.json")
(def templ-path "resources/template.adoc")
(def adoc-path "scratch/index.adoc")

(defn esc
  [s]
  (if (string? s)
    (str/replace s #"(\*[^*]+\*)" "pass:[$1]")
    s))

(defn build-adoc
  [_]
  (io/make-parents adoc-path)
  (io/copy (io/file templ-path) (io/file adoc-path))
  (with-open [w (io/writer (io/file adoc-path) :append true)]
    (let [doc-tree (get (json/read-str (slurp doc-tree-json-path)) "value")]
      ;; Per namespace
      (doseq [[ns ns-detail] (sort doc-tree)]
        (.write w (str "== " ns "\n\n"))
        ;; Per symbol
        (doseq [[sym sym-detail] (sort ns-detail)]
          (.write w (str "'''\n\n=== `" (esc sym) "`\n\n"))
          (let [doc (get sym-detail "doc")]
            ;; Description
            (let [description (get doc "description")
                  description (if (string? description) 
                                description
                                (str/join " +\n" description))]
              (.write w (str (esc description) "\n\n")))
            ;; Signature
            (let [signature (get doc "signature")]
              (when (seq signature)
                (.write w (str "===== Signature\n\n"))
                ;; Per signature variation
                (doseq [sig-variation signature]
                  (let [params (get sig-variation "params")
                        return (get sig-variation "return")]
                    (when (some? params) 
                      (.write w (str "Params: `[" (str/join " " params) "]`")))
                    (when (some? return)
                      (.write w (str " Return: `" (str/join " " params) "`")))
                    (.write w "\n\n")))))
            ;; Signature
            (let [errors (get doc "errors")]
              (when (seq errors)
                (.write w (str "===== Errors\n\n"))
                (doseq [[err-code err-desc] errors]
                  (.write w (str "`" err-code "`: " (esc err-desc) "\n\n")))))
            ;; Examples
            (let [examples (get doc "examples")]
              (when (seq examples)
                (.write w (str "===== Examples\n\n"))
                ;; Per example
                (doseq [example examples]
                  (.write w (str "[source,clojure]\n----\n" (get example "code") "\n----\n\n"))))))))))
  (System/exit 0))









