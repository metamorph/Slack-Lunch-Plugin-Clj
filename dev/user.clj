(ns user "Default namespace for the REPL"
    (:require [org.httpkit.server :as server]
              [slack-lunch-plugin-clj.core :as core]))

(defonce srv (atom nil))

(defn stop-server []
  (swap! srv
         (fn [s]
           (when-not (nil? s)
             (println "Stopping server...")
             (s :timeout 100)
             nil))))

(defn start-server []
  (stop-server)
  (println "Starting server...")
  (reset! srv (server/run-server (#'core/handler "dev"))))




