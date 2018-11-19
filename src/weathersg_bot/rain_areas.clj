(ns weathersg-bot.rain-areas
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]])
  (:import (java.time ZoneId ZonedDateTime)
           (java.time.format DateTimeFormatter)
           (java.nio.file Paths)
           (java.io FileNotFoundException File)
           (java.time.temporal ChronoUnit)))

(def ^ZoneId singapore-zone (ZoneId/of "Asia/Singapore"))
(def radar-timestamp-formatter (DateTimeFormatter/ofPattern "yyyyMMddHHmm0000"))
(def radar-overlay-prefix "https://www.weather.gov.sg/files/rainarea/50km/v2/")

(defn truncate-timestamp
  "Returns a timestamp rounded down to the closest multiple of 5 minutes."
  [ts]
  (let [ts (.truncatedTo ts ChronoUnit/MINUTES)
        minutes (.getMinute ts)
        rounded-minutes (* 5 (Math/floor (/ minutes 5)))
        delta-minutes (- minutes rounded-minutes)
        truncated-ts (.minusMinutes ts delta-minutes)]
    truncated-ts))

(defn get-formatted-timestamp
  [ts]
  (.format radar-timestamp-formatter ts))

(defn get-radar-overlay-name
  [formatted-ts]
  (str "dpsri_70km_" formatted-ts "dBR.dpsri.png"))

(defn get-radar-overlay-url
  [formatted-ts]
  (str radar-overlay-prefix (get-radar-overlay-name formatted-ts)))

(defn get-current-sg-time
  []
  (ZonedDateTime/now singapore-zone))

(defn download-radar-overlay
  "Downloads a radar overlay from `url` to `dst`."
  [url dst]
  (with-open [in (io/input-stream url)
              out (io/output-stream dst)]
    (io/copy in out)))

(defn throw-on-failure
  [sh-result]
  (let [{exit-code :exit
         stderr    :err} sh-result]
    (if (not (zero? exit-code))
      (throw (new RuntimeException stderr)))))

(defn generate-rain-areas
  [overlay-path output-path]
  (with-open [base-img (io/input-stream (io/resource "base.png"))]
    (throw-on-failure (sh "composite"
                          overlay-path
                          "png:-"
                          "-geometry" "853x479!"
                          "-blend" "50"
                          output-path
                          :in base-img))))

(defn add-timestamp
  [ts input-path output-path]
  (throw-on-failure (sh "convert"
                        input-path
                        "-fill" "white"
                        "-undercolor" "#00000080"
                        "-gravity" "NorthEast"
                        "-annotate" "+5+5"
                        (str "\\ " (.format ts DateTimeFormatter/RFC_1123_DATE_TIME) " ")
                        output-path)))

(defn get-rain-areas
  [formatted-ts output-path]
  (let [radar-overlay-url (get-radar-overlay-url formatted-ts)
        temp-file (File/createTempFile "radar" nil)
        overlay-path (.getPath temp-file)]
    (download-radar-overlay radar-overlay-url temp-file)
    (generate-rain-areas overlay-path output-path)
    (.delete temp-file)))

(defn get-latest-rain-areas
  [rain-areas-dir]
  (loop [ts (truncate-timestamp (get-current-sg-time))]
    (let [formatted-ts (get-formatted-timestamp ts)
          output-path (str (Paths/get rain-areas-dir (into-array [(str formatted-ts ".png")])))]
      (if (.exists (io/file output-path))
        output-path
        (let [success? (try
                         (do (get-rain-areas formatted-ts output-path)
                             (add-timestamp ts output-path output-path)
                             true)
                         (catch FileNotFoundException _
                           false))]
          (if success?
            output-path
            (recur (.minusMinutes ts 5))))))))
