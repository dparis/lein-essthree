(defproject lein-essthree "0.2.3-SNAPSHOT"
  :description "Leiningen plugin for easy S3 project deployment and dependency resolution"
  :url "http://github.com/dparis/lein-essthree"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :min-lein-version "2.0.0"

  :dependencies [[joda-time "2.9.3"]
                 [amazonica "0.3.53" :exclusions [com.amazonaws/aws-java-sdk]]
                 [com.amazonaws/aws-java-sdk-core "1.10.49"]
                 [com.amazonaws/aws-java-sdk-s3 "1.10.49"]
                 [org.clojars.brabster/aws-maven-fix "0.1.0"
                  :exclusions [joda-time]]
                 [funcool/cuerdas "0.7.2"]
                 [me.raynes/fs "1.4.6"]
                 [pandect "0.5.4"]
                 [pathetic "0.5.1"]
                 [prismatic/schema "1.1.0"]]

  :deploy-repositories [["releases" :clojars]]

  :profiles {:dev {:source-paths ["dev"]
                   :repl-options {:init-ns workbench}
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]]}}

  :eval-in-leiningen true)
