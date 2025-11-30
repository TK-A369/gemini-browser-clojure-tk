(ns gemini-browser-clojure-tk.gemtext
  (:gen-class)
  (:require
    clojure.string))

(defn safe-subs [s start end]
  (subs s (min (count s) start) (min (count s) end)))

(defn parse-gemtext [content]
  (let [lines (clojure.string/split content #"\r?\n")]
    (loop [l (first lines) lines-rest (rest lines) is-preformatted false result [] pref-buf ""]
      (cond
        (nil? l)
          result
        (= (safe-subs l 0 3) "```")
          (if is-preformatted
            (recur (first lines-rest) (rest lines-rest) (not is-preformatted)
              (conj result {:type :preformatted :content pref-buf}) "")
            (recur (first lines-rest) (rest lines-rest) (not is-preformatted) result ""))
        is-preformatted
          (recur (first lines-rest) (rest lines-rest) true result (str pref-buf l))
        (= (first l) \#)
          (recur (first lines-rest) (rest lines-rest) false
            (conj result {
              :type :heading
              :content l
              :level (cond
                (= (safe-subs l 0 3) "###") 3
                (= (safe-subs l 0 2) "##") 2
                :else 1)}) "")
        (= (safe-subs l 0 2) "=>")
          (let
            [[_ url text] (re-matches #"=>\s*([a-zA-Z0-9:/\-\.\?#=]+)\s*(.*)$" l)]
            (recur (first lines-rest) (rest lines-rest) false
              (conj result {
                :type :link
                :content (if (> (count text) 0)
                  text
                  url)
                :url url}) ""))
        :else
          (recur (first lines-rest) (rest lines-rest) false
            (conj result {:type :text :content l}) "")))))
