package i18nfields;

import org.hibernate.event.Initializable;
import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreDeleteEvent;
import org.hibernate.event.PreDeleteEventListener;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.SaveOrUpdateEventListener;
import org.hibernate.event.Initializable;

public class I18nFieldsListener implements SaveOrUpdateEventListener, PreDeleteEventListener, PreUpdateEventListener {

	SessionFactory sessionFactory

	def calls = [:]
	def i = 0
	public void onSaveOrUpdate(final SaveOrUpdateEvent event) {
		if (event.entity.hasProperty(I18nFields.I18N_FIELDS) && !inCall(event.entity, 'update')) {
			setCall(event.entity, 'update')
			
			I18nFieldsHelper.pushAll(event.entity)

			releaseCall(event.entity, 'update')
		}
	}

	public boolean onPreUpdate(final PreUpdateEvent event) {
		if (event.entity.hasProperty(I18nFields.I18N_FIELDS) && event.entity.dirtyPropertyNames - ['lastUpdated', 'dateCreated'] == [])
			return true
				
		return false
	}

	public boolean onPreDelete(final PreDeleteEvent event) {
		if (event.entity.hasProperty(I18nFields.I18N_FIELDS)) {
			Literal.withNewSession { session ->
				event.entity.i18nFieldsHelper.deleteFieldsFor(event.entity)
			}
		}
			
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
