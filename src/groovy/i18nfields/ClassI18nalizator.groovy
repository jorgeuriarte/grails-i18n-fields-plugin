package i18nfields

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement

import org.codehaus.groovy.ast.FieldNode
import static org.springframework.asm.Opcodes.ACC_PUBLIC
import static org.springframework.asm.Opcodes.ACC_STATIC
import java.lang.reflect.Modifier
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.ast.PropertyNode

class ClassI18nalizator {
	def classNode
	def locales
	def redisLocales

	ClassI18nalizator(ClassNode classNode, Collection<Locale> locales, Collection<Locale> redisLocales) {
		this.classNode = classNode
		this.locales = locales
		this.redisLocales = redisLocales
	}

	void transformClass() {
		configureTransformation()
		createLocalStructures()
		addFieldsAndAccessors()
	}

	private void configureTransformation() {
		addLocalesMap()
		addRedisLocalesList()
		addLocalesCache()
	}

	private void createLocalStructures() {
		addHelper()
	}

	private void addFieldsAndAccessors() {
		i18nFieldList.each { fieldName ->
			removeField(fieldName)
			makeFieldTransient(fieldName)
			addI18nFields(fieldName)
			addGettersAndSetters(fieldName)
			removeConstraintsFor(fieldName)
		}
	}

	private addLocalesMap() {
		def i18nFields = new MapExpression()
		def localeTree = [:]
		locales.each { locale ->
			if (!localeTree.containsKey(locale.language))
				localeTree.put(locale.language, new ListExpression())
			if ("" != locale.country)
				localeTree.get(locale.language).addExpression(new ConstantExpression(locale.country))
		}
		localeTree.each {
			i18nFields.addMapEntryExpression(new ConstantExpression(it.key), it.value)
		}
		addStaticField(I18nFields.LOCALES, i18nFields)
	}
	
	/**
	 * Add a list with the locales to be managed in redis
	 * @return
	 */
	private addRedisLocalesList() {
		def redisLocalesListExpression = new ListExpression()
		redisLocales.each { locale ->
			redisLocalesListExpression.addExpression(new ConstantExpression(locale.toString()))
		}
		addStaticField(I18nFields.REDIS_LOCALES, redisLocalesListExpression)
	}
	
	/**
	 * Add map to hold cahed values from redis
	 * @return
	 */
	private addLocalesCache() {
		def valuesCacheField = new MapExpression()
		addField("valuesCache", valuesCacheField);
	}

	private addTempStringMap() {
        classNode.addField(new FieldNode(I18nFields.TEMPSTRINGS, ACC_PUBLIC,
        						new ClassNode(Object.class),
        						classNode, new MapExpression()))
	}

	private addLocalCacheMap() {
		classNode.addField(new FieldNode(I18nFields.CACHESTRINGS, ACC_PUBLIC,
								new ClassNode(Object.class),
								classNode, new MapExpression()))
	}

	private addHelper() {
		classNode.addField(new FieldNode("i18nFieldsHelper", ACC_PUBLIC,
								new ClassNode(I18nFieldsHelper.class),
								classNode,
								new ConstructorCallExpression(new ClassNode(I18nFieldsHelper.class), MethodCallExpression.NO_ARGUMENTS)))
	}

	private addStaticField(name, initialExpression) {
		def field = new FieldNode(name, ACC_PUBLIC | ACC_STATIC, new ClassNode(Object.class), classNode, initialExpression)
		// TODO: Use log4j
		println "[i18nFields] Adding ${name} static field to ${classNode.name}"
		field.setDeclaringClass(classNode)
		classNode.addField(field)
	}
	
	private addField(name, initialExpression) {
		def field = new FieldNode(name, ACC_PUBLIC, new ClassNode(Object.class), classNode, initialExpression)
		// TODO: Use log4j
		println "[i18nFields] Adding ${name} field to ${classNode.name}"
		field.setDeclaringClass(classNode)
		classNode.addField(field)

	}

	private getI18nFieldList() {
		def configuredI18nFieldList = getConfiguredI18nFieldList()
		def i18nFields = configuredI18nFieldList.findAll { fieldExists(it) }
		def invalidI18nFields = configuredI18nFieldList - i18nFields
		logInvalidI18nFields(invalidI18nFields)
		return i18nFields
	}

	private getConfiguredI18nFieldList() {
		return i18nFieldListConfigurationField?.getInitialValueExpression()?.expressions.collect { it.getText() }
	}

