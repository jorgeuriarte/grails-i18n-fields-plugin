package demo

import i18nfields.I18nFieldsHelper;

@i18nfields.I18nFields
class Demo {
	transient def redisBridgeService
	
	String name
	
	static i18nFields = ["name"]
	static constraints = { name(nullable:false) }
	
	def fetched() {
		def locale = i18nfields.I18nFieldsHelper.getLocale()
		redisBridgeService.populateCache(this, locale)
		redisBridgeService.populateCache(this, locale)
		return valuesCache[locale.toString()].name 
	}
}
