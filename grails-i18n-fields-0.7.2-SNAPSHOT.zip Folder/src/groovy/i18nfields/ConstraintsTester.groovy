package i18nfields

/**
 * Author: Javier Neira Sánchez
 * Source: http://meetspock.appspot.com/script/35001
 * Links:  http://javierneirasanchez.blogspot.com/
 *         http://twitter.com/jneira
 *
 * Thanks Javier!
 */
class ConstraintsTester {
    def methodsCalled = [:]

    def invokeMethod(String name, Object args) {
        methodsCalled << [(name): args[0]]
    }

    def test(Closure constraints) {
        this.with constraints
        methodsCalled
    }
}