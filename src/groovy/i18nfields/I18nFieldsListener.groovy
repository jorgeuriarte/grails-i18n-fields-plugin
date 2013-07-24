package i18nfields;

import org.hibernate.event.Initializable;
import org.hibernate.cfg.Configuration;
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

public class I18nFieldsListener implements SaveOrUpdateEventListener, PreDeleteEventListener {

	public void onSaveOrUpdate(final SaveOrUpdateEvent event) {
	    saveEntity(event.entity);
	}

	public boolean onPreDelete(final PreDeleteEvent event) {
    	deleteEntity(event.entity);
	}

    /**
     * Persist entity values.
     * At this point entity should have a id
     */
    protected void saveEntity(entity) {
        if(!entity.hasProperty(I18nFields.I18N_FIELDS)) return; // Entity should have i18n
		if(entity.isDirty())
			I18nFieldsHelper.pushAll(entity)
    }
    
    /**
     * Delete a entity.
     * At this point the entity should ahve a id
     */
    protected void deleteEntity(entity) {
        if(!entity.hasProperty(I18nFields.I18N_FIELDS)) return; // Entity should have i18n
        I18nFieldsHelper.delete(entity)
    }
}
