(ns ryouta.director
  "This module handles the parsing and execution of reading scenes"
  (:require [malli.core :as m]
            [malli.error :as me]
            [reagent.core :as r]
            [ryouta.state :refer [db]]
            [sci.core]))

(declare VALID-ACTIONS)

(defn- nil-or-invalid-error [empty-msg invalid-msg]
  (fn [{:keys [value]} _]
    (if (nil? value) empty-msg (str value " " invalid-msg))))

;; schemas
(defonce Actor [:map
                [:name string?]])
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
   :case {:schema []}})

(defonce VALID-ACTIONS (set (map first ACTIONS)))

(defn get-action-schema [action] (get-in ACTIONS [action :schema]))


(defmulti perform first)

(defmethod perform :scene
  [[_ scene opts]]
  (swap! db assoc :scene scene))

(defmethod perform :enter
  [[_ actor]]
  (swap! db update :actors conj actor))

(defmethod perform :exit
  [[_ actor]]
  (swap! db update :actors disj actor))

(defmethod perform :says
  [[_ actor dialogue]]
  (swap! db assoc :dialogue {:content dialogue :visible true}))

(def valid-direction? (m/validator Direction))

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


(def nathan {:name "Nathan Donolli"})
(def makki {:name "Mackenzie Ferguson"})

(def town {:name "town" :background "https://lumiere-a.akamaihd.net/v1/images/sa_pixar_virtualbg_coco_16x9_9ccd7110.jpeg"})
(def beach {:name "beach" :background "https://globetrender.com/wp-content/uploads/2020/05/Caribbean-beach.jpg"})

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

(def myscript [[:scene town]
               [:enter nathan]
               [:says nathan "hi"]
               [:says nathan "another one"]
               [:scene beach]
               [:enter makki]
               [:says makki "now it's makki"]
               [:exit nathan]
               [:says makki "yoo"]
               [:choose ["hey" "ya"]]])
