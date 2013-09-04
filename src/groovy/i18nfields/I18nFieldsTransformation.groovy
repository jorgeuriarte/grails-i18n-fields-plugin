package i18nfields

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.ast.AnnotationNode

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class I18nFieldsTransformation implements ASTTransformation {
	def configProvider

	I18nFieldsTransformation() {
		configProvider = new ConfigProvider()
	}

	void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
		if (!isValidAstNodes(astNodes))
			return
			
	    ClassI18nalizator internationalizator = new ClassI18nalizator(astNodes[1], locales(), redisLocales(), constraintsEnforce())
		internationalizator.transformClass()
	}

	private isValidAstNodes(ASTNode[] astNodes) {
		return astNodes != null && astNodes[0] != null && astNodes[1] != null && astNodes[0] instanceof AnnotationNode && astNodes[0].classNode?.name == I18nFields.class.getName() && astNodes[1] instanceof ClassNode
	}
	
	private constraintsEnforce() {
	    def constraints = true
	    if( null != pluginConfig."${I18nFields.I18N_FIELDS}"?.enforceConstraintsOnRedisLocales) {
	        constraints = pluginConfig."${I18nFields.I18N_FIELDS}"?.enforceConstraintsOnRedisLocales
	    }
	    
	    return constraints
	}

	private locales() {
		def locales = []
		filterInvalidLocales(configuredLocales).each { locales << getLocale(it) }
		return locales
	}
	
	private redisLocales() {
		def locales = []
		filterInvalidLocales(configuredRedisLocales).each { locales << getLocale(it) }
		return locales
	}

	private getConfiguredLocales() {
		if (null != pluginConfig."${I18nFields.I18N_FIELDS}"?."${I18nFields.LOCALES}")
			return pluginConfig."${I18nFields.I18N_FIELDS}"."${I18nFields.LOCALES}"
		return [:]
	}
	
	private getConfiguredRedisLocales() {
		if(pluginConfig."${I18nFields.I18N_FIELDS}"?."${I18nFields.REDIS_LOCALES}")
			return pluginConfig."${I18nFields.I18N_FIELDS}"?."${I18nFields.REDIS_LOCALES}"
		return [:]
	}

	private getPluginConfig() {
		return configProvider.getConfig()
	}

	private filterInvalidLocales(configuredLocales) {
		def locales = configuredLocales.findAll { isValidLocale(it) }
		def invalidLocales = configuredLocales - locales
		logInvalidLocales(invalidLocales)
		return locales
	}

	private isValidLocale(String locale) {
		Collection availableLocales = Locale.getAvailableLocales().collect { it.toString() }
		availableLocales.addAll extraLocales
		return availableLocales.contains(locale)
	}

	private getExtraLocales() {
		if (null != pluginConfig."${I18nFields.I18N_FIELDS}"?."${I18nFields.EXTRA_LOCALES}")
			return pluginConfig."${I18nFields.I18N_FIELDS}"."${I18nFields.EXTRA_LOCALES}"
		return [:]
	}

	private logInvalidLocales(invalidLocales) {
		// TODO: Use log4j
		if (invalidLocales.size() > 0)
			println "[i18nFields] Ignoring ${invalidLocales} invalid locale(s)"
	}

	private getLocale(localeString) {
		String[] localeAsArray = localeString.split("_")
		String language = localeAsArray[0]
		String country = localeAsArray.size() > 1 ? localeAsArray[1] : null
		if (null != country)
			return new Locale(language, country)
		return new Locale(language)
	}
}
