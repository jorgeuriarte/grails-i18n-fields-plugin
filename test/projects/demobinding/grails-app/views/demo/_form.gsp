<%@ page import="demobinding.Demo" %>
<%@ page import="java.util.Locale" %>

<fieldset>
    <legend>English</legend>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_en_US', 'error')} ">
	    <label>Name</label><g:textField name="name_en_US" value="${demoInstance?.getNameOrEmpty(new Locale('en', 'US'))}"/>
    </div>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field1_en_US', 'error')} ">
	    <label>Field 1</label><g:textField name="field1_en_US" value="${demoInstance?.getField1OrEmpty(new Locale('en', 'US'))}"/>
    </div>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field2_en_US', 'error')} ">
	    <label>Field 2</label><g:textField name="field2_en_US" value="${demoInstance?.getField2OrEmpty(new Locale('en', 'US'))}"/>
    </div>
</fieldset>

<fieldset>
    <legend>French</legend>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_fr_FR', 'error')} ">
	    <label>Name</label><g:textField name="name_fr_FR" value="${demoInstance?.getNameOrEmpty(new Locale('fr', 'FR'))}"/>
    </div>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field1_fr_FR', 'error')} ">
	    <label>Field 1</label><g:textField name="field1_fr_FR" value="${demoInstance?.getField1OrEmpty(new Locale('fr', 'FR'))}"/>
    </div>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field2_fr_FR', 'error')} ">
	    <label>Field 2</label><g:textField name="field2_fr_FR" value="${demoInstance?.getField2OrEmpty(new Locale('fr', 'FR'))}"/>
    </div>
</fieldset>

<fieldset>
    <legend>Spanish</legend>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_es_ES', 'error')} ">
	    <label>Name</label><g:textField name="name_es_ES" value="${demoInstance?.getNameOrEmpty(new Locale('es', 'ES'))}"/>
    </div>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field1_es_ES', 'error')} ">
	    <label>Field 1</label><g:textField name="field1_es_ES" value="${demoInstance?.getField1OrEmpty(new Locale('es', 'ES'))}"/>
    </div>
    <div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field2_es_ES', 'error')} ">
	    <label>Field 2</label><g:textField name="field2_es_ES" value="${demoInstance?.getField2OrEmpty(new Locale('es', 'ES'))}"/>
    </div>
</fieldset>

