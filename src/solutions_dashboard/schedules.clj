(ns solutions-dashboard.schedules
  (:import
   [java.util Date]
   [org.quartz Job]
   [org.quartz JobBuilder]
   [org.quartz DateBuilder]
   [org.quartz TriggerBuilder]
   [org.quartz.impl StdSchedulerFactory]))


(deftype Task []
  org.quartz.Job
  (execute [this contenxt]
    (println (str "The job run at " (Date.)))))

(defn make-job-details [job name group]
  (let [b (JobBuilder/newJob (class job))]
    (.withIdentity b name group)
    (.build b)))

(defn make-trigger [name group run-at]
  (let [b (TriggerBuilder/newTrigger)]
    (.withIdentity b name group)
    (.startAt b run-at)
    (.build b)))


(defn -main [& args]
  (let [sched (StdSchedulerFactory/getDefaultScheduler)
        run-at (DateBuilder/evenSecondDate (Date.))
        job (Task.)
        job-d (make-job-details job "job1" "group1")
        trigger (make-trigger "trigger1" "group1" run-at)]
    (println (str "Booting the main project"))
    (println (str "Job" (.getKey job-d) "at" run-at))
    (.scheduleJob sched job-d trigger)
    (.start sched)
    (Thread/sleep 10000)
    (.shutdown sched true)))