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
                     "src/acme/demo_app.clj"
                     "test/acme/demo_app_test.clj"]}
   {:template 'jflinchbaugh/app-web-server
    :resource "jflinchbaugh/app_web_server/template.edn"
    :name 'acme/demo-web
    :dir "demo_web"
    :expected-files ["deps.edn"
                     "build.clj"
                     "README.md"
                     "src/acme/demo_web.clj"
                     "test/acme/demo_web_test.clj"]}
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
                     "src/demo_clay/core.clj"
                     "test/demo_clay/core_test.clj"]}])

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
