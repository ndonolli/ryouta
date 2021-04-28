(ns ryouta.core
  (:require [reagent.dom :as dom]
            [ryouta.crew :as crew]
            [ryouta.state :as state]
            [ryouta.styles :as styles]
            [ryouta.util :as util]))

(defn create-actor 
  "Create an actor map.  The input actor will be given a unique identifier"
  [actor]
  (assoc actor :_id (util/generate-id)))

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
   (.getElementById js/document element-id)))
