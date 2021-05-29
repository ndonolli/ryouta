(ns ryouta.core
  (:require [reagent.dom :as dom]
            [ryouta.crew :as crew]
            [ryouta.state :as state]
            [ryouta.styles :as styles]
            [ryouta.util :as util]
            [clojure.set :refer [union]]))

(defn register-assets [paths]
  (swap! state/assets update :paths union (set paths)))

(defn create-actor
  "Create an actor record and register with the engine"
  [actor]
  (let [id (util/generate-id)]
    (register-assets (vals (:models actor)))
    (assoc actor :_id id)))

(defn create-scene
  [scene]
  (let [id (util/generate-id)]
    (register-assets #{(:path scene)})
    (assoc scene :_id id)))

(defn create-screen
  [screen]
  (let [id (util/generate-id)
        assets (->> (screen)
                    (flatten)
                    (filter map?)
                    (map :src)
                    (remove nil?))]
    (register-assets assets)
    (swap! state/components assoc id screen)
    (assoc {} :_id id)))

(defn create-audio
  [audio]
  (let [id (util/generate-id)
        audio (assoc audio :_id id)]
    (register-assets #{(:path audio)})
    (swap! state/audios assoc id audio)
    audio))

(defn get-var [key]
  (get @state/vars key))

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
