(ns gemini-browser-clojure-tk.renderer
  (:gen-class)
  (:require
    clojure.string)
  (:import (javax.swing JTextArea))
  (:import (java.awt BorderLayout)))

(defn render-plain-text [panel-content content] (let
  [
    content-str (java.lang.String/new (into-array Byte/TYPE content) java.nio.charset.StandardCharsets/UTF_8)
    _ (println content-str)
    str-lines (clojure.string/split content-str #"\r?\n")
    text-area (JTextArea/new content-str)]
  (println "Rendering...")
  ; (doseq [l str-lines] 
  ;   ; (print (str l "\n"))
  ;   (.insertString doc 0 (str l "\n") nil))
  (.removeAll panel-content)
  (.setLineWrap text-area true)
  (.setEditable text-area false)
  ; (.setMaximumSize text-area (java.awt.Dimension/new 100000 100000))
  (.setLayout panel-content (BorderLayout/new))
  (.add panel-content text-area)
  (.revalidate panel-content)
  (.repaint panel-content)))

(defn render [panel-content content-type content]
  (cond
    (or (nil? content-type) (nil? content)) (render-plain-text panel-content "(Empty)")
    (= content-type "text/plain") (render-plain-text panel-content content)
    :else (render-plain-text panel-content content)))
