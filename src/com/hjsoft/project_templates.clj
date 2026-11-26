(ns com.hjsoft.project-templates
  (:require [clojure.string :as str]))

(defn add-base-ns
  "Normalize `:base`, `:artifact/id`, `:group/id`, `:scm/repo`, and
  default `:target-dir` from `:top`, `:main`, and `:target-dir`.

  When `:name` is unqualified (e.g. `my-app`), `deps-new` sets both `:top`
  and `:main` to the same value; deduplicating avoids `my-app.my-app`.
  When `:main` contains `/` or `.` (e.g. `hjsoft/com/full/stack` or
  `com.hjsoft.full.stack`), the final segment (`stack`) is used as the
  default `:target-dir`, `:artifact/id`, and `:scm/repo`."
  [{:keys [top main target-dir]}]
  (let [top-s (some-> top str)
        main-s (some-> main str)
        norm (fn [s] (when s (str/replace s "_" "-")))
        raw-base (if (or (str/blank? top-s)
                         (= (norm top-s) (norm main-s)))
                   main-s
                   (str top-s "." main-s))
        base (when raw-base (str/replace raw-base "/" "."))
        segments (when base (str/split base #"\."))
        leaf (when (seq segments) (peek segments))
        prefix (when (> (count segments) 1)
                 (str/join "." (pop segments)))
        multi-main? (and main-s (boolean (re-find #"[/.]" main-s)))]
    (cond-> {:base base}
      (and multi-main? leaf)
      (assoc :artifact/id leaf
             :scm/repo leaf)

      (and multi-main? prefix)
      (assoc :group/id (if (str/includes? prefix ".")
                         prefix
                         (str "net.clojars." prefix)))

      (and multi-main? leaf (= target-dir main-s))
      (assoc :target-dir leaf))))
