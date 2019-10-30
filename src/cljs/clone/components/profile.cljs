(ns clone.components.profile
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [clone.session :as session]
            [clone.config :refer [config]]
            [clone.components.login :refer [clear-session]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(def api-url (:api-url config))
(def loading? (reagent/atom false))

(defn generic-error-message []
  (fn []
    [:div.login--error-msg.pb1
     "Something went wrong. Please try again."]))


(defn address-form []
  (fn [s]
    [:div
      [:div
       [:input.input-field
        {:required ""
         :disabled @loading?
         :name "0-field"
         :placeholder "Address..."
         :type :text
         :value (get-in @s [:address :street-address])
         :on-change (fn [e]
                      (swap! s assoc-in [:address :street-address] (-> e .-target .-value)))}]]
      [:div
       [:input.input-field
        {:required ""
         :disabled @loading?
         :name "1-field"
         :placeholder "Postal code..."
         :type :text
         :value (get-in @s [:address :postcode])
         :on-change (fn [e]
                      (swap! s assoc-in [:address :postcode] (-> e .-target .-value)))}]]]))


(defn address-info []
  (fn [address postcode]
    [:div
     [:div.clearfix.grid--row address]
     [:div.clearfix.grid--row postcode]]))

(defn edit-button []
  (fn [edit?]
    [:div.flex {:on-click (fn [e]
                            (reset! edit? true))}
     [:div.edit-icon]
     [:span.editable--edit-text [:u "Edit"]]]))

(defn restore-old-address
  [s o]
  (swap! s assoc-in [:address :street-address] (:street-address @o))
  (swap! s assoc-in [:address :postcode] (:postcode @o)))

(defn set-old-address
  [s o]
  (swap! o assoc :street-address (get-in @s [:address :street-address]))
  (swap! o assoc :postcode  (get-in @s [:address :postcode])))

(defn send-buttons []
  (fn [s o edit?]
    [:div.editable--edit-container.flex
     [:div
      [:button.button-green {:disabled @loading?
                             :type "submit" } "Confirm"]
      " "]
     [:div.close-icon {:on-click (fn [e]
                                   (if (not @loading?)
                                     (do
                                       (restore-old-address s o)
                                       (reset! edit? false))))}]]))

(defn response-handler [response success-handler error?]
  (let [status (:status response)]
    (cond
      (= 200 status) (success-handler response)
      (= 403 status) (clear-session)
      (and (>= status 400) (< status 500)) (reset! error? true))))


(defn update-user-data [s o response edit?]
  (let [{{email :email
          customer-id :cusno
          last-name :lastName
          first-name :firstName
          {street-address :streetAddress postcode :zipCode country-code :countryCode} :address}
         :body} response]
    (swap! s assoc :email email)
    (swap! s assoc :customer-id customer-id)
    (swap! s assoc :last-name last-name)
    (swap! s assoc :first-name first-name)
    (swap! s assoc-in [:address :street-address] street-address)
    (swap! s assoc-in [:address :postcode] postcode)
    (swap! s assoc-in [:address :country-code] country-code)
    (reset! edit? false)
    (set-old-address s o)))


(defn fetch-data [s o l? e?]
  (reset! l? true)
  (go (let [response (<! (http/get
                           (str api-url "/users/" (session/get-uuid))
                           {:headers {"Authorization" (str "OAuth " (session/get-token))}}))]
        (let [success-handler
              (fn []
                (update-user-data s o response e?)
                (reset! l? false))]
          (response-handler response success-handler e?)))))



(defn update-address-action [s o edit? error?]
  (reset! loading? true)
  (go (let [response (<! (http/patch
                           (str api-url "/users/" (session/get-uuid))
                           {:headers {"Authorization" (str "OAuth " (session/get-token))}
                            :json-params {:address {:countryCode (get-in @s [:address :country-code])
                                                    :zipCode (get-in @s [:address :postcode])
                                                    :streetAddress (get-in @s [:address :street-address])}}}))]
        (let [success-handler
              (fn []
                (update-user-data s o response edit?)
                (reset! error? false))]
          (response-handler response success-handler error?)
          (reset! loading? false)))))


(defn profile-page []
  (fn [global-loading?]
    (let [user-data (reagent/atom {:first-name nil}
                                  :last-name nil
                                  :email nil
                                  :customer-id nil
                                  :address {:country-code nil
                                            :street-address nil
                                            :postcode nil})
          old-address (reagent/atom {:street-address nil
                                     :postcode nil})
          edit-address? (reagent/atom false)
          generic-error? (reagent/atom false)]
      (fetch-data user-data old-address global-loading? generic-error?)
      (fn []
        [:div.page-content.mt4.mb4.container.clearfix
          [:div.clone--container.clearfix
           [:div.col.col-12.md-col-6.lg-col-6.clone--profile
            [:div.clone--component-block-container
             [:span.clone--component-heading "USER PROFILE:"]
             [:div.clone--component-block-content
              [:dl
               [:dt "Name:"]
               [:dd
                [:div.editable
                 [:div.clearfix.grid--row
                  [:form
                   [:div.col.grid--column.col-8
                    [:div
                     [:div.clearfix.grid--row (:first-name @user-data)]
                     [:div.clearfix.grid--row (:last-name @user-data)]]]]]]]
               [:dt "Address:"]
               [:dd
                [:div.editable
                 [:div.clearfix.grid--row
                  [:form.editable--form {:on-submit (fn [e]
                                                      (.preventDefault e)
                                                      (update-address-action user-data old-address edit-address? generic-error?))}
                   [:div.col.grid--column.col-8
                    (if @generic-error?
                      [generic-error-message])
                    (if @edit-address?
                      [address-form user-data generic-error?]
                      [address-info (get-in @user-data [:address :street-address])
                                    (get-in @user-data [:address :postcode])])]
                   [:div.col.grid--column.col-4
                    (if @edit-address?
                      [send-buttons user-data old-address edit-address?]
                      [edit-button edit-address?])]]]]]
               [:dt "Email:"]
               [:dd
                [:div.clearfix.grid--row (:email @user-data)]]
               [:dt "Customer number:"]
               [:dd
                [:div.clearfix.grid--row (:customer-id @user-data)]]]]]]]]))))
