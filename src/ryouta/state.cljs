(ns ryouta.state
  (:require [reagent.core :as r]))

(defonce default {:vars {}
                  :dialogue {:visible false
                             :content ""}
                  :actors #{}
                  :directions []
                  :history []
                  :scene {}})

(def db (r/atom {}))

(defn create-db! [opts]
  (reset! db (merge default opts)))

(defn reset-db! [] (reset! db default))

(add-watch db :log
           (fn [key this old-state new-state]
             (tap> @this)))

(def dialogue (r/cursor db [:dialogue]))
(def directions (r/cursor db [:directions]))
(def scene (r/cursor db [:scene]))