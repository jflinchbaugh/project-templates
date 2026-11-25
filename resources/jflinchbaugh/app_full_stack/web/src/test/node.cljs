(ns node
  (:require [cljs.test :as test]
            [{{top/ns}}.{{main/ns}}.core-test]
            [{{top/ns}}.{{main/ns}}.ui-test]))

(defmethod test/report [:cljs.test/default :end-run-tests] [m]
  (when-not (test/successful? m)
    (set! js/process.exitCode 1)))

(defn main []
  (test/run-all-tests #".*-test$"))
