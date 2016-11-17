(ns market-analysis.utils
  (:require [clojure.data.csv :as csv]
            [postal.core :as postal]))

(defn read-data [path dt]
  (let [file (clojure.java.io/file (str path "/data." dt))]
    (if (.exists file)
      (slurp file)
      nil)))

(defn read-csv [path]
  (with-open [in-file (clojure.java.io/reader path)]
    (doall
     (csv/read-csv in-file))))

(defn read-summary [path dt typ]
  (let [summary-csv (str path "/" dt "/" typ)
        summary-data (read-csv summary-csv)
        header (map keyword (first summary-data))
        type-index (.indexOf header :type)
        deal-one-line (fn [one-line]
                        (loop [one one-line
                               data {}
                               index 0]
                          (if (empty? one)
                            [(keyword (nth one-line type-index))
                             data]
                            (if (= index type-index)
                              (recur (rest one)
                                     data
                                     (inc index))
                              (recur (rest one)
                                     (assoc data
                                            (nth header index)
                                            (first one))
                                     (inc index))))))]
    (loop [data (rest summary-data)
           summary {}]
      (if (empty? data)
        summary
        (let [[type dealed-line] (deal-one-line (first data))]
          (recur (rest data)
                 (assoc summary
                        type
                        dealed-line)))))))

(defn read-summary-de [path dt]
  (let [summary-csv (str path "/summary." dt)
        summary-data (read-csv summary-csv)
        header (map keyword (first summary-data))]
    (loop [data (rest summary-data)
           summary {}]
      (if (empty? data)
        summary
        (let [one-line (first data)
              type (keyword (nth one-line (.indexOf header :type)))
              big-buy-trans-amount (nth one-line (.indexOf header :big-buy-trans-amount))
              big-sell-trans-amount (nth one-line (.indexOf header :big-sell-trans-amount))
              big-normal-trans-amount (nth one-line (.indexOf header :big-normal-trans-amount))
              count (nth one-line (.indexOf header :count))]
          (recur (rest data)
                 (assoc summary type {:big-buy-trans-amount big-buy-trans-amount
                                      :big-sell-trans-amount big-sell-trans-amount
                                      :big-normal-trans-amount big-normal-trans-amount
                                      :count count})))))))

(defn read-hs300
  ([hs300-csv]
   (let [hs300-data (read-csv hs300-csv)
         header (map keyword (first hs300-data))
         data (second hs300-data)]
     (loop [hd header
            index 0
            hs300 {}]
       (if (empty? hd)
         hs300
         (recur (rest hd)
                (inc index)
                (assoc hs300
                       (first hd)
                       (nth data index)))))))
  ([path dt]
   (read-hs300 (str path "/SH000300." dt))))

(defn get-today-date
  []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") (java.util.Date.)))

(defn get-yesterday-date
  []
  (let [date (java.util.Calendar/getInstance)]
    (.add date java.util.Calendar/DATE -1)
    (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") (.getTime date))))

(defn format-summary [summary]
  (let [types (sort (keys summary))]
    (loop [ts types
           re-str ""]
      (if (empty? ts)
        re-str
        (let [typ (first ts)
              data (typ summary)
              data-keys (sort (keys data))
              sorted-data-str (loop [d-keys data-keys
                                     re-str ""]
                                (if (empty? d-keys)
                                  re-str
                                  (let [one-key (first d-keys)
                                        one-data (one-key data)]
                                    (recur (rest d-keys)
                                           (str re-str (name one-key) ": " one-data "\n")))))]
          (recur (rest ts)
                 (str re-str (name typ) ":\n" sorted-data-str)))))))

(defn format-summary-de [summary]
  (let [sell-data (:sell summary)
        buy-data (:buy summary)
        format-data (fn [data]
                      (str "count: " (:count data) "\n"
                           "big-buy: " (:big-buy-trans-amount data) "\n"
                           "big-sell: " (:big-sell-trans-amount data) "\n"
                           "big-normal: " (:big-normal-trans-amount data) "\n"))]
    (str "buy:\n"
         (format-data buy-data)
         "sell:\n"
         (format-data sell-data))))

(defn format-hs300 [hs300]
  (str "date: " (:date hs300) "\n"
       "p change: " (:p_change hs300) "\n"
       "price change: " (:price_change hs300) "\n"
       "open: " (:open hs300) "\n"
       "close: " (:close hs300) "\n"
       "volume: " (:volume hs300) "\n"
       "high: " (:high hs300) "\n"
       "low: " (:low hs300) "\n"))

(defn send-mail [message]
  (postal/send-message {:host "smtp.qq.com"
                        :user "309456568@qq.com"
                        :pass "bhfnzsvanysgcaei"
                        :ssl true}
                       {:from "309456568@qq.com"
                        :to ["309456568@qq.com"]
                        :subject "daily summary"
                        :body message}))
