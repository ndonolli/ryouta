(ns ryouta.crew
  (:require [reagent.dom :as dom]
            [ryouta.state :as state :refer [db]]
            [ryouta.director :as direct]))

(defn dialogue []
  (when (:visible @state/dialogue)
    [:div.ry-dialogue
     [:div.ry-dialogue-title (:actor @state/dialogue)]
     [:div.ry-dialogue-content (:line @state/dialogue)]]))

(defn actors []
  [:div.ry-actors
   (for [actor @state/actors] ^{:key actor}
     [:img.ry-actor {:src (:model actor)}])])

(defn global-click-handler []
  (direct/read! @state/directions))

(defn game []
  [:div.ry-game {:on-click global-click-handler}
   [:div.ry-background {:style 
                        {:background (str "url(\"" (:background @state/scene) "\") no-repeat center center fixed")}}
    [dialogue]
    [actors]]])