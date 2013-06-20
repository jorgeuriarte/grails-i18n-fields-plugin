
<%@ page import="demo.Demo" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'demo.label', default: 'Demo')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-demo" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-demo" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="name_es_ES" title="${message(code: 'demo.name_es_ES.label', default: 'Namees ES')}" />
					
						<g:sortableColumn property="name_pt_BR" title="${message(code: 'demo.name_pt_BR.label', default: 'Namept BR')}" />
					
						<g:sortableColumn property="name_fr_FR" title="${message(code: 'demo.name_fr_FR.label', default: 'Namefr FR')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${demoInstanceList}" status="i" var="demoInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${demoInstance.id}">${fieldValue(bean: demoInstance, field: "name_es_ES")}</g:link></td>
					
						<td>${fieldValue(bean: demoInstance, field: "name_pt_BR")}</td>
					
						<td>${fieldValue(bean: demoInstance, field: "name_fr_FR")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${demoInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
