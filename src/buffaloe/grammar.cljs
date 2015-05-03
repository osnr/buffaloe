(ns buffaloe.grammar
  (:refer-clojure :exclude [==])
  (:require [cljs.core.logic :refer [lcons conso]])
  (:require-macros [buffaloe.dcg :refer [-->e --> def-->e]]
                   [cljs.core.logic :refer [fresh run run* defne conde ==]]))

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

; direct translation from http://www.swi-prolog.org/pldoc/doc/swi/library/lists.pl?show=src
(defne deleteo [list1 elem list2]
  ([[] _ []])
  ([[elem . tail] del result]
     (conde
      [(== elem del)
       (deleteo tail del result)]
      [(fresh [rest]
         (conso elem rest result)
         (deleteo tail del rest))])))

(defn build-noun-phrase [det adj n rel np]
  (fresh [l]
    (deleteo [det adj n rel np] :epsilon l)
    (conso :np l np)))

(def-->e determiner [det number]
  ([[:det 'a] :singular] ['a])
  ([:epsilon :plural] [])
  ([[:det 'the] ?any] ['the]))

(def-->e noun [n number]
  ([[:n 'dog] :singular] ['dog])
  ([[:n 'dogs] :plural] ['dogs])
  ([[:n 'buffalo] ?any] ['buffalo]))

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
  ([[:c 'e]] [])
  ([[:c 'that]] ['that]))

(def-->e verb-phrase [vp number gap tr]
  ([[:vp ?v] ?number :no-gap :intransitive]
     (verb ?v ?number :intransitive))
  ([[:vp ?v ?np] ?number :no-gap :transitive]
     (verb ?v ?number :transitive))
  ([[:vp ?v [:np 'e]] ?number :obj-gap :transitive]
     (verb ?v ?number :transitive)))

(def-->e verb [v number tr]
  ([[:v 'sleeps] :singular :intransitive] ['sleeps])
  ([[:v 'sleep] :plural :intransitive] ['sleep])
  ([[:v 'buffaloes] :singular :transitive] ['buffaloes])
  ([[:v 'buffalo] :plural :transitive] ['buffalo]))

(defn parse [s]
  (run 1 [parse-tree]
       (sentence parse-tree s :no-gap [])))
