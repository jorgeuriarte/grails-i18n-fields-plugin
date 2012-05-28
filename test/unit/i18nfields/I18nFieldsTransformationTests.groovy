package i18nfields

import org.junit.Test
import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.*
import static org.junit.matchers.JUnitMatchers.hasItem
import org.codehaus.groovy.grails.orm.hibernate.GrailsHibernateDomainClassProperty
import org.codehaus.groovy.grails.orm.hibernate.GrailsHibernateDomainClass
import java.beans.PropertyDescriptor
import org.codehaus.groovy.tools.ast.TranformTestHelper
import org.codehaus.groovy.control.CompilePhase
import org.junit.Before
import org.springframework.context.i18n.LocaleContextHolder
import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import org.gmock.WithGMock

@WithGMock
class I18nFieldsTransformationTests {
	def chuchu
	def blabla
	def cocotero
	def dado

	@Before
	void "Create our test instances"() {
		createChuchu()
		createBlabla()
		createCocotero()
		createDado()
	}

	@Test
	void "Adds localized versions of a field"() {
		assertThat blabla.metaClass.properties*.name, hasItem("name_es")
		assertThat blabla.metaClass.properties*.name, hasItem("name_es_MX")
		assertThat blabla.metaClass.properties*.name, hasItem("name_en_US")
	}

	@Test
	void "Localized fields will not exist if i18nFieldsTable has been established"() {
		assertException MissingFieldException, { dado.@name_es }
	}

    @Test
    void "Localized fields will exist if i18nFieldsTable has NOT been setted"() {
        def tmp = cocotero.@name_es // should not fail
    }

	@Test
	void "Ignores invalid locales like Klingon"() {
		assertThat blabla.metaClass.properties*.name, not(hasItem("name_kl_KL"))
	}

	@Test
	void "Ignores non existant fields like cocotero"() {
		assertThat blabla.metaClass.properties*.name, not(hasItem("cocotero_es"))
	}

	@Test
	void "Makes original field transient"() {
		assertThat blabla.transients, hasItem("name")
	}

	@Test
	void "Creates static transients collection if the class does not have it"() {
		// Note that Chuchu.groovy doesn't define a static transients collection
		assertThat chuchu.transients, hasItem("name")
	}

	@Test
	void "Adds a proxy getter that uses locale settings from the context"() {
		LocaleContextHolder.metaClass.static.getLocale = {-> new Locale("es")}
		setNames()
		assertThat chuchu.getName(), is("Nombre")
	}

	@Test
	void "Overloads proxy getter with a new signature that specifies the wanted locale"() {
		setNames()
		assertThat chuchu.getName(new Locale("es")), is("Nombre")
		assertThat chuchu.getName(new Locale("es", "MX")), is("Nombre wei")
		assertThat chuchu.getName(new Locale("en", "US")), is("Name")
	}

	@Test
	void "Adds a proxy setter that set the correct localized value of a field"() {
		LocaleContextHolder.metaClass.static.getLocale = {-> new Locale("es")}
		blabla.setName "Nombre"
		assertThat blabla.name_es, is("Nombre")
	}

	@Test
	void "Overloads proxy setter with a new signature that specifies the wanted locale"() {
		blabla.setName("Nombre", new Locale("es"))
		blabla.setName("Nombre wei", new Locale("es", "MX"))
		blabla.setName("Name", new Locale("en", "US"))
		assertThat blabla.name_es, is("Nombre")
		assertThat blabla.name_es_MX, is("Nombre wei")
		assertThat blabla.name_en_US, is("Name")
	}

	@Test
	void "Adds to domain classes a static map with the current locales defined by the plugin"() {
		assertThat blabla."${I18nFields.LOCALES}".keySet(), hasItem("es")
		assertThat blabla."${I18nFields.LOCALES}".keySet(), hasItem("en")
		assertThat blabla."${I18nFields.LOCALES}".es, hasItem("MX")
		assertThat blabla."${I18nFields.LOCALES}".en, hasItem("US")
	}

	@Test
	void "Proxy getters fall back to a more generic Locale if wanted does not exist"() {
		setNames()
		LocaleContextHolder.metaClass.static.getLocale = {-> new Locale("es", "AR")}
		assertThat chuchu.getName(), is("Nombre")
	}

	@Test
	void "Localized getters fall back to a more generic Locale if wanted does not exist"() {
		setNames()
		assertThat chuchu.getName(new Locale("es", "AR")), is("Nombre")
	}

