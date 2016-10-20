(ns image.core
  (:require [clojure.pprint :as p]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.time :refer [format-date]]))

;TODO defn model fnctns create-user check-if-user-is-checked-in user-clock-in-time

(def user-db* (atom {}))

(defn create-user
  [name]
  (swap! user-db* #(assoc % name {:check-ins []
                                  :check-outs []}))
  (p/pprint @user-db*))

(defn checked-in?
  [name]
  (if (> (count (:check-ins (get @user-db* name)))
         (count (:check-outs (get @user-db* name))))
    (swap! user-db* #(assoc % :checked-in? true))
    (swap! user-db* #(assoc % :checked-in? false)))
  (p/pprint @user-db*))

(defn wrap-time-in-request [handler]
  (fn [request]
    (handler (assoc request :current-time (System/currentTimeMillis)))))

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
        (let [user (get (:params request) "user")]
          (create-user user)
          (html [:div
                 [:span (str "welcome to clock-in-n-out " user "!")]
                 [:form {:method "post"
                         :action "/home"}
                  [:input {:type "submit"
                           :value "clock in"}]]])))

  (POST "/home" request
        (let [user (get (:params request) "user")]
          (checked-in? user)
          (html [:div
                 [:div (str "time in: ")]])))

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
