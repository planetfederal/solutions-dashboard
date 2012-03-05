(ns solutions-dashboard.schedules
  (:import
   [java.util Date]
   [org.quartz Job]
   [org.quartz JobBuilder]
   [org.quartz DateBuilder]
   [org.quartz CronScheduleBuilder]
   [org.quartz TriggerBuilder]
   [org.quartz.impl StdSchedulerFactory]))

;; sample task right now,
;; only print out the time
(deftype Task []
  org.quartz.Job
  (execute [this context]
    (println (str "The job ran at: " (Date.)))))

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
  (let [run-at (DateBuilder/evenSecondDate (Date.))
        job (Task.)
        job-d (make-job-details job "job1" "group1")
        ;; for now run the job every 30 seconds
        trigger (make-cron-trigger "trigger1" "group1" "0,30 * * * * ?")]
    (.scheduleJob sched job-d trigger)
    (.start sched)))

(defn -main [& args]
  (let [sched (StdSchedulerFactory/getDefaultScheduler)]
    (boot-schedule sched) sched))