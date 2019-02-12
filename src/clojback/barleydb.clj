(ns clojback.barleydb
	(:require [clojback.barleydb.query :as query]))

(import 'scott.barleydb.server.jdbc.persist.QuickHackSequenceGenerator)
(import 'scott.barleydb.api.query.QueryObject)
(import 'scott.barleydb.api.core.types.JavaType)

(defn env-definition[]
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

(def get-graphql-schema (memoize (fn  [namespace custom-queries] 
	(let [env-def (env-definition) 
		  env (.create env-def)
		  spec-registry (.getFullSpecRegistry env-def)]
    	(new scott.barleydb.api.graphql.BarleyGraphQLSchema spec-registry env namespace custom-queries)))))


