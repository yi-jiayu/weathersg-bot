(ns weathersg-bot.core
  (:require [ring.middleware.json :refer [wrap-json-body]]
            [compojure.core :refer :all]
            [ring.util.response :refer [response]]
            [ring.util.json-response :refer [json-response]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(def bot-token (System/getenv "TELEGRAM_BOT_TOKEN"))

(defroutes app
  (POST "/" req (do (println (:body req))
                    {:status  200
                     :headers {"Content-Type" "text/plain"}
                     :body    "Hello, World"}))
  (POST (str "/" bot-token) req (let [chat-id (get-in req [:body :message :from :id])]
                                  (json-response {:method  "sendMessage"
                                                  :chat_id chat-id
                                                  :text    "Haha"}))))

(defn -main [& args]
  (run-server (wrap-json-body app {:keywords? true})
              {:port (Integer/valueOf ^String (or (System/getenv "PORT") "3000"))}))
