(ns user
  (:require [mount.core :as mount]
            market-analysis.core))

(defn start []
  (mount/start-without #'market-analysis.core/repl-server))

(defn stop []
  (mount/stop-except #'market-analysis.core/repl-server))

(defn restart []
  (stop)
  (start))


