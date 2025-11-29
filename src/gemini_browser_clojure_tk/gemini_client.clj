(ns gemini-browser-clojure-tk.gemini-client
  (:gen-class))

(defn gemini-request [url] (let
  [
    uri-obj (java.net.URI/new url)
    url-host (.getHost uri-obj)
    url-port (let [port (.getPort uri-obj)]
      (if (= port -1)
        1965
        port))
    _ (println (format "Requesting %s (host %s at port %d))" url url-host url-port))

    ; key-mgr-factory (javax.net.ssl.KeyManagerFactory/getInstance (.getDefaultAlgorithm javax.net.ssl.KeyManagerFactory/getInstance))
    ; _ (.init key-mgr-factory key-store key-store-password
    trust-mgr (proxy [javax.net.ssl.TrustManager javax.net.ssl.X509TrustManager] []
      (checkClientTrusted [chain auth-type]
        (println (format "Certificate chain:\n%s" (reduce
          (fn [a b] (format "%s\n%s" a b))
          (map (fn [cert] (format "principal: %s, %s"
            (.getSubjectX500Principal cert)
            (if (.checkValidity cert) "valid" "invalid"))) chain)))))
      (checkServerTrusted [chain auth-type]
        (println (format "Certificate chain:\n%s" (reduce
          (fn [a b] (format "%s\n%s" a b))
          (map (fn [cert] (format "principal: %s, %s"
            (.getSubjectX500Principal cert)
            (if (.checkValidity cert) "valid" "invalid"))) chain)))))
      (getAcceptedIssuers [] (into-array java.security.cert.X509Certificate [])))
    ssl-ctx (javax.net.ssl.SSLContext/getInstance "TLS")
    _ (.init ssl-ctx nil (into-array javax.net.ssl.TrustManager [trust-mgr]) nil)

    socket-factory (.getSocketFactory ssl-ctx)
    socket (.createSocket socket-factory url-host url-port)
    _ (.startHandshake socket)

    out-stream (.getOutputStream socket)
    _ (.write out-stream (into-array Byte/TYPE (format "%s\r\n" url)))
    _ (.flush out-stream)

    in-stream (.getInputStream socket)
    in-data (.readAllBytes in-stream)

    [resp-header resp-body] (split-with (partial not= (int \return)) (map int in-data))
    resp-header-str (java.lang.String/new
      (into-array Byte/TYPE resp-header) java.nio.charset.StandardCharsets/UTF_8)
    resp-body-str (java.lang.String/new
      (into-array Byte/TYPE resp-body) java.nio.charset.StandardCharsets/UTF_8)]
  ; (println (format "Got response: %s\n%s" resp-header-str resp-body-str))
  {:header resp-header-str :body resp-body}))

(defn parse-response-header [header]
  {
    :code (Integer/parseInt (subs header 0 2))
    :detail (if (> (count header) 3)
      (subs header 3))})

(defn tab-state-client-watch []
  (fn [_ state-agent old-state new-state]
    (when (and (not= (:url old-state) (:url new-state)) (not (nil? (:url new-state))))
      (println "Fetching page...")
      (let
        [
          {resp-header :header resp-body :body} (gemini-request (:url new-state))
          {status-code :code status-detail :detail} (parse-response-header resp-header)]
        (println (format "Code: %d, Detail: %s" status-code status-detail))
        (send state-agent (fn [s]
          (cond
            (<= 10 status-code 19) {
              :url (:url new-state)
              :content-type "text/plain"
              :content (format "Input requested: %s" status-detail)}
            (<= 20 status-code 29) {
              :url (:url new-state)
              :content-type status-detail
              :content resp-body}
            :else {
              :url (:url new-state)
              :content-type "text/plain"
              :content (format "Code: %d, Detail: %s" status-code status-detail)})))))))
