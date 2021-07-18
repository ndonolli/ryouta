(ns pets.game
  (:require [ryouta.core :as ryouta]
            [reagent.core :as r]
            [clojure.string :as s]
            [ryouta.director :as direct]
            [ryouta.state :as state]
            [ryouta.util :refer [log!]]))

;; Define your characters
(def narrator (ryouta/create-actor {:name nil}))
(def three-musketeers (ryouta/create-actor
                       {:name "Three Musketeers"
                        :models {:default "images/actors/three_musketeers.png"}}))

(def copper (ryouta/create-actor
             {:name "Copper"
              :models {:defailt "images/actors/copper.png"}}))

(def diego (ryouta/create-actor {:name "Diego"
                                 :models {:default "/images/actors/diego.png"}}))
(def foss (ryouta/create-actor {:name "Foss"
                                :models {:default "images/actors/foss.png"}}))

(def tillie (ryouta/create-actor {:name "Tilie"
                                  :models {:default "images/actors/tillie.png"}}))

(def melissumz (ryouta/create-actor {:name "Melissumz"
                                     :models {:default "images/actors/melissumz.png"}}))

;; Define your scenes
(def town (ryouta/create-scene {:name "town" :path "/images/scenes/town.jpeg"}))
(def beach (ryouta/create-scene {:name "beach" :path "/images/scenes/beach.jpg"}))

(def music (ryouta/create-audio {:name "menu" :path "/audio/hatoful-test.mp3"}))


(def characters (vector diego three-musketeers copper foss tillie melissumz))
(def character-directions (apply concat (map #(vector [:enter %] [:says % (str "hey it's me " (:name %))] [:exit %]) characters)))
(def character-test (conj character-directions [:scene town]))

(declare menu)

(def loading-screen
  (ryouta/create-screen
   (fn []
     (when (and (:loaded? @state/assets) (not @state/overlay-transitioning?))
       (direct/perform [:next-direction]))
     [:div.center-logo-container.black-background
      [:div.loading-bar
       [:div.loading-bar-progress {:style {:width (str (:percent-loaded @state/assets) "%")}}]]])))

(def imaginathan-splash
  (ryouta/create-screen
   (fn []
     [:div.center-logo-container.black-background
      {:on-click #(direct/perform [:next-direction])}
      [:div.text-align-center
       [:img.logo {:src "/images/imaginathan-games-logo.png"}]]])))

(declare script_town)

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
   (r/create-class
    {:component-did-mount
     #(direct/perform [:play-audio music])

     :reagent-render
     (fn []
       [:div
        [:h1 "Welcome to the game"]
        [:button.ry-clickable {:on-click #(direct/perform [:screen character-creation])} "start"]])})))

(def script_beach [[:scene beach]
                   [:says melissumz "The beach is so wonderful!"]
                   [:group [[:enter diego {:position :right}]
                            [:move melissumz :left]]]
                   [:says diego "Hey this is my spot"]
                   [:says melissumz "Whoa, who is this??"]
                   [:says diego "I'm diego and this is my beach"]
                   [:says melissumz "Nuh uh this beach belongs to me"]
                   [:says melissumz "Right, :vars/main-character ?"]
                   [:choose ["Yeah totally", "No, Diego is clearly right."]]
                   [:cond
                    :%1 [[:says melissumz "Hell yeah! Get lost, stinky dog!"]
                         [:says diego "Whatever"]
                         [:exit diego]
                         [:move melissumz :center]
                         [:screen menu]]
                    :%2 [[:says melissumz "Betrayed!"]
                         [:says diego "Begone you!"]
                         [:says melissumz ":("]
                         [:exit melissumz]
                         [:move diego :center]
                         [:group [:exit diego
                                  :scene
                                  [:screen menu]]]]]])

(def script_town [[:scene town]
                  [:group [[:enter melissumz]
                           [:says melissumz "Hi it's me melissumz"]]]
                  [:says melissumz "Your name is :vars/main-character, right?"]
                  [:says melissumz "Nice to meet you, this is my dog copper!"]
                  [:says melissumz "I'm just here chillin in this town"]
                  [:says melissumz "...but it would be nice to go to the beach!"]
                  [:choose [[:beach "Go to the beach"] [:nah "nah"]]]
                  [:cond
                   :beach [script_beach]
                   :nah [[:says melissumz "ok be like that"]
                         [:screen menu]]]])

;; Define your script
(def myscript [[:screen loading-screen {:transition? false}]
               [:screen imaginathan-splash]
               [:screen menu]])




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