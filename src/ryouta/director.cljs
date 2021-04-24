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
   :case {:schema []}})

(defonce VALID-ACTIONS (set (map first ACTIONS)))

(defn get-action-schema [action] (get-in ACTIONS [action :schema]))

(def valid-direction? (m/validator Direction))

;; action dispatch functions
(defmulti perform first)

(defmethod perform :scene
  [[_ scene opts]]
  (swap! db assoc :scene scene))

(defmethod perform :enter
  [[_ actor model]] 
   (let [model* (if (nil? model) 
                  (-> (:models actor) first second) ;; get first item in models as default
                  model)] 
     (swap! db update :actors conj (merge actor {:model model*}))))

(defmethod perform :exit
  [[_ actor]]
  (swap! db update :actors 
         #(set (filter 
                (fn [current-actor] (not= (:name actor) (:name current-actor))) %))))

(defmethod perform :says
  [[_ actor dialogue]]
  (swap! db assoc :dialogue {:line dialogue :actor (:name actor) :visible true}))

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


(def nathan {:name "Nathan" :models {:default "http://assets.stickpng.com/images/580b57fcd9996e24bc43c2fe.png"}})
(def makki {:name "Makki" :models {:default "https://i.pinimg.com/originals/fd/0f/2a/fd0f2ae1480569b61e2a1145af8bcbde.png"}})

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
               [:group [[:enter nathan]
                        [:says nathan "Hi it's me nathan"]]]
               [:says nathan "I'm just here chillin in this town"]
               [:says nathan "...but it would be nice to go to the beach!"]
               [:group [[:scene beach]
                        [:says nathan "This is more like it!"]]]
               [:enter makki]
               [:says nathan "Who is that babe?"]
               [:says makki "omg wtf creep"]
               [:says nathan "Wait...I"]
               [:says makki "Get lost, loser."]
               [:exit nathan]
               [:says makki "Finally I have the beach to myself."]
               [:says makki "Thank you for coming to my TED talk."]])
