(ns lein-essthree.schemas
  "Schemas used by lein-essthree."
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
         {:type (s/enum :directory :library :uberjar)}))

(s/defschema DirectoryDeployConfig
  (merge BaseDeployConfig
         {:local-root s/Str}))

(s/defschema LibraryDeployConfig
  (merge BaseDeployConfig
         {(s/optional-key :snapshots)     s/Bool
          (s/optional-key :sign-releases) s/Bool
          (s/optional-key :checksum)      (s/enum :fail :warn :ignore)
          (s/optional-key :update)        (s/enum :daily :always :never)}))

(s/defschema UberjarDeployConfig
  (merge BaseDeployConfig
         {(s/optional-key :artifact-name) s/Str}))

(s/defschema ^:private DeployConfig
  (s/conditional
   #(= :directory (:type %)) DirectoryDeployConfig
   #(= :library (:type %))   LibraryDeployConfig
   #(= :uberjar (:type %))   UberjarDeployConfig
   'valid-deploy-config?))

(s/defschema RepoConfig
  (merge BaseS3Config
         {(s/optional-key :snapshots)     s/Bool
          (s/optional-key :sign-releases) s/Bool
          (s/optional-key :checksum)      (s/enum :fail :warn :ignore)
          (s/optional-key :update)        (s/enum :daily :always :never)}))

(s/defschema EssthreeConfig
  {(s/optional-key :deploy)     DeployConfig
   (s/optional-key :repository) RepoConfig})
