(ns gemini-browser-clojure-tk.ui
  (:gen-class)
  (:require
    [clojure.core.async :as async]
    clojure.pprint)
  (:import (javax.swing JFrame JButton JLabel JPanel SwingUtilities BoxLayout)))

(defrecord browser-ui
  [root-frame tabs])
(defrecord tab
  [id state])

(defmacro fn-action-listener [args-list & body]
  `(proxy [java.awt.event.ActionListener] []
    ~(concat (list 'actionPerformed args-list) body)))

(defn make-browser-ui []
  (let
    [
      tabs (agent (sorted-map))
      next-tab-id (ref 0)
      root-frame-chan (async/chan 1)
      _ (SwingUtilities/invokeAndWait (fn [] (let
        [
          root-frame (JFrame/new "Gemini browser")
          panel-tabs (JPanel/new)
          panel-content (JPanel/new)
          label-status (JLabel/new "Status: ")
          make-tabs-buttons (fn [tabs-curr]
            (.removeAll panel-tabs)
            (doseq [t tabs-curr] (let
              [btn (JButton/new (format "Tab %d" (:id t)))]
              (.addActionListener btn (fn-action-listener [_]
                (println (format "Changing tab to %d" (:id t)))))
              (.add panel-tabs btn)))
            (let
              [btn (JButton/new "+")]
              (.addActionListener btn (fn-action-listener [_]
                (let
                  [this-tab-id (dosync
                    (commute next-tab-id (fn [s] (+ s 1))))]
                  (println (format "Adding new tab %d" this-tab-id))
                  (send tabs (fn [s] (assoc s this-tab-id (tab/new this-tab-id (agent nil))))))))
              (.add panel-tabs btn))
            (.revalidate panel-tabs)
            (.repaint panel-tabs)
            (.revalidate root-frame)
            (.repaint root-frame))]
        (add-watch tabs "main-keys-watch" (fn [_ _ _ tabs-new]
          (SwingUtilities/invokeLater (fn []
            (println "Tabs changes")
            (clojure.pprint/pprint tabs-new)
            (make-tabs-buttons tabs-new)
            (println "Successfully updated tabs buttons")))))
        (.setLayout root-frame (BoxLayout/new (.getContentPane root-frame) BoxLayout/Y_AXIS))
        (.add root-frame (JLabel/new "Tabs:"))
        (.add root-frame panel-tabs)
        (.add root-frame (JLabel/new "Content:"))
        (.add root-frame panel-content)
        (make-tabs-buttons @tabs)
        (.pack root-frame)
        (.setVisible root-frame true)
        (async/>!! root-frame-chan root-frame))))
      root-frame (async/<!! root-frame-chan)]
    (browser-ui/new root-frame tabs)))
