(ns leiningen.sass
  (:use leiningen.lein-haml-sass.render-engine
        leiningen.lein-haml-sass.options
        leiningen.lein-common.lein-utils
        leiningen.lein-common.file-utils)
  (:require [clojure.java.io   :as io]
            [leiningen.help    :as lhelp]
            [leiningen.clean   :as lclean]
            [leiningen.compile :as lcompile]
            [robert.hooke      :as hooke]))

(defn sass-files-from
  [{:keys [src output-directory output-extension auto-compile-delay]}]
  (dest-files-from "sass" src output-directory output-extension))
(defn compile-sass
  [{:keys [auto-compile-delay] :as options} watch?]
  (render-all-sass! (sass-files-from options) auto-compile-delay watch?))


(defn- once
  "Compiles the sass files once"
  [options]
  (println (str "Compiling sass files in " (:src options)))
  (compile-sass options false))

(defn- auto
  "Automatically recompiles when files are "
  [options]
  (println (str "Ready to compile sass located in " (:src options)))
  (compile-sass options true))

(defn- clean
  "Removes the autogenerated files"
  [{:keys [output-directory delete-output-dir] :as options}]
  (println "Deleting files generated by lein-sass.")
  (clean-all! (sass-files-from options) output-directory delete-output-dir))

;; Leiningen task
(defn sass
  "Compiles sass files."
  {:help-arglists '([once auto clean])
   :subtasks [#'once #'auto #'clean]}
  ([project]
     (exit-failure (lhelp/help-for "sass")))

  ([project subtask & args]
     (let [options (extract-options project)]
       (case subtask
         "once"  (once options)
         "auto"  (auto options)
         "clean" (clean options)
         (task-not-found subtask)))))


;; Hooks stuffs
(defmacro hook [task subtask args]
  `(let [options# (extract-options :sass (first ~args))]
     (apply ~task ~args)
     (when-not (~subtask (:ignore-hooks options#))
       (~(symbol (name subtask)) options#))))

(defn- compile-hook [task & args]
  (hook task :once args))

(defn- clean-hook [task & args]
  (hook task :clean args))

(hooke/add-hook #'lcompile/compile #'compile-hook)
(hooke/add-hook #'lclean/clean #'clean-hook)