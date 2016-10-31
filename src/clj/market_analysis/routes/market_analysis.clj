(ns market-analysis.routes.market-analysis
  (:require [compojure.core :refer [defroutes GET POST]]
            [clojure.tools.logging :as log]
            [clojure.data.xml :as xml]

            [market-analysis.config :refer [env]]
            [market-analysis.utils :as utils]))

(defn market-analysis-check [req]
  (log/info "req:" req)
  (let [params (:params req)
        echostr (:echostr params)]
    (log/info "echostr:" echostr)
    echostr))

(defn market-analysis [req]
  (log/info "req:" req)
  (let [body (xml/parse (:body req))
        _ (log/debug body)
        to-user-name (first (:content (first (filter #(= (:tag %) :ToUserName) (:content body)))))
        _ (log/debug to-user-name)
        from-user-name (first (:content (first (filter #(= (:tag %) :FromUserName) (:content body)))))
        _ (log/debug from-user-name)
        content (first (:content (first (filter #(= (:tag %) :Content) (:content body)))))
        _ (log/debug content)
        _ (log/debug (env :data-path))
        _ (log/debug (utils/get-today-date))
        _ (log/debug (utils/read-data (env :data-path)
                                      (utils/get-today-date)))
        re-content (if (= content "data")
                     (let [today-data (utils/read-data (env :data-path)
                                                       (utils/get-today-date))
                           yesterday-data (utils/read-data (env :data-path)
                                                           (utils/get-yesterday-date))]
                       (if-not (nil? today-data)
                         today-data
                         yesterday-data))
                     content)
        re-xml (xml/emit-str (xml/element :xml {}
                                          (xml/element :ToUserName {} (xml/cdata from-user-name))
                                          (xml/element :FromUserName {} (xml/cdata to-user-name))
                                          (xml/element :CreateTime {} (quot (System/currentTimeMillis) 1000))
                                          (xml/element :MsgType {} (xml/cdata "text"))
                                          (xml/element :Content {} (xml/cdata re-content))))
        cut-head-re (.substring re-xml 38)
        _ (log/info cut-head-re)]
    cut-head-re))

(defroutes market-analysis-routes
  (GET "/market-analysis" [] (fn [req] (market-analysis req)))
  (POST "/market-analysis" [] (fn [req] (market-analysis req))))
