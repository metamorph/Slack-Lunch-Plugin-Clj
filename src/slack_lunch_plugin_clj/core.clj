(ns slack-lunch-plugin-clj.core
  (:gen-class)
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :refer [split]]))

(def status
  "Holds the global state of the application.
  A database would be better, no?"
  (atom {}))

(defn start-order [state channel owner location]
  (assoc state channel {:location location
                        :owner owner
                        :created-at (System/currentTimeMillis)
                        :orders []}))

(defn add-order [state channel user order]
  (update-in state
             [channel :orders]
             #(conj % {:order order :user user})))

(defn create-command
  "Converts the params to a function that will be applied to the current state"
  [{channel :channel_name user :user_name text :text
    :or {text "" user "" channel ""}}]
  (println "Parsing")
  (let [[cmd data] (split text #"\s+" 2)]
    (case cmd
      "create" (fn [s] (start-order s channel user data))
      "add" (fn [s] (add-order s channel user data))
      identity)))

(defn render-channel-status [s channel]
  (let [order-status (get s channel)]
    {:status 200
     :headers {"content-type" "text/plain"}
     :body (format "Data: %s" order-status)}))

(defn handle [req]
  (let [cmd (create-command (:params req))]
    (swap! status cmd) ;; Update the state
    (render-channel-status @status (get-in req [:params :channel_name]))))

;; TODO: Maybe move middleware to separate ns?

(defn verify-token
  "Middleware that asserts that the request contains a valid token"
  [handler required-token]
  (fn [{{token :token_id} :params :as req}]
    (if (= token required-token)
      (handler req) ;; All is ok - delegate to the next handler
      {:status 403 :body "Invalid token"})))

(defn verify-params
  "Middleware that verifies that a set of parameters are available in a request"
  [handler & required-params]
  (fn [{params :params :as req}]
    ;; TODO: Implement me - check that all 'required-params' have a non-empty value in 'params'
    (handler req)))

;; Routing using Compojure
(defroutes app
  (POST "/" request (handle request))
  (route/not-found "On noez!"))

(defn handler
  "Creates the main handler for the app."
  [token]
  (-> app
      (verify-token token)
      (verify-params :channel_name :user_name :text)
      (wrap-keyword-params)
      (wrap-params)))

