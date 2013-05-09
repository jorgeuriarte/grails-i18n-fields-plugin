package i18nfields

import grails.util.GrailsUtil

class ConfigProvider {
	static final CONFIG_LOCATION = "${BuildSettingsHolder.settings.baseDir}/grails-app/conf/Config.groovy"

	def getConfig() {
		return new ConfigSlurper(GrailsUtil.environment).parse(new File(CONFIG_LOCATION).toURL())
	}
}
