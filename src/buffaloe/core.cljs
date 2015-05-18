(ns ^:figwheel-always buffaloe.core
    (:require [buffaloe.grammar :as grammar]
              [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [clojure.string :as str]))

(enable-console-print!)

(defn update-buffaloe-state [owner new-value]
  (let [input (mapv symbol (str/split (str/trim new-value) #" "))
        parse (grammar/parse-1 input)]
    (om/set-state! owner
                   {:value new-value
                    :last-valid-parse
                    (if (= parse '())
                      (om/get-state owner :last-valid-parse)
                      parse)
                    :last-parse parse})))

(defn buffaloe [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:last-valid-parse nil
       :last-parse nil
       :value ""})

    om/IRenderState
    (render-state [this {:keys [last-valid-parse last-parse value]}]
      (dom/div nil
        (dom/div nil
          (dom/span nil "# buffalo: "
                    (count (filter #(= "buffalo" %) (str/split value #" "))))
          (dom/button #js {:onClick #(update-buffaloe-state owner (str value " buffalo"))}
                      "+"))

        (dom/input #js {:style #js {:width "500px"
                                    :font-size "16px"
                                    :height "25px"}
                        :value value
                        :onChange #(update-buffaloe-state owner (-> % .-target .-value))})
        (dom/div nil
          (if (= last-parse last-valid-parse)
            (str last-parse)
            (dom/div nil
              (dom/div nil (str last-parse))
              (dom/div #js {:style #js {:color "gray"}}
                (str last-valid-parse)))))))))

(om/root
  buffaloe
  (atom {}) ; unused app state
  {:target (. js/document (getElementById "app"))})
