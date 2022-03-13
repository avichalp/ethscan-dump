(ns ethscan-dump.ethscan-dump
  (:import (org.jsoup Jsoup)))


(def URL "https://etherscan.io/contractsVerified/1?ps=100")

(defn get-page []
  (.get (Jsoup/connect URL)))

(defn get-elems [page css]
  (.select page css))

(comment

  ;; Get Rows
  (let [html (get-page)
        header (get-elems html ".table > thead > tr")]
    (take 2 (clojure.string/split (.text (first header)) #" ")))


  ;; Get data contract addr and name
  (let [html (get-page)
        rows (get-elems html ".table > tbody")]
    (mapv #(take 2 %)
          (partition-all
           10
           (first
            (for [r rows]
              (for [cell (get-elems r "td")]
                (.text cell)))))))


  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
