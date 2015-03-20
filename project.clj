(defproject drive_mirror "0.1.0-SNAPSHOT"
  :description "Google drive mirroring to any storage device"
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :profiles {:dev {:plugins [[lein-ring "0.8.5" :exclusions [org.clojure/clojure]]]
                   :ring {:handler drive-mirror.web-services/app}
                   :source-paths ["dev"]
                   :main user}}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [stuarth/clj-oauth2 "0.3.2" :exclusions [org.clojure/clojure]]
                 [cheshire "5.4.0"]
                 [compojure "1.3.1"]
                 [ring/ring-core "1.3.2" :exclusions [clj-time]]
                 [ring/ring-defaults "0.1.4"]
                 [clj-time "0.8.0"]
                 [com.palletops/leaven "0.2.0"]
                 [com.taoensso/timbre "3.3.1"]
                 [org.immutant/immutant "2.0.0-beta2"]
                 [com.cemerick/friend "0.2.1"]]
  :aliases {"google" ["with-profile" "dev"
                      "do" "ring" "server-headless"]})
