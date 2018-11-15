(defproject weathersg-bot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-jetty-adapter "1.7.1"]
                 [ring/ring-json "0.4.0"]]
  :java-source-paths ["src/java"]
  :main ^:skip-aot weathersg-bot.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
