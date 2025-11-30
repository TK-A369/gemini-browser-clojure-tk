(ns gemini-browser-clojure-tk.renderer
  (:gen-class)
  (:require
    gemini-browser-clojure-tk.gemtext)
  (:import (javax.swing JTextArea JButton BoxLayout SwingConstants))
  (:import (java.awt BorderLayout Font)))

(defn render-plain-text [panel-content content] (let
  [
    content-str (java.lang.String/new (into-array Byte/TYPE content) java.nio.charset.StandardCharsets/UTF_8)
    ; _ (println content-str)
    text-area (JTextArea/new content-str)]
  (println "Rendering plain text...")
  ; (doseq [l str-lines] 
  ;   ; (print (str l "\n"))
  ;   (.insertString doc 0 (str l "\n") nil))
  (.removeAll panel-content)
  ; (.setText panel-content content-str)
  ; (.setLineWrap panel-content true)
  ; (.setEditable panel-content false)
  ; (.setMaximumSize text-area (java.awt.Dimension/new 100000 100000))
  (.setLineWrap text-area true)
  (.setEditable text-area false)
  (.add panel-content text-area)
  (.setLayout panel-content (BoxLayout/new panel-content BoxLayout/Y_AXIS))
  (.revalidate panel-content)
  (.repaint panel-content)))

(defn render-gemtext [panel-content content] (let
  [
    content-str (java.lang.String/new (into-array Byte/TYPE content) java.nio.charset.StandardCharsets/UTF_8)
    parsing-result (gemini-browser-clojure-tk.gemtext/parse-gemtext content-str)]
  (println "Rendering Gemtext...")
  (.removeAll panel-content)
  ; (.setLayout panel-content (BoxLayout/new panel-content BoxLayout/Y_AXIS))
  (doseq [elem parsing-result]
    (condp = (:type elem)
      :text (let
        [text-area (JTextArea/new)]
        (.setText text-area (:content elem))
        (.add panel-content text-area))
      :heading (let
        [text-area (JTextArea/new)]
        (.setText text-area (:content elem))
        (.setFont text-area (Font/new "Times New Roman" Font/BOLD 32))
        (.add panel-content text-area))
      :preformatted (let
        [text-area (JTextArea/new)]
        (.setText text-area (:content elem))
        (.setFont text-area (Font/new "Courier New" Font/PLAIN 12))
        (.add panel-content text-area))
      :link (let
        [btn (JButton/new)]
        (.setText btn (:content elem))
        ; (.setFont btn (Font/new "Courier New" Font/PLAIN 12))
        (.setHorizontalAlignment btn SwingConstants/LEFT)
        (.setAlignmentX btn 0.0)
        (.add panel-content btn))))
  (.setLayout panel-content
    (BoxLayout/new panel-content BoxLayout/Y_AXIS))
  (.revalidate panel-content)
  (.repaint panel-content)))

(defn render [panel-content content-type content]
  (cond
    (or (nil? content-type) (nil? content)) (render-plain-text panel-content "(Empty)")
    (= content-type "text/plain") (render-plain-text panel-content content)
    (= content-type "text/gemini") (render-gemtext panel-content content)
    :else (render-plain-text panel-content content)))
