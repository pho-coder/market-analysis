(ns market-analysis.watcher
  (:require [mount.core :as mount]
            [clojure.tools.logging :as log]

            [market-analysis.config :refer [env]]
            [market-analysis.utils :as utils]))

(mount/defstate last-report-dt :start "")

(defn report-watcher []
  (log/info "report watching...")
  (let [dt (utils/get-today-date)
        hs300-csv (str (env :data-path) "/SH000300." dt)]
    (if (and (not= dt last-report-dt)
             (.exists (clojure.java.io/as-file hs300-csv)))
      (let [re (utils/send-mail (utils/format-hs300 (utils/read-hs300 hs300-csv)))]
        (if (= (:code re) 0)
          (mount/start-with {#'last-report-dt dt})
          (log/error re))))))
