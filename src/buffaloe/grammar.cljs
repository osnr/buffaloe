(ns buffaloe.grammar
  (:require [cljs.core.logic :refer [lcons]])
  (:require-macros [buffaloe.dcg :refer [-->e --> def-->e]]
                   [cljs.core.logic :refer [fresh run run* defne conde fresh ]]))

;; (m/run* [q]
;;   (membero q '(:cat :dog :bird :bat :debra)))

;; (defne s [s gap]
;;   ([[:s np vp] :no-gap]
;;      (fresh [number]
;;        (noun-phrase np number)
;;        (verb-phrase vp number :no-gap valence)))
  
;;   ([[:s [:np :e] vp] :subj-gap]
;;      (fresh [number]
;;        (verb-phrase vp number :no-gap valence)))
  
;;   ([[:s np vp] :obj-gap]
;;      (fresh [number]
;;        (noun-phrase np number)
;;        (verb-phrase vp number :obj-gap :transitive))))

;; (defn noun-phrase [np number]
;;   (fresh [det adj n rel]
;;     (det det number)
;;     (adj adj)
;;     (n n number)
;;     (rel rel)))

;; (-->e det
;;       ('[the])
;;       ('[a]))

;; (-->e n
;;       ('[witch])
;;       ('[wizard]))

;; (--> v '[curses])

;; (--> np det n)
;; (--> vp v np)
;; (--> s np vp)

;; success
;; (run* [q]
;;       (np '[the witch] []))

;; success
;; (run* [q]
;;       (s '[a witch curses the wizard] []))

(def-->e verb [v]
  ([[:v 'eats]] '[eats]))

(def-->e noun [n]
  ([[:n 'bat]] '[bat])
  ([[:n 'cat]] '[cat]))

(def-->e det [d]
  ([[:d 'the]] '[the])
  ([[:d 'a]] '[a]))

(def-->e noun-phrase [n]
  ([[:np ?d ?n]] (det ?d) (noun ?n)))

(def-->e verb-phrase [n]
  ([[:vp ?v ?np]] (verb ?v) (noun-phrase ?np)))

(def-->e sentence [s]
  ([[:s ?np ?vp]] (noun-phrase ?np) (verb-phrase ?vp)))

(defn test-parse []
  (run 1 [parse-tree]
       (sentence parse-tree '[the bat eats a cat] [])))
