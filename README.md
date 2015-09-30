# lein-essthree

Leiningen plugin for easy S3 project deployment and dependency resolution

[![Clojars Project](http://clojars.org/lein-essthree/latest-version.svg)](http://clojars.org/lein-essthree)

Just add `lein-essthree` to your project plugins and you're ready to
configure and deploy.

## Another S3 lein plugin?

There are already a handful of leiningen plugins to deploy various project types
and artifacts to S3, as well as maven wagon plugins to resolve dependencies
hosted in S3 buckets:

* Directory contents - [lein-s3-static-deploy](https://github.com/ThoughtWorksInc/lein-s3-static-deploy)
* Clojure library - [s3-wagon-private](https://github.com/technomancy/s3-wagon-private), [lein-aws-maven](https://github.com/robertluo/lein-aws-maven), and [lein-maven-s3-wagon](https://github.com/pjstadig/lein-maven-s3-wagon)
* Uberjar - [lein-deploy-app](https://github.com/rplevy/lein-deploy-app) and [lein-s3-uberjar-release](https://github.com/Rafflecopter/lein-s3-uberjar-release)

The goal of lein-essthree is to [unify the functionality](https://xkcd.com/927/)
of the above plugins, while also addressing the following issues:

1. All of the above plugins handle AWS credentials inconsistently. Some require
the credentials in the project.clj map, some designate special files, some
respect the AWS SDK conventions, etc. Almost none of them handled EC2 IAM roles.
2. Most of the above use different means of AWS API access. Some pull in the
native AWS Java SDK, some shell out to command line programs like s3cmd, some
use libraries like clj-aws-s3 which are going stale, etc.
3. Some of the above are either un-maintained, don't have much documentation, or
both.

In trying to deploy various lein projects to S3 through a [CI/CD service](https://circleci.com/),
I encountered friction due to the above issues. As such, I wanted a comprehensive
S3 deployment plugin which was up-to-date, well documented, and which respected
the canonical credential [provider chain](http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html)
currently used by the AWS SDK.

## Configuration

### AWS Credentials

Using the awesome [amazonica](https://github.com/mcohen01/amazonica) library,
lein-essthree is able to support all of the credential providers supported by
the Java AWS SDK. See the [docs](http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html)
for full details, but in short:

1. Configuration in project.clj - {:essthree {:deploy {:access-key-id "access-key" :secret-access-key "secret-key"}}}
2. Environment Variables – AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
3. Java System Properties – aws.accessKeyId and aws.secretKey
4. The default credential profiles file – `~/.aws/credentials`Format.
5. Instance profile credentials – Used to pass IAM role credentials to EC2 instances

Credentials will be preferred in the order listed above. Note that while setting
the values directly from project.clj is supported, it's not recommended outside
of some limited development and testing scenarios.

### Directory

This configuration will deploy a local directory structure to S3. lein-essthree
will attempt to synchronize the two directory structures with as few API calls
as possible, comparing MD5 checksums for all mutual files and only uploading
those which have changed.

```clojure
{:essthree
    {:deploy {:type       :directory
              :bucket     "essthree.directory-test"
              :local-root "essthree-local/"
              ;; :path       "test/path/"
              ;; :aws-creds  {:access-key-id     "access-key"
              ;;              :secret-access-key "secret-key"}
              }}}
```

### Library

This configuration will transparently configure and add appropriate deploy
repository entries to the project.clj configuration before passing the
deployment off to the built-in `lein deploy` task. At that point, the deploy
task will rely on the Spring project's S3 Maven wagon plugin to handle the
actual deployment, which is included and configured with lein-essthree.

All of the optional configuration parameters for `:deploy-repositories` are
supported.

```clojure
{:essthree
    {:deploy {:type          :library
              :bucket        "essthree.library-test"
              ;; :snapshots     true
              ;; :sign-releases true
              ;; :checksum      :fail
              ;; :update        :always
              ;; :path          "test/path/"
              ;; :aws-creds     {:access-key-id     "access-key"
              ;;                 :secret-access-key "secret-key"}
              }}}
```

### Uberjar

This configuration will compile and upload an application uberjar to S3. The
uberjar compilation is handled by the built-in `lein uberjar` task. By default,
the uberjar artifact will be uploaded to S3 with using the
`<project-name>-<version>-standalone.jar` naming convention, but this can be
optionally overridden in the essthree config.

```clojure
{:essthree
    {:deploy {:type          :uberjar
              :bucket        "essthree.uberjar-test"
              ;; :artifact-name "essthree-test.jar"
              ;; :path          "test/path/"
              ;; :aws-creds     {:access-key-id     "access-key"
              ;;                 :secret-access-key "secret-key"}
              }}}
```

### Repository

Along with the deployment configurations outlined above, a `:repository`
configuration can be specified. This will cause a lein-essthree middleware
function to configure and add the appropriate entries to the project's
repositories for dependency resolution. Any other plugins or tasks which
then handle dependency resolution, including the built-in lein tasks, will
be able to resolve any dependencies hosted on S3.

All of the optional configuration parameters for `:repositories` are
supported.

```clojure
{:essthree
    {:repository {:bucket        "essthree.repository-test"
                  ;; :snapshots     true
                  ;; :sign-releases true
                  ;; :checksum      :fail
                  ;; :update        :always
                  ;; :path          "test/path/"
                  ;; :aws-creds     {:access-key-id     "access-key"
                  ;;                 :secret-access-key "secret-key"}
                  }}}
```


## Usage

Once the lein-essthree configuration has been added to project.clj, just run:

    $ lein essthree

## Credits

Many thanks to [Revcaster](https://revcaster.com) and [The Rainmaker Group](http://letitrain.com/)
for supporting contributions to the open-source Clojure community.

## License

Copyright © 2015 Dylan Paris

Distributed under the MIT License.
