(ns solutions-dashboard.harvest
  (:import [org.apache.commons.codec.binary Base64])
  (:require
   [solutions-dashboard.config :as config]
   [clojure.java.jdbc  :as sql]
   [clojure.data.json  :as json]
   [clojure.string     :as string]
   [clj-http.client    :as client]))

(def harvest-url "https://opengeo.harvestapp.com/")

(defn base-64 [credentials]
  (.trim (Base64/encodeBase64String (.getBytes (string/join ":" credentials)))))

(def headers
  {:basic-auth config/harvest-auth
   :authorization (str "Basic " (base-64 config/harvest-auth))
   :accept :json
   :content-type :json})

(defn make-harvest-api-call [url]
  (json/read-json
   (:body (client/get (str harvest-url url) headers))))


(defn who-am-i? []
  (make-harvest-api-call "account/who_am_i"))

(defn get-person-by-id [id]
  (make-harvest-api-call (str "people/" id)))

(defn get-all-employees []
  (for [people (make-harvest-api-call "people/")]
    (let [e (:user people)]
      {:id (:id e)
       :first_name (:first_name e)
       :last_name (:last_name e)
       :email (:email e)
       :is_active (:is_active e)})))


(defn get-all-projects []
  (for [project (make-harvest-api-call "projects/")]
    (let [p (:project project)]
      {:id (:id p)
       :client_id ()
       :name (:name p)
       :notes (:notes p)
       :active (:active p)
       :bill_by (:bill_by p)
       :budget (:budget p) })))


(defn ingest-harvest-employee-data []
  (sql/with-connection config/db
    (doseq [e (get-all-employees)]
      (sql/insert-record :employess e))))

(defn ingest-harvest-data []
  (ingest-harvest-employee-data))

(def geonode-id 1566614)
(def rollie 176897)
