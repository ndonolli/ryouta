(ns pets.game
  (:require [ryouta.core :as ryouta]
            [ryouta.state :as state]))

;; define characters
(def nathan {:name "Nathan" :models {:default "/images/actors/nathan_default.png"}})
(def makki {:name "Makki" :models {:default "/images/actors/makki_default.png"}})

;; define scenes
(def town {:name "town" :background "/images/scenes/town.jpeg"})
(def beach {:name "beach" :background "/images/scenes/beach.jpg"})

;; scene to reference features
(def scene [[:enter nathan]
            [:says nathan "ayy lmao" :happy]
            [:exit nathan]
            [:choose [[:option1 "Option 1"] [:option2 "Option 2"]]]
            [:case
             :option1 [[:foo :bar]
                       [:foo :bar]]
             :option2 [:foo :bar]]
            [:choose ["Option 1" "Option 2"]]
            [:case
             :%1 [:hello]
             :%2 [[:hello nathan]
                  [:there nathan]]]])

;; actual script
(def myscript [[:scene town]
               [:group [[:enter nathan]
                        [:says nathan "Hi it's me nathan"]]]
               [:says nathan "I'm just here chillin in this town"]
               [:says nathan "...but it would be nice to go to the beach!"]
               [:group [[:scene beach]
                        [:says nathan "This is more like it!"]]]
               [:enter makki]
               [:says nathan "Who is that babe?"]
               [:says makki "omg wtf creep"]
               [:says nathan "Wait...I"]
               [:says makki "Get lost, loser."]
               [:exit nathan]
               [:says makki "Finally I have the beach to myself."]
               [:says makki "I'm going to say something really long-winded to see how this affects the dialogue.  I wonder how long it can be.  I mean three sentences is a lot to fit in one line of dialogue, don't you agree?"]
               [:says makki "Thank you for coming to my TED talk."]])


(defn ^:dev/after-load start []
  (ryouta/stage "game"))

(defn ^:export reset []
  (ryouta/prepare {:directions myscript}))

(defn ^:export save []
  (state/save-game! "save"))

(defn ^:export load []
  (state/load-game! "save"))

(defn ^:export main []
  (ryouta/prepare {:directions myscript})
  (ryouta/stage "game"))