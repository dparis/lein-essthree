(ns lein-essthree.plugin
  (:require [lein-essthree.repository :as er]))


(defn middleware
  [project]
  (er/update-repositories project))
