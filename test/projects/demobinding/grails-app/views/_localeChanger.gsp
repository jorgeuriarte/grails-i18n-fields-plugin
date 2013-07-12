<li style="float: right">
	<span>Locale Changer:</span>
	<select id="localeSelector">
		<option>Choose a locale...</option>
		<g:each in="${grailsApplication.config.i18nFields.locales}" var="locale">
			<option value="${locale }">${locale }</option>
		</g:each>
		<option value="es_PE">es_PE (non existent)</option>
		<option value="it_IT">it_IT (non existent)</option>
	</select>
</li>

<g:javascript library='jquery' />
<r:script>
$("#localeSelector").change(function() {
	window.location.search = "?lang=" + $(this).val()
})
</r:script>
