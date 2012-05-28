package i18nfields
import org.springframework.transaction.annotation.Transactional
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED
import static org.springframework.transaction.annotation.Isolation.READ_UNCOMMITTED

class LiteralService {

    static transactional = false

    @Transactional(readOnly = true) //, propagation = NOT_SUPPORTED, isolation = READ_UNCOMMITTED)
    def alreadySavedFieldsFor(object) {
    	def saved = [:]
    	if (object.id) {
			def hql = "\
				select lit \
				from Literal as lit \
				where lit.myclass = :myclass \
				and lit.myobject = :myid \
			"
			def existing = Literal.executeQuery(hql, [myclass:object.class.name, myid:object.id])
			existing.each {
				saved["${it.locale}-${it.field}"] = it
			}
    	}
		return saved
    }

}
