(defproject lein-essthree "0.1.0"
  :description "Leiningen plugin for easy S3 project deployment and dependency resolution"
  :url "http://github.com/dparis/lein-essthree"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :min-lein-version "2.0.0"

  :dependencies [[joda-time "2.8.2"]
                 [amazonica "0.3.33"]
                 [org.springframework.build/aws-maven "5.0.0.RELEASE"
                  :exclusions [joda-time]]
                 [funcool/cuerdas "0.6.0"]
                 [me.raynes/fs "1.4.6"]
                 [pandect "0.5.4"]
                 [pathetic "0.5.1"]
                 [prismatic/schema "1.0.1"]]

  :deploy-repositories [["releases" :clojars]]

  :profiles {:dev {:source-paths        ["dev"]
                   :repl-options        {:init-ns workbench}
                   :dependencies        [[org.clojure/tools.namespace "0.2.11"]]}}

  :eval-in-leiningen true)
