(ns ryouta.core
  (:require [reagent.dom :as dom]
            [ryouta.crew :as crew]
            [ryouta.director :as direct]
            [ryouta.state :as state]))


(defn init
  ([] (init {}))
  ([opts]
   (state/create-db! opts)))

(defn ^:dev/after-load start []
  (dom/render
   [crew/game]
   (.getElementById js/document "app")))

(defn ^:export mygame []
  (init {:directions direct/myscript}))

(defn ^:export save []
  (state/save-game! "save"))

(defn ^:export load []
  (state/load-game! "save"))

(defn ^:export main []
  (mygame)
  (start))