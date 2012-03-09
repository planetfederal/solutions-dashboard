(ns solutions-dashboard.views
  (:use
   [decline.core :only (validations validate-val)]
   [solutions-dashboard.trello :only (get-user-projects)]
   [hiccup.page :only (html5 include-js include-css)])
  (:require
   [solutions-dashboard.config  :as config]
   [solutions-dashboard.harvest :as harvest]
   [hiccup.form        :as form]
   [clojure.data.json  :as json]
   [clojure.java.jdbc  :as sql]))

(defn write-json-timestamp [x out escape-unicode?]
  (json/write-json (str x) out escape-unicode?))

(extend java.sql.Timestamp json/Write-JSON
        {:write-json write-json-timestamp})

(def title-text "OpenGeo Solutions Dashboard")
(def about-text "Welcome to the OpenGeo Dashboard, from here you should be
   able to get an overview of all of the ongoing projects")

(defn nav-bar [req]
  [:div.navbar [:div.navbar-inner
                [:div.container [:h1.brand
                                 [:a.brand {:href "/"} title-text]]]]])

(defn page
  "Base function to generate a basic page"
  [request options body]
  (html5
   [:head
    [:meta {:http-equiv "content-type" :content "text/html; charset=utf-8"}]
    [:meta {:charset "utf-8"}]
    [:title (:title options "OpenGeo Dashboard")]

    (include-css "/bootstrap/css/bootstrap.min.css")
    (include-js  "/jquery-1.7.1.min.js")
    (include-js  "/underscore-min.js")
    (include-js  "/backbone.js")
    (include-js  "/bootstrap-modal.js")
    (:header options)]
   [:body [:div.container (nav-bar request) body]]))


(defn page-not-found [req]
  (page req {} [:div.well [:h3 "We where unable to find the page you where looking."]]))

(defn json-response
  "Function to correctly format the json response for the api"
  [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/json-str data)})


(defn get-all-employees []
  (sql/with-query-results rs ["select * from employees"]
    (into [] rs)))

(defn get-employee [id]
  (sql/with-query-results rs ["select * from employees where id = ?" id]
    (first rs)))

(defn index
  "Main page, loads all of the javascript for the page"
  [req]
  (page req
        {:header (list (include-js "/index.js"))}
        [:div
         [:ul#dash-nav.nav.nav-tabs
          [:li#index.active [:a {:href "#"} "Employee list"]]
          [:li#new [:a {:href "#new"} "Add an employee"]]
          [:li#harvest [:a {:href "#harvest"} "View current Harvest projects"]]
          [:li#resocure [:a "Resources Dashboard"]]]
         [:div#application]]))

(defn show-all-employees
  "View to show all of the currently configured employees
   returns a 200 response with a json list of all of the employees"
  [req]
  (json-response (get-all-employees)))

(defn show-employee
  [req]
  (let [employee (get-employee (Integer/parseInt (:id (:params req))))]
    (json-response employee)))

(defn blank?
  "Function to check if a value is a string and if its blank"
  [s]
  (if (string? s)
    (if (= (count s) 0)
      false true) false))

(def check-employee-form
  (validations
   (validate-val :name blank? {:name "The name must be a string"})
   (validate-val :trello_username blank?
                 {:trello_name "The trello account number must be a string"})
   (validate-val :harvest_id blank?
                    {:harvest_id "The trello account number must be a string"})
   (validate-val :email blank? {:email "The trello account number must be a string"})))


(defn insert-employee! [data]
  (sql/insert-record :employees data))

(defn create-employee
  "Method to create an employee
   Returns a 201 if the creation is successful."
  [req]
  (let [form (:params req)
        errors (check-employee-form form)]
    (if errors (json-response errors 400)
        (json-response (insert-employee! form)))))

(defn remove-employee
  "We all need to be able to remove employees"
  [req]
  (let [id (Integer/parseInt (:id (:params req)))]
    (sql/delete-rows :employees ["id=?" id])
    (json-response "okay")))


(defn show-trello-info
  "Pass the trello information to the client"
  [req]
  (let [employee (get-employee (Integer/parseInt (:id (:params req))))]
    (json-response (get-user-projects (:trello_username employee)))))

(defn show-harvest-info [req]
  (let [employee (get-employee (Integer/parseInt (:id (:params req))))
        week (harvest/one-week)]
    (json-response (harvest/get-time-entries-by-person employee (first week) (second week)))))

(defn show-harvest-projects
  [req]
  (json-response (harvest/get-all-projects)))