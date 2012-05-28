import org.codehaus.groovy.grails.commons.ConfigurationHolder

def langs = ConfigurationHolder.config.i18nFields.locales

fixture {
	build {
	    demo(demo.DemoExternal) {
	    	name_es_MX= 'Pepe'
	    	name_eu = 'Pepe-eu'
	    }
	}
}
