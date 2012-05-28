package i18nfields

import org.junit.Test
import org.junit.Before
import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.*
import org.gmock.WithGMock
import grails.test.GrailsUnitTestCase

import org.springframework.context.i18n.LocaleContextHolder
import org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners

@WithGMock
class ExternalLiteralsTests extends GroovyTestCase {
    def grailsApplication
    def originalListener = null

    protected void setUp() {
        super.setUp()    
        if (originalListener != null)
            unmockI18nFieldsListener(originalListener)
    }

    protected void tearDown() {
        super.tearDown()
    }

    static transactional = true

    @Test
    void "New objects will store literals in temporary map"() {
    	def ego = new Ego()
    	I18nFieldsHelper.withLocale(new Locale("es", "ES")) {
	    	ego.name = 'ego-1'
	    	ego.save()
	    	ego.name = 'ego-2'
		   	ego.save()		    		
    	}
    	I18nFieldsHelper.withLocale(new Locale("es", "MX")) {
    		ego.name = "ego-mx"
    		ego.save()
    	}

    	def tempmap = ego."${I18nFields.TEMPSTRINGS}"
	    assertThat tempmap, notNullValue()
	    assertThat tempmap["es_ES"]["name"], is("ego-2")
	    assertThat tempmap["es_MX"]['name'], is("ego-mx")
    }

    @Test
    void "Internationalized literals for new object will be saved into Literal"() {
        def ego = newEgo()
        ego.save()
        assertThat Literal.list().findAll { it.myclass == Ego.class.name && it.myobject == ego.id }.size(), is(8)
    }

    @Test
    void "Listeners will call deleteFieldsFor when associated object goes away"() {
        def ego = newEgo()
        ego.save()
        def helper = mock(I18nFieldsHelper)
        helper.deleteFieldsFor(ego)
        ego.i18nFieldsHelper = helper
        play {
            ego.delete(flush:true)
        }
    }

    @Test
    void "Can find domains by externalized literal"() {
        createSomeEgos()
        def likeResults = I18nFieldsHelper.findAllByLiteralLike(Ego, 'name', new Locale("es"), '%Nombre%')
        def isResults = I18nFieldsHelper.findAllByLiteral(Ego, 'name', new Locale("es"), 'Nombre del 1')
        assertThat likeResults.size, is(1)
        assertThat likeResults[0], is(Ego)
        assertThat isResults.size, is(1)
    }

    @Test
    void "Can find domains by externalized literal with additional HQL"() {
        createSomeEgos()
        def likeResults = I18nFieldsHelper.findAllByLiteralLike(Ego, 'name', new Locale("es"), '%ombre%')
        assertThat likeResults.size, is(2)

        def filtered = I18nFieldsHelper.findAllByLiteralLike(Ego, 'name', new Locale("es"), '%ombre%', 'Ego.karma = :karma', [karma:1L])
        assertThat filtered.size(), is(1)
    }

    private void createSomeEgos() {
        def ego
        def i = 0
        ["Nombre del 1", "Y ahora el nombre del 2"].each { name ->
            ego = new Ego(karma: i++)
            [new Locale("es"), new Locale("es", "MX"), new Locale("en", "US"), new Locale("eu")].each { loc ->
                I18nFieldsHelper.withLocale(loc) {
                    ego.name = name
                    ego.alias = "Alias-${name}"     
                }
            }
            ego.save(flush:true, failOnError: true)
        }
    }

    private def newEgo(text='Ego') {
        def ego = new Ego()
        [new Locale("es"), new Locale("es", "MX"), new Locale("en", "US"), new Locale("eu")].each {
            ego.setName("${text}-${it}", it)
            ego.setAlias("Alias${text}-${it}", it)
        }
        ego
    }
}
