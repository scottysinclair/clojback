(ns clojback.barleydb)

(import 'scott.barleydb.server.jdbc.persist.QuickHackSequenceGenerator)

(def env-definition
    (-> (scott.barleydb.bootstrap.EnvironmentDef/build)
        (.withDataSource)
	        (.withDriver "org.hsqldb.jdbcDriver")
	        (.withUser "sa")
	        (.withPassword "")
	        (.withUrl "jdbc:hsqldb:mem:testdb;hsqldb.tx=MVCC")
	        (.end)
         (.withSequenceGenerator QuickHackSequenceGenerator)
         (.withSpecs (into-array String [ "resources/etlspec.xml"]))
         (.withNoClasses)
         (.withDroppingSchema false)
         (.withSchemaCreation true)))

(def env (.create env-definition))


