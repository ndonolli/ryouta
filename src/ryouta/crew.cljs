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
    (direct/perform! [:dialogue-line-complete])

    (:progressible? @state/dialogue)
    (direct/read! @state/directions)))

(defn typewriter [text delay]
  (let [typed (r/atom "")
        prev-text (r/atom "")]
    (letfn [(type* [chars]
              (swap! typed str (first chars))
              (let [next (seq (rest chars))]
                (if (and next (:typing? @state/dialogue))
                  (js/setTimeout #(type* (seq (rest chars))) delay)
                  (direct/perform! [:dialogue-line-complete]))))]
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
       [:div.ry-choice {:on-click #(direct/perform! [:choice-selected label])} option])]))

(defn actors []
  [css-transition-group
   {:transition-name "ry-actor"
    :transition-enter-timeout (:transition-ms @state/game-settings)
    :transition-leave-timeout (:transition-ms @state/game-settings)
    :class "ry-actors"}
   (for [[_id actor] @state/actors]
     (let [pos (util/calc-position (:position actor))]
       ^{:key _id}
       [:img.ry-actor {:src (:model actor)
                       :style {:transform (str "translateX(" pos "%)")}}]))])

(defn fade-overlay []
  [:span.ry-fade-overlay {:style {:opacity (if @state/overlay? 1 0)
                                  :z-index (if (or @state/overlay? @state/progressible?) 9999 1)}}])

(defn game []
  [:div.ry-game {:on-click global-click-handler}
   
   [fade-overlay]
   [:div.ry-background {:style
                        {:background (str "url(\"" (:background @state/scene) "\") no-repeat center center fixed")}}
    [dialogue]
    [choices]
    [actors]]])