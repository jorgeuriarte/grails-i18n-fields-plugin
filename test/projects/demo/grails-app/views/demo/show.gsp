
<%@ page import="demo.Demo" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'demo.label', default: 'Demo')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-demo" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-demo" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list demo">
			
				<g:if test="${demoInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="demo.name.label" default="Name" /></span>
					<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${demoInstance}" field="name"/></span>
				</li>
				</g:if>
			
			
				<g:if test="${demoInstance?.name_es_ES}">
				<li class="fieldcontain">
					<span id="name_es_ES-label" class="property-label"><g:message code="demo.name_es_ES.label" default="Namees ES" /></span>
					
						<span class="property-value" aria-labelledby="name_es_ES-label"><g:fieldValue bean="${demoInstance}" field="name_es_ES"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${demoInstance?.name_pt_BR}">
				<li class="fieldcontain">
					<span id="name_pt_BR-label" class="property-label"><g:message code="demo.name_pt_BR.label" default="Namept BR" /></span>
					
						<span class="property-value" aria-labelledby="name_pt_BR-label"><g:fieldValue bean="${demoInstance}" field="name_pt_BR"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${demoInstance?.name_fr_FR}">
				<li class="fieldcontain">
					<span id="name_fr_FR-label" class="property-label"><g:message code="demo.name_fr_FR.label" default="Namefr FR" /></span>
					
						<span class="property-value" aria-labelledby="name_fr_FR-label"><g:fieldValue bean="${demoInstance}" field="name_fr_FR"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${demoInstance?.id}" />
					<g:link class="edit" action="edit" id="${demoInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
