package demo

import i18nfields.*

@i18nfields.I18nFields
class DemoExternal {

	String name
	String description

	static i18nFields = ["name", "description"]
	static i18nFieldsTable = "literal"
		
    static constraints = {
    	name(nullable: false)
    	description(nullable: true)
    }
}
