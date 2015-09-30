(ns lein-essthree.uberjar
  "Deploy project as an application uberjar to S3."
  (:require [cuerdas.core :as c]
            [leiningen.core.main :as main]
            [lein-essthree.s3 :as s3]
            [lein-essthree.schemas :refer [UberjarDeployConfig]]
            [leiningen.uberjar :as uj]
            [schema.core :as s])
  (:import [com.amazonaws AmazonServiceException]))


(s/defn ^:private get-config :- UberjarDeployConfig
  [project]
  (get-in project [:essthree :deploy]))

(s/defn ^:private compile-uberjar! :- s/Str
  [project]
  (uj/uberjar project))

(s/defn ^:private put-uberjar-s3! :- (s/maybe s/Str)
  [config  :- UberjarDeployConfig
   uj-path :- s/Str]
  (let [aws-creds    (:aws-creds config)
        bucket       (:bucket config)
        path         (c/trim (:path config) "/")
        uj-artifact  (or (:artifact-name config)
                         (last (c/split uj-path "/")))
        obj-key      (->> [path uj-artifact]
                          (filter identity)
                          (c/join "/"))]
    (try
      (s3/put-file! aws-creds bucket obj-key uj-path)
      (str bucket "/" obj-key)
      (catch AmazonServiceException e
        (main/abort "Uberjar upload to S3 failed with:"
                    (:message (amazonica.core/ex->map e)))))))

(defn deploy-uberjar
  [project]
  "Deploy the current project as an application uberjar to S3."
  (let [config  (get-config project)
        uj-path (compile-uberjar! project)
        uj-obj  (put-uberjar-s3! config build-category uj-path)]
    (when uj-obj
      (main/info "Uploaded uberjar to" uj-obj))))
