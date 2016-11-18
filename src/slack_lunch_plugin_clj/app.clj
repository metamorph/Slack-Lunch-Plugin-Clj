(ns slack-lunch-plugin-clj.app
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [slack-lunch-plugin-clj.core :as core]))

(defn -main
  [& args]
  (println "Starting server..")
  (server/run-server (core/handler "ABC")))
