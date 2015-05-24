(ns ^:figwheel-always buffaloe.graph
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [dagre-react]))

(def graph (js/React.createFactory js/DagreReact.Graph))
(def vertex (js/React.createFactory js/DagreReact.Vertex))
(def edge (js/React.createFactory js/DagreReact.Edge))

(def node-id (atom 0))
(defn parse-tree-graph [tree]
  (if (or (symbol? tree)
          (= tree :e))
    (let [key (str "tree-" (swap! node-id inc))]
      {:subtree
       [(vertex #js {:width 50
                     :height 20
                     :key key}
                (dom/text #js {:y 20}
                          (str tree)))]
       :key key})
    (let [children (map parse-tree-graph (rest tree))
          key (str "leaf-" (swap! node-id inc))]
      {:subtree
       (into
        (mapcat :subtree children)
        (conj (map #(edge
                     #js {:source key
                          :target %
                          :style #js {:fill "white"
                                      :stroke "black"}})
                   (map :key children)) ; make edges

              (vertex #js {:width 50
                           :height 25
                           :key key}
                      (dom/text #js {:x 15
                                     :y 20}
                                (str (first tree))))))
       :key key})))

(defn parse-tree [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/svg #js {:width "1000px"
                    :height "1000px"}
        (graph nil
          (when data
            (swap! node-id (fn [] 0))
            (clj->js (:subtree (parse-tree-graph data)))))))))
