(ns clone.components.footer)

(defn footer []
  (fn []
    [:footer.ksf-footer
     [:div.container
      [:div.logo-wrap
       [:h3 "Clone"]]
      [:div.textwidget.custom-html-widget
       [:div.col-lg-5.col-md-6.col-sm-6.show-hidden-centered
        [:p
         [:b "Address:"]
         [:br]
         "Some street 1"
         [:br]
         "00000 Somecity"]
        [:p
         [:span.label "Phone: "]
         " "
         [:span.data [:a {:href "/"} "01 234 56"]]]
        [:p
         [:span.label "Email: "]
         " "
         [:span.data "notworking@clone.com"]]]
       [:div.col-lg-5.col-md-7.col-sm-8.show-hidden-centered
        [:p
         [:span.label "CEO: "]
         " "
         [:span.data "Andrea Morciano"]]
        [:p
         [:span.label "CFO: "]
         [:span.data "Andrea Morciano"]]
        [:p
         [:span.label "CIO: "]
         [:span.data "Andrea Morciano"]]]
       [:div.col-lg-5.col-md-7.col-sm-6.show-hidden-centered
        [:p.visible-compact
         "Some text telling what our company does."]]]]]))
