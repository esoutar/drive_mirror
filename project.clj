(defproject drive_mirror "0.1.0-SNAPSHOT"
  :description "Google drive mirroring to any storage device"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:plugins [[lein-ring "0.8.5" :exclusions [org.clojure/clojure]]]
                   :ring {:handler drive-mirror.web-services/app}}}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.5" :exclusions [ring/ring-core org.clojure/core.incubator]]
                 [com.cemerick/friend "0.2.0" :exclusions [ring/ring-core]]
                 [friend-oauth2 "0.1.1" :exclusions [org.apache.httpcomponents/httpcore]]
                 [cheshire "5.2.0"]
                 [ring-server "0.3.0" :exclusions [ring]]]
  :aliases {"google" ["with-profile" "dev"
                      "do" "ring" "server-headless"]})
