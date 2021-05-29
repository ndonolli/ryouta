(ns ryouta.crew
  (:require ["react-transition-group" :as rtg]
            [reagent.core :as r]
            [ryouta.state :as state]
            [ryouta.director :as direct]
            [ryouta.util :as util]))

(def css-transition-group
  (r/adapt-react-class rtg/CSSTransitionGroup))

(defn global-click-handler [e]
  (cond
    (or
     (.contains (-> e .-target .-classList) "ry-clickable")
     (> (count (:choices @state/dialogue)) 0)
     (:visible? @state/screen))
    nil

    (:typing? @state/dialogue)
    (direct/perform [:dialogue-line-complete])

    (:progressible? @state/dialogue)
    (direct/read @state/directions)))

(defn typewriter [text delay]
  (let [typed (r/atom "")
        prev-text (r/atom "")]
    ;; cannot use recur here because the binding is lost in the setTimeout callback
    (letfn [(type* [chars]
              (swap! typed str (first chars))
              (let [next (seq (rest chars))]
                (if (and next (:typing? @state/dialogue))
                  (js/setTimeout #(type* (seq (rest chars))) delay)
                  (direct/perform [:dialogue-line-complete]))))]
      (fn [text delay]
        (when-not (= text @prev-text)
          (reset! prev-text text)
          (reset! typed "")
          (type* (seq text)))
        [:span (if (:typing? @state/dialogue) @typed (:line @state/dialogue))]))))

(defn dialogue []
  (when (:visible? @state/dialogue)
    [:div.ry-dialogue
     (when (:actor @state/dialogue) [:div.ry-dialogue-title (:actor @state/dialogue)])
     [:div.ry-dialogue-content [typewriter (:line @state/dialogue) 25]]]))

(defn choices []
  (when-let [options (:choices @state/dialogue)]
    [:div.ry-choices
     (for [{:keys [label option]} options]
       ^{:key label}
       [:div.ry-choice {:on-click #(direct/perform [:choice-selected label])} option])]))

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
                                  :z-index (if @state/overlay-transitioning? 10000 1)}}])

(defn custom-screen [screen-component]
  [:span.ry-screen
   [screen-component]])

(defn game []
  [:div.ry-game {:on-click global-click-handler}
   [fade-overlay]
   (when (:visible? @state/screen)
     [custom-screen @state/active-component])
   [:div.ry-background {:style
                        {:background (str "url(\"" (:path @state/scene) "\") no-repeat center center fixed")}}
    [dialogue]
    [choices]
    [actors]]
   [:div#ry-assets {:style {:display "none"}}]])