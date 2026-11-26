(ns com.hjsoft.project-templates-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [org.corfield.new :as new]
            [com.hjsoft.project-templates :as sut])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(def ^:private templates
  [{:template 'jflinchbaugh/app
    :resource "jflinchbaugh/app/template.edn"
    :name 'acme/demo-app
    :dir "demo_app"
    :expected-files ["deps.edn"
                     "build.clj"
                     "README.md"
                     "src/acme/demo_app/core.clj"
                     "test/acme/demo_app/core_test.clj"]}
   {:template 'jflinchbaugh/app-web-server
    :resource "jflinchbaugh/app_web_server/template.edn"
    :name 'acme/demo-web
    :dir "demo_web"
    :expected-files ["deps.edn"
                     "build.clj"
                     "README.md"
                     "src/acme/demo_web/core.clj"
                     "test/acme/demo_web/core_test.clj"]}
   {:template 'jflinchbaugh/app-full-stack
    :resource "jflinchbaugh/app_full_stack/template.edn"
    :name 'acme/demo-fullstack
    :dir "demo_fullstack"
    :expected-files ["Makefile"
                     "README.md"
                     "server/deps.edn"
                     "server/build.clj"
                     "server/Makefile"
                     "server/Containerfile"
                     "server/entrypoint.sh"
                     "server/demo-fullstack-server.yaml"
                     "server/demo-fullstack-server.kube"
                     "server/src/acme/demo_fullstack/db.clj"
                     "server/src/acme/demo_fullstack/logging.clj"
                     "server/src/acme/demo_fullstack/server.clj"
                     "server/test/acme/demo_fullstack/db_test.clj"
                     "server/test/acme/demo_fullstack/server_test.clj"
                     "web/package.json"
                     "web/shadow-cljs.edn"
                     "web/Makefile"
                     "web/public/style.css"
                     "web/public/manifest.json"
                     "web/src/html/index.html"
                     "web/src/html/sw.js"
                     "web/src/main/acme/demo_fullstack/app.cljs"
                     "web/src/main/acme/demo_fullstack/build_hooks.clj"
                     "web/src/main/acme/demo_fullstack/core.cljs"
                     "web/src/main/acme/demo_fullstack/ui.cljs"
                     "web/src/test/node.cljs"
                     "web/src/test/acme/demo_fullstack/core_test.cljs"
                     "web/src/test/acme/demo_fullstack/ui_test.cljs"]}
   {:template 'jflinchbaugh/clay-repl
    :resource "jflinchbaugh/clay_repl/template.edn"
    :name 'acme/demo-clay
    :dir "demo_clay"
    :expected-files ["deps.edn"
                     "build.clj"
                     "clay.edn"
                     "README.md"
                     "resources/sample.txt"
                     "dev/user.clj"
                     "notebooks/sample.clj"
                     "src/acme/demo_clay/core.clj"
                     "test/acme/demo_clay/core_test.clj"]}])

(defn- delete-recursively
  "Delete a directory tree rooted at f."
  [f]
  (doseq [item (reverse (file-seq (io/file f)))]
    (io/delete-file item)))

(deftest valid-templates-test
  (doseq [{:keys [resource]} templates]
    (testing (str resource " is valid.")
      (let [res (io/resource resource)]
        (is (some? res) (str "Missing resource: " resource))
        (when res
          (let [tmpl (edn/read-string (slurp res))]
            (is
             (s/valid? :org.corfield.new/template tmpl)
             (s/explain-str :org.corfield.new/template tmpl))))))))

