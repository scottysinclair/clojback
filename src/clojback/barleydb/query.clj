(ns clojback.barleydb.query)

(import 'scott.barleydb.api.core.types.JavaType)

(def UTIL_DATE JavaType/UTIL_DATE)

(defn property [query name] 
	(new scott.barleydb.api.query.QProperty query name))

(defn where [query cond] 
	(.where query cond))

(defn param [name type]
	(new scott.barleydb.api.query.QParameter name type))

(defn date-param [name]
	(param name UTIL_DATE))

(defn gte-param [qprop qparam]
	(.greaterOrEqual qprop qparam))

(defn gte-date-param [qprop param-name]
	(gte-param qprop (date-param param-name)))

(defn lt-param [qprop qparam]
	(.lessParam qprop qparam))

(defn lt-date-param [qprop param-name]
	(.lessParam qprop (date-param param-name)))
