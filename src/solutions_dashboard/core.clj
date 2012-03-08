(ns solutions-dashboard.core
  (:use
   [ring.adapter.jetty-servlet :only (run-jetty)]
   [ring.middleware.json-params :only (wrap-json-params)]
   [compojure.core :only (defroutes GET ANY POST DELETE)])
  (:require
   [solutions-dashboard.views  :as views]
   [solutions-dashboard.config :as config]
   [compojure.handler  :as handler]
   [compojure.response :as response]
   [compojure.route    :as route]
   [clojure.java.jdbc  :as sql]))

(defroutes main-routes
  (GET    "/"          [] views/index)

  (GET    "/employees" [] views/show-all-employees)
  (GET    "/employees/:id" [id] views/show-employee)
  (POST   "/employees" [] views/create-employee)
  (DELETE "/employees/:id" [] views/remove-employee)
  (GET    "/get-trello-info/:username"  [] views/show-trello-info)
  (GET    "/get-harvest-info/:username" [] views/show-harvest-info)
  (GET    "/show-harvest-projects"  [] views/show-harvest-projects)
  (route/resources "/" )
  (route/not-found (views/page-not-found {})))

(defn wrap-dev-db-connection [handler]
  (fn [request]
    (sql/with-connection config/db
      (sql/transaction
       (handler request)))))

(def app
  (-> main-routes
      handler/site
      wrap-json-params
      wrap-dev-db-connection))

(defn run-server []
  (run-jetty #'app {:port 3000 :join? false}))