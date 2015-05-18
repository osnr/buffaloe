(ns buffaloe.grammar
  (:refer-clojure :exclude [==])
  (:require [cljs.core.logic :refer [lcons conso succeed]])
  (:require-macros [cljs.core.logic.macros
                    :refer [fresh run run* defne conde == conda
                            -->e --> def-->e !=
                            trace-lvars]]))

(declare sentence
         noun-phrase build-noun-phrase
         determiner noun adjective relative conjunction
         verb-phrase verb)

(def-->e sentence [s gap]
  ([[:s ?np ?vp] :no-gap]
     (fresh [number valence]
       (noun-phrase ?np number)
       (verb-phrase ?vp number :no-gap valence)))
  ([[:s [:np :e] ?vp] :subj-gap]
     (fresh [number valence]
       (verb-phrase ?vp number :no-gap valence)))
  ([[:s ?np ?vp] :obj-gap]
     (fresh [number]
       (noun-phrase ?np number)
       (verb-phrase ?vp number :obj-gap :transitive))))

(def-->e noun-phrase [np number]
  ([?np ?number]
     (fresh [det adj n rel]
       (determiner det number)
       (adjective adj)
       (noun n number)
       (relative rel)
       (!dcg (build-noun-phrase det adj n rel np)))))

; from https://gist.github.com/swannodette/5384670
(defne rember*o [x l o]
  ([_ [] []])
  ([_ [x . ?xs] _]
    (rember*o x ?xs o))
  ([_ [?y . ?xs] [?y . ?ys]]
    (!= x ?y)
    (rember*o x ?xs ?ys)))

(defn build-noun-phrase [det adj n rel np]
  (fresh [l]
    (rember*o :epsilon [det adj n rel] l)
    (conso :np l np)))

(def-->e determiner [det number]
  ([[:det 'a] :singular] '[a])
  ([:epsilon :plural] [])
  ([[:det 'the] ?any] '[the]))

(def-->e noun [n number]
  ([[:n 'dog] :singular] '[dog])
  ([[:n 'dogs] :plural] '[dogs])
  ([[:n 'buffalo] ?any] '[buffalo]))

(def-->e adjective [adj]
  ([:epsilon] [])
  ([[:adj 'buffalo]] ['buffalo]))

(def-->e relative [rel]
  ([:epsilon] [])
  ([[:cp ?c ?s]]
     (fresh [gap]
       (conjunction ?c)
       (sentence ?s gap)
       (!dcg (conde [(== gap :subj-gap)]
                    [(== gap :obj-gap)])))))

(def-->e conjunction [c]
  ([[:c :e]] [])
  ([[:c 'that]] '[that]))

(def-->e verb-phrase [vp number gap tr]
  ([[:vp ?v] ?number :no-gap :intransitive]
     (verb ?v ?number :intransitive))
  ([[:vp ?v ?np] ?number :no-gap :transitive]
     (verb ?v ?number :transitive)
     (fresh [any-num]
       (noun-phrase ?np any-num)))
  ([[:vp ?v [:np :e]] ?number :obj-gap :transitive]
     (verb ?v ?number :transitive)))

(def-->e verb [v number tr]
  ([[:v 'sleeps] :singular :intransitive] '[sleeps])
  ([[:v 'sleep] :plural :intransitive] '[sleep])
  ([[:v 'buffaloes] :singular :transitive] '[buffaloes])
  ([[:v 'buffalo] :plural :transitive] '[buffalo]))

(defn parse-1 [s]
  (run 1 [parse-tree]
       (sentence parse-tree :no-gap s [])))

(defn parse [s]
  (run* [parse-tree]
        (sentence parse-tree :no-gap s [])))
