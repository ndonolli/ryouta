(ns ryouta.util)

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
