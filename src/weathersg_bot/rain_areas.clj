(ns weathersg-bot.rain-areas
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]])
  (:import (java.time ZoneId ZonedDateTime)
           (java.time.format DateTimeFormatter)
           (java.nio.file Files Path LinkOption Paths)
           (java.nio.file.attribute FileAttribute)
           (java.io FileNotFoundException)))

(def ^ZoneId singapore-zone (ZoneId/of "Asia/Singapore"))
(def radar-timestamp-formatter (DateTimeFormatter/ofPattern "yyyyMMddHHmm0000"))
(def radar-overlay-prefix "https://www.weather.gov.sg/files/rainarea/50km/v2/")
(def data-dir (Paths/get "data" (make-array String 0)))
(def base-img (.resolve data-dir "base.png"))
(def overlays-dir (.resolve data-dir "overlays"))
(def ^Path rain-areas-dir (.resolve data-dir "rain-areas"))

(Files/createDirectories overlays-dir (make-array FileAttribute 0))
(Files/createDirectories rain-areas-dir (make-array FileAttribute 0))
(io/copy (io/input-stream (io/resource "base.png")) (.toFile base-img))

(defn truncate-timestamp
  "Returns a timestamp rounded down to the closest multiple of 5 minutes."
  [ts]
  (let [minutes (.getMinute ts)
        rounded-minutes (* 5 (Math/floor (/ minutes 5)))
        delta-minutes (- minutes rounded-minutes)
        truncated-ts (.minusMinutes ts delta-minutes)]
    truncated-ts))

(defn get-key
  [ts]
  (.format radar-timestamp-formatter (truncate-timestamp ts)))

(defn get-radar-overlay-name
  [key]
  (str "dpsri_70km_" key "dBR.dpsri.png"))

(defn get-radar-overlay-url
  [key]
  (str radar-overlay-prefix (get-radar-overlay-name key)))

(defn get-current-time
  []
  (ZonedDateTime/now singapore-zone))

(defn exists?
  [^Path path]
  (Files/exists path (make-array LinkOption 0)))

(defn download-radar-overlay
  ([url dest]
   (do (let [in (io/input-stream url)
             out (.toFile dest)]
         (io/copy in out))
       dest))
  ([^String key]
   (let [overlay-url (get-radar-overlay-url key)
         overlay-path (.resolve overlays-dir key)]
     (do (if (not (exists? overlay-path))
           (download-radar-overlay overlay-url overlay-path))
         (str overlay-path)))))

(defn get-rain-areas
  ([base overlay output]
   (do (sh "magick"
           "composite"
           overlay
           base
           "-geometry" "853x479!"
           "-blend" "50"
           output)
       output))
  ([^String key]
   (let [output-path (.resolve rain-areas-dir key)]
     (do (if (not (exists? output-path))
           (let [overlay-path (download-radar-overlay key)
                 output-path (str output-path)]
             (get-rain-areas (str base-img) overlay-path output-path)))
         (str output-path)))))

(defn get-latest-rain-areas
  []
  (loop [ts (get-current-time)]
    (let [rain-areas (try
                       (get-rain-areas (get-key ts))
                       (catch FileNotFoundException _
                         nil))]
      (if (nil? rain-areas)
        (recur (.minusMinutes ts 5))
        rain-areas))))
