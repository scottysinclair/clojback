(ns clojback.barleydb)

(import 'scott.barleydb.server.jdbc.persist.QuickHackSequenceGenerator)

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

(defn get-graphql-schema [namespace] 
	(new scott.barleydb.api.graphql.BarleyGraphQLSchema (get-spec-registry) env namespace nil))