(deftest create-templates-test
  (doseq [{:keys [template name dir expected-files]} templates]
    (testing (str "Creating project from " template)
      (let [tmp (.toFile
                 (Files/createTempDirectory
                  "project-templates-test"
                  (into-array FileAttribute [])))
            target (str (.getAbsolutePath tmp) "/" dir)]
        (try
          (new/create {:template template
                       :name name
                       :target-dir target})
          (doseq [rel expected-files]
            (let [f (io/file target rel)]
              (is (.exists f) (str "Missing generated file: " rel))
              (when (.exists f)
                (is (not (str/includes? (slurp f) "{{"))
                    (str "Unreplaced placeholder in " rel)))))
          (finally
            (delete-recursively tmp)))))))

(deftest add-base-ns-test
  (testing "add-base-ns combines qualifier and deduplicates single-word names"
    (is (= {:base "com.hjsoft.my-app"}
           (sut/add-base-ns {:top "com.hjsoft" :main "my-app"})))
    (is (= {:base "acme.my-app"}
           (sut/add-base-ns {:top "acme" :main "my-app"})))
    (is (= {:base "my-app"}
           (sut/add-base-ns {:top "my-app" :main "my-app"})))
    (is (= {:base "my-app"}
           (sut/add-base-ns {:top "my_app" :main "my-app"})))
    (is (= {:base "com.hjsoft.my-app"
            :artifact/id "my-app"
            :group/id "com.hjsoft"
            :scm/repo "my-app"}
           (sut/add-base-ns {:top "com.hjsoft.my-app"
                             :main "com.hjsoft.my-app"})))
    (is (= {:base "hjsoft.com.full.stack"
            :artifact/id "stack"
            :group/id "hjsoft.com.full"
            :scm/repo "stack"
            :target-dir "stack"}
           (sut/add-base-ns {:top "hjsoft"
                             :main "com/full/stack"
                             :target-dir "com/full/stack"})))
    (is (= {:base "hjsoft.com.full.stack"
            :artifact/id "stack"
            :group/id "hjsoft.com.full"
            :scm/repo "stack"}
           (sut/add-base-ns {:top "hjsoft"
                             :main "com/full/stack"
                             :target-dir "/tmp/custom"})))
    (is (= {:base "com.hjsoft.full.stack"
            :artifact/id "stack"
            :group/id "com.hjsoft.full"
            :scm/repo "stack"
            :target-dir "stack"}
           (sut/add-base-ns {:top "com.hjsoft.full.stack"
                             :main "com.hjsoft.full.stack"
                             :target-dir "com.hjsoft.full.stack"})))
    (is (= {:base "foo.bar"
            :artifact/id "bar"
            :group/id "net.clojars.foo"
            :scm/repo "bar"
            :target-dir "bar"}
           (sut/add-base-ns {:top "foo.bar"
                             :main "foo.bar"
                             :target-dir "foo.bar"})))))

(def ^:private single-word-templates
  [{:template 'jflinchbaugh/app
    :name 'demo-app
    :dir "demo_app"
    :checks [["src/demo_app/core.clj"
              "(ns demo-app.core"
              "demo-app.demo-app"]
             ["test/demo_app/core_test.clj"
              "(ns demo-app.core-test"
              "demo-app.demo-app"]]}
   {:template 'jflinchbaugh/app-web-server
    :name 'demo-web
    :dir "demo_web"
    :checks [["src/demo_web/core.clj"
              "(ns demo-web.core"
              "demo-web.demo-web"]
             ["test/demo_web/core_test.clj"
              "(ns demo-web.core-test"
              "demo-web.demo-web"]]}
   {:template 'jflinchbaugh/app-full-stack
    :name 'demo-fullstack
    :dir "demo_fullstack"
    :checks [["server/src/demo_fullstack/server.clj"
              "(ns demo-fullstack.server"
              "demo-fullstack.demo-fullstack"]
             ["server/src/demo_fullstack/db.clj"
              "(ns demo-fullstack.db"
              "demo-fullstack.demo-fullstack"]
             ["server/src/demo_fullstack/logging.clj"
              "(ns demo-fullstack.logging"
              "demo-fullstack.demo-fullstack"]
             ["web/src/main/demo_fullstack/app.cljs"
              "(ns demo-fullstack.app"
              "demo-fullstack.demo-fullstack"]
             ["web/src/main/demo_fullstack/build_hooks.clj"
              "(ns demo-fullstack.build-hooks"
              "demo-fullstack.demo-fullstack"]
             ["web/shadow-cljs.edn"
              "demo-fullstack.app/init"
              "demo-fullstack.demo-fullstack"]]}
   {:template 'jflinchbaugh/clay-repl
    :name 'demo-clay
    :dir "demo_clay"
    :checks [["src/demo_clay/core.clj"
              "(ns demo-clay.core"
              "demo-clay.demo-clay"]
             ["test/demo_clay/core_test.clj"
              "(ns demo-clay.core-test"
              "demo-clay.demo-clay"]]}])

