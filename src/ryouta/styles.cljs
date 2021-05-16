(ns ryouta.styles
  (:require [ryouta.state :as state]
            [garden.core :refer [css]]
            [garden.units :refer [percent px em vw]]
            [garden.color :refer [rgba rgb lighten]]
            [goog.style])
  (:import [goog.html SafeStyleSheet]
           [goog.string Const]))

(def BLUE [3 5 66 0.7])

(defn styles []
  (let [{:keys [transition-ms]} @state/game-settings]
    [[:.ry-background
      {'background-size 'cover
       'position 'absolute
       'width 'inherit
       'height 'inherit
       'font-family 'sans-serif
       'z-index 1
       'display 'flex
       'justify-content 'center}]

     [:.ry-fade-overlay
      {'width 'inherit
       'height 'inherit
       'background 'black
       'opacity 0
       'transition (str "opacity " transition-ms "ms ease-in-out")}]

     [:.ry-game
      {'width 'inherit
       'height 'inherit
       'display 'flex
       'user-select 'none}]

     [:.ry-dialogue
      {'position 'absolute
       'background (apply rgba BLUE)
       'border "7px solid white"
       'border-radius (px 10)
       'width (percent 90)
       'height (percent 30)
       'margin-bottom (percent 2)
       'align-self 'flex-end
       'z-index 1000}]

     [:.ry-dialogue-content
      {'padding-top (px 50)
       'padding-left (px 50)
       'padding-right (px 50)
       'padding-bottom (px 50)
       'color 'white
       'font-size (em 1.5)
       'font-family 'helvetica}]

     [:.ry-dialogue-title
      {'position 'absolute
       'border-radius "10px 10px 0px 0px"
       'border-bottom 'none
       'color (apply rgb (take 3 BLUE))
       'top (px -49)
       'left (px -7)
       'background 'white
       'height (em 2)
       'padding "0px 7px 0px 7px"
       'display 'flex
       'align-items 'center
       'font-size (em 1.5)}]

     [:.ry-actors
      {'display 'flex
       'justify-content 'center
       'align-self 'flex-end
       'height (percent 100)
       'width 'inherit
       'margin-top (percent 100)
       'z-index 500}]

     [:.ry-actor
      {'position 'absolute
       'height (percent 100)
       'object-fit 'cover
       'transition (str "transform " transition-ms "ms ease-in-out")}
      [:&-enter
       {'opacity 0}
       [:&-active
        {'opacity 1
         'transition (str "opacity " transition-ms "ms ease-in")}]]
      [:&-leave
       {'opacity 1}
       [:&-active
        {'opacity 0
         'transition (str "opacity " transition-ms "ms ease-in")}]]]

     [:.ry-choices
      {'width 'inherit
       'height 'inherit
       'position 'absolute
       'display 'flex
       'flex-direction 'column
       'justify-content 'center
       'z-index 1000
       'align-items 'center}]

     [:.ry-choice
      {'padding (em 0.7)
       'margin (em 1)
       'background (apply rgba BLUE)
       'color 'white
       'border-radius (px 7)
       'border "2px solid white"
       'font-size (em 1.5)}
      [:&:hover
       {'background (lighten (apply rgba BLUE) 20)}]]
     
     [:.ry-screen
      {'height 'inherit
       'width 'inherit
       'position 'absolute
       'z-index 9999}]]))

;; storing the style-ref is only to support hot-reloading with styles
(defonce style-ref (atom nil))
(defn- install* []
  (-> (css
       {:vendors ["webkit" "moz" "o"]
        :auto-prefix #{:background-size}}
       (styles))
      (Const/from)
      (SafeStyleSheet/fromConstant)
      (goog.style/installSafeStyleSheet)))


(defn install! []
  (when-not (nil? @style-ref)
    (goog.style/uninstallStyles @style-ref))
  (reset! style-ref (install*)))