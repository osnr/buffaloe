(ns buffalo.dcg-macros
  (:refer-clojure :exclude [==])
  (:use [cljs.core.logic]))

(defmacro --> [name & clauses]
  (let [r (range 1 (+ (count-clauses clauses) 2))
        lsyms (into [] (map lsym r))
        clauses (mark-clauses clauses)
        clauses (handle-clauses lsyms clauses)]
    `(defn ~name [~(first lsyms) ~(last lsyms)]
       (fresh [~@(butlast (rest lsyms))]
         ~@clauses))))

(defmacro def--> [name args & clauses]
  (let [r (range 1 (+ (count-clauses clauses) 2))
        lsyms (map lsym r)
        clauses (mark-clauses clauses)
        clauses (handle-clauses lsyms clauses)]
   `(defn ~name [~@args ~(first lsyms) ~(last lsyms)]
      (fresh [~@(butlast (rest lsyms))]
        ~@clauses))))

(defn handle-cclause [fsym osym cclause]
  (let [c (count-clauses cclause)
        r (range 2 (clojure.core/inc c))
        lsyms (conj (into [fsym] (map lsym r)) osym)
        clauses (mark-clauses cclause)
        clauses (handle-clauses lsyms clauses)]
    `(fresh [~@(butlast (rest lsyms))]
       ~@clauses)))

(defmacro -->e [name & cclauses]
  (let [fsym (gensym "l1_")
        osym (gensym "o")]
   `(defn ~name [~fsym ~osym]
      (conde
       ~@(map list (map (partial handle-cclause fsym osym) cclauses))))))

(defmacro def-->e [name args & pcss]
  (let [fsym (gensym "l1_")
        osym (gensym "o")]
   `(defne ~name [~@args ~fsym ~osym]
      ~@(map (fn [[p & cs]]
               (list (-> p (conj '_) (conj '_))
                     (handle-cclause fsym osym cs)))
             pcss))))
