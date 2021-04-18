(ns ryouta.core
  (:require [reagent.dom :as dom]
            [ryouta.crew :as crew]
            [ryouta.director :as dir]))

(defn hello []
  [:h1 "hello"])

(defn ^:dev/after-load start []
  (dom/render
   [crew/game-window]
   (.getElementById js/document "app")))

(defn ^:export main []
  (start))