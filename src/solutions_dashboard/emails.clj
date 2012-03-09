(ns solutions-dashboard.emails
  (:import [java.util Date])
  (:require
   [postal.core :as postal]
   [clojure.java.jdbc :as sql]
   [solutions-dashboard.config :as config]
   [solutions-dashboard.harvest :as harvest]
   [solutions-dashboard.trello :as trello]))


(defn build-harvest [str-builder harvest]
  (.append str-builder "Last week \n\n")
  (doseq [task harvest]
    (.append str-builder (str "* " (:notes task) "-----" (:hours task) " hours spent \n"))))

(defn build-trello [str-builder trello]
  (.append str-builder "\n This week \n\n")
  (doseq [project trello]
    (.append str-builder (str "* " (:name project) "\n"))
    (doseq [task (:tasks project)]
      (.append str-builder (str "\t - " (:name task) "\n")))))



(defn build-message-body [user trello harvest]
  (let [str-builder (StringBuilder.)]
    (.append str-builder "-------------------- \n")
    (build-harvest str-builder harvest)
    (build-trello str-builder trello)
    (.append str-builder "-------------------- \n")
    (.toString str-builder)))


(defn build-message [e]
  (let [trello   (:projects (trello/get-user-projects (:trello_username  e)))
        week     (harvest/one-week)
        harvest  (harvest/get-time-entries-by-person e (first week) (second week))]
    (build-message-body e trello harvest)))


(defn send-email [to body]
  (println (str "Sending the email to google at " (Date.)))
  (postal/send-message
   #^{:host "smtp.gmail.com"
      :user (first config/mail-config)
      :pass (second config/mail-config)
      :ssl :yes!!!11}
   {:from "iwillig@gmail.com"
    :to [to]
    :subject (str "Priorities for " (Date.))
    :body body}))

(defn send-message [e]
  (send-email (:email e) (build-message e)))

(defn send-emails-to-all []
  (sql/with-connection config/db
    (sql/with-query-results employees  ["select * from employees"]
      (doseq [e employees]
        (send-message e)))))