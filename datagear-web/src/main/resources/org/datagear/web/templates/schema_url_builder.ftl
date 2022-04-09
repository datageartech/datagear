<#--
 *
 * Copyright 2018 datagear.tech
 *
 * Licensed under the LGPLv3 license:
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
-->
<#include "include/page_import.ftl">
<#include "include/html_doctype.ftl">
<html>
<head>
<#include "include/html_head.ftl">
<title><#include "include/html_title_app_name.ftl"><@spring.message code='schemaUrlBuilder.schemaUrlBuilder' /></title>
</head>
<body>
<#include "include/page_obj.ftl">
<div id="${pageId}" class="page-form page-form-schemaUrlBuilder">
	<form id="${pageId}form" action="${contextPath}/schemaUrlBuilder/saveScriptCode" method="POST">
		<div class="form-head"></div>
		<div class="form-content">
			<div class="form-item">
				<div class="form-item-label">
					<label><@spring.message code='schemaUrlBuilder.scriptCode' /></label>
				</div>
				<div class="form-item-value form-item-value-scriptCode">
					<textarea name="scriptCode" class="ui-widget ui-widget-content ui-corner-all script-code-textarea" autofocus="autofocus">${scriptCode}</textarea>
					<div class="script-code-note">
					<span><@spring.message code='schemaUrlBuilder.scriptCodeNote.0' /></span>
					<pre>
{
   //<@spring.message code='schemaUrlBuilder.scriptCodeNote.required' /><@spring.message code='comma' /><@spring.message code='schemaUrlBuilder.scriptCodeNote.dbType' />
   dbType : "...",
   
   //<@spring.message code='schemaUrlBuilder.scriptCodeNote.required' /><@spring.message code='comma' /><@spring.message code='schemaUrlBuilder.scriptCodeNote.template' />
   template : "...{host}...{port}...{name}...",
   
   //<@spring.message code='schemaUrlBuilder.scriptCodeNote.optional' /><@spring.message code='comma' /><@spring.message code='schemaUrlBuilder.scriptCodeNote.defaultValue' />
   defaultValue : { host : "...", port : "...", name : "" }
}</pre>
					<span><@spring.message code='schemaUrlBuilder.scriptCodeNote.1' /></span>
					</div>
				</div>
			</div>
			<div class="form-item">
				<div class="form-item-label">
					<label>&nbsp;</label>
				</div>
				<div class="form-item-value">
					<button type="button" class="preview-btn"><@spring.message code='preview' /></button>
				</div>
			</div>
		</div>
		<div class="form-foot">
			<button type="submit" class="recommended"><@spring.message code='save' /></button>
		</div>
	</form>
</div>
<#include "include/page_obj_form.ftl">
<script type="text/javascript">
(function(po)
{
	po.initFormBtns();
	
	po.element(".preview-btn").click(function()
	{
		po.open("${contextPath}/schemaUrlBuilder/previewScriptCode",
		{
			data : { "scriptCode" : po.elementOfName("scriptCode").val() }
		});
	});
	
	po.validateAjaxJsonForm();
})
(${pageId});
</script>
</body>
</html>