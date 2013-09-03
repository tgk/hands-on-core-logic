;; NOTES FROM FIRST DRY-RUN
;; time:
;; 15 first task
;; 30 end of conso
;; 35 start conde task
;; 55 write not-containso using defne
;;
;; General:
;; Use numbers instead of :foo, :bar, :baz
;; Mention paredit in the beginning
;; Mention that copy-pasting is encouraged


(ns user
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic)
  (:require [clojure.core.logic.fd :as fd]))

;; find all solutions with run*
(run* [q]
      (== q true))

;; a logical value can take any Clojure value
(run* [q]
      (== q 1))
(run* [q]
      (== q :foo))

;; setting no constraints gives us an unbound lvar
(run* [q])

;; nothing satisfies conflicting goals
(run* [q]
      (== q true)
      (== q false))

;; we can also use != for enforcing something isn't unified
(run* [q]
      (!= q true))

;; we can have more lvars
(run* [q p]
      (== q :foo)
      (!= p :bar))

;;;; SKIP?
;; we don't have to bind them
(run* [q p])

"
- TASK: write a logic program where p is unified with 1 and q is
not unified with p
"
#_(run* [q p]
      (== p 1)
      (!= q p))

;; fresh gives us additional variables
(run* [q]
      (fresh [p]
             (== p 42)
             (== q p)))

;; do you know cons from LISP and Clojure?
(cons 1 [2 3])

;; conso is like cons, but we have to bind the result as well
(run* [q]
      (conso 1 [2 3] q))

;; and the lvar can be anywhere
(run* [q]
      (conso q [2 3] [1 2 3]))

"
- TASK: where else can q be? what happens?
- TASK: what happens if q is not used?
- TASK: what if no list can satisfy it
- TASK: use two lvars in conso in multiple places
"
;; TASK: where else can q be? what happens?
#_(run* [q]
      (conso 1 q [1 2 3]))

;; TASK: what happens if q is not used?
#_(run* [q]
      (conso 1 [2 3] [1 2 3]))

;; TASK: what if no list can satisfy it
#_(run* [q]
      (conso 1 [2 42] [1 2 3]))

;; TASK: use two lvars in conso in multiple places
#_(run* [q p]
      (conso q p [1 2 3]))

#_(run* [q p]
      (conso q [2 3] p))

#_(run* [q p u]
        (conso q p u))

;; notice the (_0 . _1) in the last example - this simply means "the
;; list element _0 with some tail _1"

;; conde gives us alternative solutions
(run* [q]
      (conde
       [(== q 1)]
       [(== q 2)]))

(run* [q]
      (conde
       [(== q 1)]
       [(!= q 2)]))

;; several clauses works as a logical and
(run* [q]
      (fresh [h t]
             (conde
              [(== q [])]
              [(conso h t q) (== 1 h)])))

"
- TASK: write a goal where the lvar q is either [1 2 3] or has only
  one element.
"
#_(run* [q]
      (fresh [h t]
             (conde
              [(== q [1 2 3])]
              [(conso h t q) (== t nil)])))

" - TASK: write a goal where the lvar q is a list with one element if
    the first element is one and unbound number of elements
    otherwise. For example:

    (1), (42, 2, 2, 4, 1)
"
#_(run* [q]
      (fresh [h t]
             (conde
              [(conso 1 t q) (== t nil)]
              [(conso h t q) (!= h 1) (!= t nil)])))

;; goals can be set up in functions
(defn containso
  [x l]
  (fresh [h t]
         (conde
          [(conso h t l) (== x h)]
          [(conso h t l) (containso x t)])))

(run* [q]
      (containso 1 [1 2 3]))

(run* [q]
      (containso q [1 2 3]))

;; if we use run* when there are an infinit number of results, we are in
;; trouble - we can use run instead
(run 3 [q]
     (containso 42 q))

"
- TASK: write not-containso
"
#_(defn not-containso
  [x l]
  (fresh [h t]
         (conde
          [(== l [])]
          [(conso h t l) (!= h x) (not-containso x t)])))

