(ns gemini-browser-clojure-tk.util
  (:gen-class))

(defmacro fn-action-listener [args-list & body]
  `(proxy [java.awt.event.ActionListener] []
    ~(concat (list 'actionPerformed args-list) body)))
