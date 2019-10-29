(ns clone.handler
  (:require
   [reitit.ring :as reitit-ring]
   [clone.middleware :refer [middleware]]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]))

(def mount-target
  [:div#app
   [:h2 "Clone"]
   [:p "Loading..."]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/critical.css" "/css/critical.min.css"))
   (include-css (if (env :dev) "/css/main.css" "/css/main.min.css"))
   (include-css "/css/clone.css")
   (include-css "/css/basscss-cp.min.css")])

(defn loading-page []
  (html5
   (head)
   [:body {:class "hbl body-container"}
    mount-target
    (include-js "/js/app.js")]))


(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(def spa-routes
  [["/" {:get {:handler index-handler}}]])


(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    spa-routes)

   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))
