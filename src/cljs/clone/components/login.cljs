(ns clone.components.login
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [reagent.debug :refer [log]]
            [goog.string :as gstring]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clone.session :as session]
            [clone.config :refer [config]]
            [accountant.core :as accountant]))

(def api-url (:api-url config))
(def loading? (reagent/atom false))

(defn login-action
  [email password invalid-credentials? generic-error?]
  (reset! loading? true)
  (go (let [response (<! (http/post
                           (str api-url "/login")
                           {:json-params {:username email
                                          :password password}}))]
        (reset! loading? false)
        (let [status (:status response)]
          (cond
            (= 200 status) (do
                             (let [{{uuid :uuid
                                     token :token}
                                    :body}
                                   response]
                               (session/set-uuid! uuid)
                               (session/set-token! token))
                             (reset! invalid-credentials? false)
                             (reset! generic-error? false)
                             (reset! session/logged-in? true))

            (= 403 status) (reset! invalid-credentials? true)
            :else (reset! generic-error? true))))))

(defn clear-session []
  (session/remove-uuid!)
  (session/remove-token!)
  (reset! session/logged-in? false)
  (accountant/navigate! "/"))


(defn logout-action
  [l?]
  (reset! l? true)
  (go (let [response (<! (http/delete
                           (str api-url "/login/" (session/get-uuid))
                           {:headers {"Authorization" (str "OAuth " (session/get-token))}}))]
        (reset! l? false)
        (let [status (:status response)]
          (if (or (= 200 status) (and (>= status 400) (< status 500)))
            (clear-session))))))

(defn logout-component []
  (fn [l?]
    [:ksf-user-login
     [:ul.user.logged-in
      [:li.user-login
       [:a {:target "_blank", :href "/"}
        [:svg.user-icon
         {:viewBox "0 0 17.5 15",
          :height "15",
          :width "17.5",
          :preserveAspectRatio "xMidYMid"}
         [:path.cls-1
          {:d
           "M15.298,11.312 C14.231,10.837 12.683,9.638 10.361,9.246 C10.947,8.604 11.407,7.632 11.867,6.476 C12.118,5.795 12.077,5.214 12.077,4.409 C12.077,3.788 12.202,2.837 12.035,2.297 C11.533,0.502 10.235,0.006 8.729,0.006 C7.202,0.006 5.905,0.502 5.403,2.297 C5.257,2.837 5.361,3.809 5.361,4.409 C5.361,5.214 5.319,5.795 5.591,6.476 C6.052,7.632 6.491,8.625 7.097,9.246 C4.796,9.658 3.227,10.858 2.182,11.312 C0.006,12.262 0.006,13.316 0.006,13.316 L0.006,15.001 L17.495,15.001 L17.495,13.316 C17.495,13.316 17.474,12.262 15.298,11.312 Z"}]]
        " " [:span "Andrea"]]]
      [:li.user-logout
       [:a {:href ""
            :on-click (fn [e]
                        (.preventDefault e)
                        (logout-action l?))}
        " LOG OUT"]]]]))

(defn invalid-credentials-message []
  (fn []
    [:div.login--error-msg.pb1
     "Wrong email or password."]))

(defn generic-error-message []
  (fn []
    [:div.login--error-msg.pb1
     "Something went wrong. Please try again."]))

(defn no-error-message []
  (fn []
    [:div.login--error-msg.pb1.clearfix
     {:dangerouslySetInnerHTML {:__html "&nbsp;"}}]))

(defn login-form [email-address password]
  (let [s (reagent/atom {:email email-address
                         :password password})
        invalid-credentials? (reagent/atom false)
        generic-error? (reagent/atom false)
        l? loading?]
    (fn []
      [:div.center
       [:div#login-form {:class (if @l?
                                  "login-form pt2 loading"
                                  "login-form pt2")}
        [:div.form-wrapper
         [:form.pb2 {:on-submit (fn [e]
                                   (.preventDefault e)
                                   (login-action
                                     (:email @s)
                                     (:password @s)
                                     invalid-credentials?
                                     generic-error?))}
          (cond
            @invalid-credentials? [invalid-credentials-message]
            @generic-error? [generic-error-message]
            :else [no-error-message])
          [:div.email-wrapper
           [:div.input-field
            [:input
             {:required true,
              :type :email
              :name "email",
              :placeholder "Email..."
              :value (:email @s)
              :on-change (fn [e]
                           (swap! s assoc :email (-> e .-target .-value)))}]]]
          [:div.password-wrapper-and-submit-wrapper
           [:div.input-field
            [:input
             {:required true,
              :type :password
              :name "password",
              :placeholder "Password..."
              :value (:password @s)
              :on-change (fn [e]
                           (swap! s assoc :password (-> e .-target .-value)))}]
            [:input.submit-button
             {:value "LOG IN"
              :type :submit}]]]]]]])))


(defn login-page []
  (fn [global-loading?]
    [:span.main
     [:div.mt4.mb4.clearfix
      [:div.col-10.lg-col-7.mx-auto
       [:div.clone--container.clearfix
        [:div
         [:div.center
          [:h1
           "Welcome to Clone"]]
         [:div.center.clearfix
          [:div
           [:p
            "Clone is a small application to manage your KSF Media user profile."]
           [:p
            "Please use the form below to login!"]]]
         [login-form]]]]]]))
