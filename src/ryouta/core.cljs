(ns ryouta.core
  (:require [reagent.dom :as dom]
            [ryouta.crew :as crew]
            [ryouta.state :as state]
            [ryouta.styles :as styles]))


(defn ^:export prepare
  ([] (prepare {}))
  ([opts]
   (state/create-db! opts)))

(defn ^:export stage [element-id]
  (styles/install!)
  (dom/render
   [crew/game]
   (.getElementById js/document element-id)))
