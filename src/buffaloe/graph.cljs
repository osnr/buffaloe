(ns ^:figwheel-always buffaloe.graph
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [dagre]
              [dagre-react]))

(def graph (js/React.createFactory js/DagreReact.Graph))
(def vertex (js/React.createFactory js/DagreReact.Vertex))
(def edge (js/React.createFactory js/DagreReact.Edge))

(def foo (atom 0))
(defn parse-tree-graph [tree]
  (if (symbol? tree)
    (let [key (str "tree-" (swap! foo inc))]
      {:subtree
       [(vertex #js {:width 50
                     :height 50
                     :key key}
                (dom/text nil (str tree)))]
       :key key})
    (let [children (map parse-tree-graph (rest tree))
          key (str "leaf-" (swap! foo inc))]
      {:subtree
       (into (mapcat :subtree children)
             (conj (map #(edge #js {:source key
                                    :target %})
                        (map :key children)) ; make edges
                   (vertex #js {:width 50
                                :height 50
                                :key key}
                           (dom/text nil (str (first tree))))))
       :key key})))

(defn parse-tree [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/svg #js {:width "1000px"
                    :height "1000px"}
        (graph nil
          (when (first data)
            (swap! foo (fn [] 0))
            (clj->js (:subtree (parse-tree-graph (first data))))))))))
