package i18nfields

import org.junit.Test
import org.junit.Before
import static org.junit.Assert.assertThat
import static org.junit.matchers.JUnitMatchers.hasItem
import static org.hamcrest.CoreMatchers.is

class ConstraintTesterTests {
	def tester
	def constraints = {
		a(unique: true, blank: false)
		b(min: 5)
	}

	@Before
	void setUp() {
		tester = new ConstraintsTester()
	}

	@Test
	void "Returns a map with called methods"() {
		def methods = tester.test(constraints)
		assertThat methods.keySet(), hasItem("a")
		assertThat methods.keySet(), hasItem("b")
	}

	@Test
	void "Each map entry contains the parameters of each constraint"() {
		def methods = tester.test(constraints)
		assertThat methods.a.unique, is(true)
		assertThat methods.a.blank, is(false)
		assertThat methods.b.min, is(5)
	}
}
