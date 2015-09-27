(ns leiningen.essthree.schemas
  (:require [schema.core :as s]))


(s/defschema AWSCreds
  {:access-key-id     s/Str
   :secret-access-key s/Str})

(s/defschema ^:private BaseS3Config
  {:bucket                     s/Str

   (s/optional-key :aws-creds) AWSCreds
   (s/optional-key :path)      s/Str})

(s/defschema ^:private BaseDeployConfig
  (merge BaseS3Config
         {:type (s/enum :uberjar :library :directory)}))

(s/defschema UberjarDeployConfig
  (merge BaseDeployConfig
         {(s/optional-key :artifact-name) s/Str}))

(s/defschema LibraryDeployConfig
  (merge BaseDeployConfig
         {(s/optional-key :snapshots)     s/Bool
          (s/optional-key :sign-releases) s/Bool
          (s/optional-key :checksum)      (s/enum :fail :warn :ignore)
          (s/optional-key :update)        (s/enum :daily :always :never)}))

(s/defschema DirectoryDeployConfig
  (merge BaseDeployConfig
         {:local-root s/Str}))

(s/defschema ^:private DeployConfig
  (s/conditional
   #(= :uberjar (:type %))   UberjarDeployConfig
   #(= :library (:type %))   LibraryDeployConfig
   #(= :directory (:type %)) DirectoryDeployConfig
   'valid-deploy-config?))

(s/defschema RepoConfig
  BaseS3Config)

(s/defschema EssthreeConfig
  {(s/optional-key :deploy)     DeployConfig
   (s/optional-key :repository) RepoConfig})
