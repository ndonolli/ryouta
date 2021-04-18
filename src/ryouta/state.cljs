(ns ryouta.state
  (:require [reagent.core :as r]))

(def default {:vars {}
              :text "hooww"
              :ui {:dialogue {:visible false
                              :content ""}
                   :actors []}
              :game {:directions []
                     :scene {}}})

(def db (r/atom default))

(add-watch db :log
           (fn [key this old-state new-state]
             (js/console.log @this)))