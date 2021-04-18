(ns ryouta.crew
  (:require [reagent.dom :as dom]
            [ryouta.state :refer [db]]))

(defn game-window []
  [:div.game-window {:style {:background-image (str "url(\"" (get-in @db [:game :scene :backdrop]) "\")")
                             :width "100vwh"
                             :height "100vh"}}
   [:h1 (get-in @db [:text])]])