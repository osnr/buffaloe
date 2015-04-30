(ns buffaloe.grammar
  (:require [cljs.core.logic :as m :refer [membero]]))

(m/run* [q]
  (membero q '(:cat :dog :bird :bat :debra)))

(defne s [s gap]
  ([[:s np vp] :no-gap]
     (fresh [number]
       (noun-phrase np number)
       (verb-phrase vp number :no-gap valence)))
  
  ([[:s [:np :e] vp] :subj-gap]
     (fresh [number]
       (verb-phrase vp number :no-gap valence)))
  
  ([[:s np vp] :obj-gap]
     (fresh [number]
       (noun-phrase np number)
       (verb-phrase vp number :obj-gap :transitive))))

(defn noun-phrase [np number]
  (fresh [det adj n rel]
    (det det number)
    (adj adj)
    (n n number)
    (rel rel)))
