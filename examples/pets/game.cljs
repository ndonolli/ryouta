(ns pets.game
  (:require [ryouta.core :as ryouta]
            [ryouta.state :as state]))

;; Define your characters
(def nathan {:name "Nathan" :models {:default "/images/actors/nathan_default.png"}})
(def makki {:name "Makki" :models {:default "/images/actors/makki_default.png"}})

;; Define your scenes
(def town {:name "town" :background "/images/scenes/town.jpeg"})
(def beach {:name "beach" :background "/images/scenes/beach.jpg"})

;; Define your script
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

;; This is your main function to initialize the game
(defn ^:export main []
  ;; Set up the directions, options, and any other game state
  (ryouta/prepare {:directions myscript})

  ;; Mount the game to an element
  (ryouta/stage "game"))

;; While developing, it might be useful to set up hot-reloading hooks with figwheel or shadow-cljs
(defn ^:dev/after-load start []
  (ryouta/stage "game"))

;; Extra functions to load and save
(defn ^:export reset []
  (ryouta/prepare {:directions myscript}))

(defn ^:export save []
  (state/save-game! "save"))

(defn ^:export load []
  (state/load-game! "save"))


;; scene to reference features, ignore
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