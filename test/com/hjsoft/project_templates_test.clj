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
    :main "demo_app"}
   {:template 'jflinchbaugh/app-web-server
    :resource "jflinchbaugh/app_web_server/template.edn"
    :name 'acme/demo-web
    :main "demo_web"}])

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
  (doseq [{:keys [template name main]} templates]
    (testing (str "Creating project from " template)
      (let [tmp (.toFile
                 (Files/createTempDirectory
                  "project-templates-test"
                  (into-array FileAttribute [])))
            target (str (.getAbsolutePath tmp) "/" main)]
        (try
          (new/create {:template template
                       :name name
                       :target-dir target})
          (let [expected ["deps.edn"
                          "build.clj"
                          "README.md"
                          (str "src/acme/" main ".clj")
                          (str "test/acme/" main "_test.clj")]]
            (doseq [rel expected]
              (let [f (io/file target rel)]
                (is (.exists f) (str "Missing generated file: " rel))
                (when (.exists f)
                  (is (not (str/includes? (slurp f) "{{"))
                      (str "Unreplaced placeholder in " rel))))))
          (finally
            (delete-recursively tmp)))))))
