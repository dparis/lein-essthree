(ns leiningen.essthree.s3
  (:require [amazonica.aws.s3 :as s3]
            [amazonica.core :as ac]
            [clojure.java.io :as io]
            [leiningen.essthree.schemas
             :refer [AWSCreds]]
            [me.raynes.fs :as fs]
            [pathetic.core :as path]
            [schema.core :as s]))


(s/defn bucket-exists? :- s/Bool
  [aws-creds :- (s/maybe AWSCreds)
   bucket    :- s/Str]
  (ac/with-credential aws-creds
    (s3/does-bucket-exist bucket)))

(s/defn list-objects
  [aws-creds :- (s/maybe AWSCreds)
   bucket    :- s/Str
   path      :- (s/maybe s/Str)]
  (ac/with-credential aws-creds
    (s3/list-objects bucket path)))

(s/defn put-file!
  [aws-creds :- (s/maybe AWSCreds)
   bucket    :- s/Str
   obj-key   :- s/Str
   file-path :- s/Str]
  (ac/with-credential aws-creds
    (with-open [is (io/input-stream file-path)]
      (s3/put-object :bucket-name  bucket
                     :key          obj-key
                     :input-stream is
                     :metadata     {:content-length (fs/size file-path)}))))

(s/defn put-folder!
  [aws-creds   :- (s/maybe AWSCreds)
   bucket      :- s/Str
   folder-path :- s/Str]
  (ac/with-credential aws-creds
    (with-open [is (io/input-stream (byte-array 0))]
      (s3/put-object :bucket-name  bucket
                     :key          (path/ensure-trailing-separator folder-path)
                     :input-stream is
                     :metadata     {:content-length 0}))))

(s/defn delete-object!
  [aws-creds :- (s/maybe AWSCreds)
   bucket    :- s/Str
   obj-key   :- s/Str]
  (ac/with-credential aws-creds
    (s3/delete-object bucket obj-key)))
