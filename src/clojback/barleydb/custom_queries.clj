(ns clojback.barleydb.custom-queries
	(:require [clojback.barleydb.query :as bq]))

(import 'scott.barleydb.api.query.QueryObject)

(defn query-transactions-in-month []
	(let [query (new QueryObject "scott.data.model.Transaction")
		  where (partial bq/where query)
		  prop (partial bq/property query)
		 ]
		(-> (where (-> (prop "date") 
			           (bq/gte-date-param "fromDate")))
		    (.and (-> (prop "date")
		    	      (bq/lt-date-param "toDate")))
		 )))



(def build (memoize (fn []
	(doto (new scott.barleydb.build.specification.graphql.CustomQueries)
		(.register "transactionsInMonth" (query-transactions-in-month))))))

