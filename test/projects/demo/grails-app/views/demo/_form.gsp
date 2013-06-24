<%@ page import="demo.Demo" %>

<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_es_ES', 'error')} ">
	<label for="name_es_ES">
		<g:message code="demo.name_es_ES.label" default="Namees ES" />
		
	</label>
	<g:textField name="name_es_ES" value="${demoInstance?.name_es_ES}"/>
</div>

<%--
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_en_US', 'error')} ">
	<label for="name_en_US">
		<g:message code="demo.name_en_US.label" default="Nameen US" />
		
	</label>
	<g:textField name="name_en_US" value="${demoInstance?.name_en_US}"/>
</div>
 --%>
  
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_pt_BR', 'error')} ">
	<label for="name_pt_BR">
		<g:message code="demo.name_pt_BR.label" default="Namept BR" />
		
	</label>
	<g:textField name="name_pt_BR" value="${demoInstance?.name_pt_BR}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_fr_FR', 'error')} ">
	<label for="name_fr_FR">
		<g:message code="demo.name_fr_FR.label" default="Namefr FR" />
		
	</label>
	<g:textField name="name_fr_FR" value="${demoInstance?.name_fr_FR}"/>
</div>

