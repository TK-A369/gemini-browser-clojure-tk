(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'gemini-browser-clojure-tk)
(def version "0.0.1")
(def class-dir "target/classes")
(def uberjar-file (format "target/gemini-browser-clojure-tk-%s.jar" version))

(def basis
  (delay
    (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar [_]
  (clean nil)
  (b/compile-clj {:basis @basis
                  :ns-compile '[gemini-browser-clojure-tk.main]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uberjar-file
           :basis @basis
           :main 'gemini_browser_clojure_tk.main}))
