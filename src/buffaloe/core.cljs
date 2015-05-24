(ns ^:figwheel-always buffaloe.core
    (:require [buffaloe.grammar :as grammar]
              [buffaloe.prolog :as prolog]
              [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [clojure.string :as str]
              [buffaloe.graph :as graph]))

(enable-console-print!)

(defn update-input [owner backend new-input]
  (let [cleaned-input (-> new-input
                          str/trim
                          str/lower-case
                          (str/replace #"\.$" "")
                          (str/split #" "))
        sentence (mapv symbol cleaned-input)]
    (case backend
      :prolog
      (do (prolog/parse-1
           sentence
           (fn [parse time-elapsed]
             (om/set-state!
              owner :parse
              {:input (om/get-state owner [:parse :input])
               :last-valid-parse
               (if parse
                 parse
                 (om/get-state owner [:parse :last-valid-parse]))
               :status
               (if parse
                 [:success time-elapsed]
                 [:failure time-elapsed])})))
          (om/set-state!
           owner :parse
           {:input new-input
            :last-valid-parse (om/get-state owner [:parse :last-valid-parse])
            :status :waiting}))

      :core.logic
      (let [start (js/performance.now)
            parse (first (grammar/parse-1 sentence))
            time-elapsed (- (js/performance.now) start)]
        (om/set-state!
         owner :parse
         {:input new-input
          :last-valid-parse
          (if parse
            parse
            (om/get-state owner [:parse :last-valid-parse]))
          :status
          (if parse
            [:success time-elapsed]
            [:failure time-elapsed])})))))

(defn buffaloe [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:backend :prolog
       :parse {:last-valid-parse nil
               :status nil
               :input ""}})

    om/IRenderState
    (render-state [this {backend :backend
                         {:keys [last-valid-parse status input]} :parse}]
      (dom/div nil
        (dom/div #js {:style #js {:float "left"}}
          (dom/span nil "# buffalo: "
                    (count (filter #(= "buffalo" %) (str/split (str/lower-case input) #" "))))
          (dom/button #js {:onClick #(update-input owner backend
                                                   (str input " buffalo"))}
                      "+"))
        (dom/div #js {:style #js {:float "right"}}
          (dom/select #js {:value (name backend)
                           :onChange #(om/set-state! owner :backend (keyword (-> % .-target .-value)))}
            (dom/option #js {:value "prolog"} "Prolog")
            (dom/option #js {:value "core.logic"} "core.logic")))

        (dom/div nil
          (dom/input #js {:style #js {:width "100%"
                                      :fontSize "16px"
                                      :height "25px"}
                          :value input
                          :onChange #(update-input owner backend
                                                   (-> % .-target .-value))}))
        (dom/div nil
          (dom/div #js {:style #js {:marginTop "-2px"
                                    :fontSize "10px"
                                    :textAlign "right"}}
            (if (vector? status)
              (let [[result time-elapsed] status
                    [whole decimal] (str/split (str time-elapsed) #"\.")
                    formatted-time (str whole "." (subs decimal 0 2))]
                (case result
                  :success
                  (dom/span #js {:style #js {:color "green"}}
                            "Valid sentence."
                            " [found in " formatted-time "ms]")

                  :failure
                  (dom/span #js {:style #js {:color "red"}}
                            "Not a valid sentence."
                            " [found in " formatted-time "ms]")))
              (case status
                :waiting
                (dom/span #js {:style #js {:color "gray"}}
                          "Waiting for Prolog parse...")

                nil)))
          (dom/div #js {:style #js {:opacity
                                    (if (= status :success)
                                      1
                                      0.5)}}
            (str last-valid-parse)
            (om/build graph/parse-tree last-valid-parse)))))))

(om/root
  buffaloe
  (atom {}) ; unused app state
  {:target (. js/document (getElementById "app"))})
