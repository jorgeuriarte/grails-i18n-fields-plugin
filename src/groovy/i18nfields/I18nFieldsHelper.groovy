package i18nfields

import org.springframework.context.ApplicationContext
import org.springframework.context.i18n.LocaleContextHolder
import net.sf.ehcache.*
import groovy.util.logging.*

import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA

@Log4j()
class I18nFieldsHelper implements Serializable {
	transient static def redis = new redis.clients.jedis.Jedis("localhost")

	transient def cacheLiterales

	static transients_model = ["fieldname"]

	static setLocale(Locale locale) {
		LocaleContextHolder.setLocale(locale)
	}

    /**
     * Gets current locale
     */
	static getLocale() {
		return LocaleContextHolder.getLocale()
	}
    
    /**
     * Gets the locale to be used
     */
	static getSupportedLocale() {
	    def locale = getLocale()
		def locales = getSpringBean("grailsApplication").config[I18nFields.I18N_FIELDS][I18nFields.LOCALES]
	    if (!locales.contains(locale.toString())) {
            throw new Exception("Locale ${locale} not found!")
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

	def literalsForField = [:]


	def cacheGet(key) {
		makeSureCacheExists()
		return cacheLiterales.get(key)?.objectValue
	}

	def cachePut(key, value) {
		makeSureCacheExists()
		cacheLiterales.put(new Element(key, value))
	}

	def makeSureCacheExists() {
		if (!cacheLiterales) {
			def springcacheService = this.applicationContext.springcacheService
			cacheLiterales = springcacheService.getOrCreateCache('i18nFields.Literals')
		}		
	}

	def findFieldFor(field, locale, object) {
		if (!isLocaleLoaded(locale, object)) {
			loadFieldsLocally(locale, object)
		}
		def value = locallyStoredField(field, locale, object)
		return value
	}

	def isLocaleLoaded(locale, object) {
		def localCopies = object?."${I18nFields.TEMPSTRINGS}"
		if (localCopies."${locale.toString()}" == null) {
			def value = cacheGet("${object.class.name}:${object.id}:${locale}")
			localCopies."${locale.toString()}" = value
		}
		return localCopies."${locale.toString()}" != null
	}

	def locallyStoredField(field, locale, object) {
		return fieldInAcceptedLocale(field, locale, object)
	}

	def fieldInAcceptedLocale(field, locale, object) {
		def strings = object?."${I18nFields.TEMPSTRINGS}"
		def locales = []
		if (locale instanceof Locale)
			locales = [locale.toString(), locale.language]
		else
			locales = [locale.toString()]
		for (loc in locales) {
			if (strings[loc]) {
				return strings[loc][field]
			}
		}
	}

	def loadFieldsLocally(locale, object) {
		loadFieldsLocallyFromDB(locale, object)
	}

	def loadFieldsLocallyFromDB(locale, object) {
		def raiz = object?."${I18nFields.TEMPSTRINGS}"
		if (!raiz?."${locale}")
			raiz[locale.toString()] = [:]

		def c = Literal.createCriteria()
		c.list {
			eq("myclass", object.class.name)
			eq("myobject", object?.id)
			eq("locale", locale.toString())
		}.each { field ->
			raiz[locale.toString()][field.field] = field.value
		}
		cachePut("${object.class.name}:${object.id}:${locale}", raiz[locale.toString()])
	}

	def loadFieldsLocallyFromRedis(locale, object) {
		println "Getting from redis! (${object?.id}:${locale.toString()})"
		def raiz = object?."${I18nFields.TEMPSTRINGS}"
		raiz[locale.toString()] = redis.hgetAll("literal:${object.class.name}:${object?.id}:${locale.toString()}")
		cachePut("${object.class.name}:${object.id}:${locale}", raiz[locale.toString()])
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

	def setFieldFor(field, locale, object, value) {
		if (!value) {
			return
		}
		def raiz = object."${I18nFields.TEMPSTRINGS}"
		if (!raiz?."${locale}")
			raiz[locale] = [:]
		raiz[locale][field] = value
	}

	void deleteFieldsFor(object) {
		Literal.executeUpdate("delete Literal lit where lit.myclass=:myclass and lit.myobject=:myobj",
			[myclass:object.class.name, myobj:object.id])
	}

    static def findAllByLiteralLike(theclass, field, locale, thelike, hql = null, params = []) {
        def clos = finderByLiteral.curry(theclass, 'like')
        return clos.call(locale, field, thelike, hql, params)
    }

    static def findAllByLiteral(theclass, field, locale, value, hql = null, params = []) {
    	def clos = finderByLiteral.curry(theclass, '=')
    	return clos.call(locale, field, value, hql, params)
    }

    static def findByLiteral(theclass, field, locale, value, hql = null, params = []) {
    	def list = findAllByLiteral(theclass, field, locale, value, hql, params)
    	if (list)
    		return list.first()
    	else
    		return null
    }

    static def findByLiteralLike(theclass, field, locale, value, hql = null, params = []) {
    	def list = findAllByLiteralLike(theclass, field, locale, value, hql, params)
    	if (list)
    		return list.first()
    	else
    		return null
    }

    static Closure finderByLiteral = { theclass, operator, locale, field, value, hql, params ->
        def classname = theclass.name
        def alias = classname.replaceAll(/^(.*\.)?([a-zA-Z]*)/, '$2')
        hql = hql?"and ${hql}":""
        hql = "\
		    select ${alias} \
			from ${classname} as ${alias}, i18nfields.Literal as lit \
			where ${alias}.id = lit.myobject \
			${hql} \
			and lit.myclass = '${classname}' \
			and lit.locale = '${locale}' \
			and lit.field = '${field}' \
			and lit.value ${operator} '${value}' \
		"
		println "HQL: ${hql}[${params}]"
		return theclass.executeQuery(hql, params)
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
		return getValue(object, "${field}_${locale}")
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
	 * Transforms the field name to the redis field name.
	 * @param field field to adapt.
	 * @return name of the hash field to be used in redis.
	 */
	static def translateField(object, field) {
		if(!object.metaClass.hasProperty(object, I18nFields.I18N_FIELDS_RENAME)) return field
		
		def redisField = object[I18nFields.I18N_FIELDS_RENAME][field]?:field
		return redisField
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
		    redis.hmset(keyName, values)
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
		
		return redis.hgetAll(keyName)
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
	            object."${value.key}_${locale}" = value.value
        }
	}
	
	static def getSpringBean(String name) {
		SCH.getServletContext().getAttribute(GA.APPLICATION_CONTEXT).getBean(name);
	}

}

