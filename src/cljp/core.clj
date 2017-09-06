(ns cljp.core
  (:require [clojure.data.csv  :as csv]
            [clojure.data.json :as json]
            [clojure.java.io   :as io]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def ^:private cli-opts
  [["-c" "--csv" "Parse input as CSV (default JSON)"]
   ["-t" "--tsv" "Parse input as TSV (default JSON)"]])

(defn string->reader
  [string]
  (io/reader (char-array (seq string))))

(defmulti json-obj-seq class)

; Lazy!
(defmethod json-obj-seq java.io.Reader
  [reader]
  (take-while
    #(not (nil? %))
    (repeatedly #(json/read reader :eof-error? false))))

; This is not lazy!
; Since it has to open it's own reader, it must also close it.
(defmethod json-obj-seq java.lang.String
  [string]
  (with-open [reader (string->reader string)]
    (doall (json-obj-seq (string->reader string)))))


(defn -main
  [& args]
  (let [opts (cli/parse-opts args cli-opts)
        {:keys [options arguments errors summary]} opts]
    (with-open [reader (io/reader *in*)]
      (let [json-objs (json-obj-seq reader)]
        (doall (map println json-objs))
        ))))

