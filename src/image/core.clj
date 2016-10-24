(ns image.core
  (:require [clojure.pprint :as p]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.time :refer [format-date]]))


(def user-db* (atom {}))

;;Middleware

(defn wrap-time-in-request [handler]
  (fn [request]
    (handler (assoc request :current-time (System/currentTimeMillis)))))

;;Model Functions

(defn create-user
  [name]
  (if (empty? (get @user-db* name))
    (swap! user-db* #(assoc % name {:clock-ins []
                                    :clock-outs []}))
    user-db*))

(defn clocked-in?
  [name]
  (> (count (:clock-ins (get @user-db* name)))
     (count (:clock-outs (get @user-db* name)))))

(defn clock-in
  [name]
  (swap! user-db* (fn [state] (let [new-clock-ins (conj (:clock-ins (get state name)) (System/currentTimeMillis))]
                                (assoc-in state [name :clock-ins] new-clock-ins)))))

(defn clock-out
  [name]
  (swap! user-db* (fn [state] (let [new-clock-outs (conj (:clock-outs (get state name)) (System/currentTimeMillis))]
                                (assoc-in state [name :clock-outs] new-clock-outs)))))

(defn millis->date
  [time]
  (format-date (java.util.Date. time)))

;;Routes

(defroutes image
  (GET "/" []
       (html
         [:h1 "login"]
         [:form {:method "post"
                 :action "/"}
          "username: "
          [:div [:input {:type "text"
                         :name "user"}]]
          [:div [:input {:type "submit"}]]]))

  (POST "/" request
        (let [user (get (:params request) "user")]
          (create-user user)
          (clocked-in? user)
          (html
            [:h1 "clock-in-n-out"]
            [:span (str "welcome to clock-in-n-out " user "!")]
            [:form {:method "post"
                    :action "/home"}
             [:input {:type "hidden"
                      :name "user"
                      :value user}]
             (if (clocked-in? user)
               [:input {:type "submit"
                        :value "clock out"}]
               [:input {:type "submit"
                        :value "clock in"}])])))

  (POST "/home" request
        (let [user (get (:params request) "user")]
          (if (clocked-in? user)
            (clock-out user)
            (clock-in user))
          (let [user-map (get @user-db* user)]
            (html
              [:h1 "timesheet"]
              [:table
               [:thead
                [:tr [:th "time-in"] [:th "time-out"] [:th "hours"]]]
               (into [:tbody]
                     (for [n (range 20)]
                       [:tr
                        (when (not-empty (drop-last n (:clock-ins user-map)))
                          [:td (millis->date (last (drop-last n (:clock-ins user-map))))])
                        (when (not-empty (drop-last n (:clock-outs user-map)))
                          [:td (millis->date (last (drop-last n (:clock-outs user-map))))])]))]))))

  (compojure.route/not-found "Page not found"))

(defn -main []
  (let [port 5000
        handler (-> image
                  wrap-params
                  wrap-time-in-request)]
    (println "starting server on port" port)
    (run-server handler {:port port})))
