(ns ryouta.util)

(defn generate-id
  "Generates a unique identifier"
  []
  (-> (js/Math.random)
      (.toString 16)
      (.slice 2)
      (keyword)))

(defn calc-position
  "Calculates the vw amount for either left or right for an actor's positioning"
  [pos]
  (case pos
      :left {:left 0}
      :center {}
      :right {:right 0}))

(defn log! [data]
  (js/console.log (clj->js data)))
