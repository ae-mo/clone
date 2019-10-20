(ns clone.core
  (:require
   [reagent.core :as reagent]
   [clone.session :as session]
   [clone.routes :as routes]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [clone.components.header :refer [header]]
   [clone.components.footer :refer [footer]]
   [clone.components.login :refer [login-page]]))

;; -------------------------
;; Page mounting component

(def loading? (reagent/atom false))

(defn current-page []
  (fn []
    (let [logged-in? @session/logged-in?
          page (if logged-in?
                 (:current-page (session/get-route))
                 login-page)]
      [:div
       [header logged-in? loading?]
       [:div {:class (if @loading?
                         "page-wrapper loading"
                         "page-wrapper")}
        [page loading?]]
       [footer]])))


;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path routes/router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/set-route! (routes/page-for current-page)
                           route-params)
        (clerk/navigate-page! path)))

    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path routes/router path)))})
  (accountant/dispatch-current!)
  (mount-root))
