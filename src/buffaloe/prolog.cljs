(ns buffaloe.prolog
  (:require [clojure.zip :as zip]))

(def worker (atom (js/Worker. "js/buffalo.js")))
(def num-queued (atom 0))

;; basic parser to convert prolog printed output into nested vectors
(defn prolog-output->tree [output]
  (loop [remaining output
         loc (zip/vector-zip [])]
    (let [c (first remaining)
          rest-remaining (subs remaining 1)]
      (case c
        nil (zip/root loc)
        "(" (recur rest-remaining
                   (-> loc
                       zip/down
                       zip/rightmost
                       (zip/edit keyword) ; hack: make prefix (np, v, etc) a keyword

                       (zip/insert-right [])
                       zip/right))
        ")" (recur rest-remaining
                   (let [child (first (zip/node loc))]
                     (if (symbol? child)  ; hack: ignore () around tokens
                       (zip/up (zip/replace loc child))
                       (zip/up loc))))
        "," (recur rest-remaining
                   (-> loc
                       (zip/insert-right [])
                       zip/right))
        " " (recur rest-remaining loc)
        (let [token (re-find #"[A-Za-z]+" remaining)]
          (recur (subs remaining (count token))
                 (zip/append-child loc (symbol token))))))))

(defn on-message [callback e]
  (let [data (.-data e)]
    (if-let [err (.-error data)]
      (case err
        "Calling stub instead of signal()"
        nil

        "no."
        (do (swap! num-queued dec)
            (callback nil)))

      (do (swap! num-queued dec)
          (callback (prolog-output->tree data))))))

(defn parse-1 [s callback]
  (when (> @num-queued 2)
    (.terminate @worker)
    (swap! worker #(js/Worker. "js/buffalo.js")))
  (let [start (js/performance.now)]
    (set! (.-onmessage @worker)
          (fn [e]
            (on-message #(callback %
                                   (- (js/performance.now)
                                      start))
                        e)))
    (.postMessage @worker (clj->js s))
    (swap! num-queued inc)))
