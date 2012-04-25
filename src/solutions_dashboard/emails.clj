(ns solutions-dashboard.emails
  (:import [java.util Date])
  (:require
   [hiccup.core :as html]
   [postal.core :as postal]
   [clojure.java.jdbc :as sql]
   [solutions-dashboard.config :as config]
   [solutions-dashboard.harvest :as harvest]
   [solutions-dashboard.trello :as trello]))

(defn sum-hours [entries]
  (/ (Math/round (* 100
                    (reduce (fn [rs n] (+ rs (Double/parseDouble (:hours n)))) 0 entries)
                    )) 100.0))

(defn build-harvest
  "TODO add time"
  [harvest]
  [:div
   [:p "Time reported over the last two weeks"]
   [:ul
    (for [pr harvest]
      [:li (:name pr)
       [:ul
        (for [t (:tasks pr)]
          [:li [:p (:name t) (str (sum-hours (:entries t)) "hour(s) spent" )]])]])]])

(defn build-trello [trello]
  [:ul
   (for [type trello]
     [:li [:p (str (clojure.string/capitalize (name (first type))) " tasks in Trello")]
      [:ul      
       (for [project (second type)]
         (for [task (:tasks project)]
           [:li (:name (:project project)) " " (:name task)]
           ))]])])


(defn build-message-body [user trello harvest]
  (html/html
   [:h3 "This is an automatically-generated email showing the time you've reported to Harvest and tasks you've been assigned in Trello."]
   (build-harvest harvest)
   [:br] [:br]
   (build-trello trello)
   [:br]
   [:p "Current and Upcoming Priorities - see the "
    [:a {:href " https://docs.google.com/a/opengeo.org/spreadsheet/ccc?key=0AjFRvgbAX5-OdGVfVGJTaHBqRlpEV1R4bDVyQTB6bkE&pli=1#gid=0"} "Solutions Team Resources document:"]]
   [:p "See any errors? Make your updates now!"]
   [:p [:a {:href "https://opengeo.harvestapp.com/daily"}
        "https://opengeo.harvestapp.com/daily"]]
   [:p [:a {:href "https://trello.com/"} "https://trello.com/"]]))


(defn build-message [e]
  (let [trello    (trello/get-filtered-tasks (:trello_username  e))
        weeks     (harvest/two-weeks)
        harvest   (harvest/get-user-tasks e (first weeks) (second weeks))]
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
    :body [{:type "text/html"
            :content body}]}))

(defn send-message [e]
  (send-email e (build-message e)))

(defn send-emails-to-all []
  (sql/with-connection config/db
    (sql/with-query-results employees  ["select * from employees"]
      (doseq [e employees]
        (send-message e)))))