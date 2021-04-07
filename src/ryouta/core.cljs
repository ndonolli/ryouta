(ns ryouta.core
  (:require [reagent.dom :as dom]))

(defn hello []
  [:h1 "hello"])

(defn ^:dev/after-load start []
  (dom/render
   [hello]
   (.getElementById js/document "app")))

(defn ^:export main []
  (start))