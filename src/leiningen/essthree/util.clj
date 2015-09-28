(ns leiningen.essthree.util
  (:require [amazonica.aws.s3 :as s3]
            [cuerdas.core :as c]
            [leiningen.essthree.schemas
             :refer [AWSCreds EssthreeConfig]]
            [schema.core :as s]))




(s/defn format-path :- (s/maybe s/Str)
  [path :- (s/maybe s/Str)]
  (when path
    (-> path
        (c/trim "/")
        (str "/"))))
