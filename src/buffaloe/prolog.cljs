(ns buffaloe.prolog
  (:require [clojure.zip :as zip]))

(defn- new-worker []
  (js/Worker. "js/buffalo.js"))

(def worker (atom (new-worker)))

(defn- reload-worker []
  (.terminate @worker)
  (swap! worker new-worker))

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
                     (if (symbol? child)
                       (zip/up (zip/replace loc child))
                       (zip/up loc))))
        "," (recur rest-remaining
                   (-> loc
                       (zip/insert-right [])
                       zip/right))
        " " (recur rest-remaining loc)
        (let [token (re-find #"[A-Za-z]+" remaining)]
          (recur (subs remaining (count token))
                 (zip/append-child loc (symbol token)))))))) ; hack: ignore () around tokens

(defn on-message [callback e]
  (let [data (.-data e)]
    (if-let [err (.-error data)]
      (case err
        "Calling stub instead of signal()"
        nil

        "no."
        (callback nil))

      (callback (prolog-output->tree data)))))

(defn parse-1 [s callback]
  (reload-worker)
  (set! (.-onmessage @worker)
        #(on-message callback %))
  (.postMessage @worker (clj->js s)))
