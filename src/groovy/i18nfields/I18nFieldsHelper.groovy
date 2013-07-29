package i18nfields

import groovy.util.logging.*
import net.sf.ehcache.*

import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA
import org.springframework.context.i18n.LocaleContextHolder

@Log4j()
class I18nFieldsHelper implements Serializable {
	static setLocale(Locale locale) {
		LocaleContextHolder.setLocale(locale)
	}

    /**
     * Gets current locale
     */
	static getLocale() {
	    def locale = LocaleContextHolder.getLocale()
	    if(!locale || locale.toString() == "") locale = new Locale(getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.DEFAULT_LOCALE])
        return locale 
	}
	
	/**
	 * Get the locale to be used instead of the current locale.
	 */
	static getSupportedLocale() {
        return getSupportedLocale(getLocale())
    }
    
    /**
     * Gets the locale to be used instead of given locale
     */
	static getSupportedLocale( locale ) {
		def locales = getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.LOCALES]
		
	    if (!locales.contains(locale.toString())) {
            def lang = locale.toString().split("_")[0]
            def selected_locale = locales.find { candidato -> candidato.startsWith(lang) }
            
            if(!selected_locale) throw new Exception("Locale ${locale} not found!")
            // log.warn("Locale ${locale} not found. Using ${selected_locale}")
            locale = selected_locale
        }
        
        return locale
	}

	static withLocale = { Locale newLocale, Closure closure ->
		def previousLocale = i18nfields.I18nFieldsHelper.getLocale()
		i18nfields.I18nFieldsHelper.setLocale(newLocale)
		def result = closure.call()
		i18nfields.I18nFieldsHelper.setLocale(previousLocale)
		return result
	}

	def acceptedLocale(locale, object) {
		def locales = object."${I18nFields.LOCALES}"
		if (locales.containsKey(locale.language)
			&& !locales[locale.language].contains(locale.country)) {
			return new Locale(locale.language)
		} else {
			return locale
		}
	}

	/**
	 * Return the field value for the desired locale.
	 * 
	 * @param object domain class instance being used.
	 * @param field field to retrieve, like name or description
	 * @param locale the locale used to localize
	 * @return
	 */
	static String getLocalizedValue( object, field, locale ) {
	    def supported_locale = getSupportedLocale(locale)
		return getValue(object, "${field}_${supported_locale}")
	}
	
	/**
	 * Return the field value for the current locale.
	 * 
	 * @param object domain class instance being used.
	 * @param field field to retrieve, like name or description
	 * @return
	 */
	static String getLocalizedValue( object, field ) {
		def locale = i18nfields.I18nFieldsHelper.getSupportedLocale()
		return getLocalizedValue(object, field, locale)
	}
	
	/**
	 * Return a field value even if it is stored in redis.
	 * @param object domain class instance being used.
	 * @param field field with locale to retrieve, like name_es_ES or description_es_ES
	 */
	static String getValue( object, field ) {
	    assert object != null, "object to retrieve value should never be null"
		
		def locale = field[-5..-1]
		def isRedisLocale =  getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.REDIS_LOCALES].contains(locale)
		
		// If requested locale is in redis, load cache and then retrieve value
		// if it is not, then use the field directly.
		if(!isRedisLocale) return object.@"${field}"
		else {
			def result = ""
			try {
			    if (!object.@"${field}")
				    populateCache(object, locale)
				result = object.@"${field}"
			}
			catch(Exception e) {
				// log.warn("There was some problem retrieving values from Redis. (${locale}, ${object.class.name}, ${field})", e)
				
				// If something goes wrong, use default language if available, otherwise return empty string.
				// default language should be a gorm language.
				def grailsApplication = getSpringBean("grailsApplication")
				def default_locale = grailsApplication.config[I18nFields.I18N_FIELDS][I18nFields.DEFAULT_LOCALE]
				if(default_locale) {
					result = object.@"${field[0..-7]}_${default_locale}"
				} 
			}
			return result;
		}
	}
	
	/**
	 * Set a field value, even if it is stored in redis.
	 * @param object where to store the value.
	 * @param field fieldname to store.
	 * @param value value to store.
	 */
	static void setValue( object, field, value ) {
		assert object != null, "object to retrieve value should never be null"
		
		def locale = field[-5..-1]
		def isRedisLocale =  getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.REDIS_LOCALES].contains(locale)

		// If requested locale is in redis, save in cache and mark object as dirty
		// if it is not, then use the field directly.
		if(!isRedisLocale) object.@"${field}" = value
		else {
			object.@"${field}" = value
			if( !object.isDirty() ){ object.version = (!object.version ? null : object.version + 1) }
		}
	}
	
	/**
	 * @param object domain class instance.
	 * @param locale locale to save
	 */
	static def push(object, locale) {
	    // get redis key to persist.
	    def objectId =  object.id
	    def className = object.class.simpleName.toLowerCase()
		String keyName = "${locale}:${className}:${objectId}"

        // Gather values to persist.	    
	    def values = [:]
	    object[I18nFields.I18N_FIELDS].each { key ->
	        if (object["${key}_${locale}"])
	            if(object.hasProperty(I18nFields.I18N_FIELDS_RENAME)) 
    	            values[object[I18nFields.I18N_FIELDS_RENAME][key]?:key] = object["${key}_${locale}"]
	            else
	                values[key] = object["${key}_${locale}"]
	    }
		
		// If there is something to save... do it.
		if (values) {
		    try {
    		    RedisHolder.instance.hmset(keyName, values)
		    }
		    catch(Exception e) {
		        log.error("Can not write in REDIS ! But it was already saved on mysql. Redis Locales were lost.", e);
		    }
        }
	}
	
	/**
	 * Save in redis all the locales
	 */
	static def pushAll(object) {
	    def locales = getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.LOCALES]
	    locales.each { locale ->
	        push(object, locale)
	    }
	}
	
	/**
	 * Delete a entity from redis
	 * WARNING: All the key is deleted, not just the locale information added by this class.
	 */
	static def delete(object) {
	    def objectId =  object.id
	    def className = object.class.simpleName.toLowerCase()

	    def locales = getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.LOCALES]
	    locales.each { locale ->
    	    String keyName = "${locale}:${className}:${objectId}"
    	    RedisHolder.instance.del(keyName)
	    }
	}
	
	/**
	 * Retrieve from redis the values for a object.
	 *
	 * @param object object which values will be fetched.
	 * @param locale locale for the values to retrieve.
	 * @return map with values.
	 */
	static def fetch(object, locale) {
		def objectId = object.id
		def className = object.class.simpleName.toLowerCase()
		
		def keyName = "${locale}:${className}:${objectId}"
		def result = RedisHolder.instance.hgetAll(keyName);
		
		log.debug "Fetching from redis ${keyName} values ${result}"
		return result;
	}
	
	/**
	 * Retrieve from redis the values of a locale. Ranames the keys if neccesary.
	 * @param object domain class instance.
	 * @param locale locale to retrieve.
	 * @return map with values.
	 */
	static def getRedisValues(object, locale) {
	    def grailsToRedis = object.hasProperty(I18nFields.I18N_FIELDS_RENAME) ? object[I18nFields.I18N_FIELDS_RENAME] : [:]
	    def redisToGrails = grailsToRedis.collectEntries { key, value -> [(value), key]}

	    def values = fetch(object, locale).collectEntries { keyRedis, valueRedis ->
	        if( redisToGrails.containsKey(keyRedis) ) {
	            return [ (redisToGrails[keyRedis]) : valueRedis ]
            }
            return [(keyRedis): valueRedis]
	    }
	    
	    return values;
	}
	
	/**
	 * Load values from redis into the cache
	 * @param object object to fill the cache
	 * @param locale locale to fill
	 * @return
	 */
	static def populateCache(object, locale) {
		def values = getRedisValues(object, locale)
		if(!values) throw new Exception("Redis value not found.")
		
	    values.each { value ->
	        if (object.hasProperty("${value.key}_${locale}"))
	            object.@"${value.key}_${locale}" = value.value
        }
	}
	
	static def getSpringBean(String name) {
		SCH.getServletContext().getAttribute(GA.APPLICATION_CONTEXT).getBean(name);
	}

}