#_(run* [q]
      (not-containso 1 [1 2 3]))
#_(run* [q]
      (not-containso 42 [1 2 3]))
#_(run 3 [q]
     (not-containso 1 q))

;; the defne macro can be used for making sweet defn conde magic
(defne containso
  [x l]
  ([_ (h . t)] (== x h))
  ([_ (h . t)] (containso x t)))

(run* [q]
      (containso q [1 2 3]))

(run 3 [q]
     (containso 1 q))

;; we can match on the first clause and skip "=="
(defne containso
  [x l]
  ([_ (x . t)]) ;; x is matching the head of the list
  ([_ (h . t)] (containso x t)))

"
- TASK: re-write not-containso using defne
"
#_(defne not-containso
  [x l]
  ([_ []])
  ([_ (h . t)] (!= h x) (not-containso x t)))

#_(run* [q]
      (not-containso 1 [1 2 3]))

#_(run* [q]
      (not-containso 42 [1 2 3]))

#_(run 3 [q]
     (not-containso 1 q))

;; There are a bunch of built in relations in core.logic. We are not going to go through them all here
(comment
  (membero x l) ;; x is a member of l
  (appendo x y z) ;; z is x appended to y
  (rembero x l o) ;; o is l with x removed
  (permuteo xl yl) ;; xl and yl are permutations
  (distincto l)) ;; all elements in l are distinct

"
- TASK: (latero x y l) - write defne that matches \"x is later than y in l\"
        e.g. (latero 1 2 [3 2 1]) is true,
             (latero 1 2 [1 2 3]) is false
"
(defne latero
  "x is later than y in l."
  [x y l]
  ([_ _ [y . t]] (containso x t))
  ([_ _ [h . t]] (!= x h) (latero x y t)))

(run* [q]
      (latero q 2 [1 2 3]))

(run 3 [q]
     (latero 2 1 q))

;; given: not-righto
(defne not-righto
  "x is not right of y in l."
  [x y l]
  ([_ _ []])
  ([_ _ [x . t]] (membero y t))
  ([_ _ [y h . t]] (!= h x) (membero x t))
  ([_ _ [h . t]] (!= y h) (!= x h) (not-righto x y t)))

(run* [q]
      (not-righto q 2 [1 2 3]))

;; TODO: Write some examples of true and false expressions
"
- TASK: given not-righto, write (not-adjacento x y l) \"x and y are not
        adjacent in l\"
        e.g. (not-adjacento 1 3 [1 2 3]) is true
             (not-adjacento 1 42 [1 2 3]) is true
             (not-adjacento 1 2 [1 2 3]) is false
             (not-adjacento 2 1 [1 2 3]) is false"
(defn not-adjacento
  "x and y are not adjacent in l."
  [x y l]
  (fresh []
         (not-righto x y l)
         (not-righto y x l)))

(run* [q]
      (not-adjacento q 3 [1 2 3]))

(run 3 [q]
     (not-adjacento 1 3 q))

;; EXAMPLE: Dinesman's multiple-dwelling problem
"Dinesman's multiple-dwelling problem

Baker, Cooper, Fletcher, Miller, and Smith live on different floors of
an apartment house that contains only five floors. Baker does not live
on the top floor. Cooper does not live on the bottom floor. Fletcher
does not live on either the top or the bottom floor. Miller lives on a
higher floor than does Cooper. Smith does not live on a floor adjacent
to Fletcher's. Fletcher does not live on a floor adjacent to
Cooper's. Where does everyone live?"

;; We are using latero, not-adjacento and permuteo
(run* [floors]
      (fresh [first second third fourth fifth]
             (== floors [first second third fourth fifth])
             (!= fifth :baker)
             (!= first :cooper)
             (!= first :fletcher)
             (!= fifth :fletcher)
             (latero :miller :cooper floors)
             (not-adjacento :smith :fletcher floors)
             (not-adjacento :fletcher :cooper floors)
             (permuteo [:baker :cooper :fletcher :miller :smith]
                       floors)))


;;;;; I don't think we'll make it further than here - I migth release
;;;;; the packeto as lecture notes instead.



