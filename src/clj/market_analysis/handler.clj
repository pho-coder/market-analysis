(ns market-analysis.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [market-analysis.layout :refer [error-page]]
            [market-analysis.routes.home :refer [home-routes]]
            [compojure.route :as route]
            [market-analysis.env :refer [defaults]]
            [mount.core :as mount]
            [market-analysis.middleware :as middleware]
            [market-analysis.routes.market-analysis :refer [market-analysis-routes]]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (-> #'market-analysis-routes
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
