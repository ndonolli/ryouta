(ns ryouta.crew
  (:require [reagent.dom :as dom]
            [ryouta.state :as state :refer [db]]
            [ryouta.director :as direct]))

(defn dialogue []
  (when (:visible @state/dialogue)
    [:div.ry-dialogue
     [:div.ry-dialogue-content
      [:p (:content @state/dialogue)]]]))

(defn global-click-handler []
  (direct/read! @state/directions))

(defn reset-btn []
  [:button {:on-click #(state/create-db! direct/myscript)}])

(defn game []
  [:div.ry-game {:on-click global-click-handler}
   [:div.ry-background {:style 
                        {:background (str "url(\"" (:background @state/scene) "\") no-repeat center center fixed")}}
    [dialogue]]])