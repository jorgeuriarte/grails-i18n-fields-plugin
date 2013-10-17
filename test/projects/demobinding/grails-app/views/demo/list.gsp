
<%@ page import="demobinding.Demo" %>
<%@ page import="java.util.Locale" %>
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
				<g:render template="/localeChanger"/>
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
					    <th rowspan="2" style="vertical-align: middle">ID</th>
					    <th rowspan="2" style="vertical-align: middle">Contextual</th>
					    <th colspan="3" style="text-align: center; border-left: 1px solid gray">Default</th>
					    <th colspan="3" style="text-align: center; border-left: 1px solid gray"">Empty</th>
					</tr>
					</th>
						<th style="border-left: 1px solid gray">English</th>
						<th>French</th>
						<th>Spanish</th>

						<th style="border-left: 1px solid gray">English</th>
						<th>French</th>
						<th>Spanish</th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${demoInstanceList}" status="i" var="demoInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					    <td><g:link action="show" id="${demoInstance.id}">${demoInstance.id}</g:link></td>
						<td>${fieldValue(bean: demoInstance, field: "name")}</td>
						<td style="border-left: 1px solid gray">${fieldValue(bean: demoInstance, field: "name_en_US")}</td>
						<td>${fieldValue(bean: demoInstance, field: "name_fr_FR")}</td>
						<td>${fieldValue(bean: demoInstance, field: "name_es_ES")}</td>

						<td style="border-left: 1px solid gray">${demoInstance.getNameOrEmpty(new Locale("en", "US"))}</td>
						<td>${demoInstance.getNameOrEmpty(new Locale("fr", "FR"))}</td>
						<td>${demoInstance.getNameOrEmpty(new Locale("es", "ES"))}</td>
					
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
