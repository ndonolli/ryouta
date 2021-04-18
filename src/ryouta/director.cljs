(ns ryouta.director
  "This module handles the parsing and execution of reading scenes"
  (:require [malli.core :as m]
            [malli.error :as me]
            [reagent.core :as r]
            [ryouta.state :refer [db]]))

(declare VALID-ACTIONS)

(defn- nil-or-invalid-error [empty-msg invalid-msg]
  (fn [{:keys [value]} _]
    (if (nil? value) empty-msg (str value " " invalid-msg))))

;; schemas
(def Actor [:map
            [:name string?]])

(def Action [:fn {:error/fn (nil-or-invalid-error "Direction requires an action." "is not a valid action.")}
             (fn [action] (seq (VALID-ACTIONS action)))])

(def Direction [:catn
                [:action keyword?]
                [:args [:* any?]]])

;; global defs
(def ACTIONS
  {:scene {:schema []}
   :enter {:schema [:cat
                    [:= :enter]
                    [:schema Actor]
                    [:? :map]]}
   :exit {:schema []}
   :pose {:schema []}
   :says {:schema []}
   :choose {:schema []}
   :case {:schema []}})

(defonce VALID-ACTIONS (set (map first ACTIONS)))

(defn get-action-schema [action] (get-in ACTIONS [action :schema]))

(def nathan {:name "Nathan Donolli"})
(def makki {:name "Mackenzie Ferguson"})

;; (defn perform [x] (tap> x))
(defmulti perform first)
(defmethod perform :scene
  ([[_ scene opts]]
   (swap! db assoc-in [:game :scene] scene)))

(def valid-direction? (m/validator Direction))

(defn read "foo" [direction]
  ;; validate
  (when-not (valid-direction? direction)
    (throw (js/Error.
            (str "\nError reading direction " direction " - "
                 (-> Direction
                     (m/explain direction)
                     (me/humanize
                      {:errors (-> me/default-errors
                                   (assoc ::m/missing-key {:error/fn (fn [{:keys [in]} _] (str "missing key " (last in)))}))})
                     flatten first)))))
  ;; main
  (perform direction))

(def scene [[:enter nathan]
            [:says nathan "ayy lmao" :happy]
            [:exit nathan]
            [:choose [[:option1 "Option 1"] [:option2 "Option 2"]]]
            [:case 
             :option1 [[:foo :bar]
                       [:foo :bar]]
             :option2 [:foo :bar]]
            [:choose ["Option 1" "Option 2"]]
            [:case
             :%1 [:hello]
             :%2 [[:hello nathan]
                  [:there nathan]]]])


(read [:scene {:backdrop "https://socialistmodernism.com/wp-content/uploads/2017/07/placeholder-image.png"}])

(defn foo [[a & b]]
  [a b])

(comment
  (foo [1 2 3])
  (perform [:scene {:backdrop "https://socialistmodernism.com/wp-content/uploads/2017/07/placeholder-image.png"}])
  (me/humanize (m/explain (get-action-schema :enter) [:enter])))

