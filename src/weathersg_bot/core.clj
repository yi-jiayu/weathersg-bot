(ns weathersg-bot.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-body]])
  (:gen-class))

(defn handler [request]
  (println (:body request))
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello, World"})

(defn -main [& args]
  (run-jetty (wrap-json-body handler)
             {:port (Integer/valueOf ^String (or (System/getenv "PORT") "3000"))}))
