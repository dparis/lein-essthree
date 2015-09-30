(ns leiningen.essthree.library
  "Deploy project as a library JAR to S3 using the Spring framework
  AWS maven library."
  (:require [cuerdas.core :as c]
            [leiningen.deploy :as ld]
            [leiningen.essthree.schemas
             :refer [LibraryDeployConfig]]
            [leiningen.pom :as pom]
            [schema.core :as s]))


(s/defn ^:private get-config :- LibraryDeployConfig
  [project]
  (get-in project [:essthree :deploy]))

(s/defn ^:private build-repo-url :- s/Str
  [config         :- LibraryDeployConfig
   build-category :- (s/enum "releases" "snapshots")]
  (let [bucket (:bucket config)
        path   (:path config)
        url    (->> [bucket path build-category]
                    (filter identity)
                    (map #(c/trim % "/"))
                    (c/join "/"))]
    (str "s3://" url)))

(s/defschema ^:private DeployRepo
  (s/pair
   (s/eq "essthree")
   "repo-name"

   {:url s/Str
    (s/optional-key :username) s/Str
    (s/optional-key :password) s/Str}
   "repo-data"))

(s/defn ^:private build-deploy-repo :- DeployRepo
  [config         :- LibraryDeployConfig
   build-category :- (s/enum "releases" "snapshots")]
  (let [url       (build-repo-url config build-category)
        lein-keys [:shapshots :sign-releases :checksum :update]
        repo-data (merge {:url url} (select-keys config lein-keys))
        aws-creds (:aws-creds config)
        username  (or (:access-key-id aws-creds)
                      :env/aws_access_key_id)
        password  (or (:secret-access-key aws-creds)
                      :env/aws_secret_access_key)]
    ["essthree" (merge repo-data
                       {:username username
                        :password password})]))

(defn deploy-library
  "Deploy the current project as a library JAR to S3. Releases and snapshots
  are handled similarly to `lein deploy`. Libraries deployed in this way can be
  consumed by including essthree as a project plugin and specifying a
  :repository configuration."
  [project]
  (let [config          (get-config project)
        build-category  (if (pom/snapshot? project) "snapshots" "releases")
        deploy-repo     (build-deploy-repo config build-category)
        updated-project (update project :deploy-repositories
                                conj deploy-repo)]
    (ld/deploy updated-project (first deploy-repo))))
