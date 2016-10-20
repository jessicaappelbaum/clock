(ns image.core
  (:require [clojure.pprint :as p]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.time :refer [format-date]]))

;TODO defn model fnctns create-user check-if-user-is-checked-in user-clock-in-time

(def example-db
  {"darwin" {:check-ins []
             :check-outs []}
   "turtle" {:check-ins []
             :check-outs []}})

(def user-db* (atom {}))

(defn create-user
  [name]
  (swap! user-db* #(assoc % name {:check-ins []
                                  :check-outs []})))

(defn wrap-time-in-request [handler]
  (fn [request]
    (handler (assoc request :current-time (System/currentTimeMillis)))))

(def user-time-in* (atom {:time-in []
                          :params {}}))

(defroutes image
  (GET "/" []
       (html
         [:div "main page"]
         [:a {:href "/login"} "go to login"]))

  (GET "/howdy/:name" [name] (str "Howdy, " name "!"))

  (GET "/login" []
       (html [:form {:method "post"
                     :action "/login"}
              "username: "
              [:div [:input {:type "text"
                             :name "user"}]]
              [:div [:input {:type "submit"}]]]))

  (POST "/login" request
        (swap! user-time-in* assoc :params (get (:params request) "user"))
        (html [:div
               [:span (str "welcome to clock-in-n-out " (get (:params request) "user") "!")]
               [:form {:method "post"
                       :action "/home"}
                [:input {:type "submit"
                         :value "clock in"}]]]))

  (POST "/home" request
        (swap! user-time-in* assoc :time-in (System/currentTimeMillis))
        (html [:div
               [:div (str (read-string (:params @user-time-in*)) "'s timesheet")]
               [:div (str "time in: " (pr-str (:time-in @user-time-in*)))]]))

  (compojure.route/not-found "Page not found"))

(defn -main []
  (let [port 5000
        handler (-> image
                    wrap-params
                    wrap-time-in-request)]
    (println "starting server on port" port)
    (run-server handler {:port port})))


#_(p/pprint (format-date (java.util.Date.)))
#_(read-string (pr-str (format-date (java.util.Date.))))