	private getI18nFieldListConfigurationField() {
		if (fieldExists(I18nFields.I18N_FIELDS))
			return classNode.getField(I18nFields.I18N_FIELDS)

		// TODO: Use log4j
		println "[i18nFields] WARNING - Visted ${classNode.name} but no ${I18nFields.I18N_FIELDS} static property found"
	}

	private logInvalidI18nFields(invalidI18nFields) {
		// TODO: Use log4j
		if (invalidI18nFields.size() > 0)
			println "[i18nFields] Ignoring ${invalidI18nFields} non existant field(s)"
	}

	private removeField(name) {
		// TODO: Use log4j
		println "[i18nFields] Removing field '${name}' from class ${classNode.name}"
		classNode.properties.remove(classNode.getProperty(name))
		classNode.removeField(name)
	}

	private makeFieldTransient(name) {
		def transients = getOrCreateTransientsField().getInitialExpression()
		// TODO: Use log4j
		println "[i18nFields] Making '${name}' field of class ${classNode.name} transient"
		transients.addExpression(new ConstantExpression(name))
	}

	private getOrCreateTransientsField() {
		if (!fieldExists(I18nFields.TRANSIENTS))
			addStaticField(I18nFields.TRANSIENTS, new ListExpression())
		return classNode.getDeclaredField(I18nFields.TRANSIENTS)
	}

	private addI18nFields(baseName) {
	    locales.each { locale ->
	        // TODO: if(!redisLocales.contains(locale)) {
	        // Redis locales should be created transients
		    def fieldName = "${baseName}_${locale}"
		    addI18nField(fieldName)
		    if (!hasConstraints(fieldName) && hasConstraints(baseName))
			    copyConstraints(baseName, fieldName)
	    }
	}

	private addI18nField(name) {
		println "[i18nFields] Adding '${name}' field to ${classNode.name}"
		classNode.addProperty(name, Modifier.PUBLIC, new ClassNode(String.class), new ConstantExpression(null), null, null)
	}

	private boolean hasConstraints(field) {
		return hasConstraints() && null != getConstraints(field)
	}

	private boolean hasConstraints() {
		return fieldExists(I18nFields.CONSTRAINTS)
	}

	private boolean fieldExists(name) {
		return null != classNode.getDeclaredField(name)
	}

	private getConstraints(field) {
		def closure = getConstraints().getInitialExpression().getCode()
		return closure.statements.find { statement ->
			containsAMethodCallExpression(statement) && field == statement.getExpression().getMethodAsString()
		}
	}

	private getConstraints() {
		return classNode.getDeclaredField(I18nFields.CONSTRAINTS)
	}

	private removeConstraintsFor(field) {
		def block = getConstraints()?.getInitialExpression()
		if (block) {
			def filtered = block.getCode().statements.findAll {
				it.getExpression().getMethodAsString() != field
			}
			block.setCode(new BlockStatement(filtered, block.variableScope))
			println "[i18nFields] Removed original constraint for '${field}'"
		}
	}

	private boolean containsAMethodCallExpression(statement) {
		statement instanceof ExpressionStatement && statement.getExpression() instanceof MethodCallExpression
	}

	private copyConstraints(from, to) {
		def baseMethodCall = getConstraints(from).getExpression()
		def methodCall = new MethodCallExpression(new VariableExpression('this'), to, baseMethodCall.getArguments())
		def newConstraints = new ExpressionStatement(methodCall)
		addConstraints(newConstraints)
	}

	private addConstraints(constraints) {
		def closure = getConstraints().getInitialExpression().getCode()
		closure.addStatement(constraints)
	}

	private addGettersAndSetters(field) {
		addProxyGetterAndSetter(field)
		addLocalizedGetterAndSetter(field)
	}

	private addProxyGetterAndSetter(field) {
		addProxyGetter(field)
		addProxySetter(field)
	}

	private addLocalizedGetterAndSetter(field) {
		addLocalizedGetter(field)
		addLocalizedSetter(field)
	}

	private addProxyGetter(field) {
		def methodName = GrailsClassUtils.getGetterName(field)
		def code = proxyGetterCode(field)
		def parameters = [] as Parameter[]
		// TODO: Use log4j
		println "[i18nFields] Adding '${methodName}()' proxy method to ${classNode.name}"
		def method = getNewMethod(methodName, parameters, code)
		classNode.addMethod(method)
	}