	@Test
	void "Adds any constraint from the original field to localized versions"() {
		def tester = new ConstraintsTester()
		def constraints = chuchu.constraints
		def constraintsMethods = tester.test(constraints)
		assertThat constraintsMethods.name_es?.nullable, is(true)
		assertThat constraintsMethods.name_es_MX?.min, is(5)
	}

	@Test
	void "Removes original constraints from removed fields"() {
		def tester = new ConstraintsTester()
		def constraints = chuchu.constraints
		def constraintsMethods = tester.test(constraints)
		assertThat constraintsMethods.containsKey('name'), is(false)
	}	

	@Test
	void "Avoids overwriting constraints for localized fields if they are already defined in the original field"() {
		assertThat constraintsFor(chuchu).name_en_US?.min, is(10)
	}

	private def constraintsFor(domain) {
		return (new ConstraintsTester()).test(domain.constraints)	
	}

	@Test
	void "Allows to define some extra Locale definitions that might not be supported natively"() {
		// Arrange phase is made in createInstanceFromFile as it has to be passed to the
		// configuration mock before test execution. We add 'eu' valid locale definition
		// not supported natively by Java.
		assertThat blabla.metaClass.properties*.name, hasItem("name_eu")
	}

	@Test
	void "Outer literals are retrieved using I18nFieldsHelper findFieldsFor if defined with i18nFieldsLiteralTable"() {
		LocaleContextHolder.metaClass.static.getLocale = {-> new Locale("es", "ES")}
		createDado()
        def helper = mock(I18nFieldsHelper)
        helper.findFieldFor("name", new Locale("es"), dado).returns("name-es-dado")
        dado.i18nFieldsHelper = helper
        play {
            assertThat dado.getName(), is("name-es-dado")
        }
	}

	@Test
	void "Adds a map for temporal store of literals changes to domain classes"() {
		assertThat blabla.hasProperty(I18nFields.TEMPSTRINGS), nullValue()
		assertThat dado."${I18nFields.TEMPSTRINGS}", notNullValue()
	}

    @Test
    void "Adds a map for keeping all loaded literals when a field is requested"() {
        assertThat blabla.hasProperty(I18nFields.CACHESTRINGS), nullValue()
        assertThat dado."${I18nFields.CACHESTRINGS}", notNullValue()
    }

    @Test
    void "Makes sure we are able of guessing the inner column name"() {
		//def column = (new DefaultNamingStrategy()).propertyToColumnName('longestNameEver')
		assertThat 'longestNameEver'.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase(),
			is('longest_name_ever')
    }

	public static String underscoreSeparatedCase(String s) { 
	    return s.replaceAll(/\B[A-Z]{2,}/) { '_' + it }.toLowerCase() 
	}

	private def createBlabla() {
		blabla = createInstanceFromFile("./test/unit/i18nfields/Blabla.groovy")
	}

	private def createChuchu() {
		chuchu = createInstanceFromFile("./test/unit/i18nfields/ChuChu.groovy")
	}

	private def createCocotero() {
		cocotero = createInstanceFromFile("./test/unit/i18nfields/Cocotero.groovy")
	}

	private def createDado() {
		dado = createInstanceFromFile("./test/unit/i18nfields/Dado.groovy")
	}

	private def createInstanceFromFile(String filePath) {
		def mockedConfigProvider = new MockFor(ConfigProvider)
		def pluginConfiguration = [:]
		pluginConfiguration.put(I18nFields.I18N_FIELDS, [:])
		pluginConfiguration.get(I18nFields.I18N_FIELDS).put(I18nFields.LOCALES, ["es", "es_MX", "en_US", "kl_KL", "eu"])
		pluginConfiguration.get(I18nFields.I18N_FIELDS).put(I18nFields.EXTRA_LOCALES, ["eu"])
		mockedConfigProvider.ignore.getConfig() { pluginConfiguration }
		def clazz
		mockedConfigProvider.use() {
			TranformTestHelper invoker = new TranformTestHelper(new I18nFieldsTransformation(), CompilePhase.CANONICALIZATION)
			clazz = invoker.parse(new File(filePath))
		}
		clazz.metaClass.useTimer = { name, closure -> closure.call() }
		return clazz.newInstance()
	}

	private def setNames() {
		chuchu.name_es = "Nombre"
		chuchu.name_es_MX = "Nombre wei"
		chuchu.name_en_US = "Name"
	}

    private def assertException(exceptionclass, closure) {
        def ex
        try {
            closure()
        } catch (Throwable t) {
            ex = t
        }
        assertThat ex, is(exceptionclass)
    }
}