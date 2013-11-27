package i18nfields

import grails.util.GrailsUtil
import grails.util.BuildSettingsHolder

class ConfigProvider {
	static final CONFIG_LOCATION = "${BuildSettingsHolder.settings.baseDir}/grails-app/conf/Config.groovy"
	static def config
	def getConfig() {
		if (!config) {
			config = new ConfigSlurper(GrailsUtil.environment).parse(new File(CONFIG_LOCATION).toURL())
		}
		return config
	}
}
