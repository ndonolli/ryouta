(ns ryouta.crew
  (:require [reagent.dom :as dom]
            [reagent.core :as r]
            [ryouta.state :as state :refer [db]]
            [ryouta.director :as direct]))


(defn global-click-handler []
  (if @state/progressible
    (direct/read! @state/directions)
    (if (:typing? @state/dialogue)
      (state/dispatch! [:dialogue-line-complete]))))

(defn typewriter [text delay] 
  (let [typed (r/atom "")
        prev-text (r/atom "")]
    (letfn [(type* [chars]
              (swap! typed str (first chars))
              (let [next (seq (rest chars))]
                (if (and next (:typing? @state/dialogue))
                  (js/setTimeout #(type* (seq (rest chars))) delay)
                  (state/dispatch! [:dialogue-line-complete]))))]
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
     [:div.ry-dialogue-content [typewriter (:line @state/dialogue) 50]]]))

(defn actors []
  [:div.ry-actors
   (for [actor @state/actors] ^{:key actor}
     [:img.ry-actor {:src (:model actor)}])])

(defn game []
  [:div.ry-game {:on-click global-click-handler}
   [:div.ry-background {:style 
                        {:background (str "url(\"" (:background @state/scene) "\") no-repeat center center fixed")}}
    [dialogue]
    [actors]]])