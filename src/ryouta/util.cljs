(ns ryouta.util)

(defn generate-id 
  "Generates a unique identifier"
  [] 
  (-> (js/Math.random)
      (.toString 16)
      (.slice 2)
      (keyword)))

(defn calc-position 
  "Calculates the vw amount for an actor's positioning"
  [pos] 
  (if (keyword? pos)
    (case pos
      :left 0
      :center 25
      :right 50)
    (/ pos 2)))
