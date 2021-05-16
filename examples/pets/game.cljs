(ns pets.game
  (:require [ryouta.core :as ryouta]
            [reagent.core :as r]
            [clojure.string :as s]
            [ryouta.director :as direct]
            [ryouta.state :as state]
            [ryouta.util :refer [log!]]))

;; Define your characters
(def nathan (ryouta/create-actor
             {:name "Nathan"
              :models {:default "/images/actors/nathan_default.png"}}))

(def makki (ryouta/create-actor
            {:name "Makki"
             :models {:default "/images/actors/makki_default.png"}}))

(def narrator (ryouta/create-actor {:name nil}))


;; Define your scenes
(def town (ryouta/create-scene {:name "town" :background "/images/scenes/town.jpeg"}))
(def beach (ryouta/create-scene {:name "beach" :background "/images/scenes/beach.jpg"}))



(def script_test [[:scene town]
                  [:says narrator "your name is :vars/main-character"]])

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
                  [:says nathan "Your name is :vars/main-character, right?"]
                  [:says nathan "I'm just here chillin in this town"]
                  [:says nathan "...but it would be nice to go to the beach!"]
                  [:choose [[:beach "Go to the beach"] [:nah "nah"]]]
                  [:cond
                   :beach [script_beach]
                   :nah [:says nathan "nevermind"]]])

(def loading-screen
  (ryouta/create-screen
   (fn []
     (when (and (:loaded? @state/assets) (not @state/overlay-transitioning?))
       (direct/perform [:next-direction]))
     [:div.center-logo-container
      [:div.loading-bar
       [:div.loading-bar-progress {:style {:width (str (:percent-loaded @state/assets) "%")}}]]])))

(def imaginathan-splash
  (ryouta/create-screen
   (fn []
     (direct/perform [:next-direction-delay 5000])
     [:div.center-logo-container.black-background
      [:div.text-align-center
       [:img.logo {:src "/images/imaginathan-games-logo.png"}]]])))

(def character-creation
  (ryouta/create-screen
   (fn []
     [:div.center-logo-container
      [:div.centered-elems
       [:h2 "What is your name?"]
       [:input {:type "text" :value (get @state/vars :main-character)
                :on-change #(let [new-val (-> % .-target .-value)]
                              (when (re-matches #"([A-Za-z]|\s)*" new-val)
                                (swap! state/vars assoc :main-character new-val)))}]
       [:button {:disabled (s/blank? (get @state/vars :main-character))
                 :on-click #(direct/read script_town)}
        "Next"]]])))

(def menu
  (ryouta/create-screen
   (fn []
     [:div
      [:h1 "Welcome to the game"]
      [:button.ry-clickable {:on-click #(direct/perform [:screen character-creation])} "start"]])))

;; Define your script
(def myscript [[:screen loading-screen {:transition? false}]
               [:screen imaginathan-splash]
               [:screen menu]])

;; (def myscript [[:screen character-creation {:transition? false}]])



;; This is your main function to initialize the game
(defn ^:export main []
  ;; Set up the directions, options, and any other game state
  (ryouta/prepare {:directions myscript
                   :game-settings {:transition-ms 1000}})
  (ryouta/register-assets ["https://c8.alamy.com/comp/F1WJN3/full-moon-harvest-moon-large-file-size-from-the-archives-of-press-F1WJN3.jpg"
                           "https://captbbrucato.files.wordpress.com/2011/08/dscf0585_stitch-besonhurst-2.jpg"
                           "https://i3g4v6w8.stackpathcdn.com/wp-content/uploads/2019/09/delete-large-folder-fast-windows10_.jpg"
                           "https://miro.medium.com/max/4800/1*lOXDHhxbqfy0unDn9x56HQ.jpeg"
                           "https://cdn.osxdaily.com/wp-content/uploads/2012/04/find-large-files-mac-os-x.jpg"
                           "https://io.dropinblog.com/uploaded/blogs/34237324/files/featured/marc-olivier-jodoin-NqOInJ-ttqM-unsplash.jpg"
                           "https://i.imgur.com/uUL3zYD.jpg"
                           "https://miro.medium.com/max/2048/1*9uNBXXGjYqJ7NzyJaoCBnQ.jpeg"
                           "https://design-milk.com/images/2020/01/DM-Wallpaper-2001-5120x2880-featureda-scaled.jpg"
                           "https://wallpaperaccess.com/full/7314.jpg"])

  ;; Mount the game to an element
  (ryouta/stage "game")
  (direct/read @state/directions))

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