package demobinding

@i18nfields.I18nFields
class Demo {
    String name
    
    static constraints = {
        name blank: false
    }
    
    static i18nFields = ['name']
}
