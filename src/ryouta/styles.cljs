(ns ryouta.styles
  (:require [garden.core :refer [css]]
            [garden.units :refer [percent px em]]
            [garden.color :refer [rgba]]
            [goog.style])
   (:import [goog.html SafeStyleSheet]
            [goog.string Const]))

(def styles 
  [[:.ry-background
    {'background-size 'cover
     'width 'inherit
     'height 'inherit
     'display 'flex}]

   [:.ry-game
    {'width 'inherit
     'height 'inherit
     'display 'flex
     'user-select 'none}]

   [:.ry-dialogue
    {'position 'absolute
     'background (rgba 0 0 0 0.7)
     'width 'inherit
     'height (percent 30)
     'margin-bottom (percent 2)
     'align-self 'flex-end
     'z-index 1000}]
   
   [:.ry-dialogue-content
    {'padding-top (px 50)
     'padding-left (px 70)
     'padding-right (px 70)
     'padding-bottom (px 20)
     'color 'white
     'font-size (em 1.5)
     'font-family 'helvetica}]
   
   [:.ry-dialogue-title
    {'position 'absolute
     'border "1px solid black"
     'border-radius (px 10)
     'top (em -1.5)
     'left (px 70)
     'background (rgba 213 213 213 1)
     'height (em 2)
     'padding (px 7)
     'display 'flex
     'align-items 'center
     'font-size (em 1.5)}]
   
   [:.ry-actors
    {'display 'flex
     'align-items 'flex-end
     'align-self 'flex-end
     'height (percent 90)
     'width 'inherit
     'margin-top (percent 100)
     'z-index 500
     'justify-content 'center}]
   
   [:.ry-actor 
    {'align-self 'flex-end
     'height (percent 100)}]])

;; (defn install! []
;;   (goog.style/installSafeStyleSheet
;;    (SafeStyleSheet/fromConstant
;;     (Const/from
;;      (css
;;       {:vendors ["webkit" "moz" "o"]
;;        :auto-prefix #{:background-size}}
;;       styles)))))

(defn install! []
  (-> (css
       {:vendors ["webkit" "moz" "o"]
        :auto-prefix #{:background-size}}
       styles)
      (Const/from) 
      (SafeStyleSheet/fromConstant)
      (goog.style/installSafeStyleSheet)))
