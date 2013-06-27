package demo

import i18nfields.I18nFieldsHelper;

@i18nfields.I18nFields
class Demo {
	String name
	String description
	
	static i18nFields = ["name", "description"]
	static i18nFieldsRename = ["description":"longname"]
	static constraints = { name(nullable:false) }
}