(deftest single-word-name-templates-test
  (doseq [{:keys [template name dir checks]} single-word-templates]
    (testing (str "Creating project from " template " with " name)
      (let [tmp (.toFile
                 (Files/createTempDirectory
                  "project-templates-single-test"
                  (into-array FileAttribute [])))
            target (str (.getAbsolutePath tmp) "/" dir)]
        (try
          (new/create {:template template
                       :name name
                       :target-dir target})
          (doseq [[rel expected-substr forbidden-substr] checks]
            (let [f (io/file target rel)]
              (is (.exists f) (str "Missing generated file: " rel))
              (when (.exists f)
                (let [content (slurp f)]
                  (is (not (str/includes? content "{{"))
                      (str "Unreplaced placeholder in " rel))
                  (is (str/includes? content expected-substr)
                      (str "Missing " expected-substr " in " rel))
                  (is (not (str/includes? content forbidden-substr))
                      (str "Unexpected " forbidden-substr " in " rel))))))
          (finally
            (delete-recursively tmp)))))))

(deftest multi-segment-name-test
  (testing "hjsoft/com/full/stack places artifacts under leaf stack"
    (let [tmp (.toFile
               (Files/createTempDirectory
                "project-templates-multi-test"
                (into-array FileAttribute [])))
          target (str (.getAbsolutePath tmp) "/stack")
          checks [["server/stack-server.yaml"
                   "name: stack-server"
                   "com/full/stack"]
                  ["server/stack-server.kube"
                   "Yaml=stack-server.yaml"
                   "com/full/stack"]
                  ["server/src/hjsoft/com/full/stack/server.clj"
                   "(ns hjsoft.com.full.stack.server"
                   "com/full/stack"]
                  ["server/src/hjsoft/com/full/stack/server.clj"
                   "(def ^:const realm \"stack\")"
                   "com/full/stack"]
                  ["server/build.clj"
                   "(def lib 'hjsoft.com.full/stack-server)"
                   "com/full/stack"]
                  ["server/build.clj"
                   "stack-server-standalone.jar"
                   "com/full/stack"]
                  ["web/src/main/hjsoft/com/full/stack/app.cljs"
                   "(ns hjsoft.com.full.stack.app"
                   "hjsoft.com/full/stack"]
                  ["web/package.json"
                   "\"stack-web\""
                   "com/full/stack"]]]
      (try
        (new/create {:template 'jflinchbaugh/app-full-stack
                     :name 'hjsoft/com/full/stack
                     :target-dir target})
        (doseq [[rel expected-substr forbidden-substr] checks]
          (let [f (io/file target rel)]
            (is (.exists f) (str "Missing generated file: " rel))
            (when (.exists f)
              (let [content (slurp f)]
                (is (not (str/includes? content "{{"))
                    (str "Unreplaced placeholder in " rel))
                (is (str/includes? content expected-substr)
                    (str "Missing " expected-substr " in " rel))
                (is (not (str/includes? content forbidden-substr))
                    (str "Unexpected " forbidden-substr " in " rel))))))
        (finally
          (delete-recursively tmp))))))
