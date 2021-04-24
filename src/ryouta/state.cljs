(ns ryouta.state
  (:require [reagent.core :as r]
            [cljs.reader :as reader]))

(defonce default {:vars {}
                  :dialogue {:visible false
                             :actor ""
                             :line ""}
                  :actors #{}
                  :directions []
                  :history []
                  :scene {}})

(def db (r/atom {}))

(defn create-db! [opts]
  (reset! db (merge default opts)))

(defn reset-db! [] (reset! db default))

(defn save-game! [key]
  (.setItem js/localStorage key (prn-str @db)))

(defn load-game! [key]
  (create-db! (reader/read-string (.getItem js/localStorage key))))

(def dialogue (r/cursor db [:dialogue]))
(def directions (r/cursor db [:directions]))
(def scene (r/cursor db [:scene]))
(def actors (r/cursor db [:actors]))


(add-watch db :log
           (fn [key this old-state new-state]
             (tap> @this)))