(ns servers
  (:require [clojure.tools.nrepl.server :as nrepl-server])
  (:gen-class))

(defn -main [& args]
  (let [n (Integer/parseInt (first args))]
    (doseq [i (range n)]
      (nrepl-server/start-server :port (+ 5000 i)))))
