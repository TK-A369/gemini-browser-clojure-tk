(ns gemini-browser-clojure-tk.renderer
  (:gen-class)
  (:require
    clojure.string))

(defn render-plain-text [panel-content data] (let
  [
    data-str (java.lang.String/new (into-array Byte/TYPE data java.nio.charset.StandardCharsets/UTF_8))
    str-lines (clojure.string/split data-str #"\r?\n")]
  (doseq [l str-lines] (
    (print l)))))
