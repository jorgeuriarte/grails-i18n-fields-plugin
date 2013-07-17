import org.codehaus.groovy.grails.commons.*
import org.apache.log4j.Logger

import i18nfields.*
import grails.util.GrailsUtil
import org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners
import org.hibernate.event.PostInsertEvent

class I18nFieldsGrailsPlugin {
    static final def log = Logger.getLogger(I18nFieldsGrailsPlugin)

	def version = "0.7.0-SNAPSHOT"
	def grailsVersion = "2.1.0 > *"
	def pluginExcludes = [
			"lib/*",
			"grails-app/i18n/*",
			"grails-app/domain/i18nfields/*",
			"grails-app/controllers/i18nfields/*",
			"grails-app/services/i18nfields/*",
			"grails-app/taglib/i18nfields/*",
			"grails-app/views/*",
			"grails-app/views/layouts/*",
			"web-app/css/*",
			"web-app/images/*",
			"web-app/images/skin/*",
			"web-app/js/*",
			"web-app/js/prototype/*",
	]
	def dependsOn = [:]

	def config = ConfigurationHolder.config

	def author = "Jorge Uriarte"
	def authorEmail = "jorge.uriarte@omelas.net"
	def title = "i18n Fields"
	def description = "This plugin provides an easy way of declarativily localize database fields of your content tables."
	def documentation = "http://grails.org/plugin/i18n-fields"

	def grailsApplication
	def setApplication(app) {
		println "[i18nFields] Grails Application injected"
		grailsApplication = app
	}

	def doWithDynamicMethods = { context ->
		println "[i18nFields] Plugin version: ${version}"
		['domain', 'controller', 'service', 'bootstrap'].each {
			println "[i18nFields] Adding 'withLocale' method to '${it}' classes"
			application."${it}Classes".each { theClass ->
				theClass.metaClass.withLocale = I18nFieldsHelper.withLocale
			}
		}
        i18nfields.I18nFieldsHelper.metaClass.getApplicationContext = { -> context }
	}

	def doWithSpring = {
    }

    def doWithApplicationContext = { applicationContext ->
        def listeners = applicationContext.sessionFactory.eventListeners
        def listener = new I18nFieldsListener()
        ['saveOrUpdate', 'preDelete'].each {
            addEventTypeListener(listeners, listener, it)
        }
    }

    private void addEventTypeListener(listeners, listener, type) {
        def typeProperty = "${type}EventListeners"
        def typeListeners = listeners."${typeProperty}"

        def expandedTypeListeners = new Object[typeListeners.length + 1]
        System.arraycopy(typeListeners, 0, expandedTypeListeners, 0, typeListeners.length)
        expandedTypeListeners[-1] = listener

        listeners."${typeProperty}" = expandedTypeListeners
    }

}

