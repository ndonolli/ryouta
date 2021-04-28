(defproject imaginathansoft/ryouta "0.0.1"
  :description "A data-driven visual novel engine for clojurescript"
  :url "https://github.com/ndonolli/ryouta"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies
  [[org.clojure/clojurescript "1.10.520" :scope "provided"]
   [org.clojure/core.async "1.3.610"]
   [reagent "1.0.0"]
   [metosin/malli "0.4.0"]
   [borkdude/sci "0.2.4"]]

  :repositories {"clojars" {:url "https://clojars.org/repo"
                            :sign-releases false}}

  :source-paths
  ["src"])