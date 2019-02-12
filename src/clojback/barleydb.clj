(ns clojback.barleydb)

(import 'scott.barleydb.server.jdbc.persist.QuickHackSequenceGenerator)
(import 'scott.barleydb.api.query.QueryObject)
(import 'scott.barleydb.api.core.types.JavaType)

(def env-definition
    (-> (scott.barleydb.bootstrap.EnvironmentDef/build)
        (.withDataSource)
	        (.withDriver "org.hsqldb.jdbcDriver")
	        (.withUser "sa")
	        (.withPassword "")
	        (.withUrl "jdbc:hsqldb:file:database/hsqldb;hsqldb.tx=MVCC")
	        (.end)
         (.withSequenceGenerator QuickHackSequenceGenerator)
         (.withNoClasses)
         (.withDroppingSchema false)
         (.withSchemaCreation false)
         (.withSpecs (into-array java.lang.Class [ scott.data.AccountingSpec ]))
))

(def env (.create env-definition))

(defn get-spec-registry []
	(.getFullSpecRegistry env-definition))

;	(first (remove #(nil? (.getDefinitionsSpec % namespace)) (-> env-definition (.getAllSpecRegistries)))))


(defn query-property [query name] 
	(new scott.barleydb.api.query.QProperty query name))

(defn query-where [query cond] 
	(.where query cond))

(defn parse-date [date-str date-format]
	(-> (new java.text.SimpleDateFormat date-format)
		(.parse date-str)))

(defn todate[date-str] 
	(parse-date date-str "dd-MM-yyyy"))

(defn param [name type]
	(new scott.barleydb.api.query.QParameter name type))

(defn query-transactions-in-month []
	(let [query (new QueryObject "scott.data.model.Transaction")
		  where (partial query-where query)
		  prop (partial query-property query)
		 ]
		(-> (where (-> (prop "date")
			     (.greaterOrEqualParam (param "fromDate" JavaType/UTIL_DATE))
			    ))
		    (.and (-> (prop "date")
		    	 (.lessParam (param "toDate" JavaType/UTIL_DATE))
		    	 ))
		 )))


(defn custom-queries []
	(doto (new scott.barleydb.build.specification.graphql.CustomQueries)
		(.register "transactionsInMonth" (query-transactions-in-month))))

(defn get-graphql-schema [namespace] 
	(new scott.barleydb.api.graphql.BarleyGraphQLSchema (get-spec-registry) env namespace (custom-queries)))


