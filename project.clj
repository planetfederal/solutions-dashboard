(defproject solutions-dashboard "1.0.0-SNAPSHOT"
  :description "FIXME: write description"  
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [hiccup "0.3.8"]
                 [clj-http "0.3.2"]
                 [rmarianski/ring-jetty-servlet-adapter "0.0.2"]
                 [com.draines/postal "1.7-SNAPSHOT"]
                 [compojure "1.0.1"]
                 [org.clojure/java.jdbc "0.1.1"]
                 [org.clojure/data.json "0.1.2"]
                 [postgresql "9.0-801.jdbc4"]]
  :dev-dependencies [[swank-clojure "1.4.0"]
                     [lein-ring "0.5.4"]]
  :ring {:handler solutions-dashboard.core/app})