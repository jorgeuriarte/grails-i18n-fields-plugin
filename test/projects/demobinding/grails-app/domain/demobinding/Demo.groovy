package demobinding

class Demo {
    String name
    String name_es_ES
    String name_en_US
    String name_fr_FR
    
    static transients = ['name', 'name_en_US', 'name_fr_FR']
    
    static constraints = {
        name_en_US bindable: true
        name_fr_FR bindable: true
        name bindable: false
    }
}
