package i18nfields

import org.junit.Test
import static org.junit.Assert.assertThat
import static org.junit.matchers.JUnitMatchers.hasItem
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.tools.ast.TranformTestHelper
import groovy.mock.interceptor.MockFor
import org.junit.Before

class BackwardsCompatibilityTests {
	def cocotero
	def blabla

	@Before
	void "Create our test instances"() {
		createCocotero()
		createBlabla()
	}

	@Test
	void "Accepts deprecated locales configuration"() {
		assertThat blabla.metaClass.properties*.name, hasItem("name_es")
		assertThat blabla.metaClass.properties*.name, hasItem("name_es_MX")
		assertThat blabla.metaClass.properties*.name, hasItem("name_en_US")
	}

	@Test
	void "Accepts deprecated extra locales configuration"() {
		assertThat blabla.metaClass.properties*.name, hasItem("name_eu")
	}

	@Test
	void "Accepts deprecated i18nalizable field list definition static property on domain classes"() {
		assertThat cocotero.metaClass.properties*.name, hasItem("name_es")
	}

	private def createBlabla() {
		blabla = createInstanceFromFile("./test/unit/i18nfields/Blabla.groovy")
	}

	private def createCocotero() {
		cocotero = createInstanceFromFile("./test/unit/i18nfields/Cocotero.groovy")
	}

	private def createInstanceFromFile(String filePath) {
		def mockedConfigProvider = new MockFor(ConfigProvider)
		def pluginConfiguration = [:]
		pluginConfiguration.put(I18nFields.I18N_FIELDS, [:])
		pluginConfiguration.get(I18nFields.I18N_FIELDS).put(I18nFields.DEPRECATED_LOCALES, ["es", "es_MX", "en_US", "kl_KL", "eu"])
		pluginConfiguration.get(I18nFields.I18N_FIELDS).put(I18nFields.DEPRECATED_EXTRA_LOCALES, ["eu"])
		mockedConfigProvider.ignore.getConfig() { pluginConfiguration }
		def clazz
		mockedConfigProvider.use() {
			TranformTestHelper invoker = new TranformTestHelper(new I18nFieldsTransformation(), CompilePhase.CANONICALIZATION)
			clazz = invoker.parse(new File(filePath))
		}
		return clazz.newInstance()
	}

	private def setNames() {
		chuchu.name_es = "Nombre"
		chuchu.name_es_MX = "Nombre wei"
		chuchu.name_en_US = "Name"
	}
}