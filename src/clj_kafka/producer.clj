(ns clj-kafka.producer
  (:import [kafka.javaapi.producer Producer]
           [kafka.producer ProducerConfig KeyedMessage]
           [kafka.message Message]
           [org.I0Itec.zkclient ZkClient]
           [kafka.utils ZkUtils]
           [java.util List])
  (:use [clj-kafka.core :only (as-properties with-resource)]
        [clojure.data.json :only (read-str)])
  (:require [zookeeper :as zk]))

(defn producer
  "Creates a Producer. m is the configuration
   metadata.broker.list : \"server:port,server:port\""
  [m]
  (Producer. (ProducerConfig. (as-properties m))))

(defn message
  ([topic value] (message topic nil value))
  ([topic key value] (KeyedMessage. topic key value)))

(defn send-message
  [^Producer producer ^KeyedMessage message]
  (.send producer message))

(defn send-messages
  [^Producer producer ^List messages]
  (.send producer messages))

(defn brokers
  "Get brokers from zookeeper"
  [m]
  (with-resource [z (zk/connect (get m "zookeeper.connect"))]
    zk/close
    (doall (map (comp #(read-str % :key-fn keyword)
                      #(String. ^bytes %)
                      :data
                      #(zk/data z (str "/brokers/ids/" %)))
                (zk/children z "/brokers/ids")))))
