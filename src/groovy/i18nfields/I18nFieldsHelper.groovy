package i18nfields

import groovy.util.logging.*

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
	
	/**
	 * Helper method to allow saving just one locale directly to redis
	 */
	static saveLocale = { Locale locale ->
		push(delegate, locale)
		return true
	}

    /**
	 * Return a field value in the current locale or the default locale if the current
	 * locale do not exists.
	 *
     * @param object domain class instance being used.
     * @param field fieldname to retrieve, without locale
     *
     * @returns field value
	 *
	 */
	static String getValueOrDefault( object, field ) {
	    getValueOrDefault(object, field, getSupportedLocale())
    }
    
    /**
     * Return a field value in the specified locale or the default if empty
     * In case of not supported locale, throws exception
     *
     * @param object domain class instance being used.
     * @param field fieldname to retrieve, without locale
     * @param locale locale to retrieve
     *
     * @returns field value
     */
    static String getValueOrDefault( object, field, locale ) {
	    def result = getValueOrEmpty(object, field, locale)
	    if(!result) {
			def grailsApplication = getSpringBean("grailsApplication")
			def default_locale = grailsApplication.config[I18nFields.I18N_FIELDS][I18nFields.DEFAULT_LOCALE]
	        result = getValueOrEmpty(object, field, default_locale)
	    }
	    
	    return result
    }

    /**
     * Return a field value without trying the default locale if there is a error
     * In case of error with redis, returns empty
     * In case of not supported locale, throws exception
     *
     * @param object domain class instance being used.
     * @param field fieldname to retrieve, without locale
     * @param locale locale to retrieve
     *
     * @returns field value
     */
    static String getValueOrEmpty( object, field, locale ) {
        assert object != null, "object to retrieve value should never be null"
        assert locale != null, "the locale to retrieve is mandatory"
        
        locale = locale.toString() // we need the locale as a string
        locale = getSupportedLocale(locale) // use a near locale if locale do not exists.
        
        def isRedisLocale =  getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.REDIS_LOCALES].contains(locale)
        if(!isRedisLocale) return object.@"${field}_${locale}"
        
        try {
            populateCache(object, locale) // can throw exception
        }
        catch(Exception e) {
            log.debug "Error retrieving redis value. Field ${field}, Locale: ${locale}"
        }
        
        return object.@"${field}_${locale}"
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
		def property = field[3..-6].toLowerCase()
		def isRedisLocale =  getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.REDIS_LOCALES].contains(locale)

		// Mark the locale as dirty
		if(!object[I18nFields.DATA].dirty) object[I18nFields.DATA].dirty = [] as Set
		object[I18nFields.DATA].dirty << locale
		
//		if( object.version && !object.isDirty() ){ object.version = (!object.version ? null : object.version + 1) }

		// If requested locale is in redis, save in cache and mark object as dirty
		// if it is not, then use the field directly.
		object.@"${property}${locale}" = value
	}
	
	/**
	 * @param object domain class instance.
	 * @param locale locale to save
	 */
	static def push(object, locale) {
		// if the locale is not dirty, there is no reason to push
		def dirties = object[I18nFields.DATA].dirty
		if(! (locale.toString() in object[I18nFields.DATA].dirty) ) {
		    log.debug "Not pushing ${locale} because it is not dirty. (${object[I18nFields.DATA].dirty*.class?.name})"
	        return;
		}
		
	    // get redis key to persist.
	    def objectId =  object.id
	    def className = object.class.simpleName.toLowerCase()
		String keyName = "${locale}:${className}:${objectId}"
		log.debug "Pushing locale ${locale} to Redis (${keyName})"

        // Gather values to persist.	    
	    def values = [:]
	    object[I18nFields.I18N_FIELDS].each { key ->
	    	def value = object.@"${key}_${locale}"
	        if (value != null)
	            if(object.hasProperty(I18nFields.I18N_FIELDS_RENAME))
    	            values[object[I18nFields.I18N_FIELDS_RENAME][key]?:key] = value
	            else
	                values[key] = value
	    }
		
		// If there is something to save... do it.
		if (values) {
		    try {
    		    RedisHolder.instance.hmset(keyName, values)
    		    
                // If the pushed locale were the last dirty locale, remove the dirty object state
                dirties = dirties - locale.toString()
//                if(object.version && dirties == [] as Set)  object.version--
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
	 * Load values from redis into the cache and properties.
	 * If cache is already loaded for a language, do nothing.
	 * 
	 * @param object object to fill the cache
	 * @param locale locale to fill
	 * @return
	 */
	static def populateCache(object, locale) {
		def origin = object[I18nFields.DATA][locale]
		if(origin != null) return;
		
		def values = getRedisValues(object, locale)
		if(!values) {
			object[I18nFields.DATA][locale] = []
			throw new Exception("Redis value not found.")
		}
		
		object[I18nFields.DATA][locale] = values
	    values.each { value ->
	        if (object.hasProperty("${value.key}_${locale}"))
	            object.@"${value.key}_${locale}" = value.value
        }
	}
	
	static def getSpringBean(String name) {
		SCH.getServletContext().getAttribute(GA.APPLICATION_CONTEXT).getBean(name);
	}

}

