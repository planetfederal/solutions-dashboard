(ns solutions-dashboard.views
  (:use
   [hiccup.page :only (html5 include-js include-css)])
  (:require
   [hiccup.form        :as form]
   [clojure.data.json  :as json]
   [clojure.java.jdbc  :as sql]))

(def title-text "OpenGeo Solutions Dashboard")
(def about-text "Welcome to the OpenGeo Dashboard, from here you should be
   able to get an overview of all of the ongoing projects")

(defn nav-bar [req]
  [:div.navbar [:div.navbar-inner [:div.container [:h1.brand title-text]]]])

(defn page
  "Base function to generate a basic page"
  [request options body]
  (html5
   [:head
    [:meta {:http-equiv "content-type" :content "text/html; charset=utf-8"}]
    [:meta {:charset "utf-8"}]
    [:title (:title options "OpenGeo Dashboard")]
    (include-css "/bootstrap/css/bootstrap.min.css")
    (include-js "/jquery-1.7.1.min.js")
    (include-js "/underscore-min.js")
    (:header options)]
   [:body [:div.container (nav-bar request) body]]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/json-str data)})

(defn add-employee-form [req]
  [:form#add-employee.well
   [:fieldset 
    (form/label :first_name "Employee's first name")
    (form/text-field :first_name)
    (form/label :last_name "Employee's last name")
    (form/text-field :last_name)
    (form/label :trello_username "Employee's Trello name")
    (form/text-field :trello_username)
    (form/label :email "Employee's Email address")
    (form/text-field :email)]

   [:button.btn-primary.btn-large "Add a new user"]])

(defn index
  "Main page, loads all of the javascript for the page"
  [req]
  (page req {:header (list (include-js "/index.js"))}
        [:div
         [:div.well [:h4 about-text]]
         [:div#show-all-employees]
         [:div#add-new-employee (add-employee-form req)]]))


(defn show-all-employees
  "View to show all of the currently configured employees
   returns a 200 response with a json list of all of the employees"
  [req]
  (json-response
   (sql/with-query-results rs ["select * from employees"]
     (into [] rs))))

(defn create-employee
  "Method to create an employee
   Returns a 201 if the creation is successful."
  [req]
  (json-response (:form-params req)))

(defn remove-employee
  "We all need to be able to remove employees"
  [req])