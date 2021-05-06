(ns ryouta.director
  "This module handles the parsing and execution of reading scenes"
  (:require [malli.core :as m]
            [malli.error :as me]
            [ryouta.state :refer [db vars] :as state]
            [ryouta.util :refer [log! in? timeout->]]
            [sci.core]))

(declare VALID-ACTIONS)
(declare read!)

(defonce SPECIAL-FORMS [:cond :if])

;; helper fns, although I'm not sure how necessary this one is
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

(defonce Choices [:cat [:+ :string]])
(defonce ChoicesLabeled [:cat [:+ [:tuple keyword? string?]]])

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

(def valid-direction? (m/validator Direction))

(defn store-history [direction]
  (swap! db update :history conj direction))

(defn set-next-directions [directions]
  (swap! db assoc :directions directions))

;;----------
;; Perform
;;----------
(defmulti perform* first)
(defn perform!
  "This function is used to parse a direction and handle the business logic for updating the game state.
   Each direction is a record of [action & arguments?].  In most cases, directions defined in a script
   will be executed by `read!`, which calls this method. However, `perform!` can be used independently 
   to dispatch game events."
  [direction] (perform* direction))

;; User-centric action dispatches for directions used in scripts and game events
(defmethod perform* :scene
  [[_ scene opts]]
  (let [default-opts {:transition? true}
        {:keys [transition?]} (merge default-opts opts)
        delay-ms (:transition-ms @state/game-settings)]
         
    (if-not transition?
      (swap! db assoc :scene scene)
      (timeout->
       0 
       #(swap! db (fn [db*] (-> db*
                                (assoc :overlay? true)
                                (assoc-in [:dialogue :progressible?] false))))
       delay-ms 
       #(swap! db (fn [db*] (-> db*
                                (assoc :scene scene)
                                (assoc :overlay? false)
                                (assoc-in [:dialogue :progressible?] true)
                                (assoc-in [:dialogue :visible?] false))))))))

(defmethod perform* :enter
  [[_ actor {:keys [position model] :as opts}]]
  (let [model (if (nil? model)
                 (-> (:models actor) first second) ;; get first item in models as default
                 (get-in actor [:models :model]))
        position (if (nil? position) :center position)]
    (swap! db update :actors assoc
           (:_id actor) (merge actor {:model model
                                      :position position}))))

(defmethod perform* :move
  [[_ {:keys [_id]} position]]
  (swap! db assoc-in [:actors _id :position] position))

(defmethod perform* :exit
  [[_ actor]]
  (swap! db update :actors dissoc (:_id actor)))

(defmethod perform* :says
  [[_ actor dialogue]]
  (swap! db assoc
         :dialogue {:line dialogue
                    :actor (:name actor)
                    :visible? true
                    :typing? true
                    :progressible? false}))

(defmethod perform* :group
  [[_ directions]]
  (doseq [direction directions]
    (perform* direction)))

(defmethod perform* :choose
  [[_ options]]
  (let [choices
        (cond
          (m/validate Choices options)
          (map-indexed
           (fn [i option] {:label (keyword (str "%" (inc i)))
                           :option option})
           options)

          (m/validate ChoicesLabeled options)
          (map (fn [[label option]] {:label label :option option})
               options))]

    (swap! db update :dialogue assoc :choices choices :progressible? false)))

(defmethod perform* :if
  [[_ pred if-cond else-cond :as direction]]
  (when (or (nil? pred) (nil? if-cond))
    (throw (js/Error. (str "Error reading direction :if - a predicate and if-condition are required - " direction))))
  (let [directions (if (get @vars pred) 
                     (vector if-cond) 
                     (if (nil? else-cond) nil (vector else-cond)))
        state-directions (:directions @db)]
    (read! (concat (vector directions) state-directions))))
    

(defmethod perform* :cond
  [[_ & clauses]]
  (when (> (mod (count clauses) 2) 0)
    (throw (js/Error. (str "Error reading direction :cond - uneven number of forms passed - " clauses))))
  (loop [clauses* (partition 2 clauses)]
    (let [[var* directions] (first clauses*)]
      (if (get @vars var*)
        (read! (concat (vector directions) (rest (:directions @db))))
        (when (next clauses*)
          (recur (next clauses*)))))))

;; Event dispatchers used more internally, although accessible to the user if needed
(defmethod perform* :dialogue-line-complete
  []
  (swap! db update :dialogue assoc :typing? false :progressible? true))

(defmethod perform* :choice-selected
  [[_ label]]
  (swap! db (fn [old-db] 
              (-> old-db
                  (assoc-in [:vars label] true)
                  (update :dialogue assoc :choices nil :progressible? true)))))

;; Main read definition
(defn read! [directions]
  (let [direction (first directions)]
    (when-not (nil? direction)
      (if (vector? (first direction))
        ;; for when direction is a vector of nested direction
        ;; nested vectors do not work like a "stack" currently.
        ;; this is only the case for :if and :cond forms
        (read! direction)
        (if (valid-direction? direction)
          (do (perform* direction)
              
              ;; special forms like :cond, :if, etc. are unique cases that handle this functionality explicitly
              (when-not (in? SPECIAL-FORMS (first direction))
                (store-history direction)
                (set-next-directions (rest directions))))

          (js/console.error
           (str "Error reading direction " direction " - "
                (-> Direction
                    (m/explain direction)
                    (me/humanize
                     {:errors (-> me/default-errors
                                  (assoc ::m/missing-key {:error/fn (fn [{:keys [in]} _] (str "missing key " (last in)))}))})
                    flatten first))))))))