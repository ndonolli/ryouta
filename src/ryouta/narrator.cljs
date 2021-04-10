(ns ryouta.narrator
  "This module handles the parsing and execution of reading scenes"
  (:require [clojure.spec.alpha :as s]
            [malli.core :as m]
            [malli.error :as me]))

(def valid-actions #{:scene :enter :exit :pose :says})

(s/def ::direction (s/cat :action keyword?
                          :args (s/+ (s/alt :arg keyword? :arg string?))))
(s/def ::name string?)
(s/def ::actor (s/keys :req-un [::name]))
(s/def ::actors (s/map-of keyword? ::actor))
(s/def ::setting (s/keys :req-un [::actors]))
(s/def ::script (s/cat :setting ::setting
                       :directions (s/+ (s/and vector? ::direction))))

(def Actor [:map
            [:name string?]])

(def Action [:fn {:error/fn (fn [{:keys [value]} _] (str "not a valid action: " value))}
              (partial valid-actions)])

(def Direction [:catn
                [:action
                 [:schema Action]]
                [:args [:* any?]]])

(def nathan {:name "Nathan Donolli"})
(def makki {:name "Mackenzie Ferguson"})

(defn perform [direction]
  (tap> direction))

(def valid-direction? (m/validator Direction))

(defn read [direction]
  ;; validate
  (when-not (valid-direction? direction)
    (throw (js/Error.
            (str "Error reading direction " direction "\n"
                 (-> Direction
                     (m/explain direction)
                     (me/humanize
                      {:errors (-> me/default-errors
                                   (assoc ::m/missing-key {:error/fn (fn [{:keys [in]} _] (str "missing key " (last in)))}))}))))))
  ;; main
  (perform direction))

(def scene [[:enter nathan]
            [:says nathan "ayy lmao" :happy]
            [:exit nathan]
            [:exit]])

(doseq [direction scene]
  (read direction))
