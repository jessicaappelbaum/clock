(ns user
  (:require [clock.core :refer [-main app]]
            [org.httpkit.server :refer [run-server]]))


(def server-state* (atom nil))

(defn start
  "Starts the server from REPL"
  []
  (reset! server-state* (-main)))

(defn stop
  "Stops the server from REPL"
  []
  (@server-state*))

(defn restart
  "If you've amended a file, compile it and then restart the server with this function"
  []
  (stop)
  (start))
