(ns ryouta.core
  (:require [reagent.dom :as dom]
            [ryouta.crew :as crew]
            [ryouta.state :as state]
            [ryouta.styles :as styles]
            [ryouta.util :as util]))


(defn create-actor 
  "Create an actor record and register with the engine"
  [actor]
  (let [id (util/generate-id)]
    (swap! state/assets update-in [:paths] conj (get-in actor [:models :default]))
    (assoc actor :_id id)))

(defn create-scene
  [scene]
  (let [id (util/generate-id)]
    (swap! state/assets update-in [:paths] conj (get-in scene [:background]))
    (assoc scene :_id id)))

(defn ^:export prepare
  "Loads the game db with any options"
  ([] (prepare {}))
  ([opts]
   (state/create-db! opts)))

(defn ^:export stage
  "Mount the game to an element identifier"
  [element-id]
  (styles/install!)
  (dom/render
   [crew/game]
   (.getElementById js/document element-id))
  (state/preload-assets))
