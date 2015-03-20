(ns user
  (:require [taoensso.timbre :as log]
            [com.palletops.leaven :as leaven]
            [drive-mirror.web-services :as services]
            [drive-mirror.web-server :as web]))

(def application-system nil)

(def application nil)

(leaven/defsystem ApplicationSystem [:server])

(defn- make-application-system
  []
  (map->ApplicationSystem {:server (web/make-web-server @services/app
                                                        {:host "0.0.0.0"
                                                         :port 8080})}))

(defn init
  "Creates and initializes the system under development in the Var
  #'application"
  []
  (alter-var-root #'application-system (constantly (make-application-system))))

(defn start
  "Starts the system running, updates the Var #'application"
  []
  (alter-var-root #'application-system leaven/start))

(defn started?
  "Return a truthy value indicating whether the application is started."
  []
  (not (nil? application-system)))

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'application"
  []
  (alter-var-root #'application-system leaven/stop)
  (alter-var-root #'application-system (fn [_] nil)))

(defn go
  "Initializes and starts the system running."
  []
  {:pre [(= false (started?))]}
  (init)
  (log/set-level! :info)
  (log/set-config! [:appenders :spit :enabled?] true)
  (log/set-config! [:shared-appender-config :spit-filename] "./drive-mirror.log")
  (start)
  :ready)

(def run go)
