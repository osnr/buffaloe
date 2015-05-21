(ns ^:figwheel-always buffaloe.graph
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]))

(def graph (js/React.createFactory js/DagreReact.Graph))
(def vertex (js/React.createFactory js/DagreReact.Vertex))
(def edge (js/React.createFactory js/DagreReact.Edge))

(defn parse-tree-graph [tree]
  [()])

(defn parse-tree [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/svg nil
        (graph nil
          (vertex #js {:width 50
                       :height 50
                       :key "foo"}
                  (dom/rect #js {:width 50
                                 :height 50})
                  (dom/text #js {:fill "blue"} "foo"))
          (vertex #js {:width 50
                       :height 50
                       :key "bar"}
                  (dom/rect #js {:width 50
                                 :height 50})
                  (dom/text #js {:fill "green"} "bar"))
          (edge #js {:style #js {:stroke "black"
                                 :fill "white"}
                     :source "foo"
                     :target "bar"}))))))
