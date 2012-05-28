package i18nfields

@I18nFields
class ChuChu {
	def name
	def description
	static i18nFields = ["name", "description"]
	static constraints = {
		name(nullable:true,min:5)
		name_en_US(min:10)
	}
}