(ns ryouta.director
  "This module handles the parsing and execution of reading scenes"
  (:require [malli.core :as m]
            [malli.error :as me]
            [ryouta.state :refer [db vars]]
            [ryouta.util :refer [log! in?]]
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


;; "peform" is a special dispatch method used by "read!" below.
;; It is used to parse a direction and handle the business logic for updating the game state.
;; Each direction has a struct of [action & arguments]
;; The arguments can range from simple parameters to other nested directions as used in the
;; special aciton forms like :cond and :if
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
                    :typing? true
                    :progressible? false}))

(defmethod perform :group
  [[_ directions]]
  (doseq [direction directions]
    (perform direction)))

(defmethod perform :choose
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


(defmethod perform :cond
  [[_ & clauses]]
  (when (> (mod (count clauses) 2) 0)
    (throw (js/Error. (str "Error reading direction: uneven number of forms passed to :cond => " clauses))))
  (loop [clauses* (partition 2 clauses)]
    (let [[var* directions] (first clauses*)]
      (if (get @vars var*)
        (read! (vector directions))
        (when (next clauses*) 
          (recur (next clauses*)))))))


;; "dispatch!" is a more generic event handler for changing the global game state.
;; Each event has a struct of [event-name data].
;; This is used more internally to handle state changes related to user events, which do not
;; fit into the more high-level directions defined by the end-user and passed into "perform"
(defmulti dispatch! first)

(defmethod dispatch! :dialogue-line-complete
  []
  (swap! db update :dialogue assoc :typing? false :progressible? true))

(defmethod dispatch! :choice-selected
  [[_ label]]
  (tap> label)
  (swap! db (fn [db*] (-> db*
                          (assoc-in [:vars label] true)
                          (update :dialogue assoc :choices nil :progressible? true)))))


(defn store-history [direction]
  (swap! db update :history conj direction))

(defn set-next-directions [directions]
    (swap! db assoc :directions directions))

(defn read! [directions]
  (let [direction (first directions)]
    (when-not (nil? direction)
      (if (vector? (first direction))
        (read! direction) ;; direction is a vector of nested directions
        (if (valid-direction? direction)
          (do (perform direction)
              (store-history direction)
              ;; special forms like :cond, :if, etc. are unique cases
              (when-not (in? SPECIAL-FORMS (first direction))
                  (set-next-directions (rest directions))))

          (js/console.error
           (str "\nError reading direction " direction " - "
                (-> Direction
                    (m/explain direction)
                    (me/humanize
                     {:errors (-> me/default-errors
                                  (assoc ::m/missing-key {:error/fn (fn [{:keys [in]} _] (str "missing key " (last in)))}))})
                    flatten first))))))))


(def a [:cond
        :option1 [[:foo :bar]
                  [:foo :bar]]
        :option2 [:foo :bar]
        :option3])
