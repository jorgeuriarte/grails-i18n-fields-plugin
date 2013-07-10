package i18nfields

import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.event.*

class I18nFieldsPersistenceEventListener extends AbstractPersistenceEventListener{

	def calls = [:]
	def i = 0
	
    public I18nFieldsPersistenceEventListener(final Datastore datastore) {
        super(datastore)
    }
 
    @Override
    protected void onPersistenceEvent(final AbstractPersistenceEvent event) {
        switch(event.eventType) {
        	case 'PreUpdate':
        		println "i18n PreUpdate ${event.entity?.name}"
                if (!(event.entity.hasProperty(I18nFields.I18N_FIELDS) && event.entity.dirtyPropertyNames - ['lastUpdated', 'dateCreated'] == [])){
                	event.cancel()
                }
                break;
            case 'PreDelete':
            	println "i18n PreUpdate ${event.entity?.name}"
                if (event.entity.hasProperty(I18nFields.I18N_FIELDS)) {
					Literal.withNewSession { session ->
						event.entity.i18nFieldsHelper.deleteFieldsFor(event.entity)
					}
				}
                break
            case 'SaveOrUpdate':
            	println "i18n SaveOrUpdate ${event.entity?.name}"
                if (event.entity.hasProperty(I18nFields.I18N_FIELDS) && !inCall(event.entity, 'update')) {
					setCall(event.entity, 'update')
					I18nFieldsHelper.pushAll(event.entity)
					releaseCall(event.entity, 'update')
				}
                break;
        }
    }
 
    @Override
    public boolean supportsEventType(Class eventType) {
        return AbstractPersistenceEvent.class.isAssignableFrom(eventType)
    }
    
	private void setCall(entity, call) {
		if (!calls[entity])
			calls[entity] = [:]
		calls[entity][call] = true
	}

	private void releaseCall(entity, call) {
		if (!calls[entity])
			calls[entity] = [:]
		calls[entity][call] = false
	}

	private boolean inCall(entity, call) {
		calls[entity] && calls[entity][call]
	}

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

    def updateFieldsFor(object) {
		def raiz = object."${I18nFields.TEMPSTRINGS}"
		def saved = this.alreadySavedFieldsFor(object)
		raiz.keySet().each { locale ->
			raiz[locale].each { field, value ->
				def literal = saved["${locale}-${field}"]
				if (!literal) {
					literal = new Literal(myclass:object.class.name, myobject:object.id, locale:locale, field:field)					
				}
				literal.value = raiz[locale][field]
				literal.save()
			}
		}
    }
 
}