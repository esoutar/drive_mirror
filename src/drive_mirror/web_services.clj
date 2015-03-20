(ns drive-mirror.web-services
  (:require [compojure.core :refer :all]
            [ring.middleware.defaults :as ring-defaults]
            [ring.util.response :refer [redirect]]
            [drive-mirror.core :refer :all]
            [clj-oauth2.client :as oauth2]
            [taoensso.timbre :as log]
            [cheshire.core :refer [parse-string]]
            [clojure.string :as str]))

(comment "https://www.googleapis.com/oauth2/v1/userinfo")

(def access-t (atom []))

(defn next-page-token
  [response]
  (let [token (-> response :body parse-string (get "nextPageToken"))]
    (when (and token (not (empty? (str/trim token))))
      token)))

(defn parse-body
  [response]
  (-> response :body parse-string))

(defn list-files
  [access-token]
  (loop [file-response-page (oauth2/get "https://www.googleapis.com/drive/v2/files"
                                        {:oauth2 access-token})
         page-count 1
         file-list []]
    (let [concatenated-list (concat file-list  (-> file-response-page parse-body (get "items")))]
      (if-let [page-token (next-page-token file-response-page)]
        (recur (oauth2/get "https://www.googleapis.com/drive/v2/files"
                           {:oauth2 access-token
                            :query-params {:pageToken page-token}})
               (inc page-count)
               concatenated-list)
        concatenated-list))))

(defn about
  [access-token]
  (parse-body (oauth2/get "https://www.googleapis.com/drive/v2/about"
                          {:oauth2 access-token})))

(defn root-folder-id
  [access-token]
  (-> access-token about (get "rootFolderId")))

(defn authenticated-request
  [fn request]
  (fn (oauth2/get-access-token google-com-oauth2
                               (:params request)
                               auth-req)))
(defn get-file
  [access-token id]
  (parse-body (oauth2/get (str "https://www.googleapis.com/drive/v2/files/" id)
                          {:oauth2 access-token})))

(defn file-name
  [grouped-files file]
  (case (get file "kind")
    "drive#file" (get file "title")
    ;;"drive#childReference" (file-name grouped-files (-> file file-id grouped-files))
    "Not known"))

(defn file-id
  [file]
  (get file "id"))

(defn file-children
  [access-token file]
  (get (parse-body (oauth2/get (str "https://www.googleapis.com/drive/v2/files/" (file-id file) "/children")
                               {:oauth2 access-token}))
       "items"))

(defn walk-folder-structure
  [access-token grouped-files parent-list node & [node-visit-fn]]
  (let [node-visit-fn (or node-visit-fn
                          (fn [grouped-files parent-list node]
                            (str "/" (str/join "/" (map #(file-name grouped-files %) parent-list))
                                 "/" (file-name grouped-files node))))
        children (file-children access-token node)]
    (if (and children (< 0 (count children)))
      (conj (map #(walk-folder-structure access-token grouped-files (conj parent-list node) % node-visit-fn) children)
            (node-visit-fn grouped-files parent-list node))
      [(node-visit-fn grouped-files parent-list node)])))

(defn do-yet-more-stuff
  [access-token]
  (let [grouped-files (group-by file-id (list-files access-token))
        root-folder (->> access-token root-folder-id (get-file access-token))]
    (str "<p>Root folder is: " (file-name grouped-files root-folder) " with ID: " (file-id root-folder) " </p>"
         "<p>Grouped files look like key: " (first (keys grouped-files)) " value: " (first (vals grouped-files)) "</p>"
         "<p>First root child: " (-> access-token (file-children root-folder) first) "</p>"
         "<p>Folder walk produces: " (walk-folder-structure access-token
                                                            grouped-files
                                                            []
                                                            root-folder) "</p>")))

(defroutes ring-app
  (GET "/" request (redirect (:uri auth-req)))
  (ANY "/authentication/callback" request
       (authenticated-request do-yet-more-stuff request))
  (GET "/status" request
       (let [count (:count (:session request) 0)
             session (assoc (:session request) :count (inc count))]
         (-> (ring.util.response/response
              (str "<p>We've hit the session page " (:count session)
                   " times.</p><p>The current session: " session "</p>"))
             (assoc :session session)))))

(def app
  (delay (ring-defaults/wrap-defaults ring-app ring-defaults/site-defaults)))
