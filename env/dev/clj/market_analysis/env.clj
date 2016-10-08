(ns market-analysis.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [market-analysis.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[market-analysis started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[market-analysis has shut down successfully]=-"))
   :middleware wrap-dev})
