(ns ryouta.crew
  (:require ["react-transition-group" :as rtg]
            [reagent.core :as r]
            [ryouta.state :as state]
            [ryouta.director :as direct]
            [ryouta.util :as util]))

(def css-transition-group
  (r/adapt-react-class rtg/CSSTransitionGroup))

(defn global-click-handler []
  (cond
    (> (count (:choices @state/dialogue)) 0)
    nil

    (:typing? @state/dialogue) 
    (direct/dispatch! [:dialogue-line-complete])

    (:progressible? @state/dialogue)
    (do
      (js/console.log "clickclick")
      (direct/read! @state/directions))))

(defn typewriter [text delay]
  (let [typed (r/atom "")
        prev-text (r/atom "")]
    (letfn [(type* [chars]
              (swap! typed str (first chars))
              (let [next (seq (rest chars))]
                (if (and next (:typing? @state/dialogue))
                  (js/setTimeout #(type* (seq (rest chars))) delay)
                  (direct/dispatch! [:dialogue-line-complete]))))]
      (fn [text delay]
        (when-not (= text @prev-text)
          (reset! prev-text text)
          (reset! typed "")
          (type* (seq text)))
        [:span (if (:typing? @state/dialogue) @typed (:line @state/dialogue))]))))

(defn dialogue []
  (when (:visible? @state/dialogue)
    [:div.ry-dialogue
     [:div.ry-dialogue-title (:actor @state/dialogue)]
     [:div.ry-dialogue-content [typewriter (:line @state/dialogue) 25]]]))

(defn choices []
  (when-let [options (:choices @state/dialogue)]
    [:div.ry-choices
     (for [{:keys [label option]} options]
       ^{:key label}
       [:div.ry-choice {:on-click #(direct/dispatch! [:choice-selected label])} option])]))

(defn actors []
  [css-transition-group
   {:transition-name "ry-actor"
    :transition-enter-timeout 500
    :transition-leave-timeout 500
    :class "ry-actors"}
   (for [[_id actor] @state/actors]
     ^{:key _id}
     (let [pos (util/calc-position (:position actor))]
       [:img.ry-actor {:src (:model actor)
                       :style {:left (when (:left pos) (str (:left pos) "vw"))
                               :right (when (:right pos) (str (:right pos) "vw"))}}]))])

(defn game []
  [:div.ry-game {:on-click global-click-handler}
   [:div.ry-background {:style
                        {:background (str "url(\"" (:background @state/scene) "\") no-repeat center center fixed")}}
    [dialogue]
    [choices]
    [actors]]])