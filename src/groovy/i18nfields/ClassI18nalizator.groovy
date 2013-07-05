package i18nfields

import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
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
import groovy.util.logging.*

/**
 * Responsable of transforming a Domain class with i18n fields and helpers.
 */
class ClassI18nalizator {
	def classNode
	def locales
	def redisLocales

    /**
     * Initialize a ClassI18nalizator
     
     * @param classNode Class being transformed
     * @param locales List of all knock locales
     * @param redisLocales List of locales stored in redis
     */
	ClassI18nalizator(ClassNode classNode, Collection<Locale> locales, Collection<Locale> redisLocales) {
		this.classNode = classNode
		this.locales = locales
		this.redisLocales = redisLocales
	}

    /**
     * Execute the transformation
     */
	void transformClass() {
		createLocalStructures()
		addFieldsAndAccessors()
	}

    /**
     * Add the needed helpers and auxiliary methods to the class.
     */
	private void createLocalStructures() {
		addHelper()
	}

    /**
     * Add the fields and methods to support locales for every i18n field.
     */
	private void addFieldsAndAccessors() {
		i18nFieldList.each { fieldName ->
			addI18nFields(fieldName)
			
			// Remove the original field and associated constraints.
			removeField(fieldName)
            removeConstraintsFor(fieldName)
            
			addGettersAndSetters(fieldName)
			// makeFieldTransient(fieldName)
		}
	}

    /**
     * Add the i18nFieldsHelpers to the class.
     * This helper is used to retrieve and store the values. It is added and initialized.
     * TODO: Why it is not a static attribute?
     */
	private addHelper() {
		classNode.addField(new FieldNode("i18nFieldsHelper", ACC_PUBLIC,
								new ClassNode(I18nFieldsHelper.class),
								classNode,
								new ConstructorCallExpression(new ClassNode(I18nFieldsHelper.class), MethodCallExpression.NO_ARGUMENTS)))
	}

    /**
     * Add a static field of a given type to the ClassNode
     */
	private addStaticField(name, initialExpression) {
		def field = new FieldNode(name, ACC_PUBLIC | ACC_STATIC, new ClassNode(Object.class), classNode, initialExpression)
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

    /**
     * Remove a property from the classNode
     */
	private removeField(String name) {
		classNode.properties.remove(classNode.getProperty(name))
		classNode.removeField(name)
	}

    /**
     * Make field Transient by adding the field to the Transients list
     * @param name name of the field to make transient.
     */
	private makeFieldTransient(String name) {
		def transients = getOrCreateTransientsField().getInitialExpression()
		transients.addExpression(new ConstantExpression(name))
	}
	
	/**
	 * Add to a field the Bindable constraint.
	 * @param name name of the field to make bindable.
	 */
	private makeFieldBindable(String name) {
	    def astNodes = new AstBuilder().buildFromString("${name}(bindable: true)");
	    def blockStatement = astNodes.get(0);
	    def listStatements = blockStatement.getStatements();
	    def returnStatement = listStatements.get(0);
	    def expression = returnStatement.getExpression();	    
	    
	    def constraints = getOrCreateConstraintsField().getInitialExpression();
	    def blockStatement2 = constraints.getCode();
	    blockStatement2.addStatement(new ExpressionStatement(expression));
	}
	
	/**
	 * Gets or create a static field for properties like Transients or Constraints.
	 * @params field to get or create.
	 * @params type type of the field to create.
	 */
	private getOrCreateField(name, type) {
		if (!fieldExists(name)) addStaticField(name, type)
		return classNode.getDeclaredField(name)
	}

    /**
     * Get or create the Transients property
     */
	private getOrCreateTransientsField() { return getOrCreateField(I18nFields.TRANSIENTS, new ListExpression()); }
	
	/**
	 * Get or create the Constraints property
	 * Constraints field is a closure without parameters and initialy a empty block
	 */ 
	private getOrCreateConstraintsField() {
	    def closureExpression = new ClosureExpression(Parameter.EMPTY_ARRAY, new BlockStatement());
	    closureExpression.setVariableScope(new VariableScope())
	    
	    return  getOrCreateField(I18nFields.CONSTRAINTS, closureExpression);
    }

    /**
     * Add fields for each locale needed
     * @param baseName base fieldname to be created in each locale.
     */
	private addI18nFields(baseName) {
	    locales.each { locale ->
		    def fieldName = "${baseName}_${locale}";
		    
		    // Create localized field and copy constraints.
		    addI18nField(fieldName)
		    if (!hasConstraints(fieldName) && hasConstraints(baseName))
			    copyConstraints(baseName, fieldName)
		    
		    // If it is a redisLocale, then make the field transient and bindable
		    if(redisLocales.contains(locale)) {
    	        makeFieldTransient(fieldName)
    	        makeFieldBindable(fieldName)
		    }
	    }
	}

    /**
     * Adds a String Field to the class.
     */
	private addI18nField(name) {
	    log.info("Adding '${name}' field to ${classNode.name}")
		classNode.addProperty(name, Modifier.PUBLIC, new ClassNode(String.class), new ConstantExpression(null), null, null)
	}
	

	private boolean hasConstraints(field) {
		return hasConstraints() && null != getConstraints(field)
	}

	private boolean hasConstraints() {
		return fieldExists(I18nFields.CONSTRAINTS)
	}

    /**
     * True if the field exists, otherwise false
     */
	private boolean fieldExists(name) {
		return null != classNode.getDeclaredField(name)
	}

    /**
     * Get the constraint block for a field.
     */
	private getConstraints(field) {
		def closure = getConstraints().getInitialExpression().getCode()
		return closure.statements.find { statement ->
			containsAMethodCallExpression(statement) && field == statement.getExpression().getMethodAsString()
		}
	}

    /**
     * Get the constraints closure.
     */
	private getConstraints() {
		return classNode.getDeclaredField(I18nFields.CONSTRAINTS)
	}

    /**
     * Remove all the constraints for a field
     */
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

    /**
     * Check if a statement is a method call.
     */
	private boolean containsAMethodCallExpression(statement) {
		statement instanceof ExpressionStatement && statement.getExpression() instanceof MethodCallExpression
	}

    /**
     * Copy constraints for one field to another.
     */
	private copyConstraints(from, to) {
		def baseMethodCall = getConstraints(from).getExpression()
		def methodCall = new MethodCallExpression(new VariableExpression('this'), to, baseMethodCall.getArguments())
		def newConstraints = new ExpressionStatement(methodCall)
		addConstraints(newConstraints)
	}

    /**
     * Add new constraints to new fields.
     */
	private addConstraints(constraints) {
		def closure = getConstraints().getInitialExpression().getCode()
		closure.addStatement(constraints)
	}

    /**
     * Add a Delegate Gatter and Setter for a field.
     */
	private addGettersAndSetters(field) {
		addProxyGetter(field)
		// addProxySetter(field)

		// addLocalizedGetter(field)
		// addLocalizedSetter(field)
	}

    /**
     * 
     */
	private addProxyGetter(field) {
		String methodName = GrailsClassUtils.getGetterName(field);
		def code = new AstBuilder().buildFromString("i18nfields.I18nFieldsHelper.getLocalizedValue(this, '${field}')").pop();

		def methodNode = new MethodNode(
		    methodName, 
		    ACC_PUBLIC, 
		    ClassHelper.STRING_TYPE, 
		    Parameter.EMPTY_ARRAY, 
		    ClassHelper.EMPTY_TYPE_ARRAY, 
		    code
	    );
	    
		classNode.addMethod(methodNode);
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
