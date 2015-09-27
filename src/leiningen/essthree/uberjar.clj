(ns leiningen.essthree.uberjar
  (:require [amazonica.aws.s3 :as s3]
            [clojure.java.io :as io]
            [cuerdas.core :as c]
            [leiningen.essthree.schemas :refer [UberjarDeployConfig]]
            [leiningen.core.main :as main]
            [leiningen.pom :as pom]
            [leiningen.uberjar :as uj]
            [me.raynes.fs :as fs]
            [schema.core :as s])
  (:import [com.amazonaws AmazonServiceException]))


(s/defn ^:private get-config :- UberjarDeployConfig
  [project]
  (get-in project [:essthree :deploy]))

(s/defn ^:private compile-uberjar! :- s/Str
  [project]
  (uj/uberjar project))

(s/defn ^:private put-uberjar-s3! :- (s/maybe s/Str)
  [config         :- UberjarDeployConfig
   build-category :- (s/enum "snapshots" "releases")
   project-name   :- s/Str
   uj-path        :- s/Str]
  (let [aws-creds    (:aws-creds config)
        bucket       (:bucket config)
        path         (c/trim (:path config) "/")
        uj-artifact  (or (:artifact-name config)
                         (last (c/split uj-path "/")))
        obj-key      (->> [path project-name build-category uj-artifact]
                          (filter identity)
                          (c/join "/"))
        uj-size      (fs/size uj-path)
        obj-metadata {:content-length uj-size}]
    (try
      (with-open [uj-inputstream (io/input-stream uj-path)]
        (if-not (empty? aws-creds)
          (s3/put-object aws-creds
                         :bucket-name  bucket
                         :key          obj-key
                         :input-stream uj-inputstream
                         :metadata     obj-metadata)
          (s3/put-object :bucket-name  bucket
                         :key          obj-key
                         :input-stream uj-inputstream
                         :metadata     obj-metadata)))
      (str bucket "/" obj-key)
      (catch AmazonServiceException e
        (main/abort "Uberjar upload to S3 failed with:"
                    (:message (amazonica.core/ex->map e)))))))

(defn deploy-uberjar
  [project]
  (let [config         (get-config project)
        build-category (if (pom/snapshot? project) "snapshots" "releases")
        project-name   (:name project)
        uj-path        (compile-uberjar! project)
        uj-obj         (put-uberjar-s3! config
                                        build-category
                                        project-name
                                        uj-path)]

    (when uj-obj
      (main/info "Uploaded uberjar to" uj-obj))))
