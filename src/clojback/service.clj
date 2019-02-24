(ns clojback.service
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.request :as req]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [clojure.string :as str]
            [clojback.barleydb :as barleydb]
            [clojback.barleydb.custom-queries :as custom-queries]))


(defn variable-map
  "Reads the `variables` query parameter, which contains a JSON string
  for any and all GraphQL variables to be associated with this request.
  Returns a map of the variables (using keyword keys)."
  [request]
  (let [vars (get-in request [:query-params :variables])]
    (if-not (str/blank? vars)
      (json/read-str vars :key-fn keyword)
      {})))

(defn extract-query
  [request]
  (case (:request-method request)
    :get (get-in request [:query-params :query])
    :post (slurp (:body request))
    :else ""))

(defn graphql-schema[] (barleydb/get-graphql-schema "scott.data" (custom-queries/build)))
(defn new-context[graphql-schema] (.newContext graphql-schema))
(defn execute [context query] (.execute context query))


(defn graph-sdl
  [request]
  (-> (graphql-schema)
      (.getSdlString)
      (ring-resp/response)))

(defn graph-query
  [request]
  (println (keys request))
      (let [vars (variable-map request)
            query (extract-query request)]
    (->  (graphql-schema)
         (new-context)
         (execute query)
         (json/write-str)
         (ring-resp/response))))

;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{
  ["/graph/schema" :get (conj common-interceptors `graph-sdl)]
  ["/graph/exec"   :get (conj common-interceptors `graph-query) :route-name :gql-exec]
  ["/graph/exec"   :post (conj common-interceptors `graph-query)  :route-name :gql-post]})


;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by clojback.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})

