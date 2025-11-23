(ns gemini-browser-clojure-tk.main
  (:gen-class)
  (:require [gemini-browser-clojure-tk.ui :as ui]))

(defn -main [& args]
  (ui/make-browser-ui))
