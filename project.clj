(defproject lein-essthree "0.1.0-SNAPSHOT"
  :description "Leiningen plugin for easy S3 project deployment"
  :url "http://github.com/dparis/lein-essthree"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}

  :min-lein-version "2.0.0"

  :dependencies [[amazonica "0.3.33"]
                 [org.springframework.build/aws-maven "5.0.0.RELEASE"
                  :exclusions [joda-time]]
                 [funcool/cuerdas "0.6.0"]
                 [me.raynes/fs "1.4.6"]
                 [joda-time "2.8.2"]
                 [pandect "0.5.4"]
                 [pathetic "0.5.1"]
                 [prismatic/schema "1.0.1"]]

  :eval-in-leiningen true)
