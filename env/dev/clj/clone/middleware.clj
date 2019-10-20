(ns clone.middleware
  (:require
    [ring.logger]
    [taoensso.timbre :as timbre]
    [ring.middleware.json :refer [wrap-json-body
                                  wrap-json-response]]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.params :refer [wrap-params]]
    [prone.middleware :refer [wrap-exceptions]]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.middleware.defaults :refer [api-defaults wrap-defaults]]))

(defn- log-fn
  [{:keys [level throwable message]}]
  (if throwable
    (timbre/log level throwable message)
    (timbre/log level message)))

(defn wrap-logging
  "Ring middleware that logs each request and response."
  [handler]
  (ring.logger/wrap-with-logger handler {:log-fn log-fn}))


(def middleware
  [#(wrap-defaults % api-defaults)
   wrap-exceptions
   wrap-reload
   wrap-logging
   wrap-json-response
   wrap-json-body])
