(ns image.core
  (:require [clojure.pprint :as p]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.time :refer [format-date]]))
#_(defn wrap-time [handler]
  (fn [request]
    (if-let [user-id (-> request :session :user-id)]
      (let [user (get-user-by-id user-id)]
        (handler (assoc request :user user)))
      (handler request))))

(defn first-name
  [request]
  (-> request
    :params
    first
    val
    read-string))

(defroutes image
  (GET "/" []
       (html
         [:div "main page"]
         [:a {:href "/login"} "go to login"]))

  #_(GET "/howdy/:name" [name] (str "Howdy, " name "!"))

  (GET "/login" []
       (html [:form
              {:method "post"
               :action "/login"}
              "first name: "
              [:div
               [:input {:type "text"
                        :name "fname"}]]
              "last name: "
              [:div
               [:input {:type "text"
                        :name "lname"}]]
              [:div
               [:input {:type "submit"}]]]))

  (POST "/login" request
        (p/pprint (format-date (java.util.Date.)))
        (html [:div
               [:span (str "welcome to clock-in-n-out " (first-name request) "!")]
               [:form {:method "post"
                       :action "/home"}
                [:input {:type "submit"
                         :value "clock in"}]]]))

  (POST "/home" request
        (html [:div (str "time in: " (read-string (pr-str (format-date (java.util.Date.)))))]))

  (compojure.route/not-found "Page not found"))

(defn -main []
  (let [port 5000]
    (println "starting server on port" port)
    (run-server (wrap-params image) {:port port})))
