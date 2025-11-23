(ns gemini-browser-clojure-tk.ui
  (:gen-class)
  (:require [clojure.core.async :as async])
  (:import (javax.swing JFrame JButton JLabel JPanel SwingUtilities BoxLayout)))

(defrecord browser-ui
  [root-frame tabs])

(defn make-browser-ui []
  (let
    [
      tabs (agent (sorted-map))
      root-frame-chan (async/chan 1)
      _ (SwingUtilities/invokeAndWait (fn [] (let
        [
          root-frame (JFrame/new "Gemini browser")
          panel-tabs (JPanel/new)
          panel-content (JPanel/new)]
        (add-watch tabs "main-keys-watch" (fn [_ _ _ tabs-new]))
        (.setLayout root-frame (BoxLayout/new (.getContentPane root-frame) BoxLayout/Y_AXIS))
        (.add root-frame (JLabel/new "Tabs:"))
        (.add root-frame panel-tabs)
        (.add root-frame (JLabel/new "Content:"))
        (.add root-frame panel-content)
        (.pack root-frame)
        (.setVisible root-frame true)
        (async/>!! root-frame-chan root-frame))))
      root-frame (async/<!! root-frame-chan)]
    (browser-ui/new root-frame tabs)))
