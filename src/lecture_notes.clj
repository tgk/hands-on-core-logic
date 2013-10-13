(ns user
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic)
  (:require [clojure.core.logic.fd :as fd]))

;; LECTURE NOTES - Finite domains

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
     (counto [1 2 3] q))

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
