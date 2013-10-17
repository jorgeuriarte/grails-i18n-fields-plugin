package i18nfields

import junit.*
import com.fiftyonred.mock_jedis.*

class PushValuesTests {
	def object
	def redisMock

	@Before
	void setUp() {
		redisMock = new MockJedis('name')
		RedisHolder.metaClass.static.getInstance = { redisMock }

		// NOTE: i18nFieldInfo should be I18nFields.DATA but I can not use a constant here.
		object = new Object() { def i18nFieldInfo, i18nFields, id, name_es_ES, name_pt_PT, name_pt_BR }
		object.id = 1
		object.i18nFields = ['name']
		object.i18nFieldInfo = [
			dirty: [],
		]
	}

	@Test
	void "save modified locales"() {
		object.i18nFieldInfo.dirty = ['es_ES']
		object.name_es_ES = "spanish name"

		I18nFieldsHelper.push(object, 'es_ES')

		def expectedKeyName = "es_ES:${object.class.simpleName.toLowerCase()}:1"
		def keys = redisMock.keys('*')

		assert keys == [expectedKeyName] as Set<String>
		assert "spanish name" == redisMock.hget(expectedKeyName	, 'name')
	}

	@Test
	void "only save modified locales"() {
		I18nFieldsHelper.push(object, 'es_ES')
		assert redisMock.keys('*').size() == 0
	}

	@Test
	void "if a value is empty save empty"() {
		// First save some data
		object.i18nFieldInfo.dirty = ['es_ES']
		object.name_es_ES = "spanish name"
		I18nFieldsHelper.push(object, 'es_ES')

		// Then empty the value
		object.i18nFieldInfo.dirty = ['es_ES']
		object.name_es_ES = ""
		I18nFieldsHelper.push(object, 'es_ES')

		def expectedKeyName = "es_ES:${object.class.simpleName.toLowerCase()}:1"
		assert '' == redisMock.hget(expectedKeyName, 'name')
	}

}