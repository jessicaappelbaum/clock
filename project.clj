(defproject image "0.1.0-SNAPSHOT"
  :description "a frontend for think.image"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [http-kit "2.2.0"]
                 [hiccup "1.0.5"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]]
  :main image.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})