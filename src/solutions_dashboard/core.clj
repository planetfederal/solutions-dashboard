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
   [hiccup.core :only (html)]
   [hiccup.page-helpers :only (doctype include-js include-css)]
   [ring.adapter.jetty-servlet :only (run-jetty)]
   [solutions-dashboard.trello :only (generate-priorities-by-person)]
   [compojure.core :only (defroutes GET ANY POST)])
  (:require
   [compojure.handler  :as handler]
   [compojure.response :as response]
   [compojure.route    :as route]
   [clojure.java.jdbc  :as sql]
   [clojure.data.json  :as json]))

(defn page [request options body]
  (html
   (doctype :html4)
   [:head
    [:meta {:http-equiv "content-type" :content "text/html; charset=utf-8"}]
    [:meta {:charset "utf-8"}]
    [:title (:title options "dashboard")]
    (include-css "/bootstrap/css/bootstrap.min.css")
    (:header options)]
   [:body body]))


(defn show-projects [req]
  (page
   req {}
   [:ol 
    (for [person '()]
      [:li [:h2 (:fullName person)]
       [:ol 
        (for [project (:projects person)]
          [:li [:h3 (:name project)]
           [:ol 
            (for [task (:tasks project)]
              [:li
               [:p (str (:name task) ", Due: " (or (:due task) "None")) ]]
              )]])]])]))


(defn show-user-info [req]
  (page req {} [:div "Hello"]))


(defroutes main-routes
  (GET "/" [] show-projects)
  (GET "/show-user-info" [] show-user-info)
  (route/resources "/" )
  (route/not-found (str "unable to find route")))

(def app
  (handler/site main-routes))

(defn run-server []
  (run-jetty #'app {:port 3000 :join? false}))