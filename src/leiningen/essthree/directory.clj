(ns leiningen.essthree.directory
  "Deploy a local directory to S3."
  (:require [clojure.set :as c-set]
            [cuerdas.core :as c]
            [leiningen.core.main :as main]
            [leiningen.essthree.s3 :as s3]
            [leiningen.essthree.schemas
             :refer [DirectoryDeployConfig]]
            [me.raynes.fs :as fs]
            [pandect.core :as pd]
            [pathetic.core :as pth]
            [schema.core :as s]))


(s/defn ^:private get-config :- DirectoryDeployConfig
  [project]
  (get-in project [:essthree :deploy]))

(s/defschema ^:private Details
  {s/Str {:type                 (s/enum :dir :file)
          (s/optional-key :md5) s/Str}})

(s/defn ^:private s3-obj-dir? :- s/Bool
  [s3-obj]
  (and (c/ends-with? (:key s3-obj) "/")
       (= 0 (:size s3-obj))))

(s/defn ^:private bucket-object-details :- Details
  [config :- DirectoryDeployConfig]
  (let [aws-creds (:aws-creds config)
        bucket    (:bucket config)
        path      (-> (:path config)
                      (c/trim "/")
                      (pth/ensure-trailing-separator))
        objects   (s3/list-objects aws-creds bucket path)]
    (into {}
     (for [obj (:object-summaries objects)
           :let  [obj-key (c/strip-prefix (:key obj) path)
                  md5     (:etag obj)
                  data    (if (s3-obj-dir? obj)
                            {:type :dir}
                            {:type :file
                             :md5  md5})]
           :when (not (empty? obj-key))]
       [obj-key data]))))

(s/defn ^:private fs-obj->detail :- Details
  [local-root root dirs files]
  (let [rel-path (if (pth/absolute-path? local-root)
                   (pth/relativize local-root root)
                   (pth/relativize (str fs/*cwd* "/" local-root) root))]
    (merge
     (into {} (for [dir dirs
                    :let [path (pth/ensure-trailing-separator
                                (pth/normalize (str rel-path "/" dir)))]]
                [path {:type :dir}]))
     (into {} (for [file files
                    :let [path (pth/normalize (str rel-path "/" file))]]
                [path {:md5  (pd/md5-file (str root "/" file))
                       :type :file}])))))

(s/defn ^:private file-object-details :- Details
  [config :- DirectoryDeployConfig]
  (let [local-root (:local-root config)
        walk-fn    (partial fs-obj->detail local-root)]
    (apply merge
     (fs/walk walk-fn local-root))))

(s/defn ^:private sync-locals!
  [config        :- DirectoryDeployConfig
   local-details :- Details
   s3-details    :- Details]
  (let [aws-creds     (:aws-creds config)
        bucket        (:bucket config)
        path          (:path config)
        local-root    (:local-root config)]
    (doseq [[rel-path data] local-details
            :when (not (contains? s3-details rel-path))
            :let [obj-key   (if path (str path "/" rel-path) rel-path)
                  file-path (str local-root "/" rel-path)]]
      (case (:type data)
        :file (s3/put-file! aws-creds bucket obj-key file-path)
        :dir  (s3/put-folder! aws-creds bucket obj-key)))))

(s/defn ^:private sync-s3!
  [config        :- DirectoryDeployConfig
   local-details :- Details
   s3-details    :- Details]
  (let [aws-creds (:aws-creds config)
        bucket    (:bucket config)
        path      (:path config)]
    (doseq [[rel-path data] s3-details
            :when (not (contains? local-details rel-path))
            :let [obj-key (if path (str path "/" rel-path) rel-path)]]
      (s3/delete-object! aws-creds bucket obj-key))))

(s/defn ^:private sync-mutual-changes!
  [config        :- DirectoryDeployConfig
   local-details :- Details
   s3-details    :- Details]
  (let [aws-creds   (:aws-creds config)
        bucket      (:bucket config)
        path        (:path config)
        local-root  (:local-root config)
        mutual-keys (c-set/intersection (set (keys local-details))
                                        (set (keys s3-details)))]
    (doseq [mutual-key mutual-keys
            :let [local-data (get local-details mutual-key)
                  s3-data    (get s3-details mutual-key)
                  obj-key    (if path (str path "/" mutual-key) mutual-key)
                  file-path  (str local-root "/" mutual-key)]
            :when (and (= :file (:type local-data))
                       (not= (:md5 local-data) (:md5 s3-data)))]
      (s3/put-file! aws-creds bucket obj-key file-path))))

(defn deploy-directory
  "Deploy a local directory to S3. Will attempt to synchronize the local
  directory to S3 using the fewest number of API calls possible. All local
  additions will be added to S3, all files on S3 without a corresponding
  local file will be deleted, and only files with differing checksums will
  be updated."
  [project]
  (let [config        (get-config project)
        local-details (file-object-details config)
        s3-details    (bucket-object-details config)]
    (sync-locals! config local-details s3-details)
    (sync-s3! config local-details s3-details)
    (sync-mutual-changes! config local-details s3-details)
    (main/info "Deployed directory to S3")))
