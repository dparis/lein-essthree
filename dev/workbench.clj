(ns workbench
  (:require [lein-essthree.directory :as ed]
            [lein-essthree.library :as el]
            [lein-essthree.uberjar :as eu]
            [leiningen.core.project :as lp]
            [leiningen.essthree :as ess3]
            [repl]
            [schema.core :as s]))


(s/set-fn-validation! true)

(def project (lp/read))

(def directory-config
  {:essthree {:deploy {:type       :directory
                       :bucket     "essthree.directory-test"
                       :local-root "essthree-local/"
                       ;; :path       "test/path/"
                       ;; :aws-creds  {:access-key-id     "access-key"
                       ;;              :secret-access-key "secret-key"}
                       }}})

(def library-config
  {:essthree {:deploy {:type          :library
                       :bucket        "essthree.library-test"
                       ;; :snapshots     true
                       ;; :sign-releases true
                       ;; :checksum      :fail
                       ;; :update        :always
                       ;; :path          "test/path/"
                       ;; :aws-creds     {:access-key-id     "access-key"
                       ;;                 :secret-access-key "secret-key"}
                       }}})

(def uberjar-config
  {:essthree {:deploy {:type          :uberjar
                       :bucket        "essthree.uberjar-test"
                       ;; :path          "test/path/"
                       ;; :aws-creds     {:access-key-id     "access-key"
                       ;;                 :secret-access-key "secret-key"}
                       ;; :artifact-name "essthree-test.jar"
                       }}})

(def repo-config
  {:essthree {:repository {:bucket        "essthree.repository-test"
                           ;; :snapshots     true
                           ;; :sign-releases true
                           ;; :checksum      :fail
                           ;; :update        :always
                           ;; :path          "test/path/"
                           ;; :aws-creds     {:access-key-id     "access-key"
                           ;;                 :secret-access-key "secret-key"}
                           }}})
