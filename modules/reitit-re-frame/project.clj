(defproject metosin/reitit-re-frame "0.1.0-SNAPSHOT"
  :description "Reitit: Re-frame integration"
  :url "https://github.com/metosin/reitit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-parent "0.3.2"]]
  :parent-project {:path "../../project.clj"
                   :inherit [:deploy-repositories :managed-dependencies]}
  :dependencies [[metosin/reitit-core]
                 [metosin/reitit-frontend]
                 [re-frame]])
