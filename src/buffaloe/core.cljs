(ns ^:figwheel-always buffaloe.core
    (:require [buffaloe.grammar :as grammar]
              [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]))

(enable-console-print!)

(println "Parse result: " (time (grammar/parse '[buffalo buffalo buffalo buffalo buffalo])))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(om/root
  (fn [data owner]
    (reify om/IRender
      (render [_]
        (dom/h1 nil (:text data)))))
  app-state
  {:target (. js/document (getElementById "app"))})
