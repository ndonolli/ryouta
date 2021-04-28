(ns ryouta.director
  "This module handles the parsing and execution of reading scenes"
  (:require [malli.core :as m]
            [malli.error :as me]
            [ryouta.state :refer [db]]
            [sci.core]))

(declare VALID-ACTIONS)

(defn- nil-or-invalid-error [empty-msg invalid-msg]
  (fn [{:keys [value]} _]
    (if (nil? value) empty-msg (str value " " invalid-msg))))

;; schemas
(defonce Actor [:map
                [:_id keyword?]
                [:name string?]
                [:models [:map-of :keyword :string]]])

(defonce Action [:fn {:error/fn (nil-or-invalid-error "Direction requires an action." "is not a valid action.")}
                 (partial VALID-ACTIONS)])

(defonce Direction [:catn
                    [:action :keyword]
                    [:args [:* any?]]])
;; global defs
(defonce ACTIONS
  {:scene {:schema []}
   :enter {:schema [:cat
                    [:= :enter]
                    [:schema Actor]
                    [:? :map]]}
   :exit {:schema []}
   :pose {:schema []}
   :says {:schema []}
   :choose {:schema []}
   :case {:schema []}
   :move {:schema []}})

(defonce VALID-ACTIONS (set (map first ACTIONS)))

(defn get-action-schema [action] (get-in ACTIONS [action :schema]))

(def valid-direction? (m/validator Direction))

;; action dispatch functions
(defmulti perform first)

(defmethod perform :scene
  [[_ scene opts]]
  (swap! db assoc :scene scene))

(defmethod perform :enter
  [[_ actor {:keys [position model] :as opts}]]
  (let [model* (if (nil? model)
                 (-> (:models actor) first second) ;; get first item in models as default
                 (get-in actor [:models :model]))]
    (swap! db update :actors assoc
           (:_id actor) (merge actor {:model model*
                                      :position position}))))

(defmethod perform :move
  [[_ {:keys [_id]} position]]
  (swap! db assoc-in [:actors _id :position] position))

(defmethod perform :exit
  [[_ actor]]
  (swap! db update :actors dissoc (:_id actor)))

(defmethod perform :says
  [[_ actor dialogue]]
  (swap! db assoc 
         :dialogue {:line dialogue
                    :actor (:name actor)
                    :visible? true
                    :typing? true}
         :progressible false))

(defmethod perform :group
  [[_ directions]]
  (doseq [direction directions]
    (perform direction)))

(defn store-history [direction]
  (swap! db update :history conj direction))

(defn next-direction []
  (swap! db update :directions rest))

(defn read! [directions]
  (let [direction (first directions)]
    (when-not (nil? direction)
      (if (valid-direction? direction)
        (do (perform direction)
            (store-history direction)
            (next-direction))
        
        (js/console.error 
         (str "\nError reading direction " direction " - "
              (-> Direction
                  (m/explain direction)
                  (me/humanize
                   {:errors (-> me/default-errors
                                (assoc ::m/missing-key {:error/fn (fn [{:keys [in]} _] (str "missing key " (last in)))}))})
                  flatten first)))))))
