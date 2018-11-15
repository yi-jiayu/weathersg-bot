(ns weathersg-bot.core
  (:require [ring.middleware.json :refer [wrap-json-body]]
            [compojure.core :refer :all]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defroutes app
           (POST "/" req (do (println (:body req))
                             {:status  200
                              :headers {"Content-Type" "text/plain"}
                              :body    "Hello, World"})))

(defn -main [& args]
  (run-server (wrap-json-body app)
              {:port (Integer/valueOf ^String (or (System/getenv "PORT") "3000"))}))
