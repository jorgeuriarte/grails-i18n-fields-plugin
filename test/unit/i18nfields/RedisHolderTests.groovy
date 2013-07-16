package i18nfields

import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.junit.matchers.JUnitMatchers.hasItem
import grails.test.GrailsUnitTestCase;

import org.codehaus.groovy.grails.plugins.MockGrailsPluginManager;
import org.junit.Before;
import org.junit.Test

class RedisHolderTests extends GrailsUnitTestCase {
	def mockGrailsApplication = new Expando()
	
	@Before
	void setUp() {
		registerMetaClass(RedisHolder)
		RedisHolder.metaClass .static.getSpringBean = { String name -> mockGrailsApplication }
	}
	
	@Test
	void "if no configuration defaults to localhost"() {
		this.mockGrailsApplication.config = new ConfigSlurper().parse("i18nFields = {}")
		
		def config = RedisHolder.configuration
		assert config.host == "localhost", "Default should be localhost"
		assert config.port == 6379, "Default redis port should be 6789"
	}
	
	@Test
	void "returns redis configuration"() {
		this.mockGrailsApplication.config = new ConfigSlurper().parse("""
			i18nFields {
				redisConfig  {
					host = 'remotehost'
					port = 1234
				}
			}"""
		)
		
		def config = RedisHolder.configuration
		assert config.host == "remotehost", "Should be customized host"
		assert config.port == 1234, "Should be customized port"
	}
}