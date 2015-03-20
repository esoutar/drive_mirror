(ns drive-mirror.web-server
  (:require [immutant.web :as web]
            [com.palletops.leaven :as leaven]
            [com.palletops.leaven.protocols :refer [Startable Stoppable]]))

(defrecord WebServer [handler opts]

  Startable
  (start [component]
    (let [server (web/run handler opts)]
      (assoc component :server server)))

  Stoppable
  (stop [component]
    (let [{:keys [server]} component]
      (when server (web/stop server))
      (dissoc component :server)))

  java.io.Closeable
  (close [component] (leaven/stop component)))

(defn make-web-server
  [handler & [opts]]
  (->WebServer handler opts))
