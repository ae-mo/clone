(ns clone.components.header
  (:require [clone.components.login :refer [logout-component]]))

(defn header []
  (fn [logged-in? global-loading?]
    [:div#header-nav-menus-wrapper
     [:div.ksf-main-nav
      [:div.container
       [:div#logo_home
        [:h2 "Clone"]]
       [:div#nav-container
        [:ul.main-menu]
        [:div.ksf-main-nav-right
         (if logged-in?
           [logout-component global-loading?]
           [:div])]]]]]))
