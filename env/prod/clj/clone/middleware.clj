(ns clone.middleware
  (:require
    [ring.middleware.json :refer [wrap-json-body
                                  wrap-json-response]]
    [ring.middleware.defaults :refer [secure-api-defaults wrap-defaults]]))

(def middleware
  [#(wrap-defaults % secure-api-defaults)
   wrap-json-response
   wrap-json-body])
