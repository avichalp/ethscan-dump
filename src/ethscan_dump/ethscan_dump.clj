(ns ethscan-dump.ethscan-dump
  (:import (org.jsoup Jsoup)))


(def URL "https://etherscan.io/contractsVerified/")


(def page-size 100)


(defn get-page [i]
  (.get (Jsoup/connect (str URL i "?ps=" page-size))))


(defn get-elems [page css]
  (.select page css))


(def result-set (atom {}))



(defn fetch-verified-contracts! []
  (dotimes [i 5]
    (let [html           (get-page (inc i))
          rows           (get-elems html ".table > tbody")
          contract-addrs (->> (for [r rows]
                                (for [cell (get-elems r "td")]
                                  (.text cell)))
                              (first)
                              (partition-all 10)
                              (mapv (partial take 2)))]
      (doseq [[addr contract] contract-addrs]
        (reset! result-set (merge @result-set {addr contract}))))))



(comment

  ;; Get Rows
  (let [html (get-page 2)
        header (get-elems html ".table > thead > tr")]
    (take 2 (clojure.string/split (.text (first header)) #" ")))


  ;; Should be 500
  (do (fetch-verified-contracts!)
      (= (count  @result-set) 500))


  ;; Dump verified contracts in a file
  (spit "verified_contracts.edn" (pr-str @result-set))

  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
