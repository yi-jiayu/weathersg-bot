(ns weathersg-bot.rain-areas-test
  (:require [clojure.test :refer :all])
  (:require [weathersg-bot.rain-areas :refer :all])
  (:import (java.time LocalDateTime)))

(def reference-date-time (LocalDateTime/of 2018 11 15 8 45))

(deftest truncate-timestamp-test
  (testing "should not truncate when minutes is already multiple of 5"
    (is (= reference-date-time (truncate-timestamp reference-date-time))))
  (testing "should truncate minutes to closest multiple of 5"
    (is (= reference-date-time (truncate-timestamp (.plusMinutes reference-date-time 3))))))

(deftest get-formatted-timestamp-test
  (is (= "2018111508450000" (get-formatted-timestamp reference-date-time))))

(deftest get-radar-overlay-name-test
  (is (= "dpsri_70km_2018111508450000dBR.dpsri.png"
         (get-radar-overlay-name (get-formatted-timestamp reference-date-time)))))

(deftest get-radar-overlay-url-test
  (is (= "https://www.weather.gov.sg/files/rainarea/50km/v2/dpsri_70km_2018111508450000dBR.dpsri.png"
         (get-radar-overlay-url (get-formatted-timestamp reference-date-time)))))

;; todo: test remaining functions with mocks