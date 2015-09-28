(ns leiningen.essthree
  (:require [leiningen.essthree.directory :as ed]
            [leiningen.essthree.library :as el]
            [leiningen.essthree.uberjar :as eu]))

(defn essthree
  [project & args]
  (when-let [deploy-type (get-in project [:essthree :deploy :type])]
    (case deploy-type
      :directory (ed/deploy-directory project)
      :library   (el/deploy-library project)
      :uberjar   (eu/deploy-uberjar project))))
