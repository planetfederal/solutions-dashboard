(ns solutions-dashboard.core
  (:use
   [ring.adapter.jetty-servlet :only (run-jetty)]
   [ring.middleware.json-params :only (wrap-json-params)]
   [compojure.core :only (defroutes context GET ANY POST DELETE)])
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
  (POST   "/employees" [] views/create-employee)
  (GET    "/show-harvest-projects"  [] views/show-harvest-projects)

  (context "/employees/:id" [id]
     (GET    "/" [id] views/show-employee)
     (DELETE "/" [id] views/remove-employee)
     (GET    "/get-trello-info"  [id] views/show-trello-info)
     (GET    "/get-harvest-info" [id] views/show-harvest-info))

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