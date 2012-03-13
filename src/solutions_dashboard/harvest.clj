(ns solutions-dashboard.harvest
  (:import
   [java.util Calendar GregorianCalendar]
   [org.apache.commons.codec.binary Base64])
  (:require
   [solutions-dashboard.config :as config]
   [clojure.data.csv   :as csv]
   [clojure.java.io    :as io]
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

(defn limit? []
  (make-harvest-api-call "account/rate_limit_status" {}))

(defn get-people []
  (map :user (make-harvest-api-call "people/" {})))

(defn get-person-by-id [id]
  (make-harvest-api-call (str "people/" id) {}))

(defn get-task [id]
  (:task (make-harvest-api-call (str "/tasks/" id) {})))

(def project-url (partial str "projects/"))

(defn get-projects []
  (map :project (make-harvest-api-call (project-url) {})))

(defn get-project [id]
  (make-harvest-api-call (project-url "/" id) {}))

(defn get-tasks []
  (map :task (make-harvest-api-call "tasks" {})))

(defn get-task [id]
  (make-harvest-api-call (str "tasks/" id) {}))

(defn get-project-by-name [name]
  (first (filter #(= (:name %) name) (get-projects))))

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

(defn tasks-from-task-assignments [project]
  (map #(merge % (get-task (:task_id % ))) (get-task-assignments {:id project})))

(defn get-entries-tasks-and-projects [entries]
  (map #(assoc (:task (get-task (key %))) :project_id (:project_id (first (val %)))) entries))

(defn get-user-tasks [user from to]
  (let [entries (get-time-entries-by-person user from to)
        entries_tasks (group-by :task_id entries)
        tasks (map
               #(assoc % :entries (get entries_tasks (:id %)))
               (get-entries-tasks-and-projects entries_tasks))]
    (map #(assoc (:project (get-project (key %))) :tasks (val %)) (group-by :project_id tasks))))


(defn export-harvest-users []
  (sql/with-connection config/db     
    (doseq  [p (get-people)]
      (sql/insert-record :employees
                         {:name (str (:first_name p) " " (:last_name p))
                          :trello_username ""
                          :harvest_id  (:id p)
                          :email (:email p)
                          }))))