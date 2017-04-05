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
        content (clojure.string/lower-case (first (:content (first (filter #(= (:tag %) :Content) (:content body))))))
        _ (log/debug content)
        data-path (env :data-path)
        _ (log/debug data-path)
        summary-path (env :summary-path)
        _ (log/debug (utils/get-today-date))
        get-dt (fn [content which]
                 (let [content-length (.length content)
                       which-length (.length which)]
                   (if (= content-length which-length)
                     (utils/get-today-date)
                     (.substring content (inc which-length)))))
        re-content (cond
                     (.startsWith content "hs300") (utils/format-hs300 (utils/read-hs300 data-path
                                                                                         (get-dt content "hs300")))
                     (.startsWith content "all") (utils/format-summary (utils/read-summary summary-path
                                                                                           (get-dt content "all")
                                                                                           "all"))
                     (.startsWith content "morning") (utils/format-summary (utils/read-summary summary-path
                                                                                              (get-dt content "morning")
                                                                                              "morning"))
                     (.startsWith content "afternoon") (utils/format-summary (utils/read-summary summary-path
                                                                                                 (get-dt content "afternoon")
                                                                                                 "afternoon"))
                     (.startsWith content "amount-10") (utils/format-summary (utils/read-summary summary-path
                                                                                                 (get-dt content "amount-10")
                                                                                                 "amount-10"))
                     (.startsWith content "volume-400") (utils/format-summary (utils/read-summary summary-path
                                                                                                  (get-dt content "volume-400")
                                                                                                  "volume-400"))
                     (.startsWith content "top") (utils/format-summary (utils/read-summary summary-path
                                                                                           (get-dt content "top")
                                                                                           "all.top-trans-amount"))
                     :else content)
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
