package i18nfields
class RedisBridgeService {
	def redisService
    static transactional = false
	
	/**
	 * Retrieve from redis the values for a object
	 * 
	 * @param object object which values will be fetched
	 * @param locale locale for the values to retrieve
	 * @return map with values
	 */
	def fetch(object, locale) {
		def objectId = object.id
		def className = object.class.simpleName.toLowerCase()
		
		def keyName = "${locale}:${className}:${objectId}"
		return redisService.hgetAll(keyName)
	}
	
	/**
	 * Load values from redis into the cache
	 * @param object object to fill the cache
	 * @param locale locale to fill
	 * @return
	 */
	def populateCache(object, locale) {
		if(object.valuesCache[locale.toString()]) return;

		def values = fetch(object, locale)
		object.valuesCache[locale.toString()] = values
	}
}
