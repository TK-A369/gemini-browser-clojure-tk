(ns gemini-browser-clojure-tk.ui
  (:gen-class)
  (:require
    [clojure.core.async :as async]
    clojure.pprint
    gemini-browser-clojure-tk.gemini-client
    gemini-browser-clojure-tk.renderer)
  (:import (javax.swing JFrame JButton JLabel JPanel JScrollPane JTextField JTextArea SwingUtilities BoxLayout))
  (:import (java.awt BorderLayout)))

(defrecord browser-ui
  [root-frame tabs])

; state shall be an agent of type tab-state
(defrecord tab
  [id state])

(defrecord tab-state
  [url content-type content])

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
          scroll-pane-tabs (JScrollPane/new panel-tabs
            javax.swing.ScrollPaneConstants/VERTICAL_SCROLLBAR_NEVER
            javax.swing.ScrollPaneConstants/HORIZONTAL_SCROLLBAR_ALWAYS)
          panel-url (JPanel/new)
          text-field-url (JTextField/new)
          button-go (JButton/new "Go!")
          text-area-content (JTextArea/new)
          scroll-pane-content (JScrollPane/new text-area-content
            javax.swing.ScrollPaneConstants/VERTICAL_SCROLLBAR_ALWAYS
            javax.swing.ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER)
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
                  (send tabs (fn [s] (let
                    [state-agent (agent (tab-state/new "" nil nil))]
                    ; fetch watch
                    (add-watch state-agent (format "tab-%d-fetch-watch" this-tab-id)
                      (gemini-browser-clojure-tk.gemini-client/tab-state-client-watch))
                    ; renderer watch
                    (add-watch state-agent (format "tab-%d-render-watch" this-tab-id) (fn [_ state-agent old-state new-state]
                      (when
                        (not=
                          [(:content-type old-state) (:content old-state)]
                          [(:content-type new-state) (:content new-state)])
                        (SwingUtilities/invokeLater (fn []
                          (gemini-browser-clojure-tk.renderer/render
                            text-area-content (:content-type new-state) (:content new-state)))))))
                    (assoc s this-tab-id (tab/new this-tab-id state-agent))))))))
              (.add panel-tabs btn))
            (.revalidate panel-tabs)
            (.repaint panel-tabs)
            (.revalidate root-frame)
            (.repaint root-frame))]

        (add-watch tabs "main-tabs-watch" (fn [_ _ _ tabs-new]
          (SwingUtilities/invokeLater (fn []
            (println "Tabs changes")
            (clojure.pprint/pprint tabs-new)
            (make-tabs-buttons tabs-new)
            (println "Successfully updated tabs buttons")))))

        ; Layout
        (.setLayout root-frame
          (BoxLayout/new (.getContentPane root-frame) BoxLayout/Y_AXIS))
        (.add root-frame (JLabel/new "Tabs:"))
        (.setLayout panel-tabs (BoxLayout/new panel-tabs BoxLayout/X_AXIS))
        ; (.setMaximumSize panel-tabs (java.awt.Dimension/new 100000 80))
        ; (.setMinimumSize panel-tabs (java.awt.Dimension/new 100 80))
        (.setMaximumSize scroll-pane-tabs (java.awt.Dimension/new 100000 120))
        (.setMinimumSize scroll-pane-tabs (java.awt.Dimension/new 100 120))
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
                (assoc s :url nil)))
              (send (:state curr-tab) (fn [s]
                (println (format "Setting URL of tab %d to %s (was %s)"
                  (:id curr-tab)
                  url-text
                  (:url s)))
                (tab-state/new url-text nil nil)))))))
        (.add panel-url button-go)
        (.add root-frame panel-url)
        (.add root-frame (JLabel/new "Content:"))
        (.setLayout text-area-content
          (BorderLayout/new))
        (.add root-frame scroll-pane-content)
        (.add root-frame label-status)
        (make-tabs-buttons @tabs)
        (.pack root-frame)
        (.setVisible root-frame true)

        (async/>!! root-frame-chan root-frame))))
      root-frame (async/<!! root-frame-chan)]
    (browser-ui/new root-frame tabs)))
