(ns market-analysis.utils)

(defn read-data [path dt]
  (let [file (clojure.java.io/file (str path "/data." dt))]
    (if (.exists file)
      (slurp file)
      nil)))

(defn get-today-date
  []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") (java.util.Date.)))

(defn get-yesterday-date
  []
  (let [date (java.util.Calendar/getInstance)]
    (.add date java.util.Calendar/DATE -1)
    (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") (.getTime date))))
