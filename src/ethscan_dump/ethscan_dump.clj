(ns ethscan-dump.ethscan-dump
  (:import (org.jsoup Jsoup)))


(def etherscan-url "https://etherscan.io/")
(def contract-url (str etherscan-url "address/"))
(def v-contracts-url (str etherscan-url "contractsVerified/"))


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

(defn v-contract-addrs [rows]
  (->> (for [r rows]
         (for [cell (.select r "td")]
           (.text cell)))
       (first)
       (partition-all 10)
       (mapv (partial take 2))))

;; TODO: make abi fetching concurrent
(defn fetch-verified-contracts []
  (dotimes [i 5]
    (let [rows           (fetch! (str URL (inc i) "?ps=" page-size)
                                 ".table > tbody")
          contract-addrs (v-contract-addrs rows)]
      (doseq [[addr contract] contract-addrs]
        (reset! result-set
                (merge @result-set {addr
                                    {:name contract
                                     :abi  (fetch-abi addr)}}))))))



(comment

  (fetch-abi "0x8551880f3FE9bd29aA6bB8D89DD6ffa36DB2c73A")


  ;; Should be 500
  (do (fetch-verified-contracts)
      (= (count  @result-set) 508))

  (first @result-set)


  ;; Dump verified contracts in a file
  (spit "verified_contracts.edn" (pr-str @result-set))

  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
