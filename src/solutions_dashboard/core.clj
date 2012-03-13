(ns solutions-dashboard.core
  (:use
   [ring.adapter.jetty-servlet :only (run-jetty)]
   [ring.util.response :only (redirect)]
   [ring.middleware.json-params :only (wrap-json-params)]
   [compojure.core :only (defroutes context GET ANY POST DELETE PUT)])
  (:require
   [solutions-dashboard.views  :as views]
   [solutions-dashboard.config :as config]
   [solutions-dashboard.auth   :as auth]
   [clojure.java.io    :as io]
   [compojure.handler  :as handler]
   [compojure.response :as response]
   [compojure.route    :as route]
   [clojure.java.jdbc  :as sql]))

(defroutes main-routes
  (GET    "/"          [] (io/resource "public/index.html"))
  (GET    "/login"     [] (io/resource "public/login.html"))
  (POST   "/login"     [] views/post-login)
  (GET    "/logout"    [] views/logout)
  (GET    "/employees" [] views/show-all-employees)
  (POST   "/employees" [] views/create-employee)
  (GET    "/show-harvest-projects"  [] views/show-harvest-projects)
  
  (context "/employees/:id" [id]
     (GET    "/" [id] views/show-employee)
     (PUT   "/"  [id] views/update-employee)
     (POST   "/send-email" [] views/view-send-email)
     (DELETE "/" [id] views/remove-employee)
     (GET    "/get-trello-info"  [id] views/show-trello-info)
     (GET    "/get-harvest-info" [id] views/show-harvest-info))

  (route/resources "/public" )
  (route/not-found (views/page-not-found {})))


(defn wrap-dev-db-connection [handler]
  (fn [request]
    (sql/with-connection config/db
      (sql/transaction
       (handler request)))))

(defn wrap-servlet-session [handler]
  (fn [request]
    (handler
     (if-let [servlet-request (:servlet-request request)]
       (assoc request :ses (.getSession servlet-request true))
       request))))


(defn wrap-force-auth [handler]
  (fn [req]
    (if (or (-> req :uri (.startsWith "/public"))  (-> req :uri (.startsWith "/login")))
      (handler req)
      (do (if (auth/session-get-user req)
            (handler req)
            (redirect "/login"))))))

(def app
  (-> main-routes
      handler/site
      wrap-json-params
      wrap-force-auth
      wrap-servlet-session
      wrap-dev-db-connection))

(defn run-server []
  (run-jetty #'app {:port 3000 :join? false}))