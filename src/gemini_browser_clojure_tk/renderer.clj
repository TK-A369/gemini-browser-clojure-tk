(ns gemini-browser-clojure-tk.renderer
  (:gen-class)
  (:require
    clojure.string)
  (:import (javax.swing JTextArea))
  (:import (java.awt BorderLayout)))

(defn render-plain-text [text-area-content content] (let
  [
    content-str (java.lang.String/new (into-array Byte/TYPE content) java.nio.charset.StandardCharsets/UTF_8)
    _ (println content-str)
    str-lines (clojure.string/split content-str #"\r?\n")]
  (println "Rendering...")
  ; (doseq [l str-lines] 
  ;   ; (print (str l "\n"))
  ;   (.insertString doc 0 (str l "\n") nil))
  (.removeAll text-area-content)
  (.setText text-area-content content-str)
  (.setLineWrap text-area-content true)
  (.setEditable text-area-content false)
  ; (.setMaximumSize text-area (java.awt.Dimension/new 100000 100000))
  (.setLayout text-area-content (BorderLayout/new))
  (.revalidate text-area-content)
  (.repaint text-area-content)))

(defn render [text-area-content content-type content]
  (cond
    (or (nil? content-type) (nil? content)) (render-plain-text text-area-content "(Empty)")
    (= content-type "text/plain") (render-plain-text text-area-content content)
    :else (render-plain-text text-area-content content)))
