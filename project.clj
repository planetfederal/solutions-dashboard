(defproject solutions-dashboard "1.0.0-SNAPSHOT"
  :description "OpenGeo solutions dashboard"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [clj-http "0.3.2"]
                 [rmarianski/ring-jetty-servlet-adapter "0.0.2"]
                 [ring-json-params "0.1.3"]
                 [compojure "1.0.1"]
                 [clj-decline "0.0.5"]
                 [org.quartz-scheduler/quartz "2.1.2"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [org.slf4j/slf4j-simple "1.6.4"]
                 [com.draines/postal "1.7-SNAPSHOT"]
                 [org.clojure/java.jdbc "0.1.1"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/data.csv "0.1.2"]
                 [postgresql "9.0-801.jdbc4"]]
  :dev-dependencies [[swank-clojure "1.4.0"]
                     [lein-ring "0.5.4"]]
  :ring {:handler solutions-dashboard.core/app})