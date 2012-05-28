package demo
import org.junit.Test
import org.junit.Before
import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.*
import org.gmock.WithGMock
import grails.test.GrailsUnitTestCase

import i18nfields.*

@WithGMock
class DemoTests extends GroovyTestCase {
    protected void setUp() {
      super.setUp()
      def demo = DemoExternal.build(name_es_MX: 'manito').save(failOnError: true, flush: true)
    }

    protected void tearDown() {
        super.tearDown()
    }

    @Test
    void "Usual behaviour with intable literals"() {
      def demo = new Demo()

      I18nFieldsHelper.withLocale(new Locale("es", "MX")) {
        demo.name = "Nombre1"
        demo.save()
      }
      assertThat demo.@name_es_MX, is(equalTo("Nombre1"))
    }

    @Test
    void "Externalized literals will be saved in Literal"() {
      assert Literal.count() == 8
      createDemoExternal()
      assert Literal.count() == 16
    }

    @Test
    void "Literals are deleted after its owner"() {
      assert Literal.count() == 8
      def demo = createDemoExternal()
      assert Literal.count() == 16
      demo.delete(flush:true)
      assert Literal.count() == 8
    }

    @Test
    void "Literals are updated if locales of the object change"() {
      assert Literal.count() == 8
      def demo = createDemoExternal()
      assert Literal.count() == 16
      i18nfields.I18nFieldsHelper.withLocale(new Locale("es", "MX")) {
        demo.name = "Spanish default"
      }
      demo.save(flush: true)
      assert Literal.count() == 16
    }

    @Test
    void "You can search for domain objects based on its literals"() {
      
    }

    private def createDemoExternal() {
      return DemoExternal.build().save(flush: true, failOnError: true)
    }
}
