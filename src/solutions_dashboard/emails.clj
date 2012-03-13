(ns solutions-dashboard.emails
  (:import [java.util Date])
  (:require
   [postal.core :as postal]
   [clojure.java.jdbc :as sql]
   [solutions-dashboard.config :as config]
   [solutions-dashboard.harvest :as harvest]
   [solutions-dashboard.trello :as trello]))

(defn sum-hours [entries]
  (reduce (fn [rs n] (+ rs (Double/parseDouble (:hours n)))) 0 entries))

(defn build-harvest [str-builder harvest]
  (.append str-builder "Last week \n\n")
  (doseq [pr harvest]
    (.append str-builder (str "* " (:name pr) "\n"))
    (doseq [t (:tasks pr)]
      (.append str-builder (str "\t- " (:name t) " " (sum-hours (:entries t)) " hour(s) spent" "\n")))))

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
        harvest  (harvest/get-user-tasks e (first week) (second week))]
    (build-message-body e trello harvest)))


(defn send-email [e body]
  (println (str "Sending the email to google at " (Date.)))
  (postal/send-message
   #^{:host "smtp.googlemail.com"
      :user (first config/mail-config)
      :ssl :yes!!!11
      :pass (second config/mail-config)}
   {:from ""
    :to [(:email e)]
    :subject (str "Priorities for " (:name e))
    :body body}))

(defn send-message [e]
  (send-email e (build-message e)))

(defn send-emails-to-all []
  (sql/with-connection config/db
    (sql/with-query-results employees  ["select * from employees"]
      (doseq [e employees]
        (send-message e)))))