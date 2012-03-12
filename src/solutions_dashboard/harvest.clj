(ns solutions-dashboard.harvest
  (:import
   [java.util Calendar GregorianCalendar]
   [org.apache.commons.codec.binary Base64])
  (:require
   [solutions-dashboard.config :as config]
   [clojure.java.jdbc  :as sql]
   [clojure.data.json  :as json]
   [clojure.string     :as string]
   [clj-http.client    :as client]))

(def harvest-url "https://opengeo.harvestapp.com/")

(defn base-64 [credentials]
  (.trim (Base64/encodeBase64String (.getBytes (string/join ":" credentials)))))


(defn make-harvest-api-call [url  query-params] 
  (json/read-json (:body (client/get
                          (str harvest-url url)
                          {:basic-auth config/harvest-auth
                           :authorization (str "Basic " (base-64 config/harvest-auth))
                           :query-params query-params
                           :accept :json
                           :content-type :json}))))



(defn time-difference [diff]
  (let [now (GregorianCalendar/getInstance)
        past (.clone now)]
    (.add past (Calendar/DATE) diff)
    (list past now)))

(defn one-week []
  (map #(vector
         (.get % (Calendar/YEAR))
         (+ (.get % (Calendar/MONTH)) 1)
         (.get % (Calendar/DAY_OF_MONTH))) (time-difference -7)))

(defn who-am-i? []
  (make-harvest-api-call "account/who_am_i" {}))


(defn get-person-by-id [id]
  (make-harvest-api-call (str "people/" id) {}))

(defn get-task [id]
  (:task (make-harvest-api-call (str "/tasks/" id) {})))

(def project-url (partial str "projects/"))

(defn get-all-projects []
  (map :project (make-harvest-api-call (project-url) {})))


(defn get-project-by-name [name]
  (first (filter #(= (:name %) name) (get-all-projects))))

(defn get-task-assignments [project]
  (map :task_assignment
       (make-harvest-api-call (project-url (:id project) "/task_assignments") {})))


(defn get-user-assignments [project]
  (make-harvest-api-call (project-url  (:id project) "/user_assignments") {}))

(defn format-number [x]
  (if (> 10 x)
    (str "0" x)
    (str x)))

(defn format-date [date]
  (apply str (map format-number date)))


(defn get-time-entries [url resource from to]
  (make-harvest-api-call
   (str url (:harvest_id resource) "/entries")
   {:from (format-date from) :to (format-date to)}))

(defn get-time-entries-by-project [project from to]
  (map :day_entry (get-time-entries "projects/" project from to)))

(defn get-time-entries-by-person [person from to]
  (map :day_entry (get-time-entries "people/" person from to)))

(defn main []
  (let [week (one-week)
        entries (get-time-entries-by-person {:harvest_id 295657}  [2012 1 1] [2012 3 1])]
    entries))