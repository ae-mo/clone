(ns clone.session
  (:require
    [reagent.session :as session]
    [clone.storage :as storage]
    [reagent.core :as reagent]))

(defn get-route []
  (session/get :route))

(defn set-route! [current-page route-params]
  (session/put! :route {:current-page current-page
                        :route-params route-params}))

(defn get-uuid []
  (storage/get-item "uuid"))

(defn set-uuid! [uuid]
  (storage/set-item! "uuid" uuid))

(defn remove-uuid! []
  (storage/remove-item! "uuid"))

(defn get-token []
  (storage/get-item "token"))

(defn set-token! [token]
  (storage/set-item! "token" token))

(defn remove-token! []
  (storage/remove-item! "token"))

(def logged-in? (reagent/atom (not (nil? (get-token)))))
