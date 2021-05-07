(ns ryouta.state
  (:require [reagent.core :as r]
            [ryouta.util :refer [log!]]
            [cljs.reader :as reader]))

(defonce default {:vars {}
                  :screen {:visible? false
                           :component nil}
                  :game-settings {:transition-ms 500}
                  :dialogue {:visible? false
                             :typing? false
                             :progressible? true ;; can the game be progressed by a click anywhere on the screen?
                             :actor ""
                             :line ""
                             :choices nil}
                  :actors {}
                  :directions []
                  :history []
                  :scene {}
                  :overlay? false})

(def db (r/atom {}))

(defn preload [url]
  (let [img (js/Image.)]
    (log! img)
    (set! (.-src img) url)
    (.append (.getElementById js/document "ry-assets") img)))

(def assets (r/atom {:loaded? false
                     :paths []}))

(defn preload-assets []
  (doseq [path (:paths @assets)]
    (preload path))
  (swap! assets assoc :loaded? true))

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
(def vars (r/cursor db [:vars]))
(def overlay? (r/cursor db [:overlay?]))
(def progressible? (r/cursor db [:dialogue :progressible?]))
(def game-settings (r/cursor db [:game-settings]))
(def screen (r/cursor db [:screen]))

(add-watch db :log
           (fn [key this old-state new-state]
             (tap> @this)))