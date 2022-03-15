(ns ethscan-dump.ethscan-dump
  (:import (org.jsoup Jsoup)))


(def etherscan-url "https://etherscan.io/")
(def contract-url (str etherscan-url "address/"))
(def v-contracts-url (str etherscan-url "contractsVerified/"))
(def accounts-url (str etherscan-url "accounts"))


(def page-size 100)


(def result-set (atom {}))


(defn fetch!
  [url selector]
  (-> url
      (Jsoup/connect)
      (.get)
      (.select selector)))


(defn fetch-abi
  [addr]
  (-> (str contract-url "/" addr "#code")
      (fetch! "#js-copytextarea2")
      (.text)))


(defn processed-html-table
  [rows col-count col-selector]
  (->> (for [r rows]
         (for [cell (.select r "td")]
           (.text cell)))
       (first)
       (partition-all col-count)
       (mapv col-selector)))


(defn fetch-name-tags []
  (dotimes [i 50]
    (let [rows          (fetch!  (str accounts-url "/" (inc i) "?ps=" page-size)
                                 "table > tbody")
          accs          (processed-html-table
                         rows
                         6
                         #(->> % (take 3) (drop 1)))
          filtered-accs (filter #(-> % second empty? not) accs)]
      (doseq [[addr name] filtered-accs]
        (reset! result-set
                (merge @result-set {addr {:name name}}))))))



;; TODO: make abi fetching concurrent
(defn fetch-verified-contracts []
  (dotimes [i 5]
    (let [rows  (fetch! (str v-contracts-url (inc i) "?ps=" page-size)
                        ".table > tbody")
          addrs (processed-html-table rows
                                      10
                                      (partial take 2))]
      (doseq [[addr contract] addrs]
        (reset! result-set
                (merge @result-set {addr
                                    {:name contract
                                     :abi  (fetch-abi addr)}}))))))



(comment

  (fetch! (str v-contracts-url (inc 1) "?ps=" page-size)
          ".table > tbody")

  (fetch-abi "0x8551880f3FE9bd29aA6bB8D89DD6ffa36DB2c73A")

  (count @result-set)

  (processed-html-table
   (fetch! accounts-url
           "table > tbody")
   6
   #(->> %
         (take 3)
         (drop 1)))

  ;; Should be 500
  (do (fetch-verified-contracts)
      (= (count  @result-set) 508))

  (fetch-name-tags)

  (count @result-set)


  (->> (for [r
             (fetch! accounts-url
                     "table > tbody")]
         (for [cell (.select r "td")]
           (.text cell)))
       (first)
       (partition-all 6)
       (mapv (partial take 2)))

  ;; Dump verified contracts in a file
  (spit "verified_contracts.edn" (pr-str @result-set))

  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
