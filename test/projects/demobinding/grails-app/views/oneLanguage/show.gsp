
<%@ page import="demobinding.Demo" %>
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
				<li class="fieldcontain">
					<span id="name_en_US-label" class="property-label">Name</span>
					<span class="property-value" aria-labelledby="name_en_US-label">${demoInstance.getName(lang)}</span>
				</li>
				<li class="fieldcontain">
					<span id="field1_en_US-label" class="property-label">Field 1</span>
					<span class="property-value" aria-labelledby="field1_en_US-label">${demoInstance.getField1(lang)}</span>
				</li>
				<li class="fieldcontain">
					<span id="field2_en_US-label" class="property-label">Field 2</span>
					<span class="property-value" aria-labelledby="field2_en_US-label">${demoInstance.getField2(lang)}</span>
				</li>
				<li class="fieldcontain">
					<span id="version-label" class="property-label">Version</span>
					<span class="property-value" aria-labelledby="version-label">${demoInstance.version}</span>
				</li>
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
