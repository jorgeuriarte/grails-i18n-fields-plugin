<%@ page import="demobinding.Demo" %>

<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'name_${lang}', 'error')} ">
	<label for="name_en_US">
		Name
	</label>
	<g:textField name="name_${lang}" value="${demoInstance?.getName(lang)}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field1_${lang}', 'error')} ">
	<label for="field1_en_US">
		Field 1
	</label>
	<g:textField name="field1_${lang}" value="${demoInstance?.getField1(lang)}"/>
</div>
<div class="fieldcontain ${hasErrors(bean: demoInstance, field: 'field2_${lang}', 'error')} ">
	<label for="field2_en_US">
		Field 2
	</label>
	<g:textField name="field2_${lang}" value="${demoInstance?.getField2(lang)}"/>
</div>
