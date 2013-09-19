class BootStrap {

    def init = { servletContext ->
        // Agregamos un dato demo
        def demo = new demobinding.Demo()
        demo.name_es_ES = "Español"
        demo.name_fr_FR = "Frances"
        demo.name_en_US = "Ingles"

        demo.field1_es_ES = "Español1"
        demo.field1_fr_FR = "Frances1"
        demo.field1_en_US = "Ingles1"
        
        demo.field2_es_ES = "Español2"
        demo.field2_fr_FR = "Frances2"
        demo.field2_en_US = "Ingles2"
        
        demo.save(failOnError: true)
    }
    
    def destroy = {
    }
}
