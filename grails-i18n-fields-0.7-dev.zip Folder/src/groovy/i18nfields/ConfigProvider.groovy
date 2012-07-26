package i18nfields

import grails.util.GrailsUtil

class ConfigProvider {
	static final CONFIG_LOCATION = "${System.properties['user.dir']}/grails-app/conf/Config.groovy"

	def getConfig() {
		return new ConfigSlurper(GrailsUtil.environment).parse(new File(CONFIG_LOCATION).toURL())
	}
}
