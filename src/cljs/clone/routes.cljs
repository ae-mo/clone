(ns clone.routes
  (:require [reitit.frontend :as reitit]
            [clone.session :as session]
            [clone.components.login :refer [login-page]]
            [clone.components.profile :refer [profile-page]]))

(def router
  (reitit/router
   [["/" :index]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

(defn page-for [route]
  (case route
    :index #'profile-page))
