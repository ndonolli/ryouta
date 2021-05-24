(ns ryouta.util
  (:require [clojure.string :as s]))

(defn generate-id
  "Generates a unique identifier"
  []
  (-> (js/Math.random)
      (.toString 16)
      (.slice 2)
      (keyword)))

(defn calc-position
  "Calculates the translateX percent amount for an actor's positioning"
  [pos]
  (case pos
    :left -50
    :center 0
    :right 50))

(defn log!
  "Converts and prints data to the javascript console"
  [& data]
  (apply js/console.log (clj->js data)))

(defn in?
  "Returns true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn timeout->
  "Given sequential args delay (in ms) followed by a callback function, calls setTimeout for each.

   Example:
   ```clojure
   (timeout->
    0 #(println \"Runs Immediately\")
    500 #(println \"Runs after 500 ms\"))
   ```
   "
  [& forms]
  (doall (->> forms
              (partition 2)
              (map (fn [[ms cb]] (js/setTimeout cb ms))))))

(defn get-file-type
  "Given a path string to a file, returns its file type as a keyword"
  [path]
  (let [file-re #"\w+\.\w+$"
        types {:image ["png" "jpg" "jpeg" "bmp"]
               :audio ["wav" "mp3" "ogg"]}
        type-map (->> types
                      (map (fn [[t exts]] (zipmap exts (repeat t))))
                      (apply merge))]
    (if-let [file (re-seq file-re path)]
      (get type-map (-> (first file)
                        (s/split ".")
                        (second)))
      nil)))
