package i18nfields

import org.springframework.context.ApplicationContext
import org.springframework.context.i18n.LocaleContextHolder
import net.sf.ehcache.*
import groovy.util.logging.*

import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA

@Log4j()
class I18nFieldsHelper implements Serializable {
	transient def redis = new redis.clients.jedis.Jedis("localhost")

	transient def cacheLiterales

	static transients_model = ["fieldname"]

	static setLocale(Locale locale) {
		LocaleContextHolder.setLocale(locale)
	}

	static getLocale() {
		return LocaleContextHolder.getLocale()
	}
	
	static getSupportedLocale(object) {
	    def locale = getLocale()
	    if (object."${I18nFields.LOCALES}".containsKey(locale.language) && !object."${I18nFields.LOCALES}"[locale.language].contains(locale.country)) {
            locale = new Locale(locale.language)
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
	 * Return a field value
	 * @param object
	 * @param field
	 * @return
	 */
	def getLocalizedValue( object, field ) {
		assert object != null, "object to retrieve value should never be null"
		
		// If current locale is on readis, load cache and then retrieve value
		// if it is not, then use the field as field_${locale}
		def locale = i18nfields.I18nFieldsHelper.getSupportedLocale(object)
		def isRedisLocale =  object[I18nFields.REDIS_LOCALES].contains(locale.toString())
		
		if(!isRedisLocale) return object["${field}_${locale}"]
		else {
			def result = ""
			try {
				this.populateCache(object, locale)
				result = object.valuesCache[locale.toString()][field]
			}
			catch(Exception e) {
				log.error("There was some problem retrieving values from Redis. (${locale}, ${object.class.name}, ${field})", e)
				
				// If something goes wrong, use default language if avaible, otherwise return empty string.
				// default language should be a gorm language.
				def grailsApplication = getSpringBean("grailsApplication")
				def default_locale = grailsApplication.config[I18nFields.I18N_FIELDS][I18nFields.DEFAULT_LOCALE]
				if(default_locale) {
					result = object["${field}_${default_locale}"]
				} 
			}
			return result;
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
		
		return redis.hgetAll(keyName)
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
	
	static getSpringBean(String name) {
		SCH.getServletContext().getAttribute(GA.APPLICATION_CONTEXT).getBean(name);
	}

}

