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
          (map (fn [cert] (format "principal: %s, %s" (.getSubjectX500Principal cert) (if (.checkValidity cert) "valid" "invalid"))) chain)))))
      (checkServerTrusted [chain auth-type]
        (println (format "Certificate chain:\n%s" (reduce
          (fn [a b] (format "%s\n%s" a b))
          (map (fn [cert] (format "principal: %s, %s" (.getSubjectX500Principal cert) (if (.checkValidity cert) "valid" "invalid"))) chain)))))
      (getAcceptedIssuers [] (into-array java.security.cert.X509Certificate [])))
    ssl-ctx (javax.net.ssl.SSLContext/getInstance "TLS")
    _ (.init ssl-ctx nil (into-array javax.net.ssl.TrustManager [trust-mgr]) nil)

    socket-factory (.getSocketFactory ssl-ctx)
    socket (.createSocket socket-factory url-host url-port)
    _ (.startHandshake socket)

    out-stream (.getOutputStream socket)
    in-stream (.getInputStream socket)
    in-data (.readAllBytes in-stream)

    [resp-header resp-body] (split-with (partial not= \return) in-data)]
  (println (format "Got response: %s\n%s" (doall resp-header) (doall resp-body)))))

(defn tab-state-client-watch []
  (fn [watch-key _ _ state-new]))
