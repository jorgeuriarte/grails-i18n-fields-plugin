package demobinding

@i18nfields.I18nFields
class Demo {
    String name
    String field1
    String field2
    String demoDescription
    
    static constraints = {
        name blank: false
    }
    
    static i18nFields = ['name', 'field1', 'field2', 'demoDescription']
    //static i18nFieldsRename = ['name': 'nombre']
}
