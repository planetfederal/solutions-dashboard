"
client name
project name
project code (maybe use to distinguish between time&materials, fixed price, etc.?)
budget?
list of tasks
for each task:
  task name
  hours budgeted
  hours spent (this month?)
  list of people

for each person
name
rate
total budgeted hours
total hours spent (entire project duration)
total hours spent this month (or week? or other time duration?)

"
(ns solutions-dashboard.core
  (:use
   [ring.adapter.jetty-servlet :only (run-jetty)]
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
  (POST   "/employees/add" [] views/create-employee)
  (DELETE "/employees" [] views/remove-employee)
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
      wrap-dev-db-connection))

(defn run-server []
  (run-jetty #'app {:port 3000 :join? false}))