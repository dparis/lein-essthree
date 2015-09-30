(ns repl
  (:require [clojure.tools.namespace.repl :as nr]))

(defn reset
  []
  (nr/set-refresh-dirs "dev/" "src/")
  (nr/refresh))
