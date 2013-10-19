;; Hands-on logic programming with core.logic
;; Thomas G. Kristensen

;; The Reasoned Schemer
;; http://www.amazon.com/The-Reasoned-Schemer-Daniel-Friedman/dp/0262562146

;; Contains the full code for miniKanren - two pages in the back!

;; Explain session format
;; Mention that copy-pasting is encouraged

;; Hajime setup: window.teacher = true


(ns user
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic)
  (:require [clojure.core.logic.fd :as fd]))

;; find all solutions with run*
(run* [q]
      (== q true))

;; since we are just expressing relations, order is not important
(run* [q]
      (== true q))

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
      (== q 1)
      (== p 2))

(run* [q p]
      (== q 1)
      (!= p 2))

;; we don't have to bind them
(run* [q p])

"
- TASK: write a logic program where p
  is unified with 1 and q is
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
- TASK: where else can q be in conso?
        what happens?
- TASK: what happens if q is not used
        in conso?
- TASK: what if no list can satisfy it?
- TASK: use two lvars in conso in
        multiple places
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
- TASK: write a goal where the lvar q is
  either [1 2 3] or has only one element.
"
#_(run* [q]
      (fresh [h]
             (conde
              [(== q [1 2 3])]
              [(conso h nil q)])))

#_(run* [q]
      (fresh [p]
             (conde
              [(== q [1 2 3])]
              [(== q [p])])))

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
#_(run* [q]
     (containso 42 q))

(run 3 [q]
     (containso 42 q))

"
- TASK: write not-containso
  HINT: the empty list does not contain
        the element; that's a good base
        case
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
- TASK: (latero x y l) - write defne
        that matches

          \"x is later than y in l\"

        e.g. (latero 1 2 [3 2 1]) is true,
             (latero 1 2 [1 2 3]) is false,
             (latero 1 2 [3 2]) is false
"
(defne latero
  "x is later than y in l."
  [x y l]
  ([_ _ [y . t]] (membero x t))
  ([_ _ [h . t]] (!= x h) (latero x y t)))

(run* [q]
      (latero q 2 [1 2 3]))

(run 3 [q]
     (latero 2 1 q))

;; given: not-righto
(defne not-righto
  "x is not immediately right of y in l."
  [x y l]
  ([_ _ []])
  ([_ _ [x . t]] (membero y t))
  ([_ _ [y h . t]] (!= h x) (membero x t))
  ([_ _ [h . t]] (!= y h) (!= x h) (not-righto x y t)))

(run* [q]
      (not-righto q 3 [1 2 3 4 5]))

"
- TASK: given not-righto, write (not-adjacento x y l)

          \"x and y are not adjacent in l\"

        e.g. (not-adjacento 1 3 [1 2 3]) is true
             (not-adjacento 1 42 [1 2 3]) is false
             (not-adjacento 1 2 [1 2 3]) is false
             (not-adjacento 2 1 [1 2 3]) is false"
#_(defn not-adjacento
  "x and y are not adjacent in l."
  [x y l]
  (fresh []
         (not-righto x y l)
         (not-righto y x l)))

(defne not-adjacento
  "x and y are not adjacent in l."
  [x y l]
  ([_ _ _]
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
(defn dinesman
  []
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
                         floors))))

(dinesman)
