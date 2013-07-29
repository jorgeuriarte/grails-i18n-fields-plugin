<%@ page import="demobinding.Demo" %>



<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_en_US', 'error')} ">
	<label for="name_en_US">
		<g:message code="demo.name_en_US.label" default="Name en_US" />
	</label>
	<g:textField name="name_en_US" value="${demoInstance?.name_en_US}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field1_en_US', 'error')} ">
	<label for="field1_en_US">
		<g:message code="demo.field1_en_US.label" default="field1 en_US" />
	</label>
	<g:textField name="field1_en_US" value="${demoInstance?.field1_en_US}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field2_en_US', 'error')} ">
	<label for="field2_en_US">
		<g:message code="demo.field2_en_US.label" default="field2 en_US" />
	</label>
	<g:textField name="field2_en_US" value="${demoInstance?.field2_en_US}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_fr_FR', 'error')} ">
	<label for="name_fr_FR">
		<g:message code="demo.name_fr_FR.label" default="Name fr_FR" />
	</label>
	<g:textField name="name_fr_FR" value="${demoInstance?.name_fr_FR}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field1_fr_FR', 'error')} ">
	<label for="field1_fr_FR">
		<g:message code="demo.field1_fr_FR.label" default="field1 fr_FR" />
	</label>
	<g:textField name="field1_fr_FR" value="${demoInstance?.field1_fr_FR}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field2_fr_FR', 'error')} ">
	<label for="field2_fr_FR">
		<g:message code="demo.field2_fr_FR.label" default="field2 fr_FR" />
	</label>
	<g:textField name="field2_fr_FR" value="${demoInstance?.field2_fr_FR}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_es_ES', 'error')} ">
	<label for="name_es_ES">
		<g:message code="demo.name_es_ES.label" default="Name es_ES" />
	</label>
	<g:textField name="name_es_ES" value="${demoInstance?.name_es_ES}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field1_es_ES', 'error')} ">
	<label for="field1_es_ES">
		<g:message code="demo.field1_es_ES.label" default="field1 es_ES" />
	</label>
	<g:textField name="field1_es_ES" value="${demoInstance?.field1_es_ES}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field2_es_ES', 'error')} ">
	<label for="field2_es_ES">
		<g:message code="demo.field2_es_ES.label" default="field2 es_ES" />
	</label>
	<g:textField name="field2_es_ES" value="${demoInstance?.field2_es_ES}"/>
</div>


