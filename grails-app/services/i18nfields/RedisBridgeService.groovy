package i18nfields
class RedisBridgeService {
	def redisService
    static transactional = false
	
	/**
	 * Return a field value
	 * @param object
	 * @param field
	 * @return
	 */
	def getLocalizedValue( object, field ) {
		// If current locale is on readis, load cache and then retrieve value
		// if it is not, then use the field as field_${locale}
		def locale = i18nfields.I18nFieldsHelper.getLocale()
		def isRedisLocale =  object[I18nFields.REDIS_LOCALES].contains(locale.toString())
		
		if(!isRedisLocale) return object["${field}_${locale}"]
		else {
			this.populateCache(object, locale)
			return object.valuesCache[locale.toString()].name
		}
	}
	
	
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
