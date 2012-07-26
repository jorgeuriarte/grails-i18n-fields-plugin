package i18nfields

class Literal implements Serializable {
	String myclass
	long myobject
	String locale
	String field
	String value

	static constraints = {
		myclass(nullable:false, maxSize:100)
		myobject(nullable:false)
		locale(nullable:false, maxSize: 6)
		field(nullable:false, maxSize:40)
		value(nullable:false, maxSize:5000)
	}

	static mapping = {
		myclass index:'LocaleObjectFields_Idx,AllObjectFields_Idx,FullLiteral_Idx'
		myobject index:'LocaleObjectFields_Idx,AllObjectFields_Id,FullLiteral_Idx'
		locale index:'LocaleObjectFields_Idx,FullLiteral_Idx'
		field index:'FullLiteral_Idx'
		value index:'FullLiteral_Idx'
		id composite: ["myclass", "myobject", "locale", "field"], generator: "assigned"
	}

	String toString() {
		"${myclass}-${myobject}: ${locale}->${field}=${value}"
	}
}