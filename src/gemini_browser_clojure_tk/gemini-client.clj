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
    socket-factory (javax.net.ssl.SSLSocketFactory/getDefault)
    socket (.createSocket socket-factory url-host url-port)
    _ (.startHandshake socket)
    out-stream (.getOutputStream socket)
    in-stream (.getInputStream socket)
    in-buf-stream (java.util.stream.Stream/new in-stream)
    _ (.print out-stream (format "%s\r\n" url-host))
    _ (.flush out-stream)
    lines (.lines in-buf-stream)
    resp-header (first lines)
    resp-body (rest lines)]
  (println (format "Got response: %s\n%s" resp-header resp-body))))

(defn tab-state-client-watch []
  (fn [watch-key _ _ state-new]))
