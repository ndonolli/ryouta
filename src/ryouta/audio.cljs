(ns ryouta.audio
  (:require ["jquery" :as $]
            [ryouta.util :refer [log! timeout->]]))

(defn play
  ([audio-selector] (play audio-selector {}))
  ([audio-selector opts]
   (let [default-opts {:fade 0 :loop? false}
         {:keys [fade loop?]} (merge default-opts opts)]
     (-> ($ audio-selector)
         (.animate #js {:volume 1} fade)
         (.get 0)
         ((fn [elem]
            (set! (.-currentTime elem) 0)
            (when loop?
              (set! (.-loop elem) true))
            (comment .play elem)))))))

(defn stop
  ([audio-selector] (stop audio-selector {}))
  ([audio-selector opts]
   (let [default-opts {:fade 0}
         {:keys [fade]} (merge default-opts opts)
         audio-elem ($ audio-selector)]
     (timeout->
      0 #(.animate audio-elem #js {:volume 0} fade)
      fade #(.pause (.get audio-elem 0))))))

(defn get-asset-id [id]
  (str "audio[data-asset_id='ry-audio" id "']"))