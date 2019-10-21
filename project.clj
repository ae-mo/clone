(defproject clone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://clone-persona.herokuapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [environ "1.1.0"]
                 [ring-server "0.5.0"]
                 [reagent "0.8.1"]
                 [reagent-utils "0.3.3"]
                 [ring "1.7.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-logger "1.0.1"]
                 [ring/ring-defaults "0.3.2"]
                 [clj-http "3.6.0"]
                 [cljs-http "0.1.46"]
                 [cheshire "5.5.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [hiccup "1.0.5"]
                 [yogthos/config "1.1.3"]
                 [org.clojure/clojurescript "1.10.520"
                  :scope "provided"]
                 [com.google.javascript/closure-compiler-unshaded "v20190325"]
                 [org.clojure/google-closure-library "0.0-20190213-2033d5d9"]
                 [metosin/reitit "0.3.7"]
                 [pez/clerk "1.0.0"]
                 [venantius/accountant "0.2.4"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-environ "1.1.0"]
            [lein-shadow "0.1.6"]
            [lein-cljsbuild "1.1.7"]
            [lein-asset-minifier "0.4.6"
             :exclusions [org.clojure/clojure]]]

  :ring {:handler clone.handler/app
         :uberwar-name "clone.war"}

  :min-lein-version "2.5.0"
  :uberjar-name "clone.jar"
  :main clone.server
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources" "target/cljsbuild"]

  :minify-assets
  [[:css {:source "resources/public/css/main.css"
          :target "resources/public/css/main.min.css"}]
   [:css {:source "resources/public/css/critical.css"
           :target "resources/public/css/critical.min.css"}]]

  :cljsbuild
  {:builds {:min
            {:source-paths ["src/cljs" "src/cljc" "env/prod/cljs"]
             :compiler
             {:output-to        "target/cljsbuild/public/js/app.js"
              :output-dir       "target/cljsbuild/public/js"
              :source-map       "target/cljsbuild/public/js/app.js.map"
              :optimizations :advanced
              :infer-externs true
              :pretty-print  false}}
            :app
            {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
             :figwheel {:on-jsload "clone.core/mount-root"}
             :compiler
             {:main "clone.dev"
              :asset-path "/js/out"
              :output-to "target/cljsbuild/public/js/app.js"
              :output-dir "target/cljsbuild/public/js/out"
              :source-map true
              :optimizations :none
              :pretty-print  true}}}}






  :figwheel
  {:http-server-root "public"
   :server-port 3449
   :nrepl-port 7002
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl]

   :css-dirs ["resources/public/css"]
   :ring-handler clone.handler/app}

  :less {:source-paths ["src/less"]
         :target-path "resources/public/css"}

  :aliases {"prod" ["with-profile" "dev" "run" "-m" "shadow.cljs.devtools.cli" "--npm" "release" "app"]
            "npm" [""]}


  :profiles {:dev {:repl-options {:init-ns clone.repl}
                   :dependencies [[cider/piggieback "0.4.1"]
                                  [binaryage/devtools "0.9.10"]
                                  [ring/ring-mock "0.4.0"]
                                  [ring/ring-devel "1.7.1"]
                                  [prone "1.6.3"]
                                  [figwheel-sidecar "0.5.18"]
                                  [nrepl "0.6.0"]
                                  [thheller/shadow-cljs "2.8.37"]
                                  [pjstadig/humane-test-output "0.9.0"]

                                  ;; To silence warnings from less4clj dependecies about missing logger implementation
                                  [org.slf4j/slf4j-nop "1.7.25"]]


                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.18"]
                             [deraen/lein-less4j "0.6.2"]]


                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :env {:dev true}}

             :uberjar {:hooks [minify-assets.plugin/hooks]
                       :source-paths ["env/prod/clj"]
                       :prep-tasks ["compile" ["prod"]]
                       :env {:production true}
                       :aot :all
                       :omit-source true}})
