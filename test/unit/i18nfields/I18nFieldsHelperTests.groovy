package i18nfields

import org.junit.Test
import org.junit.Before
import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.*
import org.gmock.WithGMock
import grails.test.GrailsUnitTestCase

import org.springframework.context.i18n.LocaleContextHolder

@WithGMock
class I18nFieldsHelperTests extends GrailsUnitTestCase {
	def mockedLocaleContextHolder

	@Before
	void setUp() {
		super.setUp()
		mockedLocaleContextHolder = mock(LocaleContextHolder)
	}

	@Test
	void "Proxies setLocale calls over to LocaleContextHolder"() {
		mockedLocaleContextHolder.static.setLocale(any(Locale)).once()
		play {
			I18nFieldsHelper.setLocale(new Locale("es", "ES"))
		}
	}

	@Test
	void "Proxies getLocale calls over to LocaleContextHolder"() {
		def locale = new Locale("es", "ES")
		mockedLocaleContextHolder.static.getLocale().returns(locale).once()
		play {
			assertThat I18nFieldsHelper.getLocale(), is(locale)
		}
	}

	@Test
	void "withLocale saves the current locale sets a new one and resets it after closure is run"() {
		// TODO: Don't reproduce implementation in test
		// Currently I'd use a partial mock to ignore calls to getLocale,
		// but as they're static calls, I don't know how to do it
		def oldLocale = new Locale("es", "ES")
		def newLocale = new Locale("kl", "KL")
		Closure mockedClosure = mock(Closure)
		ordered {
			mockedLocaleContextHolder.static.getLocale().returns(oldLocale)
			mockedLocaleContextHolder.static.setLocale(newLocale)
			mockedClosure.call()
			mockedLocaleContextHolder.static.setLocale(oldLocale)
		}
		play {
			I18nFieldsHelper.withLocale(newLocale, mockedClosure)
		}
	}

	@Test
	void "withLocale returns whatever the closure returns"() {
		def closure = { return "Chuchu blabla" }
		play {
			assertThat I18nFieldsHelper.withLocale(new Locale("es"), closure), is("Chuchu blabla")
		}
	}

	@Test
	void "findFieldFor returns localized information properly"() {
		def dados = [
			new Literal(myclass:'i18nfields.Dado', myobject:1, locale:'es_ES', field:'name', value:'Nombre en español'),
			new Literal(myclass:'i18nfields.Dado', myobject:2, locale:'es_MX', field:'name', value:'Nombre en mexicano')
			]
		mockDomain(Literal)
		MockCriteria.mock(Literal, dados)
		def dado = new Dado()
		assertThat dado.i18nFieldsHelper.findFieldFor('name', new Locale("es", "ES"), new Dado(id:1)), is('Nombre en español')
		assertThat dado.i18nFieldsHelper.findFieldFor('name', new Locale("es", "MX"), new Dado(id:2)), is('Nombre en mexicano')
	}

	@Test
	void "New literals will be passed through I18nFieldsHelper - setFieldFor"() {
		def dado = new Dado()
		def locale = new Locale("es")
		LocaleContextHolder.metaClass.static.getLocale = {-> locale}
		def helper = mock(I18nFieldsHelper)
		helper.setFieldFor('name', locale.toString(), dado, "minombre")
		dado.i18nFieldsHelper = helper
		play {
			dado.name = "minombre"
		}
	}

	@Test
	void "New literals will be temporarily held in the in-object map"() {
		def dado = new Dado()
		LocaleContextHolder.metaClass.static.getLocale = { -> new Locale("es", "ES")}
		dado.name = 'nombre-1'
		dado.setName('nombre-2', new Locale("es", "MX"))
		assertThat dado.i18nFieldsTempStrings["es_ES"]['name'], is('nombre-1')
		assertThat dado.i18nFieldsTempStrings["es_MX"]['name'], is('nombre-2')
	}

}

class MockCriteria {
	def element
	boolean fits = true
	def MockCriteria(element) {
		this.element = element
	}

	def eq(a, b) {
		if (fits) {
			fits = (element."${a}" == b)
		}
	}

	def fits() {
		fits
	}

	static def datos
	static def crit = [
		list: { Closure cls ->
			datos.findAll {
				cls.delegate = new MockCriteria(it)
				cls.call()
				cls.delegate.fits()
			}
		}
	]
	static def mock(theclass, data) {
		this.datos = data
		theclass.metaClass.static.createCriteria = {MockCriteria.crit}
	}
}
