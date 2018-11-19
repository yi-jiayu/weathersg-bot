(ns weathersg-bot.core
  (:require [clojure.java.io :as io]
            [ring.middleware.json :refer [wrap-json-body]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [redirect]]
            [ring.util.json-response :refer [json-response]]
            [org.httpkit.server :refer [run-server]]
            [weathersg-bot.rain-areas :refer [get-latest-rain-areas]])
  (:gen-class))

(def hostname (System/getenv "HOSTNAME"))
(def bot-token (System/getenv "TELEGRAM_BOT_TOKEN"))
(def rain-areas-dir "data")

(defn get-latest-rain-areas-url
  []
  (let [basename (.getName (io/file (get-latest-rain-areas rain-areas-dir)))]
    (str hostname "rain-areas/" basename)))

(defroutes app
  (GET "/" _ (redirect (get-latest-rain-areas-url)))
  (POST (str "/" bot-token) req (let [chat-id (get-in req [:body :message :from :id])
                                      rain-areas-url (get-latest-rain-areas-url)]
                                  (json-response {:method  "sendPhoto"
                                                  :chat_id chat-id
                                                  :photo   rain-areas-url})))
  (route/files "/rain-areas" {:root rain-areas-dir}))

(defn -main [& args]
  (run-server (wrap-json-body app {:keywords? true})
              {:port (Integer/valueOf ^String (or (System/getenv "PORT") "3000"))}))
