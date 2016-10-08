(ns market-analysis.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[market-analysis started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[market-analysis has shut down successfully]=-"))
   :middleware identity})
