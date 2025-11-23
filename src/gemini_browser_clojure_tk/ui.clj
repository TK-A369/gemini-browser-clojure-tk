(ns gemini-browser-clojure-tk.ui
  (:gen-class)
  (:require
    [clojure.core.async :as async]
    clojure.pprint)
  (:import (javax.swing JFrame JButton JLabel JPanel JScrollPane JTextField SwingUtilities BoxLayout)))

(defrecord browser-ui
  [root-frame tabs])

; state shall be an agent of type tab-state
(defrecord tab
  [id state])

(defrecord tab-state
  [url content])

(defmacro fn-action-listener [args-list & body]
  `(proxy [java.awt.event.ActionListener] []
    ~(concat (list 'actionPerformed args-list) body)))

(defn make-browser-ui []
  (let
    [
      tabs (agent (sorted-map))
      next-tab-id (ref 0)
      active-tab-id (agent nil)
      root-frame-chan (async/chan 1)
      _ (SwingUtilities/invokeAndWait (fn [] (let
        [
          root-frame (JFrame/new "Gemini browser")
          panel-tabs (JPanel/new)
          scroll-pane-tabs (JScrollPane/new panel-tabs)
          panel-url (JPanel/new)
          text-field-url (JTextField/new)
          button-go (JButton/new "Go!")
          panel-content (JPanel/new)
          label-status (JLabel/new "Status: ")
          make-tabs-buttons (fn [tabs-curr]
            (.removeAll panel-tabs)
            (doseq [t tabs-curr] (let
              [
                tab-id (-> t second :id)
                btn (JButton/new (format "Tab %d" tab-id))]
              (.addActionListener btn (fn-action-listener [_]
                (println (format "Changing tab to %d" tab-id))
                (send active-tab-id (fn [_] tab-id))))
              (.add panel-tabs btn)))
            (let
              [btn (JButton/new "+")]
              (.addActionListener btn (fn-action-listener [_]
                (let
                  [this-tab-id (dosync
                    (commute next-tab-id (fn [s] (+ s 1))))]
                  (println (format "Adding new tab %d" this-tab-id))
                  (send tabs (fn [s] (assoc s this-tab-id (tab/new this-tab-id (agent (tab-state/new "" nil)))))))))
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

        ; Layout
        (.setLayout root-frame (BoxLayout/new (.getContentPane root-frame) BoxLayout/Y_AXIS))
        (.add root-frame (JLabel/new "Tabs:"))
        (.setLayout panel-tabs (BoxLayout/new panel-tabs BoxLayout/X_AXIS))
        (.setMaximumSize panel-tabs (java.awt.Dimension/new 100000 80))
        (.setMaximumSize scroll-pane-tabs (java.awt.Dimension/new 100000 120))
        (.setHorizontalScrollBarPolicy scroll-pane-tabs javax.swing.ScrollPaneConstants/HORIZONTAL_SCROLLBAR_ALWAYS)
        (.add root-frame scroll-pane-tabs)
        (.setLayout panel-url (BoxLayout/new panel-url BoxLayout/X_AXIS))
        (.setMaximumSize panel-url (java.awt.Dimension/new 100000 80))
        (.add panel-url text-field-url)
        (.addActionListener button-go (fn-action-listener [_]
          (let
            [
              curr-tab (get @tabs @active-tab-id)
              url-text (.getText text-field-url)]
            (when-not (nil? curr-tab)
              (send (:state curr-tab) (fn [s]
                (println (format "Setting URL of tab %d to %s" (:id curr-tab) url-text))
                (tab-state/new url-text nil)))))))
        (.add panel-url button-go)
        (.add root-frame panel-url)
        (.add root-frame (JLabel/new "Content:"))
        (.add root-frame panel-content)
        (.add root-frame label-status)
        (make-tabs-buttons @tabs)
        (.pack root-frame)
        (.setVisible root-frame true)

        (async/>!! root-frame-chan root-frame))))
      root-frame (async/<!! root-frame-chan)]
    (browser-ui/new root-frame tabs)))
