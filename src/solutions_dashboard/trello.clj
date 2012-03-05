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
  (:members (make-trello-api-call :get (str "organization/" config/trello-opengeo-id ) {:members "all"})))

(defn get-tasks-by-user [name]
  (:cards (make-trello-api-call :get (str "members/" name)  {:cards "all" :card_fields "all"})))

(defn get-boards-by-user [name]
  (:boards (make-trello-api-call :get (str "members/" name) {:boards "all" :board_fields "all"})))

(defn get-organization-boards [org]
  (make-trello-api-call :get (str "organizations/" org "/boards") {}))


(defn get-user-projects
  "Function to query the trello api. Finds all of the boards and cards
  associated with a user. Groups the cards by what boards they are
  associated with.
  TODO FIX ME.

  In the solutions dashboard boards are projects and
  cards are task."
  [person]
  (println (str "Fetching the user information from Trello" (Date.)))
  (let [user-info (make-trello-api-call
                   :get
                   (str "members/" person)
                   {:boards "all" :board_fields "all" :cards "all" :card_fields "all"})
        grouped-tasks (group-by :idBoard (:cards user-info))]
    (dissoc  (assoc user-info :projects
                    (for [project (:boards user-info)]
                      (assoc project  :tasks (get grouped-tasks (:id project)))))
             :boards :cards)))


(defn display-user-priorities
  "Function to handle displaying the employee's info"
  [employee]
  (let [employee-info (get-user-projects (:trello_username employee))]
    [:div [:h3 (str "Employee: "(:name employee))]
     [:table.table.table-bordered
      [:thead [:tr [:th "Projects"] [:th "Tasks"]]]
      [:tbody
       (for [project (:projects employee-info)]
         [:tr
          [:td (:name project)]
          [:td
           [:table
            (for [task (:tasks project)]
              [:tr
               [:td [:a {:href (:url task)}  (:name task)]]
               [:td (or (:due task) "None")]])]]])]]]))