(defproject image "0.1.0-SNAPSHOT"
  :description "a frontend for think.image"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [http-kit "2.2.0"]
                 [hiccup "1.0.5"]
                 [ring/ring "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler image.core/app}
  :main image.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :source-paths ["src" "env"]
  :repl-options {:init-ns user})
