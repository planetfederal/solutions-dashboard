(ns solutions-dashboard.trello 
  (:import [java.util Date])
  (:require
   [postal.core :as postal]
   [solutions-dashboard.config :as config]
   [clojure.data.json  :as json]
   [clj-http.client    :as client]))


(def trello-url "https://api.trello.com/")
(def version 1) ;; version of the trello api


(defn make-trello-api-call [method uri query-params]
  (json/read-json
   (:body (client/request
           {:method method
            :content-type :json
            :accept :json
            :query-params (merge query-params
                                 {:key config/trello-key
                                  :token config/trello-token})
            :url (str trello-url version "/" uri)}))))

(defn get-organization [id]
  (make-trello-api-call :get (str "organizations/"  id) {}))

(defn get-opengeo []
  (get-organization config/trello-opengeo-id))

(defn get-opengeo-people []
  (:members
   (make-trello-api-call :get (str "organization/" config/trello-opengeo-id ) {:members "all"})))

(defn get-tasks-by-user [name]
  (:cards (make-trello-api-call :get (str "members/" name)  {:cards "all" :card_fields "all"})))

(defn get-boards-by-user [name]
  (:boards (make-trello-api-call :get (str "members/" name) {:boards "all" :board_fields "all"})))

(defn get-organization-boards [org]
  (make-trello-api-call :get (str "organizations/" org "/boards") {}))


(defn get-list [id]
  (make-trello-api-call :get (str "list/" id) {}))

(defn classify-tasks-by-project
  " This function groups the cards by their board in a manner thats useful for us.
    returns a seq where each atom is an map with the boards under
  the :tasks key"
  [projects tasks]
  (let [grouped-tasks (group-by :idBoard tasks)]
    (for [project projects]
      (assoc project :tasks (get grouped-tasks (:id project))))))

(defn classify-tasks-by-list
  "After grouping each card by project we also need to group each sub
  task by list"
  [projects]
  (for [project projects]
    (let [grouped-tasks (group-by :idList (:tasks project))]
      (assoc project :lists 
             (for [list (keys grouped-tasks)]
               (assoc (get-list list) :tasks (get grouped-tasks list))
               )))))


(defn get-user-projects
  "Function to query the trello api. Finds all of the boards and cards
  associated with a user. Groups the cards by what boards they are
  associated with.
  TODO FIX ME.

  In the solutions dashboard boards are projects and
  cards are task."
  [person]
  (let [user-info (make-trello-api-call
                   :get
                   (str "members/" person)
                   {:boards "all" :board_fields "all" :cards "all" :card_fields "all"})
        projects (classify-tasks-by-project (:boards user-info) (:cards user-info))]
    (assoc user-info :projects (classify-tasks-by-list projects))))


(defn test-lists []
  (get-user-projects "ivanwillig"))