	private addProxySetter(field) {
		def methodName = GrailsClassUtils.getSetterName(field)
		def code = proxySetterCode(field)
		def parameters = [new Parameter(ClassHelper.make(String, false), "value")] as Parameter[]
		// TODO: Use log4j
		println "[i18nFields] Adding '${methodName}(String value)' proxy method to ${classNode.name}"
		def method = getNewMethod(methodName, parameters, code)
		classNode.addMethod(method)
	}

	private addLocalizedGetter(field) {
		def methodName = GrailsClassUtils.getGetterName(field)
		def code = localizedGetterCode(field)
		def parameters = [new Parameter(ClassHelper.make(Locale, false), "locale")] as Parameter[]
		// TODO: Use log4j
		println "[i18nFields] Adding '${methodName}(Locale locale)' helper getter to ${classNode.name}"
		def method = getNewMethod(methodName, parameters, code)
		classNode.addMethod(method)
	}

	private addLocalizedSetter(field) {
		def methodName = GrailsClassUtils.getSetterName(field)
		def code = localizedSetterCode(field)
		Parameter[] parameters = [
				new Parameter(ClassHelper.make(String, false), "value"),
				new Parameter(ClassHelper.make(Locale, false), "locale")
		] as Parameter[]
		// TODO: Use log4j
		println "[i18nFields] Adding '${methodName}(String value, Locale locale)' helper setter to ${classNode.name}"
		def method = getNewMethod(methodName, parameters, code)
		classNode.addMethod(method)
	}

	private addLocalizedNamedSetters(field) {
		locales.each { locl ->
			def methodName = GrailsClassUtils.getSetterName("${field}_${locl}")
			def code = localizedNamedSetterCode(field, locl)
			Parameter[] parameters = [
				new Parameter(ClassHelper.make(Object, false), "value")
			] as Parameter[]
			// TODO: Use log4j
			println "[i18nFields] Adding '${methodName}' localized setter to ${classNode.name}"
			def method = getNewMethod(methodName, parameters, code)
			classNode.addMethod(method)
		}
	}

	private addLocalizedNamedGetters(field) {
		locales.each { locl ->
			def methodName = GrailsClassUtils.getGetterName("${field}_${locl}")
			def code = localizedNamedGetterCode(field, locl)
			def parameters = [] as Parameter[]
			// TODO: Use log4j
			println "[i18nFields] Adding '${methodName}' localized getter to ${classNode.name}"
			def method = getNewMethod(methodName, parameters, code, true)
			classNode.addMethod(method)
		}
	}

	private localizedNamedGetterCode = { field, locale ->
		"""
def locale = new Locale('${locale.language}', '${locale.country}')
def fieldValue = ${getterCode(field)}
return fieldValue
"""
	}

	private localizedNamedSetterCode = { field, locale ->
		"""
def locale = new Locale('${locale.language}', '${locale.country}')
${setterCode(field)}
"""
	}

	private proxyGetterCode = { field ->
		"""
def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
if (${I18nFields.LOCALES}.containsKey(locale.language) && !${I18nFields.LOCALES}[locale.language].contains(locale.country)) {
	locale = new Locale(locale.language)
}
def fieldValue = ${getterCode(field)}
return fieldValue
"""
	}

	private proxySetterCode = { field ->
		"""
def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
${setterCode(field)}
"""
	}

	private localizedGetterCode = { field ->
		// TODO: Review this logic!!
		"""
println "I am inside a localizedGetterCode for '${field}'"
if (${I18nFields.LOCALES}.containsKey(locale.language) && !${I18nFields.LOCALES}[locale.language].contains(locale.country)) {
	locale = new Locale(locale.language)	
}
def fieldValue = ${getterCode(field)}
return fieldValue
"""
	}

	private localizedSetterCode = { field ->
		"""
if (${I18nFields.LOCALES}.containsKey(locale.language) && !${I18nFields.LOCALES}[locale.language].contains(locale.country)) {
	locale = new Locale(locale.language)
}
${setterCode(field)}
"""
	}

	private getterCode = { field ->
		"this.i18nFieldsHelper.getLocalizedValue(this, '${field}')"
	}

	private setterCode = { field ->
        "this.\"${field}_\${locale}\" = value"
	}

	private getColumnName(field) {
		return field.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase()
	}
	
	private getNewMethod(name, parameters, code, transientField = false) {
		def blockStatement = new AstBuilder().buildFromString(code).pop()
		return new MethodNode(name, ACC_PUBLIC, ClassHelper.make(transientField?Object.class:String.class, false), parameters, [] as ClassNode[], blockStatement)
	}
}
