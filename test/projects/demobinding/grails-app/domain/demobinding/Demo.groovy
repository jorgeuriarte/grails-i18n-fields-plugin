package demobinding

@i18nfields.I18nFields
class Demo {
    String name
    String field1
    String field2
    
    static constraints = {
        name blank: false
    }
    
    static i18nFields = ['name', 'field1', 'field2']
    //static i18nFieldsRename = ['name': 'nombre']
}
