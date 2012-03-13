(ns solutions-dashboard.auth)

(defn session-get-user [request]
  (.getAttribute (:ses request) "user"))

(defn session-save-user [request user]
  (.setAttribute (:ses request) "user" user))

(defn session-delete [request]
  (.invalidate (:ses request)))
