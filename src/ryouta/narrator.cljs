(ns ryouta.narrator
  "This module handles the parsing and execution of reading scenes"
  (:require [clojure.spec.alpha :as s]))

(def valid-actions #{:scene :enter :exit :pose})

(s/def ::direction (s/cat :action keyword?
                          :args (s/+ (s/alt :arg keyword? :arg string?))))

; (def direction (s/and vector? ::direction))
(s/def ::name string?)
(s/def ::actor (s/keys :req-un [::name]))
(s/def ::actors (s/map-of keyword? ::actor))
(s/def ::setting (s/keys :req-un [::actors]))

(s/def ::script (s/cat :setting ::setting
                       :directions (s/+ (s/and vector? ::direction))))

(defn perform [action args]
  (tap> [action args]))

(defn- validate-direction [[action & args :as direction] setting]
  (let [err-reason (partial str "Error reading direction: " direction " - ")]
    (when-not (vector? direction)
      (throw (js/Error. (err-reason "direction must be a vector type"))))
    (when-not (> (count direction) 1)
      (throw (js/Error. (err-reason "direction must contain an action and one or more arguments"))))
    (when-not (or (valid-actions action) (get-in setting [:actors action]))
      (throw (js/Error. (err-reason "invalid action or actor: '" action "'"))))))

(defn read [script]
  (let [setting (first script)
        directions (rest script)]
    (doseq [[action & args :as direction] directions]
      (validate-direction direction setting)
      (perform action args))))

(s/fdef read :args (s/cat :script ::script))

(def nathan {:name "Nathan Donolli"})
(def makki {:name "Mackenzie Ferguson"})

(def script_1 [{:actors {:nathan nathan
                         :makki makki}}
               [:scene :foo]
               [:enter :nathan]
               [:nathan "ayy lmao" :nice]
               [:makki "nice"]
               [:exit :nathan]])

(read script_1)