(defproject lein-essthree "0.3.1-SNAPSHOT"
  :description "Leiningen plugin for easy S3 project deployment and dependency resolution"
  :url "http://github.com/dparis/lein-essthree"
  :license {:name "MIT License"
            :url  "http://opensource.org/licenses/MIT"}

  :min-lein-version "2.8.1"

  :dependencies [[com.amazonaws/aws-java-sdk-core "1.11.787"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.787"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [joda-time "2.9.9"]

                 [amazonica "0.3.152" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client]]
                 [org.springframework.build/aws-maven "5.0.0.RELEASE"
                  :exclusions [joda-time
                               com.amazonaws/aws-java-sdk]]
                 [commons-logging "1.2"]
                 [funcool/cuerdas "2.0.4"]
                 [digest "1.4.6"]
                 [me.raynes/fs "1.4.6"]
                 [pathetic "0.5.1"]
                 [prismatic/schema "1.1.7"]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :deploy-repositories [["clojars" {:url           "https://clojars.org/repo"
                                    :creds         :gpg
                                    :sign-releases false}]]

  :profiles {:dev {:source-paths ["dev"]
                   :repl-options {:init-ns workbench}
                   :plugins      [[lein-ancient "0.6.14"]
                                  [lein-pprint "1.2.0"]]}}

  :eval-in-leiningen true)
