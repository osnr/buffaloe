(ns ^:figwheel-always buffaloe.core
    (:require [buffaloe.grammar :as grammar]
              [buffaloe.prolog :as prolog]
              [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [clojure.string :as str]
              [buffaloe.graph :as graph]))

(enable-console-print!)

(defn update-input [owner backend new-value]
  (let [input (mapv symbol (str/split (str/trim new-value) #" "))]
    (case backend
      :prolog
      (do (prolog/parse-1
           input
           (fn [parse]
             (om/set-state!
              owner :parse
              {:input (om/get-state owner [:parse :input])
               :last-valid-parse parse
               :last-parse parse})))
          (om/set-state!
           owner :parse
           {:input new-value
            :last-valid-parse (om/get-state owner [:parse :last-valid-parse])
            :last-parse "waiting..."}))

      :core.logic
      (let [parse (first (grammar/parse-1 input))]
        (om/set-state!
         owner :parse
         {:input new-value
          :last-valid-parse
          (if (= parse '())
            (om/get-state owner :last-valid-parse)
            parse)
          :last-parse parse})))))

(defn buffaloe [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:backend :prolog
       :parse {:last-valid-parse nil
               :last-parse nil
               :input ""}})

    om/IRenderState
    (render-state [this {backend :backend
                         {:keys [last-valid-parse last-parse input]} :parse}]
      (dom/div nil
        (dom/div nil
          (dom/select #js {:value (name backend)
                           :onChange #(om/set-state! owner :backend (keyword (-> % .-target .-value)))}
            (dom/option #js {:value "prolog"} "Prolog")
            (dom/option #js {:value "core.logic"} "core.logic"))
          (dom/span nil "# buffalo: "
                    (count (filter #(= "buffalo" %) (str/split input #" "))))
          (dom/button #js {:onClick #(update-input owner backend
                                                   (str input " buffalo"))}
                      "+"))

        (dom/div nil
          (dom/input #js {:style #js {:width "500px"
                                      :font-size "16px"
                                      :height "25px"}
                          :value input
                          :onChange #(update-input owner backend
                                                   (-> % .-target .-value))}))
        (dom/div nil
          (when (not= last-parse last-valid-parse)
            (dom/div nil (str last-parse)))
          (dom/div #js {:style #js {:opacity
                                    (if (= last-parse last-valid-parse)
                                      1
                                      0.5)}}
            (str last-valid-parse)
            (om/build graph/parse-tree last-valid-parse)))))))

(om/root
  buffaloe
  (atom {}) ; unused app state
  {:target (. js/document (getElementById "app"))})
