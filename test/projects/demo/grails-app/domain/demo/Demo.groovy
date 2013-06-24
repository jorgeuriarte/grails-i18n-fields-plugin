package demo

import i18nfields.I18nFieldsHelper;

@i18nfields.I18nFields
class Demo {
	String name
	
	static i18nFields = ["name"]
	static constraints = { name(nullable:false) }
}
