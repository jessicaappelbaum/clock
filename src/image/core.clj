(ns image.core
  (:require [clojure.pprint :as p]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.time :refer [format-date]])
  (:use ring.middleware.reload
        ring.adapter.jetty))


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

(defn millis->hours
  [time]
  (let [decimal-hours (/ time 3600000.0)]
    (str (int decimal-hours) " hrs and "
         (Math/round (* 60 (- decimal-hours
                              (int decimal-hours)))) " mins")))

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
                     (for [n (range (count (:clock-ins user-map)))]
                       (let [all-but-n-clock-ins (drop-last n (:clock-ins user-map))
                             all-but-n-clock-outs (drop-last n (:clock-outs user-map))]
                         [:tr
                          (if (not-empty all-but-n-clock-ins)
                            [:td (millis->date (last all-but-n-clock-ins))]
                            [:td " "])
                          (if (not-empty all-but-n-clock-outs)
                            [:td (millis->date (last all-but-n-clock-outs))]
                            [:td " "])
                          (if (and (not-empty all-but-n-clock-ins)
                                   (not-empty all-but-n-clock-outs))
                            [:td (millis->hours
                                   (- (last all-but-n-clock-ins)
                                      (last all-but-n-clock-outs)))]
                            [:td "still clocked in..."])])))]
              [:a {:href "/"} "back to login"]))))

  (compojure.route/not-found "Page not found"))

(def handler
  (-> image
    wrap-params
    wrap-time-in-request))

(defn -main
  []
  (let [port 3000]
    (println "starting server on port" port)
    (run-server handler {:port port})))
