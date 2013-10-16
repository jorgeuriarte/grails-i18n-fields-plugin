package i18nfields

import junit.*

/**
 * hot should we read i18n values.
 */
class GetValueTests {
	def object
	Locale systemLocale

	def config = [
		(I18nFields.I18N_FIELDS): [
			(I18nFields.LOCALES): ["es_ES", "pt_PT", "pt_BR"],
			(I18nFields.REDIS_LOCALES): ["es_ES", "pt_PT", "pt_BR"],
			(I18nFields.DEFAULT_LOCALE): "es_ES",
		]
	]

	@Before
	void setUp() {
		systemLocale = new Locale("es", "ES")

		object = new Object() { def name_es_ES; def name_pt_PT; def name_pt_BR }
		object.name_es_ES = "spanish spain"
		object.name_pt_PT = "portuguese portugal"
		object.name_pt_BR = "portuguese brazil"

		I18nFieldsHelper.metaClass.static.getSpringBean = { String name -> [config: config] }
		I18nFieldsHelper.metaClass.static.getLocale = { systemLocale }
		I18nFieldsHelper.metaClass.static.populateCache = { object, locale -> null }
	}

	@Test
	void "return text in current locale"() {
		systemLocale = new Locale("es", "ES"); assert I18nFieldsHelper.getValue(object, "name") == "spanish spain"
		systemLocale = new Locale("pt", "PT"); assert I18nFieldsHelper.getValue(object, "name") == "portuguese portugal"
		systemLocale = new Locale("pt", "BR"); assert I18nFieldsHelper.getValue(object, "name") == "portuguese brazil"
	}

	@Test
	void "use similar language if exact culture do not exists"() {
		systemLocale = new Locale("es", "AR"); 
		assert I18nFieldsHelper.getValue(object, "name") == "spanish spain"
		assert I18nFieldsHelper.getValue(object, "name", "es_AR") == "spanish spain"
	}

	@Test
	void "use default locale if current locale is empty"() {
		systemLocale = new Locale("pt", "BR"); 
		object.name_pt_BR = ""
		assert I18nFieldsHelper.getValue(object, "name") == "spanish spain"
	}

	@Test
	void "returns empty if asked for specific empty locale"() {
		object.name_pt_BR = ""
		assert I18nFieldsHelper.getValue(object, "name", "pt_BR") == ""
	}
}