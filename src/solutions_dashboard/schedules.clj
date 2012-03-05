(ns solutions-dashboard.schedules
  (:require
   [clojure.java.jdbc :as sql]
   [solutions-dashboard.trello :as trello]
   [solutions-dashboard.views :as views]
   [solutions-dashboard.config :as config]
   [postal.core :as postal])
  (:import
   [java.util Date]
   [org.quartz Job]
   [org.quartz JobBuilder]
   [org.quartz DateBuilder]
   [org.quartz CronScheduleBuilder]
   [org.quartz TriggerBuilder]
   [org.quartz.impl StdSchedulerFactory]))


(defn send-email [user password to body]
  (println (str "sending the email to google" (Date.)))
  (postal/send-message
   #^{:host "smtp.gmail.com"
      :user user
      :pass password
      :ssl :yes!!!11}
   {:from "iwillig@gmail.com"
    :to [to]
    :subject (str "Priorities for " (Date.))
    :body [{:type "text/html"
             :content body}]}))

(defn send-employee-priorities
  "Formats and sends a priority email to an employee"
  [e]
  (let [[user password] config/mail-config]
    (send-email user password (:email e)
                     (hiccup.core/html (trello/display-user-priorities e)))))

(defn send-priorities!
  "Function to send all of the priorities to each employee"
  []
  (sql/with-connection config/db
    (sql/with-query-results employees ["select * from employees"]
      (doseq [employee employees]
        (send-employee-priorities employee)))))

(deftype Task []
  org.quartz.Job
  (execute [this context]
    (println "Starting priorities")
    (send-priorities!)))

(defn make-job-details
  "Function to make building the job details less of a pain"
  [job name group]
  (let [b (JobBuilder/newJob (class job))]
    (.withIdentity b name group)
    (.build b)))

(defn make-cron-trigger
  "Function to make building a cron trigger easier"
  [name group cron]
  (let [b (TriggerBuilder/newTrigger)]
    (.withIdentity b name group)
    (.withSchedule b (CronScheduleBuilder/cronSchedule cron))
    (.build b)))

(defn boot-schedule
  "Function to boot the scheduler"
  [sched]
  (let [job (Task.)
        job-d (make-job-details job "job1" "group1")
        ;; for now run the job every 30 seconds
        trigger (make-cron-trigger "trigger1" "group1" "0,30 * * * * ?")]
    (.scheduleJob sched job-d trigger)
    (.start sched)))

(defn -main [& args]
  (let [sched (StdSchedulerFactory/getDefaultScheduler)]
    (boot-schedule sched) sched))