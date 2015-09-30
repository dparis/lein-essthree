(ns leiningen.essthree.repository
  "Middleware to update project repositories to include any
  configured S3 buckets."
  (:require [cuerdas.core :as c]
            [leiningen.essthree.schemas
             :refer [RepoConfig]]
            [schema.core :as s]))


(s/defn ^:private get-config :- (s/maybe RepoConfig)
  [project]
  (get-in project [:essthree :repository]))

(s/defn ^:private build-repo-url :- s/Str
  [config         :- RepoConfig
   build-category :- (s/enum "releases" "snapshots")]
  (let [bucket (:bucket config)
        path   (:path config)
        url    (->> [bucket path build-category]
                    (filter identity)
                    (map #(c/trim % "/"))
                    (c/join "/"))]
    (str "s3://" url)))

(s/defschema ^:private Repo
  (s/pair
   (s/enum "essthree-releases" "essthree-snapshots")
   "repo-name"

   {:url s/Str
    (s/optional-key :username) s/Str
    (s/optional-key :password) s/Str}
   "repo-data"))

(s/defn ^:private build-repo :- Repo
  [config         :- RepoConfig
   build-category :- (s/enum "releases" "snapshots")]
  (let [url       (build-repo-url config build-category)
        lein-keys [:sign-releases :checksum :update]
        snapshots (= "snapshots" build-category)
        repo-data (merge {:url       url
                          :snapshots snapshots}
                         (select-keys config lein-keys))
        aws-creds (:aws-creds config)
        username  (or (:access-key-id aws-creds)
                      :env/aws_access_key_id)
        password  (or (:secret-access-key aws-creds)
                      :env/aws_secret_access_key)]
    [(str "essthree-" build-category)
     (merge repo-data
            {:username username
             :password password})]))

(defn update-repositories
  [project]
  (if-let [config (get-config project)]
    (-> project
        (update :repositories conj
                (build-repo config "snapshots"))
        (update :repositories conj
                (build-repo config "releases")))

    project))
