package demo
import org.junit.Test
import org.junit.Before
import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.*
import org.gmock.WithGMock
import grails.test.GrailsUnitTestCase

import i18nfields.*

@WithGMock
class FixturesTests extends GroovyTestCase {

    def fixtureLoader

    protected void setUp() {
      super.setUp()
      fixtureLoader.load('fixturesDemo')
    }

    protected void tearDown() {
        super.tearDown()
    }

    @Test
    void "Fixtures correctly initialized"() {
      assertThat Literal.count(), is(8)
    }
}
