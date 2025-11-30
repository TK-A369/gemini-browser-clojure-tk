(ns gemini-browser-clojure-tk.gemtext
  (:gen-class)
  (:require
    clojure.string))

(defn parse-gemtext [content]
  (let [lines (clojure.string/split content #"\r?\n")]
    (loop [l (first lines) lines-rest (rest lines) is-preformatted false result [] pref-buf ""]
      (cond
        (nil? l)
          result
        (= (subs l 0 3) "```")
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
                (= (subs l 0 3) "###") 3
                (= (subs l 0 2) "##") 2
                :else 1)}) "")
        :else
          (recur (first lines-rest) (rest lines-rest) false
            (conj result {:type :text :content l}) "")))))
