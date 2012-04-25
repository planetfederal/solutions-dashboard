(ns solutions-dashboard.views
  (:use
   [decline.core :only (validations validate-val)]
   [ring.util.response :only (redirect response)])
  (:require
   [solutions-dashboard.config  :as config]
   [solutions-dashboard.harvest :as harvest]
   [solutions-dashboard.auth    :as auth]
   [solutions-dashboard.trello    :as trello]
   [solutions-dashboard.emails  :as emails]
   [clojure.data.json  :as json]
   [clojure.java.jdbc  :as sql]))

(defn write-json-timestamp [x out escape-unicode?]
  (json/write-json (str x) out escape-unicode?))

(extend java.sql.Timestamp json/Write-JSON
        {:write-json write-json-timestamp})

(defn post-login [req]
  (let [form (:form-params req)
        user (get form "username")
        passwd (get form "password")]
    (if (and (= user (first config/auth)) (= passwd (second config/auth)))
      (do (auth/session-save-user req user) (redirect "/"))
      (redirect "/login"))))

(defn logout [req]
  (auth/session-delete req)
  (redirect "/login"))

(defn json-response
  "Function to correctly format the json response for the api"
  [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/json-str data)})

(defn page-not-found [req]
  (json-response "We where unable to find the page you where looking."))


(defn get-all-employees []
  (sql/with-query-results rs ["select * from employees order by trello_username desc"]
    (into [] rs)))

(defn get-employee [id]
  (sql/with-query-results rs ["select name,email,trello_username,harvest_id from employees where id = ?" id]
    (first rs)))

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


(defn update-employee [req]
  (let [id (Integer/parseInt (:id (:params req)))
        form (:params req)]
    (sql/update-values
     :employees ["id = ?" id]
     ;; drop the id from the form map
     ;; because we don't want to allow users to update an employee's id
     (dissoc form :id))
    (response "")))

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
    (json-response (trello/get-filtered-tasks (:trello_username employee)))))

(defn show-harvest-info [req]
  (let [employee (get-employee (Integer/parseInt (:id (:params req))))
        weeks (harvest/two-weeks)]
    (json-response (harvest/get-user-tasks employee (first weeks) (second weeks)))))

(defn show-harvest-projects
  [req]
  (json-response (harvest/get-projects)))

(defn view-send-email [req]
  (let [e (get-employee (Integer/parseInt (:id (:params req))))]
    (emails/send-message e)
    (json-response "okay")))