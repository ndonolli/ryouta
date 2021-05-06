(ns pets.game
  (:require [ryouta.core :as ryouta]
            [ryouta.director :as direct]
            [ryouta.state :as state]))

;; Define your characters
(def nathan (ryouta/create-actor
             {:name "Nathan"
              :models {:default "/images/actors/nathan_default.png"}}))

(def makki (ryouta/create-actor
            {:name "Makki"
             :models {:default "/images/actors/makki_default.png"}}))

;; Define your scenes
(def town {:name "town" :background "/images/scenes/town.jpeg"})
(def beach {:name "beach" :background "/images/scenes/beach.jpg"})


(def script_beach [[:scene beach]
                   [:says nathan "This is more like it!"]
                   [:group [[:enter makki {:position :right}]
                            [:move nathan :left]]]
                   [:says makki "CHAAANGE PLACEESS"]
                   [:group [[:move nathan :right]
                            [:move makki :left]]]
                   [:says nathan "Who is that babe?"]
                   [:says makki "omg wtf creep"]
                   [:says nathan "Wait...I"]
                   [:says makki "Get lost, loser."]
                   [:exit nathan]
                   [:group [[:says makki "Finally I have the beach to myself."]
                            [:move makki :center]]]
                   [:says makki "I'm going to say something really long-winded to see how this affects the dialogue.  I wonder how long it can be.  I mean three sentences is a lot to fit in one line of dialogue, don't you agree?"]
                   [:says makki "Thank you for coming to my TED talk."]
                   [:choose [[:option1 "Option 1"]
                             [:option2 "Option 2"]]]
                   [:cond
                    :option1 [:says makki "you have chosen option1"]
                    :option2 [[:says makki "you have chosen option2"]
                              [:says makki "here is an anonymous poll"]
                              [:choose ["the first thing" "the second thing"]]
                              [:cond
                               :%1 [:says makki "numbuh 1"]
                               :%2 [:says makki "numbuh 2"]]]]])

(def script_town [[:scene town]
                  [:group [[:enter nathan]
                           [:says nathan "Hi it's me nathan"]]]
                  [:says nathan "I'm just here chillin in this town"]
                  [:says nathan "...but it would be nice to go to the beach!"]
                  [:choose [[:beach "Go to the beach"] [:nah "nah"]]]
                  [:cond
                   :beach [script_beach]
                   :nah [:says nathan "nevermind"]]])


(defn menu []
  [:div
   [:h1 "Welcome to the game"]
   [:button.ry-clickable {:on-click #(direct/read! script_town)} "start"]])

;; Define your script
(def myscript [[:screen menu]])



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