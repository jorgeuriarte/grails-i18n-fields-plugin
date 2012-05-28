package i18nfields

@I18nFields
class Ego {
	def name
	long karma
	def alias
	static i18nFieldsTable = "literal"
	static i18nFields = ["name", "alias"]

	static constraints = {
		karma(nullable: true)
		alias(nullable: true)
	}
}