;; Finite domain

;; Not everything is lists! Let's do some maths!
(run* [q]
      (fd/+ 1 1 2))

(run* [q]
      (fd/+ 1 2 2))

(run* [q]
      (fd/+ 1 1 q))

(run* [q]
      (fd/+ 1 q 2))

;; TASK: Write (counto l n), n is the length of l
#_(defne counto
  [l n]
  ([[] 0])
  ([(h . t) _]
     (fresh [m]
            (fd/+ m 1 n)
            (counto t m))))

#_(run* [q]
     (counto [:foo :bar :baz] q))

;; If we try to generate lists of a length, we will fail. The fact that
;; m is not bound to an interval means that the logic engine is unable
;; to figure out what to do

;; FIX: Bind m to an interval
(defne counto
  [l n]
  ([[] 0])
  ([(h . t) _]
     (fresh [m]
            (fd/in m (fd/interval 0 Integer/MAX_VALUE))
            (fd/+ m 1 n)
            (counto t m))))

;; And now it works
(run* [q]
      (counto q 3))

;; An interesting example: binaryo
(defne binaryo [p n]
  ([() 0])
  ([[h . t] _]
     (fresh [m m-times-two]
            (fd/in m (fd/interval 0 Integer/MAX_VALUE))
            (fd/in h (fd/interval 0 1))
            (binaryo t m)
            (fd/* m 2 m-times-two)
            (fd/+ m-times-two h n))))

(run 3 [p]
     (binaryo p 1))
(run 3 [p]
     (binaryo p 2))
(run 3 [p]
     (binaryo p 3))

;; Erlang packet destructing is so simple and nice, we should be jealoux!
"<<Size:4/binary,C:Size,_Rest>> = Data."

;; Let's do something similar for Clojure:
(defne packeto [p pattern]
  ([() ()])
  ([_ [:chunk n c . t]]
     (fresh [pt]
            (appendo c pt p)
            (counto c n)
            (packeto pt t)))
  ([_ [:binary n v . t]]
     (fresh [c np]
            (appendo [:chunk n c] t np)
            (packeto p np)
            (binaryo c v))))

;; we can now parse packets, just like Erlang
(run 1 [q]
     (fresh [b c
             rest-size rest-chunk]
            (packeto [1 0 0 1 , 0 1 0 0 1 0 1 1 1 ,,, 1 1 0]
                     [:binary 4 b
                      :chunk b c
                      :chunk rest-size rest-chunk])
            (== q [b c])))

;; we can do better - we can generate packets as well!
(run 1 [q]
     (fresh [b]
            (packeto q
                     [:binary 4 b
                      :chunk b [0 1 0 1 0 1 0 1 1]])))


;;; HOMEWORK

;; lasto, reverso, palindromo

;; Send more money puzzle:
;;
;;     S E N D
;; +   M O R E
;; = M O N E Y
;;
;; which values from 0 to 9 unifies the equation?


;; Eight queens puzzle
;; Place eight queens on a chess board such that no queen can take any others


;; Balanced tree goal
;; construct and check trees are balanced


;;; WHERE TO GO NOW?

;; - compose with core.logic
;;   http://tgk.github.io/2012/12/the-composing-schemer.html

;; - zebra puzzle
;;   https://github.com/swannodette/logic-tutorial

;; - connected components algorithm
;;   http://tgk.github.io/2012/08/finding-cliques-in-graphs-using-core-logic.html

;; - schema planning by Edmund Jackson
;;   http://vimeo.com/45128721

;; - ClojureConj miniKanren talk
;;   http://www.youtube.com/watch?v=5Q9x16uIsKA

;; - Read "The Reasoned Schemer"
;;   http://mitpress.mit.edu/books/reasoned-schemer

;; - David Nolen's sudoku solver
;;   https://gist.github.com/swannodette/3217582

;; - Ancestral relationships
;;   https://github.com/swannodette/logic-tutorial

;; - Quine generation in miniKanren
;;   http://www.cs.indiana.edu/~eholk/papers/sfp2012.pdf
