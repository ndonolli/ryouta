(ns ryouta.styles
  (:require [garden.core :refer [css]]
            [garden.units :refer [percent px em vw]]
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
     'justify-content 'center
     'align-self 'flex-end
     'height (percent 100)
     'width 'inherit
     'margin-top (percent 100)
     'z-index 500}]

   [:.ry-actor
    {'position 'absolute
     'height (percent 100)
     'object-fit 'cover}
    [:&-enter
     {'opacity 0.01}
     [:&-active
      {'opacity 1
       'transition "opacity 500ms ease-in"}]]]])

;; storing the style-ref is only to support hot-reloading with styles
(defonce style-ref (atom nil))
(defn- install* []
  (-> (css
       {:vendors ["webkit" "moz" "o"]
        :auto-prefix #{:background-size}}
       styles)
      (Const/from)
      (SafeStyleSheet/fromConstant)
      (goog.style/installSafeStyleSheet)))


(defn install! []
  (when-not (nil? @style-ref)
    (goog.style/uninstallStyles @style-ref))
  (reset! style-ref (install*)))

(def foo [:.ry-actor
          {'position 'absolute
           'height (percent 100)
           'object-fit 'cover}
          [:&-enter
           {'opacity 0.01}
           [:&-active
            {'opacity 1
             'transition "opacity 500ms ease-in"}]]])

(comment (css foo))