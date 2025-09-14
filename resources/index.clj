
;; # Quick ref 

;; This is the index file

^{:kindly/options {:kinds-that-hide-code #{:kind/md
                                           :kind/hiccup}}}
^:kindly/hide-code
(ns index
  (:refer-clojure :exclude [ns])
  (:require [scicloj.kindly.v4.kind :as kind]
            [data :refer [doc-tree]]))

;; ## Namespaces
(kind/hiccup 
  [:ul
   (for [ns (keys doc-tree)]
     [:li [:pre ns]]
     )
   ])
